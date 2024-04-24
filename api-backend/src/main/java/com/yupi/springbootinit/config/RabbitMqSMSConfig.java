package com.yupi.springbootinit.config;

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

import static com.anyan.apicommon.constant.RabbitmqConstant.*;

/**
 * @author anyan
 * DateTime: 2024/4/24
 */
@Configuration
public class RabbitMqSMSConfig {

    /**
     * 短信交换机
     *
     * @return
     */
    @Bean(EXCHANGE_SMS_INFORM)
    public Exchange exchange_sms_inform() {
        return new DirectExchange(EXCHANGE_SMS_INFORM, true, false);
    }

    /**
     * 短信队列
     *
     * @return
     */
    @Bean(QUEUE_LOGIN_SMS)
    public Queue queue_sms_code() {
//        Map<String, Object> args = new HashMap<>();
//        //信息存活时间30分钟
//        args.put("x-message-ttl", 2 * 60000);
        return new Queue(QUEUE_LOGIN_SMS, true, false, false);
    }

    @Bean
    public Binding inform_login_sms() {
        return new Binding(QUEUE_LOGIN_SMS,
                Binding.DestinationType.QUEUE,
                EXCHANGE_SMS_INFORM, ROUTING_KEY_SMS, null);
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
