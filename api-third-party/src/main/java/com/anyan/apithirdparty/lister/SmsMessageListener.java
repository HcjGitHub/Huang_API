package com.anyan.apithirdparty.lister;

import com.anyan.apicommon.common.SmsMessage;
import com.anyan.apithirdparty.utils.MailClientUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.anyan.apicommon.constant.RabbitmqConstant.QUEUE_LOGIN_SMS;
import static com.anyan.apicommon.constant.RedisConstant.CODE_REGISTER_PRE;

/**
 * 监听发送code队列的消息
 *
 * @author anyan
 * DateTime: 2024/4/23
 */
@Slf4j
@Component
public class SmsMessageListener {

    @Resource
    private MailClientUtils mailClientUtils;

    //queuesToDeclare注解可以让监听者自己定义自己监听的队列，并且生产者不会重复定义队列
    @RabbitListener(queuesToDeclare = {@Queue(QUEUE_LOGIN_SMS)})
    public void sendCode(SmsMessage smsMessage, Message message, Channel channel) throws IOException {
        String email = smsMessage.getEmail();
        String code = smsMessage.getCode();
        log.info("监听发送code队列的消息，email:{},code:{}", email, code);

        //发送消息
        mailClientUtils.sendMail(smsMessage.getEmail(), "LaoHuang API",
                "亲爱的 <span style='color: blue'>" + email + "</span> 用户，您的注册 or 登录 验证码为：" + code + ",验证码5分钟内有效!!!" + "[Chen API]");

        //确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
