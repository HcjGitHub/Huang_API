package com.anyan.common.service;

import com.anyan.common.model.entity.User;

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
