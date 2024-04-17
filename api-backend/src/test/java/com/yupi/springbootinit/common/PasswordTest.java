package com.yupi.springbootinit.common;

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
        String userPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex(("yupi" + userPassword).getBytes());
        System.out.println(encryptPassword);
    }
}
