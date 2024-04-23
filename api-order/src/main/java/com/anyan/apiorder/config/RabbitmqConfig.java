package com.anyan.apiorder.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单支付的消息队列和死信队列
 *
 * @author 兕神
 * DateTime: 2024/4/22
 */
@Configuration
public class RabbitmqConfig {

    /**
     * 订单消息队列相关
     */
    public static final String EXCHANGE_ORDER_PAY = "exchange_order_pay";
    public static final String QUEUE_ORDER_PAY = "queue_order_pay";
    public static final String ROUTING_KEY_ORDER_PAY = "routing_key_order_pay";

    /**
     * 订单死信队列相关 用于处理超时支付（超时间未支付）
     */
    public static final String EXCHANGE_DLX_ORDER_PAY = "exchange_dlx_order_pay";
    public static final String QUEUE_DLX_ORDER_PAY = "queue_dlx_order_pay";
    public static final String ROUTING_KEY_DLX_ORDER_PAY = "routing_key_dlx_order_pay";

    /**
     * 声明订单交换机
     */
    @Bean(EXCHANGE_ORDER_PAY)
    public Exchange exchange_order_pay() {
        return new DirectExchange(EXCHANGE_ORDER_PAY, true, false);
    }

    /**
     * 声明订单队列
     * 需要绑定死信队列
     */
    @Bean(QUEUE_ORDER_PAY)
    public Queue queue_order_pay() {
        Map<String, Object> args = new HashMap<>();
        //正常的消息时间到了或被废弃后会被路由到死信队列(前提是有绑定死信队列)
        //绑定死信交换机
        args.put("x-dead-letter-exchange", EXCHANGE_DLX_ORDER_PAY);
        //绑定路由Key
        args.put("x-dead-letter-routing-key", ROUTING_KEY_DLX_ORDER_PAY);
        //信息存活时间30分钟
        args.put("x-message-ttl", 1 * 60000);
        // 这里测试1分钟
        //args.put("x-message-ttl", 60000);

        return new Queue(QUEUE_ORDER_PAY, true, false, false, args);
    }

    /**
     * 声明订单绑定
     */
    @Bean
    public Binding routing_key_order_pay() {
        return new Binding(QUEUE_ORDER_PAY,
                Binding.DestinationType.QUEUE, EXCHANGE_ORDER_PAY,
                ROUTING_KEY_ORDER_PAY, null);
    }

    /**
     * 声明死信交换机
     */
    @Bean(EXCHANGE_DLX_ORDER_PAY)
    public Exchange exchange_dlx_order_pay() {
        return new DirectExchange(EXCHANGE_DLX_ORDER_PAY, true, false);
    }

    /**
     * 声明名死信队列名
     */
    @Bean(QUEUE_DLX_ORDER_PAY)
    public Queue queue_dlx_order_pay() {
        return new Queue(QUEUE_DLX_ORDER_PAY, true, false, false);
    }

    /**
     * 声明死信绑定
     */
    @Bean
    public Binding routing_key_dlx_order_pay() {
        return new Binding(QUEUE_DLX_ORDER_PAY,
                Binding.DestinationType.QUEUE, EXCHANGE_DLX_ORDER_PAY,
                ROUTING_KEY_DLX_ORDER_PAY, null);
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
