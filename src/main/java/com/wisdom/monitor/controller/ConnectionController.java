package com.wisdom.monitor.controller;

import com.wisdom.monitor.model.Result;
import com.wisdom.monitor.model.ResultCode;
import com.wisdom.monitor.utils.ConnectionDbUtil;
import com.wisdom.monitor.utils.ConnectionUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConnectionController {

    @GetMapping(value = {"/connection"})
    public Object connection(@RequestParam("ip") String ip,@RequestParam("username") String username,@RequestParam("password") String password){
        Result rs = new Result();
        ConnectionUtil connectionUtil=new ConnectionUtil(ip, username,password);
        if (connectionUtil.login()){
            rs.setCode(ResultCode.SUCCESS);
            return rs;
        }
        rs.setCode(ResultCode.FAIL);
        return rs;
    }

    @GetMapping(value = {"/connectionDB"})
    public Object connectionDB(@RequestParam("ip") String ip,@RequestParam("port") String port,@RequestParam("database") String database,@RequestParam("username") String username,@RequestParam("password") String password){
        ConnectionDbUtil connectionDbUtil = new ConnectionDbUtil(ip+":"+port,database,username,password);
        return connectionDbUtil.login();
    }
}
