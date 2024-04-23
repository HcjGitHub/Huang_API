package com.anyan.apiorder.service;


import com.anyan.apicommon.model.entity.Order;
import com.anyan.apicommon.vo.OrderVO;
import com.anyan.apiorder.model.dto.OrderAddRequest;
import com.anyan.apiorder.model.dto.OrderQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @description 针对表【order】的数据库操作Service
 * @createDate 2024-04-22 16:58:51
 */
public interface OrderService extends IService<Order> {

    /**
     * 添加订单
     *
     * @param orderAddRequest
     * @param request
     * @return
     */
    Long addOrder(OrderAddRequest orderAddRequest, HttpServletRequest request);

    /**
     * 分页获取当前登录订单数据
     *
     * @param orderQueryRequest
     * @param request
     * @return
     */
    Page<OrderVO> listOrderByPage(OrderQueryRequest orderQueryRequest, HttpServletRequest request);
}
