package com.steam.datasource.service.impl;


import com.steam.datasource.annotation.DS;
import com.steam.datasource.entity.User;
import com.steam.datasource.dao.UserMapper;
import com.steam.datasource.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhangshizhu
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired UserService userService;

    @Resource
    private UserMapper userMapper;

    @Override
    public void addUser(User user) {
        userMapper.addUser(user.getName(), user.getAge());
    }

    @Override
    public List selectUsersFromDs() {
        return userMapper.selectUsers(1);
    }

    @DS("master")
    @Override
    public List selectUsersFromMasterDs() {
        return userMapper.selectUsers(1);
    }

    @DS("slave_1")
    @Override
    public List selectUsersFromSlaveDs() {
        return userMapper.selectUsers(1);
    }

    @DS("slave")
    @Override
    public List selectUserFromDsGroup() {
        return userMapper.selectUsers(1);
    }

    @Override
    @DS("master")
    public List selectUsersFromManyDs() {
        userService.selectUsersFromSlaveDs();
        return userMapper.selectUsers(1);
    }
}
