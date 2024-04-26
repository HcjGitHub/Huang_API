package com.anyan.apicommon.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author anyan
 * DateTime: 2024/4/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInterfaceInfoMessage {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 接口id
     */
    private Long interfaceInfoId;
}
