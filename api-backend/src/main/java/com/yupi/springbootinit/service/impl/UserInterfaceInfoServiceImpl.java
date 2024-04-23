package com.yupi.springbootinit.service.impl;

import java.util.Date;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.UserInterfaceInfoMapper;
import com.anyan.apicommon.model.entity.UserInterfaceInfo;
import com.yupi.springbootinit.model.dto.userinterfaceinfo.UserInterfaceInfoUpdateDTO;
import com.yupi.springbootinit.service.UserInterfaceInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 兕神
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
 * @createDate 2024-04-11 23:15:43
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {
    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {

        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 创建时，参数不能为空
        if (add) {
            if (userInterfaceInfo.getUserId() < 0 || userInterfaceInfo.getInterfaceInfoId() < 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id 或 接口id小于0");
            }
        }
        // 有参数则校验

        if (userInterfaceInfo.getLeftNum() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分配接口调用次数小于0");
        }
    }

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {

        if (interfaceInfoId < 0 || userId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceInfoId", interfaceInfoId);
        updateWrapper.eq("userId", userId);
        updateWrapper.gt("leftNum", 0);
        updateWrapper.setSql("totalNum = totalNum + 1, leftNum = leftNum - 1");
        return this.update(updateWrapper);
    }

    @Override
    public List<UserInterfaceInfo> statisticsTopTotalNum(int limit) {
        return userInterfaceInfoMapper.statisticsTopTotalNum(limit);
    }

    @Override
    public boolean updateUserInterfaceInfo(UserInterfaceInfoUpdateDTO userInterfaceInfoUpdateDTO) {
        Long userId = userInterfaceInfoUpdateDTO.getUserId();
        Long interfaceInfoId = userInterfaceInfoUpdateDTO.getInterfaceInfoId();
        Integer lockNum = userInterfaceInfoUpdateDTO.getLockNum();
        if (userId == null || interfaceInfoId == null || lockNum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("interfaceInfoId", interfaceInfoId);
        UserInterfaceInfo selectOne = userInterfaceInfoMapper.selectOne(queryWrapper);

        //selectOne不为空，说明是更新用户接口调用次数
        if (selectOne != null) {
            UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("userId", userId).eq("interfaceInfoId", interfaceInfoId)
                    .setSql("leftNum = leftNum + " + lockNum);
            return this.update(updateWrapper);
        }

        //新建用户接口调用
        if (lockNum < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        userInterfaceInfo.setUserId(userId);
        userInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
        userInterfaceInfo.setLeftNum(lockNum);
        return this.save(userInterfaceInfo);
    }
}




