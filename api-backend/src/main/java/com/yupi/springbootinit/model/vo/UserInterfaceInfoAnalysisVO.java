package com.yupi.springbootinit.model.vo;

import lombok.Data;

/**
 * @author anyan
 * DateTime: 2024/4/25
 */
@Data
public class UserInterfaceInfoAnalysisVO extends UserInterfaceInfoVO{

    /**
     * 统计每个接口被用户调用的总数
     */
    private Integer sumNum;
}
