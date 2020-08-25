package com.wisdom.monitor.service;

import com.wisdom.monitor.dao.UserDao;
import com.wisdom.monitor.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class Database {

    private Map<String, CustomUser> data;
    private List<User> userList;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public static Database database;


    @Autowired
    private UserDao userDao;


//    @PostConstruct
//    public void init(){
//        database = this;
//        database.userDao = this.userDao;
//    }

    public Database() {
        data = new HashMap<>();
        List<User> list = userDao.findAll();
        if (list.isEmpty()) {
            User user = new User(0L, "admin", getPassword("admin"), "ROLE_ADMIN");
            userDao.save(user);
            CustomUser customUser = new CustomUser(0, user.getName(), user.getPassword(), getGrants(user.getRole()));
            data.put(user.getName(), customUser);
        }else {
            for (int i = 0; i < list.size(); i++) {
                CustomUser customUser = new CustomUser(i, list.get(i).getName(), list.get(i).getPassword(), getGrants(list.get(i).getRole()));
                data.put(userList.get(i).getName(), customUser);
            }
        }
    }

    public Map<String, CustomUser> getDatabase() {
        return data;
    }

    private String getPassword(String raw) {
        return passwordEncoder.encode(raw);
    }

    public Collection<GrantedAuthority> getGrants(String role) {
        return AuthorityUtils.commaSeparatedStringToAuthorityList(role);
    }
}
