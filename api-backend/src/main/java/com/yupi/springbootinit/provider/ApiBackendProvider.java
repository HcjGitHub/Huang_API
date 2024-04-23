package com.yupi.springbootinit.provider;

import com.anyan.apicommon.model.entity.InterfaceCharging;
import com.anyan.apicommon.model.entity.InterfaceInfo;
import com.anyan.apicommon.model.entity.User;
import com.anyan.apicommon.model.entity.UserInterfaceInfo;
import com.anyan.apicommon.service.ApiBackendService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.InterfaceInfoMapper;
import com.yupi.springbootinit.mapper.UserMapper;
import com.yupi.springbootinit.model.dto.userinterfaceinfo.UserInterfaceInfoUpdateDTO;
import com.yupi.springbootinit.service.InterfaceChargingService;
import com.yupi.springbootinit.service.UserInterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * 接口管理平台提供的对外开发接口实现
 *
 * @author 兕神
 * DateTime: 2024/4/23
 */
@DubboService
public class ApiBackendProvider implements ApiBackendService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Resource
    private InterfaceChargingService interfaceChargingService;

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;


    @Override
    public User getUserByAccessKey(String accessKey) {
        if (StringUtils.isAnyBlank(accessKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accessKey", accessKey);
        return userMapper.selectOne(queryWrapper);
    }


    @Override
    public InterfaceInfo getInterfaceInfoByUrlAndMethod(String url, String method) {

        if (StringUtils.isAnyBlank(url, method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", url).eq("method", method);
        return interfaceInfoMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        return userInterfaceInfoService.invokeCount(interfaceInfoId, userId);
    }

    @Override
    public int getLeftCount(long interfaceInfoId, long userId) {
        if (interfaceInfoId < 0 || userId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("interfaceInfoId", interfaceInfoId);
        queryWrapper.eq("userId", userId);
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getOne(queryWrapper);
        if (userInterfaceInfo == null) {
            return 0;
        }
        return userInterfaceInfo.getLeftNum();
    }

    @Override
    public InterfaceInfo getInterfaceInfoById(Long interfaceInfoId) {
        return interfaceInfoMapper.selectById(interfaceInfoId);
    }

    @Override
    public int getInterfaceStockById(Long interfaceInfoId) {
        QueryWrapper<InterfaceCharging> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("interfaceId", interfaceInfoId);
        InterfaceCharging interfaceCharging = interfaceChargingService.getOne(queryWrapper);
        if (interfaceCharging == null || StringUtils.isBlank(interfaceCharging.getAvailablePieces())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口不存在");
        }
        return Integer.parseInt(interfaceCharging.getAvailablePieces());
    }

    @Override
    public boolean deductInterfaceStock(Long interfaceInfoId, Integer num) {
        UpdateWrapper<InterfaceCharging> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceId", interfaceInfoId).gt("availablePieces", num)
                .setSql("availablePieces = availablePieces - " + num);
        return interfaceChargingService.update(updateWrapper);
    }

    @Override
    public boolean recoverInterfaceStock(Long interfaceInfoId, Integer num) {
        UpdateWrapper<InterfaceCharging> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceId", interfaceInfoId)
                .setSql("availablePieces = availablePieces + " + num);
        return interfaceChargingService.update(updateWrapper);
    }

    @Override
    public boolean updateUserInterfaceInfoInvokeCount(Long userId, Long interfaceInfoId, Integer num) {
        UserInterfaceInfoUpdateDTO userInterfaceInfo = new UserInterfaceInfoUpdateDTO();
        userInterfaceInfo.setUserId(userId);
        userInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
        userInterfaceInfo.setLockNum(num);
        return userInterfaceInfoService.updateUserInterfaceInfo(userInterfaceInfo);
    }
}
