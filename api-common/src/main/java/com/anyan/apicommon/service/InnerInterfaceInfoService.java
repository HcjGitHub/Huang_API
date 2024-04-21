package com.anyan.apicommon.service;

import com.anyan.apicommon.model.entity.InterfaceInfo;

/**
 * @author 兕神
 * DateTime: 2024/4/16
 */
public interface InnerInterfaceInfoService {

    /**
     * 查询请求接口是否存在
     *
     * @param url
     * @param method
     * @return
     */
    InterfaceInfo getInterfaceInfoByUrlAndMethod(String url, String method);

}