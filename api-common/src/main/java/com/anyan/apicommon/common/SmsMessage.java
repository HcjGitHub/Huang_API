package com.anyan.apicommon.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 邮箱短信消息对象
 *
 * @author 兕神
 * DateTime: 2024/4/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsMessage implements Serializable {

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String code;
}
