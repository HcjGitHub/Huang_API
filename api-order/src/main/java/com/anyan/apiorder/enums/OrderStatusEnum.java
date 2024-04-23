package com.anyan.apiorder.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单状态枚举类
 *
 * @author 兕神
 * DateTime: 2024/4/23
 */

public enum OrderStatusEnum {
    UNPAID(0, "未支付"),
    PAID(1, "已支付"),
    TIMEOUT_PAID(2, "超时支付");
    private Integer value;
    private String text;


    OrderStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 获取所有的值value
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(OrderStatusEnum::getValue).collect(Collectors.toList());
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
