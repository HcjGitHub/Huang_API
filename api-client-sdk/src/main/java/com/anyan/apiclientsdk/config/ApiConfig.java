package com.anyan.apiclientsdk.config;

import com.anyan.apiclientsdk.client.NameApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author 兕神
 * DateTime: 2024/4/10
 */
@Configuration
@ConfigurationProperties("huang.api")
@Data
@ComponentScan
public class ApiConfig {

    private String accessKey;
    private String secretKey;


    @Bean
    public NameApiClient httpClient() {
        return new NameApiClient(accessKey, secretKey);
    }
}
