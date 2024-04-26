package com.yupi.springbootinit.service;

import com.anyan.apicommon.model.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.userinterfaceinfo.UserInterfaceInfoUpdateDTO;
import com.yupi.springbootinit.model.vo.UserInterfaceInfoVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
 * @createDate 2024-04-11 23:15:43
 */
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    /**
     * 校验
     *
     * @param userInterfaceInfo
     * @param add
     */
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * 接口剩余调用次数是否大于0+接口调用次数加1
     *
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 统计调用次数前limit的的接口
     *
     * @param limit
     * @return
     */
    List<UserInterfaceInfo> listTopInterfaceInfoInvoke(int limit);

    /**
     * 创建或更新用户某接口的剩余调用次数
     *
     * @param userInterfaceInfoUpdateDTO
     * @return
     */
    boolean updateUserInterfaceInfo(UserInterfaceInfoUpdateDTO userInterfaceInfoUpdateDTO);

    /**
     * 获取用户所拥有的接口调用
     *
     * @param userId
     * @param request
     * @return
     */
    List<UserInterfaceInfoVO> getUserInterfaceInfoVOById(Long userId, HttpServletRequest request);

    /**
     * 回滚调用接口时接口调用+1
     *
     * @param userId
     * @param interfaceInfoId
     * @return
     */
    boolean recoverInvokeCount(Long userId, Long interfaceInfoId);
}
