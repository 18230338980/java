package com.steam.datasource.service.impl;

import com.steam.datasource.annotation.DS;
import com.steam.datasource.entity.User;
import com.steam.datasource.mapper.UserMapper;
import com.steam.datasource.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

  @Resource
  private UserMapper userMapper;

  @Override
  public void addUser(User user) {
    userMapper.addUser(user.getName(), user.getAge());
  }

  @DS("slave_1")
  @Override
  public List selectUsersFromDs() {
    return userMapper.selectUsers(1);
  }

  @DS("slave")
  @Override
  public List selectUserFromDsGroup() {
    return userMapper.selectUsers(1);
  }
}
