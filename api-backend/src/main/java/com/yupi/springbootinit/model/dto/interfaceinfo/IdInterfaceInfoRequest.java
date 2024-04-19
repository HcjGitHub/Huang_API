package com.yupi.springbootinit.model.dto.interfaceinfo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 封装id 给前端好传输数据
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class IdInterfaceInfoRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}