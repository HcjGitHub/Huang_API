package com.anyan.apiorder.mapper;


import com.anyan.apicommon.model.entity.Order;
import com.anyan.apicommon.vo.OrderVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 兕神
* @description 针对表【order】的数据库操作Mapper
* @createDate 2024-04-22 16:58:51
* @Entity com.anyan.apiorder.model.domain.Order
*/
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 获取status状态下次数
     * @param status
     * @return
     */
    List<OrderVO> listTopInterfaceInfoByStatus(@Param("status") Integer status, @Param("limit") Integer limit);
}




