package com.anyan.apiorder.listener;

import com.anyan.apicommon.model.entity.Order;
import com.anyan.apicommon.service.ApiBackendService;
import com.anyan.apiorder.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.io.IOException;

import static com.anyan.apiorder.config.RabbitmqConfig.QUEUE_DLX_ORDER_PAY;
import static com.anyan.apiorder.enums.OrderStatusEnum.TIMEOUT_PAID;
import static com.anyan.apiorder.enums.OrderStatusEnum.UNPAID;

/**
 * 订单到期的处理 未支付-回滚 支付-确认
 *
 * @author 兕神
 * DateTime: 2024/4/23
 */
@Slf4j
@Component
public class OrderTimeoutListener {

    @Resource
    private OrderService orderService;

    @DubboReference
    private ApiBackendService apiBackendService;

    //监听queue_order_dlx_queue死信队列，实现支付超时的回滚功能
    //queuesToDeclare注解可以让监听者自己定义自己监听的队列，并且生产者不会重复定义队列
    @RabbitListener(queuesToDeclare = {@Queue(QUEUE_DLX_ORDER_PAY)})
    public void receiveOrderMsg(Order order, Message message, Channel channel) throws IOException {
        //获取订单id
        Long orderId = order.getId();
        //获取最新订单对象
        Order newOrder = orderService.getById(orderId);
        //若订单状态为未支付则回滚接口调用次数
        if (newOrder.getStatus().equals(UNPAID.getValue())) {
            Long interfaceId = order.getInterfaceId();
            Integer count = order.getCount();

            try {
                //回滚接口调用次数
                boolean success = apiBackendService.recoverInterfaceStock(interfaceId, count);
                if (!success) {
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
                }
                //更新订单状态
                UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", newOrder.getId());
                updateWrapper.set("status", TIMEOUT_PAID.getValue());
                orderService.update(updateWrapper);
                //确认该消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                e.printStackTrace();
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            }
        } else {
            //若订单状态为已支付或超时支付，则确认该消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }

    }
}
