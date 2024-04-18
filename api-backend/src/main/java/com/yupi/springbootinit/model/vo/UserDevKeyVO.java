package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * ak/sk信息
 *
 * @author 兕神
 * DateTime: 2024/4/18
 */
@Data
public class UserDevKeyVO implements Serializable {
    private static final long serialVersionUID = 6703326011663561616L;

    /**
     * ak
     */
    private String accessKey;
    /**
     * sk
     */
    private String secretKey;
}
