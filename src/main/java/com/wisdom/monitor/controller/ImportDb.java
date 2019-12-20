package com.wisdom.monitor.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ImportDb {
    private static final Logger logger = LoggerFactory.getLogger(ImportDb.class);

    @Autowired
    private JdbcTemplate tmpl;

    @Value("${spring.datasource.url}")
    public String DB_URL;
    @Value("password")
    public String password;

    @RequestMapping("/index")
    public String indexw() {
        System.out.println("====this is index");
        return "index";
    }

    public void importDb(String path){
//        psql -U postgres(用户名)  数据库名(缺省时同用户名) < /data/dum.
//
//                String restartShell = "echo "+password+" |sudo -S -u postgres psql -U postgres test</home/zt599/database/tt.sql";
//        JavaShellUtil.executeShell(restartShell);


        //导入前先删除数据库
        tmpl.batchUpdate( "DROP TABLE IF EXISTS incubator_state",
                                "DROP TABLE IF EXISTS account",
                                "DROP TABLE IF EXISTS transaction",
                                "DROP TABLE IF EXISTS transaction_index",
                                "DROP TABLE IF EXISTS header");

    }

//    $ pg_dump -U postgres(用户名)  (-t 表名)  数据库名(缺省时同用户名)  > 路径/文件名.sql
//
//    postgres@debian:~$ pg_dump -U postgres -t system_calls wangye > ./test.sql
//    postgres@debian:~$ ls

}
