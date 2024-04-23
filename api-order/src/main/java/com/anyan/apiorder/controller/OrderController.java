package com.anyan.apiorder.controller;

/**
 * @author 兕神
 * DateTime: 2024/4/22
 */

import com.anyan.apicommon.common.BaseResponse;
import com.anyan.apicommon.common.ResultUtils;
import com.anyan.apicommon.constant.UserConstant;
import com.anyan.apicommon.model.entity.Order;
import com.anyan.apicommon.model.entity.User;
import com.anyan.apicommon.vo.OrderVO;
import com.anyan.apiorder.model.dto.OrderAddRequest;
import com.anyan.apiorder.model.dto.OrderQueryRequest;
import com.anyan.apiorder.service.OrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 订单接口
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 添加接口订单
     *
     * @param orderAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceOrder(@RequestBody OrderAddRequest orderAddRequest, HttpServletRequest request) {
        Long orderId = orderService.addOrder(orderAddRequest, request);
        return ResultUtils.success(orderId);
    }

    /**
     * 分页获取当前登录订单列表
     *
     * @param orderQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<OrderVO>> listOrderByPage(@RequestBody OrderQueryRequest orderQueryRequest,
                                                     HttpServletRequest request) {
        Page<OrderVO> orderPage = orderService.listOrderByPage(orderQueryRequest,request);
        return ResultUtils.success(orderPage);
    }
}
