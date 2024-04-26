package com.anyan.apiinterface.entity;

import lombok.Data;

/**
 * @author anyan
 * DateTime: 2024/4/26
 */
@Data
public class IP {
    private String ip;
    private String country;
    private String province;
    private String city;
    private String area;
    private String isp;
    private String os;
    private String browser;
}
