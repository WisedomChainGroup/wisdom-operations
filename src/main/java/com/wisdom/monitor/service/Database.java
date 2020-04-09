package com.wisdom.monitor.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.leveldb.Leveldb;
import com.wisdom.monitor.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.*;

public class Database {

    private Map<String, CustomUser> data;
    private List<User> userList;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Database() throws IOException {
        data = new HashMap<>();
        Leveldb leveldb = new Leveldb();
        String account = leveldb.readAccountFromSnapshot("user");
        if (account.length() > 0) {
            userList = JSONObject.parseArray(account, User.class);
            for (int i = 0; i < userList.size(); i++) {
                CustomUser customUser = new CustomUser(i, userList.get(i).getName(), userList.get(i).getPassword(), getGrants(userList.get(i).getRole()));
                data.put(userList.get(i).getName(), customUser);
            }
        } else {
            userList = new ArrayList<>();
            User user = new User("admin", getPassword("admin"), "ROLE_ADMIN");
            userList.add(user);
            leveldb.addAccount("user",JSON.toJSONString(userList));
            CustomUser customUser = new CustomUser(0, user.getName(), user.getPassword(), getGrants(user.getRole()));
            data.put(user.getName(), customUser);
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
