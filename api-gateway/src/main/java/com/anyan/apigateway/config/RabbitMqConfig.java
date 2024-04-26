package com.anyan.apigateway.config;

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
 * 接口数据一致性补偿消息相关队列定义
 *
 * @author anyan
 * DateTime: 2024/4/26
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 声明交换机
     */
    @Bean(EXCHANGE_INTERFACE_CONSISTENT)
    public Exchange exchange() {
        return new DirectExchange(EXCHANGE_INTERFACE_CONSISTENT, true, false);
    }

    /**
     * 声明队列
     */
    @Bean(QUEUE_INTERFACE_CONSISTENT)
    public Queue queue() {
        return new Queue(QUEUE_INTERFACE_CONSISTENT, true, false, false);
    }

    @Bean
    public Binding binding() {
        return new Binding(QUEUE_INTERFACE_CONSISTENT,
                Binding.DestinationType.QUEUE,
                EXCHANGE_INTERFACE_CONSISTENT, ROUTING_KEY_INTERFACE_CONSISTENT, null);
    }

    /**
     * 定义消息的序列化json方式
     */
    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
