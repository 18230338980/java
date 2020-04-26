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
    public Object selectUsersFromDs() {
        return userService.selectUsersFromDs();
    }

    @GetMapping("/select/master")
    public Object selectUsersFromMasterDs() {
        return userService.selectUsersFromMasterDs();
    }

    @GetMapping("/select/slave")
    public Object selectUsersFromSlaveDs() {
        return userService.selectUsersFromSlaveDs();
    }

    @GetMapping("/select/many")
    public Object selectUsersFromManyDs() {
        return userService.selectUsersFromManyDs();
    }

    @GetMapping("/select/ds/group")
    public Object selectUserFromDsGroup() {
        return userService.selectUserFromDsGroup();
    }
}
