package com.anyan.apiorder.utils;

import cn.hutool.core.util.IdUtil;
import com.anyan.apicommon.model.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.anyan.apiorder.config.RabbitmqConfig.EXCHANGE_ORDER_PAY;
import static com.anyan.apiorder.config.RabbitmqConfig.ROUTING_KEY_ORDER_PAY;

/**
 * 订单消息发送队列工具类
 *
 * @author 兕神
 * DateTime: 2024/4/23
 */
@Slf4j
@Component
public class OrderMqUtils implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 向mq发送订单
     * @param order
     */
    public void sendOrderInfo(Order order){
        String messageId = IdUtil.simpleUUID();
        rabbitTemplate.convertAndSend(EXCHANGE_ORDER_PAY,ROUTING_KEY_ORDER_PAY,order,message -> {
            //message是order实体发送前的数据封装
            MessageProperties messageProperties = message.getMessageProperties();
            //设置全局消息唯一id
            messageProperties.setMessageId(messageId);
            //设置消息有效时间  错误写法 60*1000
//            messageProperties.setExpiration("60000");
            //设置消息编码
            messageProperties.setContentEncoding("utf-8");
            return message;
        });
    }

    /**
     * 1、只要消息抵达服务器，那么success=true
     *
     * @param correlationData 当前消息的唯一关联数据（消息的唯一id）
     * @param success         消息是否成功收到
     * @param cause           失败的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean success, String cause) {
        if (!success) {
            log.error("订单--消息投递到服务端失败：{}---->{}", correlationData, cause);
        }
    }

    /**
     * 当前类的实例设置为RabbitTemplate的确认回调和返回回调
     */
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    /**
     * 用于处理那些无法被路由到任何队列的消息(发送到消息队列无法被消费)
     *
     * @param returned
     */
    @Override
    public void returnedMessage(ReturnedMessage returned) {
        log.error("发生异常，返回消息回调:{}", returned);
    }
}
