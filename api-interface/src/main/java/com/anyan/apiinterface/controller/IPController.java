package com.anyan.apiinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.anyan.apiinterface.entity.CommonResponse;
import com.anyan.apiinterface.entity.IP;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author anyan
 * DateTime: 2024/4/26
 */
@RestController
@RequestMapping("/ip")
public class IPController {

    /**
     * 获取本地ip地址
     * @return
     */
    @GetMapping("/local")
    public String getLocalIP() {
        HttpResponse response = HttpRequest.post("https://tenapi.cn/v2/getip")
                .execute();
        String body = response.body();
        CommonResponse<IP> imageCommonResponse = new CommonResponse<>();
        CommonResponse commonResponse = JSONUtil.toBean(body, imageCommonResponse.getClass());
        IP data = (IP) commonResponse.getData();
        return data.getIp();
    }
}
