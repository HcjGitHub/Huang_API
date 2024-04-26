package com.anyan.apiclientsdk.client;

import cn.hutool.http.HttpRequest;
import com.anyan.apiclientsdk.utils.HeaderUtils;

/**
 * @author 兕神
 * DateTime: 2024/4/10
 */

public class DayApiClient {

    private static String accessKey;
    private static String secretKey;

    public DayApiClient(String accessKey, String secretKey) {
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


    public String getDayWallpaperUrl() {
        return HttpRequest.post(GATEWAY_HOST+BASIS_PATH+"/day/wallpaper")
                .addHeaders(HeaderUtils.getHeader("image",accessKey,secretKey))
                .execute().body();
    }


}
