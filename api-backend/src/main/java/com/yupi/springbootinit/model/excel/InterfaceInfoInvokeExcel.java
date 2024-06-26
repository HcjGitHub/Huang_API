package com.yupi.springbootinit.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author anyan
 * DateTime: 2024/4/25
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(30)
@ColumnWidth(25)
public class InterfaceInfoInvokeExcel implements Serializable {
    private static final long serialVersionUID = -5504766653977176865L;
    /**
     * 主键
     */
    @ExcelProperty("接口id")
    private Long id;

    /**
     * 接口名字
     */
    @ExcelProperty("接口名")
    private String name;

    /**
     * 描述
     */
    @ExcelProperty("接口描述")
    private String description;

    /**
     * 接口地址
     */
    @ExcelProperty("接口地址")
    private String url;

    /**
     * 创建者
     */
    @ExcelProperty("创建者id")
    private Long userId;

    /**
     * 创建者名称
     */
    @ExcelProperty("创建者名称")
    private String username;

    /**
     * 接口状态（0 - 关闭， 1 - 开启））
     */
    @ExcelProperty("接口状态（0 - 关闭， 1 - 开启）")
    private Integer status;

    /**
     * 请求类型
     */
    @ExcelProperty("请求类型")
    private String method;

    /**
     * 总已调用次数
     */
    @ExcelProperty("已调用次数")
    private Integer totalNum;

    /**
     * 创建时间
     */
    @ExcelProperty("创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @ExcelProperty("更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
