package com.anyan.apithirdparty.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 兕神
 * DateTime: 2024/4/23
 */

@Data
public class AlipayRequest implements Serializable {
    private static final long serialVersionUID = -8597630489529830444L;

    private String traceNo;
    private double totalAmount;
    private String subject;
    private String alipayTraceNo;
}
