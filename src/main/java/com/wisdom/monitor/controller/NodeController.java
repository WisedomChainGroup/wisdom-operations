package com.wisdom.monitor.controller;

import com.wisdom.monitor.model.Result;
import com.wisdom.monitor.model.ResultCode;
import com.wisdom.monitor.service.Impl.NodeServiceImpl;
import com.wisdom.monitor.utils.MapCacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@RestController
public class NodeController {

    @Value("${Image}")
    private String image;

    @Autowired
    public NodeServiceImpl nodeService;

    private static final Logger logger = LoggerFactory.getLogger(NodeController.class);

    @GetMapping(value = {"/stop"})
    public Object stop(){
        Result result = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        try {
            if (mapCacheUtil.getCacheItem("bindNode") != null){
                return nodeService.stop(mapCacheUtil.getCacheItem("bindNode").toString());
            }
        }catch (Exception e){
            result.setMessage("失败");
            result.setCode(ResultCode.FAIL);
            return result;
        }
        result.setMessage("请绑定节点");
        result.setCode(ResultCode.FAIL);
        return result;
    }

    @GetMapping(value = {"/restart"})
    public Object restart() {
        Result result = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        try {
            if (mapCacheUtil.getCacheItem("bindNode") != null){
                return nodeService.restart(mapCacheUtil.getCacheItem("bindNode").toString());
            }
        }catch (Exception e){
            result.setMessage("失败");
            result.setCode(ResultCode.FAIL);
            return result;
        }
        result.setMessage("请绑定节点");
        result.setCode(ResultCode.FAIL);
        return result;
    }
}
