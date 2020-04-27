package com.steam.datasource.controller;

import com.steam.datasource.entity.User;
import com.steam.datasource.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * @description: UserController
 * @date: 2020/4/26 11:09
 * @author: zsz
 */
@RestController
@AllArgsConstructor
@RequestMapping("/user")
@Api(tags = "用户信息")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/add")
    @ApiOperation("新增用户")
    public void addUser() {
        User user = new User();
        Random random = new Random();
        user.setName("测试用户" + random.nextInt());
        user.setAge(random.nextInt(100));
        userService.addUser(user);
    }


    @GetMapping("/select")
    @ApiOperation("默认数据源")
    public Object selectUsersFromDs() {
        return userService.selectUsersFromDs();
    }

    @GetMapping("/select/master")
    @ApiOperation("指定主数据源")
    public Object selectUsersFromMasterDs() {
        return userService.selectUsersFromMasterDs();
    }

    @GetMapping("/select/slave")
    @ApiOperation("指定从数据源")
    public Object selectUsersFromSlaveDs() {
        return userService.selectUsersFromSlaveDs();
    }

    @GetMapping("/select/ds/group")
    @ApiOperation("指定组数据源，采用策略动态选择数据源")
    public Object selectUserFromDsGroup() {
        return userService.selectUserFromDsGroup();
    }

    @GetMapping("/select/many")
    @ApiOperation("同一方法内数据源切换")
    public Object selectUsersFromManyDs() {
        return userService.selectUsersFromManyDs();
    }
}
