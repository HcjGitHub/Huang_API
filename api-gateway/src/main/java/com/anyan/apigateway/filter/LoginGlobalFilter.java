package com.anyan.apigateway.filter;

import cn.hutool.core.text.AntPathMatcher;
import com.anyan.apicommon.common.ErrorCode;
import com.anyan.apicommon.exception.BusinessException;
import com.anyan.apicommon.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 网关登录全局过滤器
 *
 * @author 兕神
 * DateTime: 2024/4/21
 */
@Component
@Slf4j
public class LoginGlobalFilter implements GlobalFilter, Ordered {

    //不需要用户登录路径
    public static final List<String> NOT_LOGIN_PATH = Arrays.asList(
            "/api/user/login", "/api/user/email/login", "/api/user/register", "/api/user/email/register", "/api/user/sendSMSCode",
            "/api/user/getCaptcha", "/api/interface/**", "/api/third/alipay/**", "/api/interfaceInfo/sdk", "/api/user/get/login");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = request.getHeaders();

        //请求日志
        log.info("全局登录验证请求URI:" + request.getURI());
        log.info("全局登录验证请求PATH:" + request.getPath());
        log.info("全局登录验证请求参数:" + request.getQueryParams());
        log.info("全局登录验证本地请求地址:" + request.getLocalAddress());
        log.info("全局登录验证请求地址：" + request.getRemoteAddress());
        //1.判断是否需要登录
        String path = request.getPath().value();
        Set<Boolean> collect = NOT_LOGIN_PATH.stream().map(notLoginPath -> {
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            return antPathMatcher.match(notLoginPath, path);
        }).collect(Collectors.toSet());
        //不需要登录
        if (collect.contains(true)) {
            return chain.filter(exchange);
        }
        //2.获取token的Cookie
        String cookie = headers.getFirst("Cookie");
        if (StringUtils.isBlank(cookie)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (Boolean.FALSE.equals(checkToken(cookie))) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //3.判断是否登录
        return chain.filter(exchange);
    }

    /**
     * 检查token
     *
     * @param cookie
     * @return
     */
    private Boolean checkToken(String cookie) {
        String[] cookies = cookie.split(";");
        for (String coo : cookies) {
            String[] cookieSplit = coo.split("=");
            String cookieName = cookieSplit[0].trim();
            if ("token".equals(cookieName)) {
                String token = cookieSplit[1];
                return JwtUtils.checkToken(token);
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
