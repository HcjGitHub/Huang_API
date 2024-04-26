package com.anyan.apiinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @author anyan
 * DateTime: 2024/4/26
 */
@RestController
@RequestMapping("/qq")
public class QQController {

    /**
     * 获取qq头像地址
     *
     * @param qq QQ号
     * @return
     */
    @PostMapping("/avatar")
    public String getAvatarUrl(String qq) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("qq", qq);
        HttpResponse response = HttpRequest.post("https://tenapi.cn/v2/qqimg")
                .form(paramMap)
                .execute();
        return response.body();
    }
}
