package com.anyan.apiinterface.entity;

import lombok.Data;

/**
 * @author anyan
 * DateTime: 2024/4/26
 */
@Data
public class CommonResponse<T> {

    private String code;
    private String msg;

    private T data;
}
