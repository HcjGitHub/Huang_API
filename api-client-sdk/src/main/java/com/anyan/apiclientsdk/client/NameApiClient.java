package com.anyan.apiclientsdk.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.anyan.apiclientsdk.model.User;
import com.anyan.apiclientsdk.utils.HeaderUtils;

/**
 * @author 兕神
 * DateTime: 2024/4/10
 */

public class NameApiClient {

    private static String accessKey;
    private static String secretKey;

    public NameApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    /**
     * 网关host
     */
    public static final String GATEWAY_HOST = "http://localhost:8040";
    /**
     * 用于区分接口管理和模拟接口平台
     */
    public static final String BASIS_PATH = "/api/interface";

    public String getNameApi(String name) {
        HttpRequest httpRequest = HttpUtil.createGet(GATEWAY_HOST + BASIS_PATH + "/name/?name=" + name);
        HttpResponse response = httpRequest
                .addHeaders(HeaderUtils.getPostHeader(name, accessKey, secretKey))
                .execute();
        return response.body();
    }


    public String postNameApi(User user) {
        String json = JSONUtil.toJsonStr(user);
        HttpResponse response = HttpRequest.post(GATEWAY_HOST + BASIS_PATH + "/name/user/")
                .addHeaders(HeaderUtils.getPostHeader(json, accessKey, secretKey))
                .body(json)
                .execute();
        return response.body();
    }


}
