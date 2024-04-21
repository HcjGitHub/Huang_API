package com.yupi.springbootinit.common;

import com.yupi.springbootinit.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

/**
 * @author 兕神
 * DateTime: 2024/4/21
 */

public class BeanTest {

    @Test
    public void userTest(){
        User user1 = new User();
        user1.setUserName("hhhhhh");
        User user2 = new User();
        user2.setUserName("fffff");
        user2.setUserAccount("dsdsdsd");
        BeanUtils.copyProperties(user1,user2);
        System.out.println(user2.getUserName());
        System.out.println(user2.getUserAccount());
    }
}
