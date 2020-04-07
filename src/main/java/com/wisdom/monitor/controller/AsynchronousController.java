package com.wisdom.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.utils.HttpRequestUtil;
import com.wisdom.monitor.utils.SysStatusUtil;
import org.hyperic.sigar.SigarException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AsynchronousController {

    @Value("${node}")
    private String ip;

    @GetMapping(value = {"/cpu"})
    public String cpu(){
        SysStatusUtil sysStatusUtils = new SysStatusUtil();
        JSONObject jsonObject = null;
        try {
            jsonObject = sysStatusUtils.status();
        } catch (SigarException e) {
            e.printStackTrace();
        }
        return jsonObject.getString("sysCpuUsed");
    }

    @GetMapping(value = {"/block"})
    public boolean block(){
        Monitor monitor = new Monitor();
        boolean blockStatus = monitor.checkBlockIsStuck();
        return blockStatus;
    }

    @GetMapping(value = {"/outageStatus"})
    public boolean outageStatus(){
        //获取当前高度
        String heightUrl = "http://"+ip+"/height";
        JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl,""));
        boolean outage = false ;
        if(result != null){
            if(result.getInteger("code") == 2000) {
                outage = true;
            }
        }else {
            return outage;
        }
        return outage;
    }

    @GetMapping(value = {"/bifurcation"})
    public boolean bifurcation(){
        Monitor monitor = new Monitor();
        boolean bifurcation = monitor.checkBifurcate();
        return bifurcation;
    }
}
