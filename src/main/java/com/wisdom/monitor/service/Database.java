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

@NoArgsConstructor
@Data
@AllArgsConstructor
public class Database {

    private Map<String, CustomUser> data;
    private List<User> userList;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public static Database database;


    public Map<String, CustomUser> getDatabase() {
        return data;
    }

    public String getPassword(String raw) {
        return passwordEncoder.encode(raw);
    }

    public Collection<GrantedAuthority> getGrants(String role) {
        return AuthorityUtils.commaSeparatedStringToAuthorityList(role);
    }
}
