package com.wisdom.monitor.controller;

import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.leveldb.Leveldb;
import com.wisdom.monitor.model.Nodes;
import com.wisdom.monitor.model.Result;
import com.wisdom.monitor.model.ResultCode;
import com.wisdom.monitor.service.Impl.NodeServiceImpl;
import com.wisdom.monitor.utils.MapCacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
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

    @GetMapping(value = {"/searchNode"})
    public Nodes searchNode(@RequestParam("ipPort") String ipPort) {
        return nodeService.searchNode(ipPort);
    }

    @GetMapping(value = {"/updateNode"})
    public boolean updateNode(@ModelAttribute Nodes node) {
        return nodeService.updateNode(node);
    }

    @GetMapping(value = {"/getBindNode"})
    public JSONObject getBindNode() {
        JSONObject jsonObject = new JSONObject();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            Nodes nodes = nodeService.searchNode(mapCacheUtil.getCacheItem("bindNode").toString());
            jsonObject.put("ip",nodes.getNodeIP()+":"+nodes.getNodePort());
            JSONObject jo = (JSONObject) JSONObject.toJSON(NodeinfoController.getVersion());
            if ((int)jo.get("code") == 2000){
                JSONObject result = (JSONObject) jo.get("data");
                String version = result.get("version").toString();
                jsonObject.put("code","2000");
                jsonObject.put("status","运行中");
                jsonObject.put("version",version);
                jsonObject.put("type",nodes.getNodeType());
                return jsonObject;
            }
            jsonObject.put("code","3000");
            return jsonObject;
        }
        jsonObject.put("code","5000");
        return jsonObject;
    }
}
