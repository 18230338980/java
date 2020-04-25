package com.steam.datasource.service;


import com.steam.datasource.entity.User;

import java.util.List;

public interface UserService {

  void addUser(User user);

  List selectUsersFromDs();

  List selectUserFromDsGroup();
}
