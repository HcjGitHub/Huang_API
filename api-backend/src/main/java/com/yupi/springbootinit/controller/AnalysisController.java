package com.yupi.springbootinit.controller;

/**
 * @author 兕神
 * DateTime: 2024/4/16
 */

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.anyan.apicommon.model.entity.InterfaceInfo;
import com.anyan.apicommon.model.entity.User;
import com.anyan.apicommon.model.entity.UserInterfaceInfo;
import com.anyan.apicommon.service.ApiOrderService;
import com.anyan.apicommon.vo.OrderVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.excel.InterfaceInfoBuyExcel;
import com.yupi.springbootinit.model.excel.InterfaceInfoInvokeExcel;
import com.yupi.springbootinit.model.vo.UserInterfaceInfoVO;
import com.yupi.springbootinit.service.InterfaceInfoService;
import com.yupi.springbootinit.service.UserInterfaceInfoService;
import com.yupi.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分析接口调用统计接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @DubboReference
    private ApiOrderService apiOrderService;

    /**
     * 获取top3的接口调用统计
     *
     * @return
     */
    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<UserInterfaceInfoVO>> listTopInterfaceInfoInvoke() {
        List<UserInterfaceInfo> userInterfaceInfos = userInterfaceInfoService.listTopInterfaceInfoInvoke(3);
        Map<Long, List<UserInterfaceInfo>> map = userInterfaceInfos.stream().collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", map.keySet());
        List<InterfaceInfo> list = interfaceInfoService.list(queryWrapper);

        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<UserInterfaceInfoVO> userInterfaceInfoVOList = list.stream().map(interfaceInfo -> {
            UserInterfaceInfoVO userInterfaceInfoVO = new UserInterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo, userInterfaceInfoVO);
            userInterfaceInfoVO.setTotalNum(map.get(interfaceInfo.getId()).get(0).getTotalNum());


            return userInterfaceInfoVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(userInterfaceInfoVOList);
    }

    /**
     * 生成top100的接口调用统计excel文件
     *
     * @return
     */
    @GetMapping("/top/interface/invoke/excel")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void listTopInterfaceInfoInvokeExcel(HttpServletResponse response) throws IOException {
        List<UserInterfaceInfo> userInterfaceInfos = userInterfaceInfoService.listTopInterfaceInfoInvoke(100);
        Map<Long, List<UserInterfaceInfo>> interfaceInfoIdMap = userInterfaceInfos.stream().collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", interfaceInfoIdMap.keySet());
        List<InterfaceInfo> list = interfaceInfoService.list(queryWrapper);

        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<InterfaceInfoInvokeExcel> interfaceInfoInvokeExcelList = list.stream().map(interfaceInfo -> {
            InterfaceInfoInvokeExcel interfaceInfoInvokeExcel = new InterfaceInfoInvokeExcel();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoInvokeExcel);
            interfaceInfoInvokeExcel.setTotalNum(interfaceInfoIdMap.get(interfaceInfo.getId()).get(0).getTotalNum());

            //创建用户信息
            User user = userService.getById(interfaceInfo.getUserId());
            interfaceInfoInvokeExcel.setUsername(user.getUserName());
            return interfaceInfoInvokeExcel;
        }).sorted((a, b) -> b.getTotalNum() - a.getTotalNum()).collect(Collectors.toList());

        String fileName = "interface_invoke.xlsx";
        writeExcel(response, interfaceInfoInvokeExcelList, InterfaceInfoInvokeExcel.class, fileName);
    }


    /**
     * 获取top3的接口调用统计
     *
     * @return
     */
    @GetMapping("/top/interface/buy")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<OrderVO>> listTopInterfaceInfoBuy() {
        List<OrderVO> orderVOList = apiOrderService.listTopInterfaceInfoBuy(3);

        orderVOList = orderVOList.stream().map(orderVO -> {
            InterfaceInfo interfaceInfo = interfaceInfoService.getById(orderVO.getInterfaceId());
            orderVO.setInterfaceName(interfaceInfo.getName());

            return orderVO;
        }).collect(Collectors.toList());

        return ResultUtils.success(orderVOList);
    }

    /**
     * 生成top100的接口购买统计excel文件
     *
     * @return
     */
    @GetMapping("/top/interface/buy/excel")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void listTopInterfaceInfoBuyExcel(HttpServletResponse response) throws IOException {
        List<OrderVO> orderVOList = apiOrderService.listTopInterfaceInfoBuy(3);

        List<InterfaceInfoBuyExcel> interfaceInfoBuyExcelList = orderVOList.stream().map(orderVO -> {
            InterfaceInfoBuyExcel interfaceInfoBuyExcel = new InterfaceInfoBuyExcel();
            BeanUtils.copyProperties(orderVO, interfaceInfoBuyExcel);

            InterfaceInfo interfaceInfo = interfaceInfoService.getById(orderVO.getInterfaceId());
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoBuyExcel);

            User user = userService.getById(interfaceInfo.getUserId());
            interfaceInfoBuyExcel.setUsername(user.getUserName());

            return interfaceInfoBuyExcel;
        }).sorted((a, b) -> b.getTotal() - a.getTotal()).collect(Collectors.toList());

        String fileName = "interface_buy.xlsx";
        writeExcel(response, interfaceInfoBuyExcelList, InterfaceInfoBuyExcel.class, fileName);
    }


    private void writeExcel(HttpServletResponse response, List list,
                            Class entityClass, String fileName) throws IOException {
        String sheetName = "analysis";
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        //创建ExcelWriter对象
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), entityClass).build();
        //创建工作表
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();

        //写入数据工作表
        excelWriter.write(list, writeSheet);
        // 关闭ExcelWriter对象
        excelWriter.finish();
    }
}
