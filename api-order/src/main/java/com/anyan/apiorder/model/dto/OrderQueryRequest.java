package com.anyan.apiorder.model.dto;

import com.anyan.apicommon.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单查询请求
 *
 */
@Data
public class OrderQueryRequest extends PageRequest implements Serializable {

    /**
     * 交易状态【0->待付款；1->已完成；2->无效订单】
     */
    private String type;

    private static final long serialVersionUID = 1L;
}