package com.anyan.apithirdparty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.anyan.apithirdparty.mapper")
@EnableConfigurationProperties
public class ApiThirdPartyApplication {

    public static void main(String[] args) {
        //将dubbo缓存的绝对目录改成相对目录，避免后续项目上线出现问题 已实现
        String rootPath = System.getProperty("user.dir");
        String subDirectory = "/api-third-party/thirdPartyDubboCache";
        String fullPath = rootPath + "/" + subDirectory;
        System.setProperty("user.home", fullPath);
        SpringApplication.run(ApiThirdPartyApplication.class, args);
    }

}
