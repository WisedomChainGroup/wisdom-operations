package com.wisdom.monitor;

import com.wisdom.monitor.utils.JavaShellUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;


class MonitorApplicationTests {

    @Value("${applica}")
    private String password;
    @Autowired
    private JdbcTemplate tmpl;
    @Test
    void contextLoads() throws IOException {
        String stopShell = "echo "+password+" |sudo -S docker stop 6c20180d9ee4";
        System.out.printf(String.valueOf(JavaShellUtil.executeShell(stopShell)));
    }

}
