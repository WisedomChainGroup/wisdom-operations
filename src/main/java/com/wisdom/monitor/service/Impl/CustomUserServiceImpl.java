package com.wisdom.monitor.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.dao.UserDao;
import com.wisdom.monitor.leveldb.Leveldb;
import com.wisdom.monitor.model.User;
import com.wisdom.monitor.service.CustomUser;
import com.wisdom.monitor.service.CustomUserService;
import com.wisdom.monitor.service.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class CustomUserServiceImpl implements CustomUserService {

    @Autowired
    private UserDao userDao;

    @Override
    public String getRole() {
        Object[] list= ((CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getAuthorities().toArray();
        return ((SimpleGrantedAuthority) list[0]).getAuthority();
    }

    @Override
    public CustomUser getCustomUserByName(String name) {
        Database database = new Database();
        if (userDao.findByName(name).isPresent()){
            User user = userDao.findByName(name).get();
            CustomUser customUser = new CustomUser(0, user.getName(), user.getPassword(), database.getGrants(user.getRole()));
            return customUser;
        }
        return null;
    }
}
