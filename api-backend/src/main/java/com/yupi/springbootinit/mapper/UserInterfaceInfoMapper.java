package com.yupi.springbootinit.mapper;

import com.anyan.apicommon.model.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author 兕神
* @description 针对表【user_interface_info(接口信息)】的数据库操作Mapper
* @createDate 2024-04-20 09:12:00
* @Entity com.yupi.springbootinit.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    /**
     * 统计调用次数前limit的的接口
     *
     * @param limit
     * @return
     */
    List<UserInterfaceInfo> statisticsTopTotalNum(int limit);
}




