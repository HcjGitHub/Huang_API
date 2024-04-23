package com.anyan.apicommon.service;

import com.anyan.apicommon.model.entity.InterfaceInfo;
import com.anyan.apicommon.model.entity.User;

/**
 * 接口管理平台提供的对外开发接口
 *
 * @author 兕神
 * DateTime: 2024/4/23
 */
public interface ApiBackendService {

    /**
     * 从数据库根据ak查询sk
     *
     * @param accessKey
     * @return
     */
    User getUserByAccessKey(String accessKey);

    /**
     * 查询请求接口是否存在
     *
     * @param url
     * @param method
     * @return
     */
    InterfaceInfo getInterfaceInfoByUrlAndMethod(String url, String method);

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

    /**
     * 根据id获取接口
     *
     * @param interfaceInfoId
     * @return
     */
    InterfaceInfo getInterfaceInfoById(Long interfaceInfoId);

    // region 对接口库存的操作

    /**
     * 根据id获取接口库存
     *
     * @param interfaceInfoId
     * @return
     */
    int getInterfaceStockById(Long interfaceInfoId);

    /**
     * 下订单   扣取接口库存
     *
     * @param interfaceInfoId
     * @param num
     * @return
     */
    boolean deductInterfaceStock(Long interfaceInfoId, Integer num);

    /**
     * 超时订单 回滚接口库存
     *
     * @param interfaceInfoId
     * @param num
     * @return
     */
    boolean recoverInterfaceStock(Long interfaceInfoId, Integer num);
    // endregion

    /**
     * 给用户分配指定数量接口调用
     *
     * @param userId
     * @param interfaceInfoId
     * @param num
     * @return
     */
    boolean updateUserInterfaceInfoInvokeCount(Long userId, Long interfaceInfoId, Integer num);

}
