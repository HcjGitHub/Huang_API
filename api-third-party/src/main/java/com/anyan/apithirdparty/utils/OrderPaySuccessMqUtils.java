package com.anyan.apithirdparty.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.UUID;

import static com.anyan.apicommon.constant.RabbitmqConstant.*;
import static com.anyan.apicommon.constant.RedisConstant.SEND_ORDER_PAY_SUCCESS_INFO;

/**
 * @author 兕神
 * DateTime: 2024/4/23
 */
@Component
@Slf4j
public class OrderPaySuccessMqUtils {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private String finalId = null;

    /**
     * 发送支付成功消息到队列
     *
     * @param outTradeNo 我们自己的订单号
     */
    public void sendOrderPaySuccess(String outTradeNo) {

        finalId = outTradeNo;
        redisTemplate.opsForValue().set(SEND_ORDER_PAY_SUCCESS_INFO + outTradeNo, outTradeNo);
        String finalMessageId = UUID.randomUUID().toString();
        rabbitTemplate.convertAndSend(EXCHANGE_ORDER_PAY_SUCCESS, ROUTING_KEY_ORDER_PAY_SUCCESS, outTradeNo, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            //生成全局唯一id
            messageProperties.setMessageId(finalMessageId);
            messageProperties.setContentEncoding("utf-8");
            return message;
        });
        log.info("消息队列给订单服务发送支付成功消息，订单号：" + outTradeNo);
    }

}

