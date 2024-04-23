package com.anyan.apiorder;

import com.anyan.apicommon.model.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static com.anyan.apiorder.config.RabbitmqConfig.EXCHANGE_ORDER_PAY;
import static com.anyan.apiorder.config.RabbitmqConfig.ROUTING_KEY_ORDER_PAY;

@SpringBootTest
class ApiOrderApplicationTests {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {
        Order object = new Order();
        object.setId(76l);
        object.setCharging(65.0);
        rabbitTemplate.convertAndSend(EXCHANGE_ORDER_PAY,ROUTING_KEY_ORDER_PAY, object);
    }

}
