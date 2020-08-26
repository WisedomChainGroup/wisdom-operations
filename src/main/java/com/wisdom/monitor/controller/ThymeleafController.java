package com.wisdom.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.dao.NodeDao;
import com.wisdom.monitor.dao.UserDao;
import com.wisdom.monitor.model.*;
import com.wisdom.monitor.security.IsUser;
import com.wisdom.monitor.service.CustomUser;
import com.wisdom.monitor.service.Impl.CustomUserServiceImpl;
import com.wisdom.monitor.utils.HttpRequestUtil;
import com.wisdom.monitor.utils.MapCacheUtil;
import com.wisdom.monitor.utils.SysStatusUtil;
import org.apache.logging.log4j.status.StatusData;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.tdf.common.store.LevelDb;

import javax.xml.soap.Node;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@IsUser
@Controller
@EnableScheduling
public class ThymeleafController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private CustomUserServiceImpl customUserService;

    @Autowired
    private LevelDb levelDb;

    @RequestMapping(value = {"/", "/home"})
    public String home(ModelMap map) {
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        WDCInfo info = new WDCInfo();
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            String url_node = mapCacheUtil.getCacheItem("bindNode").toString();
            JSONObject get_info = new JSONObject();
            if (HttpRequestUtil.sendGet(String.format("http://%s/WisdomCore/ExplorerInfo", url_node), null)!=""){
                get_info = JSON.parseObject(HttpRequestUtil.sendGet(String.format("http://%s/WisdomCore/ExplorerInfo", url_node), null)).getJSONObject("data");
            }
            if (get_info != null){
                info = JSON.toJavaObject(get_info, WDCInfo.class);
            }else {
                info = JSON.toJavaObject((JSONObject)JSONObject.toJSON(new WDCInfo()), WDCInfo.class);
            }
        }
        map.addAttribute("result", info);
        map.addAttribute("role",customUserService.getRole());
        return "home";
    }

    @ResponseBody
    @RequestMapping("/getLinedata/Json")
    public String control() {
        JSONArray arrys = JSON.parseObject(HttpRequestUtil.sendGet("https://api.ceobi.com/api/market/kline", "market=wdc_qc&type=1day")).getJSONObject("data").getJSONArray("data");
        for (Object o : arrys) {
            JSONArray json = (JSONArray) o;
            json.add(new BigInteger(json.get(0).toString() + "000"));
            json.add(Double.parseDouble(json.get(5).toString()));
            for (int i = 1; i <= 6; i++) {
                json.remove(0);
            }
        }
        return arrys.toJSONString();
    }

    @RequestMapping("/user")
    public String user(ModelMap map){
        List<User> users = userDao.findAll();
        CustomUser customUser = (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        map.addAttribute(customUser);
        map.addAttribute(users);
        map.addAttribute("role",customUserService.getRole());
        return "user";
    }

    @ResponseBody
    @RequestMapping("/adduser")
    public String adduser(@ModelAttribute User user) {
        boolean tag = false;
        if (!userDao.findByName(user.getName()).isPresent()){
            user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
            userDao.save(user);
            tag = true;
        }
        Result rs = new Result();
        if (tag) {
            rs.setCode(ResultCode.SUCCESS);
            rs.setMessage("添加用户成功");
        } else {
            rs.setCode(ResultCode.Warn);
            rs.setMessage("已存在相同登录名");
        }
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/deleteuser")
    public String deleteuser(@ModelAttribute User user) {
        Result rs = new Result();
        List<User> userList = userDao.findAll();
        if (userDao.findByName(user.getName()).isPresent()){
            userDao.deleteByName(user.getName());
            rs.setCode(ResultCode.SUCCESS);
            rs.setMessage("删除成功");
        }else {
            rs.setCode(ResultCode.FAIL);
            rs.setMessage("删除失败");
        }
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/addnode")
    public String addnode(@ModelAttribute Nodes node) throws IOException {
        Result rs = new Result();
        if (!nodeDao.findNodesByNodeIPAndNodePort(node.getNodeIP(),node.getNodePort()).isPresent()){
            nodeDao.save(node);
            rs.setCode(ResultCode.SUCCESS);
            rs.setMessage("添加节点成功");
        }else {
            rs.setCode(ResultCode.FAIL);
            rs.setMessage("已存在相同节点");
        }
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/deleteNode")
    public String deleteNode(@RequestParam("nodeStr") String nodeStr) throws IOException {
        Result rs = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            if (mapCacheUtil.getCacheItem("bindNode").toString().equals(nodeStr)){
                mapCacheUtil.removeCacheItem("bindNode");
            }
        }
        String[] nodeinfo = nodeStr.split(":");
        nodeDao.deleteByNodeIPAndNodePort(nodeinfo[0],nodeinfo[1]);
        rs.setCode(ResultCode.SUCCESS);
        rs.setMessage("删除成功");
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/bindNode")
    public String bindNode(@RequestParam("nodeStr") String nodeStr) throws IOException {
        Result rs = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") == null){
            mapCacheUtil.putCacheItem("bindNode",nodeStr);
        }else {
            mapCacheUtil.removeCacheItem("bindNode");
            mapCacheUtil.putCacheItem("bindNode",nodeStr);
        }

        rs.setCode(ResultCode.SUCCESS);
        rs.setMessage("绑定成功");
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/unbindNode")
    public String unbindNode(@RequestParam("nodeStr") String nodeStr) throws IOException {
        Result rs = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        mapCacheUtil.removeCacheItem("bindNode");
        rs.setCode(ResultCode.SUCCESS);
        rs.setMessage("解绑成功");
        return rs.toString();
    }



    @RequestMapping("/control")
    public String control(ModelMap map) {
        try {
            map.addAttribute((CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            map.addAttribute("dateNow", new java.util.Date().getTime());
            List<String> nodeinfoList = new ArrayList<>();
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            if (mapCacheUtil.getCacheItem("bindNode") != null){
                nodeinfoList.add(mapCacheUtil.getCacheItem("bindNode").toString());
            }
            map.addAllAttributes(nodeinfoList);
            //节点详情
            Nodes nodeinfo = new Nodes();
            String version= "";
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(NodeinfoController.getVersion());
            if ((int)jsonObject.get("code") == 2000){
                JSONObject result = (JSONObject) jsonObject.get("data");
                version = result.get("version").toString();
                nodeinfo.setNodeIP(mapCacheUtil.getCacheItem("bindNode").toString());
                String[] bindNodeiphost = mapCacheUtil.getCacheItem("bindNode").toString().split(":");
                List<Nodes> nodeList = nodeDao.findAll();
                for (int i = 0; i < nodeList.size(); i++) {
                    if (nodeList.get(i).getNodeIP().equals(bindNodeiphost[0]) && nodeList.get(i).getNodePort().equals(bindNodeiphost[1])) {
                        if (nodeList.get(i).getNodeType().equals("1")){
                            nodeinfo.setNodeType("全节点");
                            break;
                        }
                        nodeinfo.setNodeType("矿工节点");
                        break;
                    }
                }
                map.addAttribute("info",nodeinfo);
                map.addAttribute("isrun","运行中");
            }else{
                nodeinfo.setNodeIP(jsonObject.getString("message"));
            }
            map.addAttribute("info",nodeinfo);
            map.addAttribute("version",version);
            map.addAttribute("role",customUserService.getRole());
        }catch (Exception e){
            e.printStackTrace();
        }
        return "control";
    }

    @ResponseBody
    @RequestMapping("/editmail")
    public String editMail(@ModelAttribute Mail mail) throws IOException {
        Result rs = new Result();
        levelDb.put("mail".getBytes(StandardCharsets.UTF_8),JSON.toJSONString(mail).getBytes(StandardCharsets.UTF_8));
        rs.setCode(ResultCode.SUCCESS);
        rs.setMessage("成功");
        return rs.toString();
    }

    @RequestMapping("/mail")
    public String mail(ModelMap map) throws IOException {
        Mail mail = new Mail();
        if (levelDb.get("mail".getBytes(StandardCharsets.UTF_8)).isPresent()){
        Object read = JSONObject.parseObject(new String(levelDb.get("mail".getBytes(StandardCharsets.UTF_8)).get(),StandardCharsets.UTF_8), Mail.class);
            mail= (Mail) read;
        }
        map.addAttribute(mail);
        map.addAttribute("role",customUserService.getRole());
        return "mail";
    }

    @RequestMapping("/node")
    public String node(ModelMap map) throws IOException {
        List<Nodes> nodeList = nodeDao.findAll();
        List<String> list = new ArrayList<>();
        if (nodeList != null) {
            for (int i = 0;i<nodeList.size();i++){
                String heightUrl = "http://"+nodeList.get(i).getNodeIP()+":"+nodeList.get(i).getNodePort()+"/version";
                if (HttpRequestUtil.sendGet(heightUrl,"") == ""){
                    nodeList.get(i).setNodeState("未运行");
                }else {
                    nodeList.get(i).setNodeState("运行中");
                    nodeList.get(i).setNodeVersion(JSON.parseObject(JSON.parseObject(HttpRequestUtil.sendGet(heightUrl,"")).get("data").toString()).get("version").toString());
                }
            }

            map.addAttribute(nodeList);
            map.addAllAttributes(list);
        }
        List<String> nodeinfoList = new ArrayList<>();
        String nodeinfo = "";
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            nodeinfoList = Arrays.asList(mapCacheUtil.getCacheItem("bindNode").toString().split(":"));
            map.addAttribute("nodeip",mapCacheUtil.getCacheItem("bindNode").toString());
        }
        map.addAttribute("role",customUserService.getRole());
        return "node";
    }

    @RequestMapping("/basic_info")
    public String localRefresh(ModelMap map) {
        WDCInfo info = new WDCInfo();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        String url_node = "";
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            url_node = mapCacheUtil.getCacheItem("bindNode").toString();
        }
        try {
            JSONObject get_info = JSON.parseObject(HttpRequestUtil.sendGet(String.format("http://%s/WisdomCore/ExplorerInfo", url_node), null)).getJSONObject("data");
            String get_price = "";
            if (JSON.parseObject(HttpRequestUtil.sendGet(url_price, "market=wdc_qc")) != null){
                get_price = JSON.parseObject(HttpRequestUtil.sendGet(url_price, "market=wdc_qc")).getJSONObject("data").getString("last");
            }
            info = JSON.toJavaObject(get_info, WDCInfo.class);
            info.setPrice(get_price);
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.addAttribute("result", info);
        return "basic_info::basic_info";
    }

    @RequestMapping("/pass")
    public String pass(ModelMap map) {
        return "pass";
    }

    private static String url_price = "https://api.ceobi.com/api/market/ticker";

    @RequestMapping("/fork")
    public String fork(ModelMap map) throws IOException {
        map.addAttribute("role",customUserService.getRole());
        return "fork";
    }
}
