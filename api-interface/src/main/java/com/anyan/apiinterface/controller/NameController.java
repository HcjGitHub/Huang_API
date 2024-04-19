package com.anyan.apiinterface.controller;


import com.anyan.apiclientsdk.model.User;
import com.anyan.apiclientsdk.utils.SignUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 兕神
 * DateTime: 2024/4/10
 */
@RestController
@RequestMapping("/name")
public class NameController {


    @GetMapping("/")
    public String getName(String name) {
        return "Get 你的名字是：" + name;
    }

    @PostMapping("/user")
    public String postName(@RequestBody User user, HttpServletRequest request) {

        String sign = request.getHeader("sign");
        String body = request.getHeader("body");
        String accessKeys = request.getHeader("accessKey");
        String secretKey = "70bf15b1924986aba2260b1f262bb71c";
        String accessKey = "d12cbb4792fc7eb89139e18c0cb74020";
        secretKey = SignUtils.getSign(body, secretKey);

        if (!sign.equals(secretKey) || !accessKey.equals(accessKeys)) {
            throw new RuntimeException("参数不正确");
        }
        return "你的名字是：" + user.getName();
    }
}
