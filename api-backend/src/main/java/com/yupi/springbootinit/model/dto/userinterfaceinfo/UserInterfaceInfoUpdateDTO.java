package com.yupi.springbootinit.model.dto.userinterfaceinfo;

import lombok.Data;

/**
 * 更新请求用户剩余接口调用次数
 *
 * @author 兕神
 * DateTime: 2024/4/23
 */
@Data
public class UserInterfaceInfoUpdateDTO {
    private static final long serialVersionUID = 1472097902521779075L;

    private Long userId;

    private Long interfaceInfoId;

    //leftNum的更新数量或创建时数量
    private Integer lockNum;
}
