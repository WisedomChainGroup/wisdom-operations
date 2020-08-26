package com.wisdom.monitor.service.Impl;

import com.wisdom.monitor.dao.UserDao;
import com.wisdom.monitor.model.User;
import com.wisdom.monitor.service.CustomUser;
import com.wisdom.monitor.service.CustomUserService;
import com.wisdom.monitor.service.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, CustomUser> data = new HashMap<>();
        List<User> list = userDao.findAll();
        if (list.isEmpty()) {
            User user = new User(0L, "admin", database.getPassword("admin"), "ROLE_ADMIN");
            userDao.save(user);
            CustomUser customUser = new CustomUser(0, user.getName(), user.getPassword(), database.getGrants(user.getRole()));
            data.put(user.getName(), customUser);
        }else {
            for (int i = 0; i < list.size(); i++) {
                CustomUser customUser = new CustomUser(i, list.get(i).getName(), list.get(i).getPassword(), database.getGrants(list.get(i).getRole()));
                data.put(list.get(i).getName(), customUser);
            }
        }
        database.setData(data);
        if (userDao.findByName(name).isPresent()){
            User user = userDao.findByName(name).get();
            CustomUser customUser = new CustomUser(0, user.getName(), user.getPassword(), database.getGrants(user.getRole()));
            return customUser;
        }
        return null;
    }
}
