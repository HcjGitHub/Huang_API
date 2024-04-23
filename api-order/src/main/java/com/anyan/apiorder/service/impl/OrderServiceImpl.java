package com.anyan.apiorder.service.impl;


import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.anyan.apicommon.common.ErrorCode;
import com.anyan.apicommon.exception.BusinessException;
import com.anyan.apicommon.model.entity.InterfaceInfo;
import com.anyan.apicommon.model.entity.Order;
import com.anyan.apicommon.model.entity.User;
import com.anyan.apicommon.service.ApiBackendService;
import com.anyan.apicommon.utils.JwtUtils;
import com.anyan.apicommon.vo.OrderVO;
import com.anyan.apiorder.enums.OrderStatusEnum;
import com.anyan.apiorder.mapper.OrderMapper;
import com.anyan.apiorder.model.dto.OrderAddRequest;
import com.anyan.apiorder.model.dto.OrderQueryRequest;
import com.anyan.apiorder.service.OrderService;
import com.anyan.apiorder.utils.OrderMqUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.anyan.apicommon.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 兕神
 * @description 针对表【order】的数据库操作Service实现
 * @createDate 2024-04-22 16:58:51
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private OrderMqUtils orderMqUtils;

    @DubboReference
    private ApiBackendService apiBackendService;

    public static final Gson gson = new Gson();

    @Transactional
    @Override
    public Long addOrder(OrderAddRequest orderAddRequest, HttpServletRequest request) {

        //1.检验参数是否为空 购买次数和价格不能小于零
        if (orderAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long userId = orderAddRequest.getUserId();
        Long interfaceId = orderAddRequest.getInterfaceId();
        Double charging = orderAddRequest.getCharging();
        Integer count = orderAddRequest.getCount();
        BigDecimal totalAmount = orderAddRequest.getTotalAmount();
        if (userId == null || interfaceId == null || count == null || totalAmount == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //购买次数和价格不能小于零
        if (count < 0 || totalAmount.compareTo(new BigDecimal(0)) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //2.检验用户是否登录
        User user = getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //3.检验接口是否存在
        InterfaceInfo interfaceInfo = apiBackendService.getInterfaceInfoById(interfaceId);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口不存在");
        }

        //4.后端检验总价是否计算正确
        Double computeAmount = charging * count;
        BigDecimal bd = new BigDecimal(computeAmount);
        double finalAmount = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (finalAmount != totalAmount.doubleValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "总价计算错误");
        }
        //5.检验接口库存是否足够
        int interfaceStock = apiBackendService.getInterfaceStockById(interfaceId);
        if (interfaceStock < 0 || interfaceStock - count < 0) {
            //5.2 报异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口库存不足");
        }
        //5.1 库存足够，则扣减库存
        boolean deductStatus = apiBackendService.deductInterfaceStock(interfaceId, count);
        if (!deductStatus) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口库存扣减失败");
        }
        //6.创建订单，持久化保存
        Order order = new Order();
        order.setUserId(userId);
        order.setInterfaceId(interfaceId);
        order.setCount(count);
        order.setTotalAmount(totalAmount);
        //订单号
        order.setOrderSn(generateOrderNum(userId));
        order.setCharging(charging);
        orderMapper.insert(order);

        //7.发送订单事件到消息队列
        orderMqUtils.sendOrderInfo(order);
        //8.返回结果
        return order.getId();
    }

    @Override
    public Page<OrderVO> listOrderByPage(OrderQueryRequest orderQueryRequest, HttpServletRequest request) {
        //获取订单类型
        Integer type = Integer.parseInt(orderQueryRequest.getType());
        if (!OrderStatusEnum.getValues().contains(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //获取当前登录用户
        User user = getLoginUser(request);

        int current = orderQueryRequest.getCurrent();
        int pageSize = orderQueryRequest.getPageSize();
        Page<Order> page = new Page<>(current, pageSize);
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        Long userId = user.getId();
        queryWrapper.eq("userId", userId).eq("status", type);
        Page<Order> orderPage = orderMapper.selectPage(page, queryWrapper);

        Page<OrderVO> orderVOPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<OrderVO> orderVOList = orderPage.getRecords().stream().map(order -> {
            OrderVO orderVO = new OrderVO();

            Long interfaceId = order.getInterfaceId();
            InterfaceInfo interfaceInfo = apiBackendService.getInterfaceInfoById(interfaceId);
            orderVO.setInterfaceId(interfaceId);
            orderVO.setUserId(userId);
            orderVO.setOrderNumber(order.getOrderSn());
            orderVO.setTotal(order.getCount());
            orderVO.setCharging(order.getCharging());
            orderVO.setTotalAmount(order.getTotalAmount().doubleValue());
            orderVO.setStatus(type);
            orderVO.setCreateTime(order.getCreateTime());
            orderVO.setExpirationTime(DateUtil.offset(order.getCreateTime(), DateField.MINUTE,30));

            orderVO.setInterfaceName(interfaceInfo.getName());
            orderVO.setInterfaceDesc(interfaceInfo.getDescription());
            return orderVO;
        }).collect(Collectors.toList());
        orderVOPage.setRecords(orderVOList);
        return orderVOPage;
    }

    /**
     * 生成订单号
     *
     * @return
     */
    private String generateOrderNum(Long userId) {
        String timeId = IdWorker.getTimeId();
        String substring = timeId.substring(0, timeId.length() - 15);
        return substring + RandomUtil.randomNumbers(5) + userId;
    }

    /**
     * 从redis中获取登录用户
     *
     * @param request
     * @return
     */
    private User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        Long userId = JwtUtils.parserUserIdByToken(request);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        //获取redis中User,防止该用户被删除
        String userJson = stringRedisTemplate.opsForValue().get(USER_LOGIN_STATE + userId);
        User currentUser = gson.fromJson(userJson, User.class);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
}




