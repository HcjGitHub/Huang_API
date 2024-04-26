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
        return "你的名字是：" + user.getName();
    }
}
