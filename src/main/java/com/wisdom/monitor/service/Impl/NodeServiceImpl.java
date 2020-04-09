package com.wisdom.monitor.service.Impl;

import com.wisdom.monitor.controller.Monitor;
import com.wisdom.monitor.service.NodeService;
import com.wisdom.monitor.utils.JavaShellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class NodeServiceImpl implements NodeService {
    @Value("${password}")
    private String password;
    @Value("${imageVersion}")
    private String imageVersion;
    private static final Logger logger = LoggerFactory.getLogger(NodeServiceImpl.class);
    @Override
    public List<String> stop() {
        List<String> strList = new ArrayList<String>();
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c","echo "+password+" |sudo -S docker stop  wdc_core_v"+imageVersion},null,null);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line;
            process.waitFor();
            while ((line = input.readLine()) != null){
                strList.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strList;
    }

    @Override
    public List<String> restart() {
        List<String> strList = new ArrayList<String>();
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c","echo "+password+" |sudo -S docker restart  wdc_core_v"+imageVersion},null,null);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line;
            process.waitFor();
            while ((line = input.readLine()) != null){
                strList.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strList;
    }


}
