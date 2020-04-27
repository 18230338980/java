package com.steam.datasource.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steam.datasource.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductDao extends BaseMapper<Product> {

}