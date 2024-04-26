package com.yupi.springbootinit.model.dto.interfaceinfo;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoQueryRequest extends PageRequest implements Serializable {
    /**
     * 主键
     */
    private Long id;
    /**
     * 接口名字
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 接口地址
     */
    private String url;


    /**
     * 创建者
     */
    private Long userId;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 接口状态（0 - 关闭， 1 - 开启））
     */
    private Integer status;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 接口对应的SDK类路径
     */
    private String sdk;

    /**
     * 参数示例
     */
    private String parameterExample;

}