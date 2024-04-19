package com.anyan.apiclientsdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.anyan.apiclientsdk.model.User;
import com.anyan.apiclientsdk.utils.SignUtils;

import java.util.HashMap;
import java.util.Map;

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

    public static final String LOCAL_ADDRESS = "http://localhost:8030";

    public String getNameApi(String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        return HttpUtil.get(LOCAL_ADDRESS + "/api/name/", map);
    }


    public String postNameApi(User user) {
        String json = JSONUtil.toJsonStr(user);
        HttpResponse response = HttpRequest.post(LOCAL_ADDRESS + "/api/name/user/")
                .addHeaders(getHeader(json))
                .body(json)
                .execute();

        System.out.println("状态码：" + response.getStatus());
        return response.body();
    }

    private Map<String, String> getHeader(String body) {
        Map<String, String> map = new HashMap<>();
        map.put("accessKey", accessKey);
        //密钥一定不要明文传输
//        map.put("secretKey", secretKey);
        map.put("body", body);
        map.put("nonce", RandomUtil.randomNumbers(5));
        map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        map.put("sign", SignUtils.getSign(body, secretKey));
        return map;
    }

}
