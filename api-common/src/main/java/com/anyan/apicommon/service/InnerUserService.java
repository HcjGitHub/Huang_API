package com.anyan.apicommon.service;

import com.anyan.apicommon.model.entity.User;

/**
 * @author 兕神
 * DateTime: 2024/4/16
 */
public interface InnerUserService {

    /**
     * 从数据库根据ak查询sk
     *
     * @param accessKey
     * @return
     */
    User getUserByAccessKey(String accessKey);

}
