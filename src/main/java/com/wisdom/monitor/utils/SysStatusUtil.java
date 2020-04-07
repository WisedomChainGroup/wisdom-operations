package com.wisdom.monitor.utils;


import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.model.SysStatusData;
import org.hyperic.sigar.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.DecimalFormat;

public class SysStatusUtil {
    static {
        try {
            String classPath = SysStatusUtil.class.getClassLoader().getResource("").getPath().concat("sigarPackages");
            String path = System.getProperty("java.library.path");
            String osName = System.getProperty("os.name").toLowerCase();
            path += osName.contains("windows") ? ";".concat(classPath) :
                    osName.contains("linux") ? ":".concat(classPath) : "" ;
            System.setProperty("java.library.path", path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isLock = false;

    private static Logger logger = LoggerFactory.getLogger(SysStatusUtil.class);

    /**
     * 更新系统状态配置内容
     * @return
     */
    public JSONObject status() throws SigarException {
        JSONObject jsonObject = new JSONObject();
        SysStatusData statusData = new SysStatusData();
        CpuPerc perc = statusData.getPerc();
        // 获取cpu使用率
        String sysCpuUsed = CpuPerc.format(perc.getSys());
        // 获取内存使用率
        Mem mem = statusData.getMem();
        String sysMemoryUsed = new DecimalFormat("0.0%").format(
                Double.valueOf(mem.getUsed()) / Double.valueOf(mem.getTotal()));
        jsonObject.put("sysCpuUsed", sysCpuUsed);
        jsonObject.put("sysMemoryUsed", sysMemoryUsed);
        return jsonObject;
    }
}
