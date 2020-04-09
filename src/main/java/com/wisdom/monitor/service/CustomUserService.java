package com.wisdom.monitor.service;

import com.wisdom.monitor.model.User;

public interface CustomUserService {
    String getRole();
    CustomUser getCustomUserByName(String name);
}
