package com.yupi.springbootinit.listener;

import com.anyan.apicommon.common.UserInterfaceInfoMessage;
import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.service.UserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.io.IOException;

import static com.anyan.apicommon.constant.RabbitmqConstant.QUEUE_INTERFACE_CONSISTENT;

/**
 * 接口调用监听器，如果接口调用失败则需要回滚数据库的接口统计数据
 *
 * @author anyan
 * DateTime: 2024/4/26
 */
@Component
@Slf4j
public class InterfaceInvokeListener {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    //监听queue_sms_code队列，实现接口统计功能
    //生产者是懒加载机制，消费者是饿汉加载机制，二者机制不对应，所以消费者要自行创建队列并加载，否则会报错
    @RabbitListener(queuesToDeclare = {@Queue(QUEUE_INTERFACE_CONSISTENT)})
    public void receiveSms(UserInterfaceInfoMessage userInterfaceInfoMessage, Message message, Channel channel) throws IOException {
        log.info("监听到消息啦，内容是：" + userInterfaceInfoMessage);

        Long userId = userInterfaceInfoMessage.getUserId();
        Long interfaceInfoId = userInterfaceInfoMessage.getInterfaceInfoId();

        boolean result = false;
        try {
            result = userInterfaceInfoService.recoverInvokeCount(userId, interfaceInfoId);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("回滚调用接口次数错误，拒绝消息，重新入队");
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

        if (!result) {
            log.info("回滚调用接口次数错误,不确定消息，重入队");
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }

        //确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
