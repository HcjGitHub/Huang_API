package com.anyan.apigateway.filter;

import com.anyan.apiclientsdk.utils.SignUtils;
import com.anyan.apicommon.model.entity.InterfaceInfo;
import com.anyan.apicommon.model.entity.User;
import com.anyan.apicommon.service.ApiBackendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 模拟接口调用自定义拦截器
 *
 * @author 兕神
 * DateTime: 2024/4/14
 */
@Component
@Slf4j
public class InterfaceInfoInvokeFilter implements GatewayFilter, Ordered {

    @DubboReference
    private ApiBackendService apiBackendService;

    public static final List<String> LIST_WHITE = Arrays.asList(new String[]{"127.0.0.1"});

    public static final String INTERFACE_HOST = "http://localhost:8030";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.打上请求日志
        //2.黑白名单(可做可不做)
        //3.用户鉴权(API签名认证)
        //4.远程调用判断接口是否存在以及获取调用接口信息
        //5.判断接口是否还有调用次数，如果没有则直接拒绝
        //6.发起接口调用
        //7.获取响应结果，打上响应日志
        //8.接口调用次数+1

//        1. 客户端发送请求到网关
//        2. 请求日志
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String url = INTERFACE_HOST + request.getPath().value();
        String method = request.getMethod().toString();

        log.info("请求id:" + request.getId());
        log.info("请求URI:" + request.getURI());
        log.info("请求PATH:" + request.getPath());
        log.info("请求参数:" + request.getQueryParams());
        log.info("本地请求地址:" + request.getLocalAddress());
        log.info("请求地址：" + request.getRemoteAddress());

        //3. 黑白名单
        if (!LIST_WHITE.contains("127.0.0.1")) {
            return handlerNoAuth(response);
        }
        //4. 用户鉴权（ak、sk）
        HttpHeaders headers = request.getHeaders();
        String body = headers.getFirst("body");
        String accessKey = headers.getFirst("accessKey");
        String sign = headers.getFirst("sign");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");

        // 这里需要从数据库根据ak查询sk
        User user = null;
        try {
            user = apiBackendService.getUserByAccessKey(accessKey);
        } catch (Exception e) {
            return handlerNoAuth(response);
        }

        if (user == null) {
            return handlerNoAuth(response);
        }

        String accessKeyBySql = user.getAccessKey();
        String secretKeyBySql = user.getSecretKey();
        String signBySK = SignUtils.getSign(body, secretKeyBySql);

        if (Long.parseLong(nonce) > 100090) {
            return handlerNoAuth(response);
        }

        final int FIVE_MINUTE = 5 * 60;
        if ((System.currentTimeMillis() / 1000) - Integer.parseInt(timestamp) > FIVE_MINUTE) {
            return handlerNoAuth(response);
        }

        assert sign != null;
        if (!sign.equals(signBySK) || !accessKeyBySql.equals(accessKey)) {
            return handlerNoAuth(response);
        }
        //5. 查询请求接口是否存在
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo = apiBackendService.getInterfaceInfoByUrlAndMethod(url, method);
        } catch (Exception e) {
            return handlerInvokeError(response);
        }

        if (interfaceInfo == null) {
            return handlerInvokeError(response);
        }
        Long interfaceInfoId = interfaceInfo.getId();
        Long userId = user.getId();

        //判断接口剩余调用次数是否大于0
        int leftCount = 0;
        try {
            leftCount = apiBackendService.getLeftCount(interfaceInfoId, userId);
        } catch (Exception e) {
            return handlerInvokeError(response);
        }
        if (leftCount <= 0 && !user.getUserRole().equals("admin")) {
            return handlerInvokeError(response);
        }
//        6. 转发请求，调用接口
        if (user.getUserRole().equals("admin")) {
            return chain.filter(exchange);
        }
        return handleResponse(exchange, chain, interfaceInfoId, userId);
    }

    /**
     * 响应日志
     */
    private Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
        ServerHttpResponse originalResponse = exchange.getResponse();

        //装饰器模式  增加功能
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                    return super.writeWith(fluxBody.map(dataBuffer -> {
                        //7. 响应日志
                        log.info("响应数据:");
                        // 对响应体进行记录
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        // 记录日志
                        String arg = new String(content, StandardCharsets.UTF_8);
                        //获取响应结果，打上响应日志
                        log.info("接口调用响应状态码：" + originalResponse.getStatusCode());
                        log.info("响应数据: {}", arg);
                        DataBufferUtils.release(dataBuffer);

                        //8. 请求成功，调用次数加1
                        // 接口调用次数加1
                        boolean invokeCount = apiBackendService.invokeCount(interfaceInfoId, userId);
                        if (!invokeCount) {
                            log.error("<=== 响应code异常：{}", getStatusCode());
                        }
                        return bufferFactory().wrap(content);
                    }));
                } else {
                    //9. 请求失败，返回一个规范的失败码
                    log.error("<=== 响应code异常：{}", getStatusCode());
                }

                return super.writeWith(body);
            }
        };

        // 将修改后的ServerHttpResponse放入上下文
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    private Mono<Void> handlerNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    private Mono<Void> handlerInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
