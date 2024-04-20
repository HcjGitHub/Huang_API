package com.anyan.common.service;

/**
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
 * @createDate 2024-04-11 23:15:43
 */
public interface InnerUserInterfaceInfoService {

    /**
     * 调用接口+1
     *
     * @param interfaceId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceId, long userId);

    /**
     * 获取接口剩余调用次数
     *
     * @param interfaceId
     * @param userId
     * @return
     */
    int getLeftCount(long interfaceId, long userId);

}
