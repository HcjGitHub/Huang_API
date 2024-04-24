package com.anyan.apithirdparty.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.anyan.apicommon.constant.RabbitmqConstant.*;

/**
 * 订单支付成功的消息队列
 *
 * @author 兕神
 * DateTime: 2024/4/22
 */
@Configuration
public class RabbitmqConfig {

    /**
     * 声明订单交换机
     */
    @Bean(EXCHANGE_ORDER_PAY_SUCCESS)
    public Exchange exchange_order_pay() {
        return new DirectExchange(EXCHANGE_ORDER_PAY_SUCCESS, true, false);
    }

    /**
     * 声明名队列名
     */
    @Bean(QUEUE_ORDER_PAY_SUCCESS)
    public Queue queue_dlx_order_pay() {
        return new Queue(QUEUE_ORDER_PAY_SUCCESS, true, false, false);
    }

    /**
     * 声明订单绑定
     */
    @Bean
    public Binding routing_key_order_pay() {
        return new Binding(QUEUE_ORDER_PAY_SUCCESS,
                Binding.DestinationType.QUEUE, EXCHANGE_ORDER_PAY_SUCCESS,
                ROUTING_KEY_ORDER_PAY_SUCCESS, null);
    }

    /**
     * 定义消息的序列化json方式
     *
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
