package com.anyan.apiorder;

import com.anyan.apicommon.vo.OrderVO;
import com.anyan.apiorder.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author anyan
 * DateTime: 2024/4/25
 */
@SpringBootTest
public class AnalysisOrderTest {

    @Resource
    private OrderMapper orderMapper;

    @Test
    public void test(){
        List<OrderVO> orderVOS = orderMapper.listTopInterfaceInfoByStatus(1, 3);
        for (OrderVO orderVO : orderVOS) {
            System.out.println(orderVO);
        }
    }
}
