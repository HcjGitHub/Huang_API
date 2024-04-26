package com.anyan.apiclientsdk.utils;

import cn.hutool.core.util.RandomUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求头参数构造类
 *
 * @author 兕神
 * DateTime: 2024/4/20
 */

public class HeaderUtils {
    public static Map<String, String> getHeader(String body, String accessKey, String secretKey) {
        Map<String, String> map = new HashMap<>();
        map.put("accessKey", accessKey);
        //密钥一定不要明文传输
//        map.put("secretKey", secretKey);
        map.put("body", body);
        map.put("nonce", RandomUtil.randomNumbers(5));
        //当下时间/1000，时间戳大概10位
        map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        map.put("sign", SignUtils.getSign(body, secretKey));
        return map;
    }
}
