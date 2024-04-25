package com.anyan.apiorder.provider;

import com.anyan.apicommon.service.ApiOrderService;
import com.anyan.apicommon.vo.OrderVO;
import com.anyan.apiorder.enums.OrderStatusEnum;
import com.anyan.apiorder.service.OrderService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author anyan
 * DateTime: 2024/4/25
 */
@DubboService
public class ApiOrderProvider implements ApiOrderService {

    @Resource
    private OrderService orderService;

    @Override
    public List<OrderVO> listTopInterfaceInfoBuy(Integer limit) {
        return orderService.listTopInterfaceInfoByStatus(OrderStatusEnum.PAID.getValue(), limit);
    }
}
