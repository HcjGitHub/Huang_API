package com.yupi.springbootinit.model.vo;


import com.anyan.apicommon.model.entity.InterfaceInfo;
import lombok.Data;

/**
 * 帖子视图
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */

@Data
public class InterfaceInfoVO extends InterfaceInfo {

    /**
     * 统计每个接口被用户调用的总数
     */
    private Integer totalNum;

    /**
     * 统计用户每个接口剩余调用的总数
     */
    private Integer leftNum;


    /**
     * 计费规则（元/条）
     */
    private Double charging;

    /**
     * 计费Id
     */
    private Long chargingId;

    /**
     * 接口剩余可购买次数
     */
    private String availablePieces;

}
