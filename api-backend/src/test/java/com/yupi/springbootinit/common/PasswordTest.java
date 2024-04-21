package com.yupi.springbootinit.common;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.yupi.springbootinit.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

/**
 * @author 兕神
 * DateTime: 2024/4/17
 */
//@SpringBootTest
public class PasswordTest {

    @Test
    public void productPassword(){
        String userPassword = " 12345678";
        System.out.println(userPassword.trim());
        String encryptPassword = DigestUtils.md5DigestAsHex(("huang" + userPassword).getBytes());
        System.out.println(encryptPassword);
    }

    @Test
    public void JsonUtilTest(){
        User user = new User();
        user.setId(1l);
        user.setUserName("vfbvhf");
        Gson gson = new Gson();
        String s = gson.toJson(user);
        System.out.println(s);
        User user1 = gson.fromJson(s, User.class);
        System.out.println(user==user1);
        System.out.println(user1);
    }
}
