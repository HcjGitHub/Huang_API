package com.anyan.apiinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.anyan.apiinterface.entity.Image;
import com.anyan.apiinterface.entity.CommonResponse;
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
@RequestMapping("/random")
public class RandomController {

    /**
     * 获取随机鸡汤
     *
     * @return
     */
    @GetMapping("/text")
    public String getRandomText() {
        HttpResponse response = HttpRequest.get("https://tenapi.cn/v2/yiyan")
                .execute();
        return response.body();
    }

    /**
     * 获取随机动漫图
     *
     * @return
     */
    @PostMapping("/image")
    public String getRandomImageUrl() {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("format", "json");
        HttpResponse response = HttpRequest.post("https://tenapi.cn/v2/acg")
                .form(paramMap)
                .execute();
        String body = response.body();
        CommonResponse<Image> imageCommonResponse = new CommonResponse<>();
        CommonResponse commonResponse = JSONUtil.toBean(body, imageCommonResponse.getClass());
        Image data = (Image) commonResponse.getData();
        return data.getUrl();
    }
}
