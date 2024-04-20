package com.anyan.apigateway.config;

import com.anyan.apigateway.filter.InterfaceInfoInvokeFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 模拟接口平台拦截器配置
 *
 * @author 兕神
 * DateTime: 2024/4/20
 */
@Configuration
public class GatewayFilterConfig {

    @Bean
    public RouteLocator getRouteLocator(RouteLocatorBuilder builder, InterfaceInfoInvokeFilter interfaceInfoInvokeFilter) {
        return builder.routes()
                .route(p -> p.path("/api/interface/**")
                        .filters(f -> f.filter(interfaceInfoInvokeFilter))
                        .uri("lb://api-interface"))
                .build();
    }
}
