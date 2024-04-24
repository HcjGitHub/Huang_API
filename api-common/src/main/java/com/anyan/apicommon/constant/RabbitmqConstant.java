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

}
