package com.wisdom.monitor.controller;

import com.wisdom.monitor.model.Result;
import com.wisdom.monitor.model.ResultCode;
import com.wisdom.monitor.service.Impl.NodeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NodeController {

    @Value("${imageVersion}")
    private String imageVersion;

    @Autowired
    public NodeServiceImpl nodeService;

    @GetMapping(value = {"/stop"})
    public String stop(){
        List<String> list = nodeService.stop();
        if (list == null)
            return "失败";
        if (list.get(0).equals("wdc_core_v"+imageVersion))
            return "成功";
        return "失败";
    }

    @GetMapping(value = {"/restart"})
    public String restart() {
        List<String> list = nodeService.restart();
        if (list == null)
            return "失败";
        if (list.get(0).equals("wdc_core_v" + imageVersion))
            return "成功";
        return "失败";
    }
}
