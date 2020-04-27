package com.steam.datasource.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steam.datasource.entity.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountDao extends BaseMapper<Account> {

}