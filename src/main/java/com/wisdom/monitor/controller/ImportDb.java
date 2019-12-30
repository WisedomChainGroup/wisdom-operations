package com.wisdom.monitor.controller;

import com.wisdom.monitor.model.Result;
import com.wisdom.monitor.model.ResultCode;
import com.wisdom.monitor.utils.JavaShellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class ImportDb {
    private static final Logger logger = LoggerFactory.getLogger(ImportDb.class);

    //region zhangtong code
       /*@Autowired
        private JdbcTemplate tmpl;

        @Value("${spring.datasource.url}")
        public String DB_URL;
        @Value("password")
        public String password;*/
    //endregion

    @Value("${postgres.port}")
    private String postgres_port;
    @Value("${sys.password}")
    private String sys_password;
    @Value("${core.name}")
    private String core_name;
    @Value("${postgres.name}")
    private String postgres_name;
    @Value("${import.valve}")
    private String import_valve;

    @ResponseBody
    @GetMapping("/importDb")
    public String importDb() {
        logger.info("importDb--------------------" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "--------------------------");
        Result rs = new Result();
        if (!import_valve.equals("true")) {
            rs.setCode(ResultCode.Warn);
            rs.setMessage("导入区块功能未打开");
            return rs.toString();
        }
        //检查环境
        String[] containers = new String[]{core_name, postgres_name};
        String cmd_judge = String.format("echo %s |sudo -S docker ps", sys_password);
        boolean tag = JavaShellUtil.judgeImages(cmd_judge, containers);
        if (!tag) {
            rs.setCode(ResultCode.FAIL);
            rs.setMessage("请检查程序与节点运行环境");
            return rs.toString();
        }
        //copy至容器
        String cmd_cp = String.format("echo %s |sudo -S docker cp /home/postgres.sql %s:/", sys_password, postgres_name);
        int flag = JavaShellUtil.executeShell(cmd_cp);
        if (1 == flag) {
            //Step.1 stop容器 | Step.2 执行psql |Step.3 restart容器
            String cmd_container = String.format("echo %1$s |sudo -S docker stop %2$s && echo %1$s |sudo -S docker exec %3$s /bin/bash -c \"psql -h localhost -p %4$s -U wdcadmin postgres -f postgres.sql && rm -f postgres.sql\" && echo %1$s |sudo -S docker restart %2$s", sys_password, core_name, postgres_name, postgres_port);
            flag = JavaShellUtil.executeShell(cmd_container);
        }
        rs.setCode(flag == 0 ? ResultCode.FAIL : ResultCode.SUCCESS);
        rs.setMessage(flag == 0 ? "区块数据导入失败" : "区块数据导入成功");
        return rs.toString();

        //region  zhangtong Script
         /*   //psql -U postgres(用户名)  数据库名(缺省时同用户名) < /data/dum.
            //
            //                String restartShell = "echo "+password+" |sudo -S -u postgres psql -U postgres test</home/zt599/database/tt.sql";
            //JavaShellUtil.executeShell(restartShell);


            //导入前先删除数据库
            tmpl.batchUpdate( "DROP TABLE IF EXISTS incubator_state",
            "DROP TABLE IF EXISTS account",
            "DROP TABLE IF EXISTS transaction",
            "DROP TABLE IF EXISTS transaction_index",
            "DROP TABLE IF EXISTS header");


            //    $ pg_dump -U postgres(用户名)  (-t 表名)  数据库名(缺省时同用户名)  > 路径/文件名.sql
            //    postgres@debian:~$   -U postgres -t system_calls wangye > ./test.sql
            //    postgres@debian:~$ ls*/
        //endregion
    }

    // TODO: 2019/12/30 以后再做 
    public String getPath() {
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        if (System.getProperty("os.name").contains("dows")) {
            path = path.substring(1, path.length());
        }
        if (path.contains("jar")) {
            path = path.substring(0, path.lastIndexOf("."));
            return path.substring(0, path.lastIndexOf("/"));
        }
        //return path.replace("target/classes/", "");
        return path + "----------" + System.getProperty("os.name");
    }
}

