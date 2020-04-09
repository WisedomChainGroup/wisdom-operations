package com.wisdom.monitor.controller;

import com.wisdom.monitor.model.Result;
import com.wisdom.monitor.model.ResultCode;
import com.wisdom.monitor.utils.JavaShellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    @Value("${postgres.ip}")
    private String postgres_ip;
    @Value("${postgres.pwd}")
    private String postgres_pwd;


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
        String[] containers = {core_name, postgres_name};
        String cmd_judge = String.format("echo %s |sudo -S docker ps", sys_password);
        boolean tag = JavaShellUtil.judgeImages(cmd_judge, containers);
        String[] path = {""};
        if (!tag || !getPath(path)) {
            rs.setCode(ResultCode.FAIL);
            rs.setMessage("请检查程序与节点运行环境");
            return rs.toString();
        }
        int flag = JavaShellUtil.executeShell(String.format("echo %s |sudo -S docker stop %s", sys_password, core_name));
        if (flag == 1) {
            String cmd = "PGPASSWORD=PqR_w9hk6Au-jq5ElsFcEjq\\!wvULrYXeF3*oDKp5i@A/D5m03VaB1M/hyKY psql -h " + postgres_ip + " -U wdcadmin  -p " + postgres_port + " -d postgres < " + path[0] + "/WDC.sql";
            flag = JavaShellUtil.executeShell(cmd);
            JavaShellUtil.executeShell(String.format("echo %s |sudo -S docker restart %s", sys_password, core_name));
        }
        rs.setCode(flag == 0 ? ResultCode.FAIL : ResultCode.SUCCESS);
        rs.setMessage(flag == 0 ? "区块数据导入失败" : "区块数据导入成功");
        return rs.toString();
    }

    public boolean getPath(String[] ref_path) {
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        if (path.contains("jar")) {
            path = path.substring(0, path.lastIndexOf("."));
            ref_path[0] = path.substring(0, path.lastIndexOf("/")).replace("file:", "");
            return true;
        }
        return false;
    }

    @GetMapping("/logout")
    public String logout() {
        logger.info("===========================in============================");
        return "logout";
    }

    @GetMapping("/login")
    public String login() {
        logger.info("===========================out============================");
        return "login";
    }
}

