package com.anyan.apiclientsdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

/**
 * @author 兕神
 * DateTime: 2024/4/10
 */

public class SignUtils {

    public static String getSign(String body, String secretKey) {
        Digester md5 = new Digester(DigestAlgorithm.SHA1);
        return md5.digestHex(body + "." + secretKey);
    }
}
