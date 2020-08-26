package com.wisdom.monitor.security;


import com.wisdom.monitor.service.CustomUser;
import com.wisdom.monitor.service.Impl.CustomUserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 实现官方提供的UserDetailsService接口即可
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomUserServiceImpl customUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomUser user = customUserService.getCustomUserByName(username);
        if (user == null) {
            throw new  UsernameNotFoundException("该用户不存在");
        }
        LOGGER.info("用户名："+username+" 角色："+user.getAuthorities().toString());
        return user;
    }
}
