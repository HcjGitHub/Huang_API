package com.anyan.apicommon.service;

import com.anyan.apicommon.vo.OrderVO;

import java.util.List;

/**
 * 订单管理提供的对外开发接口
 *
 * @author anyan
 * DateTime: 2024/4/25
 */
public interface ApiOrderService {

    /**
     * 获取购买接口次数前limit的接口
     *
     * @param limit
     * @return 接口名+购买次数
     */
    List<OrderVO> listTopInterfaceInfoBuy(Integer limit);
}
