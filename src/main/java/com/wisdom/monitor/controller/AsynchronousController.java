package com.wisdom.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.utils.HttpRequestUtil;
import com.wisdom.monitor.utils.MapCacheUtil;
import com.wisdom.monitor.utils.SysStatusUtil;
import org.hyperic.sigar.SigarException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

@RestController
public class AsynchronousController {


    @GetMapping(value = {"/cpu"})
    public String cpu(){
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor processor = hal.getProcessor();
        double useRate = processor.getSystemCpuLoadBetweenTicks();
        return String.format("%.2f",useRate*100)+"%";
    }

    @GetMapping(value = {"/block"})
    public String block(){
        int blockStatus = Monitor.checkBlockIsStuck(false);
        if (blockStatus == -1){
            return "异常";
        }else if(blockStatus == 0){
            return "待确认";
        }else {
            return "正常";
        }
    }

    @GetMapping(value = {"/outageStatus"})
    public boolean outageStatus(){
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        String ip = "";
        boolean outage = false ;
        if (mapCacheUtil.getCacheItem("bindNode") != null){
             ip = mapCacheUtil.getCacheItem("bindNode").toString();
            //获取当前高度
            String heightUrl = "http://"+ip+"/height";
            JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl,""));

            if(result != null){
                if(result.getInteger("code") == 2000) {
                    outage = true;
                }
            }else {
                return outage;
            }
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
