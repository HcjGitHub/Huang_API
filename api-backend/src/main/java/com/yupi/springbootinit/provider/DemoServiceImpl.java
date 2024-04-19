package com.yupi.springbootinit.provider;


import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author 兕神
 * DateTime: 2024/4/14
 */
@DubboService
public class DemoServiceImpl implements DemoService {

    @Override
    public String sayHello(String name) {
        return "hello " + name;
    }
}
