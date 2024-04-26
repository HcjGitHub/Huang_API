package com.anyan.apiinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.anyan.apiinterface.entity.CommonResponse;
import com.anyan.apiinterface.entity.Image;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @author anyan
 * DateTime: 2024/4/26
 */

@RestController
@RequestMapping("/day")
public class DayController {

    @PostMapping("/wallpaper")
    public String getDayWallpaperUrl() {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("format", "json");
        HttpResponse response = HttpRequest.post("https://tenapi.cn/v2/bing")
                .form(paramMap)
                .execute();
        String body = response.body();
        CommonResponse<Image> imageCommonResponse = new CommonResponse<>();
        CommonResponse commonResponse = JSONUtil.toBean(body, imageCommonResponse.getClass());
        Image data = (Image) commonResponse.getData();
        return data.getUrl();
    }
}
