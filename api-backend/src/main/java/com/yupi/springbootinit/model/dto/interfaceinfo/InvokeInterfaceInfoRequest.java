package com.yupi.springbootinit.model.dto.interfaceinfo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 测试数据传输
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class InvokeInterfaceInfoRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    private String userRequestParams;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}