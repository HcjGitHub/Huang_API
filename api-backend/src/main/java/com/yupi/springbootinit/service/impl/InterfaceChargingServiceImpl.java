package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.anyan.apicommon.model.entity.InterfaceCharging;
import com.yupi.springbootinit.service.InterfaceChargingService;
import com.yupi.springbootinit.mapper.InterfaceChargingMapper;
import org.springframework.stereotype.Service;

/**
* @author 兕神
* @description 针对表【interface_charging(接口单价信息)】的数据库操作Service实现
* @createDate 2024-04-22 14:49:24
*/
@Service
public class InterfaceChargingServiceImpl extends ServiceImpl<InterfaceChargingMapper, InterfaceCharging>
    implements InterfaceChargingService{

}




