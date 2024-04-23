package com.anyan.apiorder.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单创建请求
 *
 * @author 兕神
 * DateTime: 2024/4/22
 */
@Data
public class OrderAddRequest {


    /**
     * 用户id
     */
    private Long userId;

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 计费Id
     */
    private Long chargingId;

    /**
     * 单价
     */
    private Double charging;

    /**
     * 购买数量
     */
    private Integer count;

    /**
     * 订单应付价格
     */
    private BigDecimal totalAmount;
}

