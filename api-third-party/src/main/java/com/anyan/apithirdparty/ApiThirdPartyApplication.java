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
        SpringApplication.run(ApiThirdPartyApplication.class, args);
    }

}
