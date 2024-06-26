package com.anyan.apicommon.constant;

/**
 * 消息队列相关静态变量
 *
 * @author 兕神
 * DateTime: 2024/4/23
 */
public interface RabbitmqConstant {
    /* 订单相关 */
    String EXCHANGE_ORDER_PAY_SUCCESS = "exchange_order_pay_success";
    String QUEUE_ORDER_PAY_SUCCESS = "queue_order_pay_success";
    String ROUTING_KEY_ORDER_PAY_SUCCESS = "routing_key_order_pay_success";

    /* 短信相关 */
    String EXCHANGE_SMS_INFORM ="exchange_sms_inform";
    String QUEUE_LOGIN_SMS = "queue_sms_code";
    String ROUTING_KEY_SMS ="inform_login_sms";

    /*接口数据一致性相关*/
    String EXCHANGE_INTERFACE_CONSISTENT = "exchange_interface_consistent";
    String QUEUE_INTERFACE_CONSISTENT = "queue_interface_consistent";
    String ROUTING_KEY_INTERFACE_CONSISTENT = "routing_key_interface_consistent";
}
