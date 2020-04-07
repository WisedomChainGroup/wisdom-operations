package com.wisdom.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.leveldb.Leveldb;
import com.wisdom.monitor.model.*;
import com.wisdom.monitor.security.IsUser;
import com.wisdom.monitor.service.CustomUser;
import com.wisdom.monitor.utils.HttpRequestUtil;
import com.wisdom.monitor.utils.SysStatusUtil;
import org.apache.logging.log4j.status.StatusData;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.xml.soap.Node;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@IsUser
@Controller
@EnableScheduling
public class ThymeleafController {

    @Value("${node}")
    private String url_node;

    @RequestMapping("/thymeleaf")
    public String index(ModelMap map) {
        // 加入一个属性，用来在模板中读取
        map.addAttribute("baidu", "http://www.baidu.com");
        // return模板文件的名称，对应src/main/resources/templates/index.html
        return "test";
    }

    @RequestMapping(value = {"/", "/home"})
    public String home(ModelMap map) throws Exception {
        WDCInfo info = new WDCInfo();
        JSONObject get_info = JSON.parseObject(HttpRequestUtil.sendGet(String.format("http://%s/WisdomCore/ExplorerInfo", url_node), null)).getJSONObject("data");
        String get_price = JSON.parseObject(HttpRequestUtil.sendGet(url_price, "market=wdc_qc")).getJSONObject("data").getString("last");
        info = JSON.toJavaObject(get_info, WDCInfo.class);
        info.setPrice(get_price);
        map.addAttribute("result", info);
        SysStatusUtil sysStatusUtils = new SysStatusUtil();
        JSONObject jsonObject = sysStatusUtils.status();
        map.addAttribute("sysCpuUsed",jsonObject.getString("sysCpuUsed"));
        map.addAttribute("sysMemoryUsed",jsonObject.getString("sysMemoryUsed"));
        return "home";
    }

    @ResponseBody
    @RequestMapping(value = {"/cpu"})
    public String cpu() throws Exception {
        SysStatusUtil sysStatusUtils = new SysStatusUtil();
        JSONObject jsonObject = sysStatusUtils.status();
        return jsonObject.getString("sysCpuUsed");
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
    public String user(ModelMap map) throws IOException {
        Leveldb leveldb = new Leveldb();
        List<User> userList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("user"), User.class);
        map.addAttribute(userList);
        return "user";
    }

    @ResponseBody
    @RequestMapping("/adduser")
    public String adduser(@ModelAttribute User user) throws IOException {
        boolean tag = false;
        Leveldb leveldb = new Leveldb();
        List<User> userList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("user"), User.class);
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getName().equals(user.getName())) {
                tag = true;
                break;
            }
        }
        Result rs = new Result();
        if (!tag) {
            user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
            userList.add(user);
            leveldb.addAccount("user", JSON.toJSONString(userList));
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
    public String deleteuser(@ModelAttribute User user) throws IOException {
        Result rs = new Result();
        Leveldb leveldb = new Leveldb();
        List<User> userList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("user"), User.class);
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getName().equals(user.getName())) {
                userList.remove(i);
                leveldb.addAccount("user", JSON.toJSONString(userList));
                rs.setCode(ResultCode.SUCCESS);
                rs.setMessage("删除成功");
                break;
            }
        }
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/addnode")
    public String addnode(@ModelAttribute Nodes node) throws IOException {
        boolean tag = false;
        Leveldb leveldb = new Leveldb();
        List<Nodes> nodeList = new ArrayList<Nodes>();
        Object read = JSONObject.parseArray(leveldb.readAccountFromSnapshot("node"), Nodes.class);
        if (read != null) {
            nodeList = (List<Nodes>) read;
            for (int i = 0; i < nodeList.size(); i++) {
                if (nodeList.get(i).getNodeName().equals(node.getNodeName())) {
                    tag = true;
                    break;
                }
            }
        }
        Result rs = new Result();
        if (!tag) {
            nodeList.add(node);
            leveldb.addAccount("node", JSON.toJSONString(nodeList));
            rs.setCode(ResultCode.SUCCESS);
            rs.setMessage("添加节点成功");
        } else {
            rs.setCode(ResultCode.Warn);
            rs.setMessage("已存在相同节点");
        }
        return rs.toString();
    }

    @RequestMapping("/control")
    public String control(ModelMap map) {
        map.addAttribute((CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        map.addAttribute("dateNow", new java.util.Date().getTime());
        return "control";
    }

    @ResponseBody
    @RequestMapping("/editmail")
    public String editMail(@ModelAttribute Mail mail) throws IOException {
        Result rs = new Result();
        Leveldb leveldb = new Leveldb();
        leveldb.addAccount("mail", JSON.toJSONString(mail));
        rs.setCode(ResultCode.SUCCESS);
        rs.setMessage("成功");
        return rs.toString();
    }

    @RequestMapping("/mail")
    public String mail(ModelMap map) throws IOException {
        Leveldb leveldb = new Leveldb();
        Mail mail = new Mail();
        Object read = JSONObject.parseObject(leveldb.readAccountFromSnapshot("mail"), Mail.class);
        if (read != null) {
            mail= (Mail) read;
        }
        map.addAttribute(mail);
        return "mail";
    }

    @RequestMapping("/node")
    public String node(ModelMap map) throws IOException {
        Leveldb leveldb = new Leveldb();
        List<Nodes> nodeList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("node"), Nodes.class);
        if (nodeList != null) {
            map.addAttribute(nodeList);
        }
        return "node";
    }

    @RequestMapping("/basic_info")
    public String localRefresh(ModelMap map) {
        WDCInfo info = new WDCInfo();
        try {
            JSONObject get_info = JSON.parseObject(HttpRequestUtil.sendGet(String.format("http://%s/WisdomCore/ExplorerInfo", url_node), null)).getJSONObject("data");
            String get_price = JSON.parseObject(HttpRequestUtil.sendGet(url_price, "market=wdc_qc")).getJSONObject("data").getString("last");
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
}
