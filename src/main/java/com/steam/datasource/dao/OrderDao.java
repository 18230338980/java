package com.steam.datasource.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steam.datasource.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDao extends BaseMapper<Order> {

}