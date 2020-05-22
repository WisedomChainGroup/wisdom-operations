package com.wisdom.monitor.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.leveldb.Leveldb;
import com.wisdom.monitor.model.Mail;
import com.wisdom.monitor.model.Nodes;
import com.wisdom.monitor.model.Result;
import com.wisdom.monitor.model.ResultCode;
import com.wisdom.monitor.service.Impl.NodeServiceImpl;
import com.wisdom.monitor.service.TransactionService;
import com.wisdom.monitor.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;


@Component
public class Monitor {
    private static final Logger logger = LoggerFactory.getLogger(Monitor.class);
    @Autowired
    private Leveldb leveldb;
    @Autowired
    TransactionService transactionService;
    @Value("${Image}")
    private String image;
    @Autowired
    private JdbcTemplate tmpl;
    @Autowired
    public NodeServiceImpl nodeService;

    //分叉监测
    public int checkBifurcate(){
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            if (mapCacheUtil.getCacheItem("bindNode") != null){
                String ip = mapCacheUtil.getCacheItem("bindNode").toString();
                //获取当前高度
                String heightUrl = "http://"+ip+"/height";
                JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl,""));
                if(result != null){
                    Long nHeight = result.getLong("data");
                    String nBlockhash = getBlockHash(ip,nHeight);
                    int confirmNum =0;
                    List<String> proposersList = getPeers();
                    if(proposersList.size()>0) {
                        for (String str : proposersList) {
                            String proposersBlockHash = getBlockHash(str, nHeight);
                            if(proposersBlockHash != null){
                                if (proposersBlockHash.equals(nBlockhash)) {
                                    confirmNum++;
                                }
                            }
                        }
                        //不满足2/3一致则删除对于高度的区块
                        if (divisionRoundingUp(proposersList.size() * 2, 3) > confirmNum)
                            return -1;
                    }
                }
                return 1;
            }
            return 0;
    }

    //分叉修复
    public  void recoveryBifurcate(boolean ismail){
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        try {
            if (mapCacheUtil.getCacheItem("bindNode") != null) {
                String ip = mapCacheUtil.getCacheItem("bindNode").toString();
                //获取当前高度
                String heightUrl = "http://"+ip+"/height";
                JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl,""));
                Long nHeight = 0L;
                if(result != null) {
                    nHeight  = result.getLong("data");
                }
                if(checkBifurcate() == -1){
                    logger.info("Wrong Block Height: "+ nHeight);
                    //查看数据库是否连接
                    Leveldb leveldb = new Leveldb();
                    Object read = JSONObject.parseArray(leveldb.readAccountFromSnapshot("node"), Nodes.class);
                    List<Nodes> nodeList = new ArrayList<Nodes>();
                    String dbIp = "";
                    String dbPort = "";
                    String dbname = "";
                    String dbusername = "";
                    String dbpassword = "";
                    if (read != null) {
                        nodeList = (List<Nodes>) read;
                        for (int i = 0; i < nodeList.size(); i++) {
                            if (nodeList.get(i).getNodeIP().equals(mapCacheUtil.getCacheItem("bindNode").toString().split(":")[0]) && nodeList.get(i).getNodePort().equals(mapCacheUtil.getCacheItem("bindNode").toString().split(":")[1])) {
                                dbIp = nodeList.get(i).getDbIP();
                                dbPort = nodeList.get(i).getDbPort();
                                dbname = nodeList.get(i).getDatabaseName();
                                dbusername = nodeList.get(i).getDbUsername();
                                dbpassword = nodeList.get(i).getDbPassword();
                                break;
                            }
                        }
                        ConnectionDbUtil connectionDbUtil = new ConnectionDbUtil(dbIp+":"+dbPort,dbname,dbusername,dbpassword);
                        Result connectResult = (Result) connectionDbUtil.login();
                        if (connectResult.getCode() == 5000){
                            logger.warn("connection db failed");
                            return;
                        }
                    }else{
                        logger.warn("connection db failed");
                        return;
                    }
                    Result bindNode = (Result) nodeService.stop(mapCacheUtil.getCacheItem("bindNode").toString());
                    if (bindNode.getCode() == 5000){
                        logger.info("stop node failed");
                        return;
                    }
                    if (bindNode.getCode() == 2000){
                        logger.info("stop node success");
                        Result deletResult = (Result) nodeService.deleteData(nHeight);
                        Result bindNode2 = (Result) nodeService.restart(mapCacheUtil.getCacheItem("bindNode").toString());
                        if (bindNode2.getCode() == 2000){
                            logger.info("restart node success");
                        }else{
                            logger.info("restart node failed");
                        }
                        if (ismail && deletResult.getCode() == 2000){
                            StringBuffer messageText=new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
                            messageText.append("<span>通知:</span></br>");
                            messageText.append("<span>您绑定的节点出现分叉，分叉区块已修复！</span></br>");
                            SendMailUtil.sendMailOutLook("通知",messageText.toString());
                        }
                    }else {
                        logger.warn("stop node failed,System_Password is error");
                        return;
                    }
                }
            }
        } catch (IOException e) {
            nodeService.restart(mapCacheUtil.getCacheItem("bindNode").toString());
            logger.error("IOException when sendEmail",e);
        }

    }

    //卡块监测
    public static int checkBlockIsStuck(boolean ismail){
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null) {
            //获取当前高度
            String ip = mapCacheUtil.getCacheItem("bindNode").toString();
            String heightUrl = "http://" + ip + "/height";
            JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl, ""));
            if (result == null)
                return -1;
            if (mapCacheUtil.getCacheItem("BlockHeight") == null) {
                if ((int) result.get("code") == 2000) {
                    mapCacheUtil.putCacheItem("BlockHeight", result.getLong("data"));
                }
                return 0;
            }
            String height = mapCacheUtil.getCacheItem("BlockHeight").toString();
            if (!height.equals(result.getLong("data"))) {
                return 1;
            }else {
                if (ismail){
                    StringBuffer messageText=new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
                    messageText.append("<span>警告:</span></br>");
                    messageText.append("<span>您绑定的节点存在卡块风险！请检查！！！</span></br>");
                    try {
                        SendMailUtil.sendMailOutLook("通知",messageText.toString());
                    } catch (IOException e) {
                        logger.error("IOException when sendEmail",e);
                    }
                }
                return -1;
            }
        }
        mapCacheUtil.removeCacheItem("BlockHeight");
        return -1;
    }
    //整体监测
    @Scheduled(fixedRate=20000)
    public void monitorStatus() throws IOException {
        boolean ismail = false;
        Leveldb leveldb = new Leveldb();
        Mail mail = new Mail();
        Object read = JSONObject.parseObject(leveldb.readAccountFromSnapshot("mail"), Mail.class);
        if (read != null) {
            mail= (Mail) read;
            ismail = true;
        }
        recoveryBifurcate(ismail);
        checkBlockIsStuck(ismail);

    }



    public  List<String> getPeers(){
        List<String> list = new ArrayList<>();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null) {
            //获取当前高度
            String ip = mapCacheUtil.getCacheItem("bindNode").toString();
            String url = "http://" + ip + "/peers/status";
            JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(url, ""));

            if (result != null) {
                JSONObject data = result.getJSONObject("data");
                JSONArray peersArray = data.getJSONArray("peers");
                if (peersArray.size() > 0) {
                    for (Object s : peersArray) {
                        list.add(s.toString().substring(s.toString().indexOf("@") + 1, s.toString().length() - 5) + ":19585");
                    }
                }
            }
        }

        return list;
    }

    public String getBlockHash(String ip,Long height){
        String blockHash = " ";
        try {
            String blockUrl = "http://"+ip+"/block/"+height;
            JSONObject jsonObject= JSON.parseObject(HttpRequestUtil.sendGet(blockUrl,""));
            if(jsonObject != null){
                blockHash = (String) jsonObject.get("blockHash");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return blockHash;
    }

    public static int divisionRoundingUp(int divisor, int dividend){
        Float number = Float.valueOf(divisor)/Float.valueOf(dividend);
        return (int)Math.ceil(number);

    }
}
