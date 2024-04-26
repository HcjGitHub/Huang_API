package com.anyan.apigateway.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author anyan
 * DateTime: 2024/4/26
 */
@Configuration
public class GuavaRateLimiterConfig {


    @SuppressWarnings("UnstableApiUsage")
    @Bean
    public RateLimiter rateLimiter(){
        /*每秒控制5个许可*/
        return RateLimiter.create(3);
    }
}
