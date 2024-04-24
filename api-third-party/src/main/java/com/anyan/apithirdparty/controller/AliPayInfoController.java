package com.anyan.apithirdparty.controller;

/**
 * @author 兕神
 * DateTime: 2024/4/22
 */

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.anyan.apicommon.common.BaseResponse;
import com.anyan.apicommon.common.ResultUtils;
import com.anyan.apithirdparty.config.AliPayConfig;
import com.anyan.apithirdparty.model.dto.AlipayRequest;
import com.anyan.apithirdparty.model.entity.AlipayInfo;
import com.anyan.apithirdparty.service.AlipayInfoService;
import com.anyan.apithirdparty.utils.OrderPaySuccessMqUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.anyan.apicommon.constant.RedisConstant.ALIPAY_TRADE_SUCCESS_RECORD;
import static com.anyan.apicommon.constant.RedisConstant.EXIST_KEY_VALUE;

/**
 * 订单接口
 */
@RestController
@RequestMapping("/alipay")
@Slf4j
public class AliPayInfoController {

    @Resource
    private AliPayConfig aliPayConfig;

    @Resource
    private Environment environment;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AlipayInfoService alipayInfoService;

    @Resource
    private OrderPaySuccessMqUtils orderPaySuccessMqUtils;

    @PostMapping("/payCode")
    public BaseResponse<String> payCode(@RequestBody AlipayRequest alipayRequest) throws AlipayApiException {

        String outTradeNo = alipayRequest.getTraceNo();
        String subject = alipayRequest.getSubject();
        double totalAmount = alipayRequest.getTotalAmount();


        AlipayClient alipayClient = new DefaultAlipayClient(aliPayConfig.getGatewayUrl(),
                aliPayConfig.getAppId(),
                aliPayConfig.getPrivateKey(),
                "json", aliPayConfig.getCharset(),
                aliPayConfig.getPublicKey(), aliPayConfig.getSignType());

        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();

        request.setNotifyUrl("http://72d39372.r26.cpolar.top/api/third/alipay/notify");

        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(String.valueOf(totalAmount));
        model.setSubject(subject);

        request.setBizModel(model);
        AlipayTradePrecreateResponse response = alipayClient.execute(request);
        log.info("响应支付二维码详情：" + response.getBody());

        String base64 = QrCodeUtil.generateAsBase64(response.getQrCode(), new QrConfig(300, 300), "png");

        return ResultUtils.success(base64);
    }


    /**
     * 支付成功回调,注意这里必须是POST接口
     *
     * @param request
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/notify")
    public synchronized void payNotify(HttpServletRequest request) throws Exception {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            // 支付宝验签
            if (AlipaySignature.rsaCheckV1(params, aliPayConfig.getPublicKey(), aliPayConfig.getCharset(), aliPayConfig.getSignType())) {
                //验证成功
                log.info("支付成功:{}", params);
                // 幂等性保证：判断该订单号是否被处理过，解决因为多次重复收到阿里的回调通知导致的订单重复处理的问题
                Object outTradeNo = stringRedisTemplate.opsForValue().get(ALIPAY_TRADE_SUCCESS_RECORD + params.get("out_trade_no"));
                if (null == outTradeNo) {
                    // 验签通过，将订单信息存入数据库
                    AlipayInfo alipayInfo = new AlipayInfo();
                    alipayInfo.setSubject(params.get("subject"));
                    alipayInfo.setTradeStatus(params.get("trade_status"));
                    alipayInfo.setTradeNo(params.get("trade_no"));
                    alipayInfo.setOrderNumber(params.get("out_trade_no"));
                    alipayInfo.setTotalAmount(Double.valueOf(params.get("total_amount")));
                    alipayInfo.setBuyerId(params.get("buyer_id"));
                    alipayInfo.setGmtPayment(DateUtil.parse(params.get("gmt_payment")));
                    alipayInfo.setBuyerPayAmount(Double.valueOf(params.get("buyer_pay_amount")));
                    alipayInfoService.save(alipayInfo);
                    //记录处理成功的订单，实现订单幂等性
                    stringRedisTemplate.opsForValue().set(ALIPAY_TRADE_SUCCESS_RECORD + alipayInfo.getOrderNumber(), EXIST_KEY_VALUE, 30, TimeUnit.MINUTES);
                    //发送支付成功消息，修改order支付状态数据库，完成整个订单功能
                    orderPaySuccessMqUtils.sendOrderPaySuccess(params.get("out_trade_no"));
                }
            }
        }
    }
}
