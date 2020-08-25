package com.wisdom.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.wisdom.monitor.model.Mail;
import com.wisdom.monitor.model.Nodes;
import com.wisdom.monitor.model.Result;
import com.wisdom.monitor.service.Impl.NodeServiceImpl;
import com.wisdom.monitor.utils.*;
import com.wisdom.monitor.utils.ApiResult.APIResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tdf.common.store.LevelDb;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class AsynchronousController {
    private static final Logger logger = LoggerFactory.getLogger(AsynchronousController.class);

    @Autowired
    public NodeServiceImpl nodeService;
    @Autowired
    private LevelDb levelDb;

//    public NodeDao nodeDao;

    @GetMapping(value = {"/cpu"})
    public String cpu() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor processor = hal.getProcessor();
        double useRate = processor.getSystemCpuLoadBetweenTicks();
        return String.format("%.2f", useRate * 100) + "%";
    }

    @GetMapping(value = {"/block"})
    public String block() {
        int blockStatus = Monitor.checkBlockIsStuck(false);
        if (blockStatus == -1) {
            return "异常";
        } else if (blockStatus == 0) {
            return "待确认";
        } else {
            return "正常";
        }
    }

    @GetMapping(value = {"/outageStatus"})
    public boolean outageStatus() {
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        String ip = "";
        boolean outage = false;
        if (mapCacheUtil.getCacheItem("bindNode") != null) {
            ip = mapCacheUtil.getCacheItem("bindNode").toString();
            //获取当前高度
            String heightUrl = "http://" + ip + "/height";
            JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl, ""));

            if (result != null) {
                if (result.getInteger("code") == 2000) {
                    outage = true;
                }
            } else {
                return outage;
            }
        }
        return outage;
    }

    @GetMapping(value = {"/bifurcation"})
    public Object bifurcation() {
        Monitor monitor = new Monitor();
        return monitor.checkBifurcate();
    }

//    @GetMapping(value = {"/restore"})
//    public Object restore(@RequestParam("height") String height) {
//        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
//        try {
//            if (mapCacheUtil.getCacheItem("bindNode") != null) {
//                Long id = (Long)mapCacheUtil.getCacheItem("bindNode");
//                if (nodeDao.findNodesById(id).isPresent()){
//                    Nodes nodes = nodeDao.findNodesById((Long)mapCacheUtil.getCacheItem("bindNode")).get();
//                    ConnectionDbUtil connectionDbUtil = new ConnectionDbUtil(nodes.getNodeIP() + ":" + nodes.getNodePort(), nodes.getDatabaseName(), nodes.getDbUsername(), nodes.getDbPassword());
//                    Result connectResult = (Result) connectionDbUtil.login();
//                    if (connectResult.getCode() == 5000) {
//                        logger.warn("connection db failed");
//                        return APIResult.newFailResult(5000, "connection db failed");
//                    }
//                    if (nodes.getLeveldbPath().equals("")) {
//                        logger.info("levelDbPath can not be null");
//                        return APIResult.newFailResult(5000, "levelDbPath can not be null");
//                    }
//                    ConnectionUtil connectionUtil = new ConnectionUtil(nodes.getNodeIP(), nodes.getUserName(), nodes.getPassword());
//                    String shellResult = connectionUtil.executeSuccess("[ -d "+nodes.getLeveldbPath()+" ] && echo yes || echo no");
//                    if (shellResult.contains("no")){
//                        logger.info("levelDbPath does not exist");
//                        return APIResult.newFailResult(5000, "levelDbPath does not exist");
//                    }
//                }
//
////            查看数据库是否连接
////                Object read = null;
////                read = JSONObject.parseArray(leveldb.readAccountFromSnapshot("node"), Nodes.class);
////
////
////                List<Nodes> nodeList = new ArrayList<Nodes>();
////                String dbIp = "";
////                String dbPort = "";
////                String dbname = "";
////                String dbusername = "";
////                String dbpassword = "";
////                String levelDbPath = "";
////                String ssh_username = "";
////                String ssh_usepassword = "";
//
////                    nodeList = (List<Nodes>) read;
////                    for (int i = 0; i < nodeList.size(); i++) {
////                        if (nodeList.get(i).getNodeIP().equals(mapCacheUtil.getCacheItem("bindNode").toString().split(":")[0]) && nodeList.get(i).getNodePort().equals(mapCacheUtil.getCacheItem("bindNode").toString().split(":")[1])) {
////                            dbIp = nodeList.get(i).getDbIP();
////                            dbPort = nodeList.get(i).getDbPort();
////                            dbname = nodeList.get(i).getDatabaseName();
////                            dbusername = nodeList.get(i).getDbUsername();
////                            dbpassword = nodeList.get(i).getDbPassword();
////                            levelDbPath = nodeList.get(i).getLeveldbPath();
////                            ssh_username = nodeList.get(i).getUserName();
////                            ssh_usepassword = nodeList.get(i).getPassword();
////                            break;
////                        }
////                    }
//
//                } else {
//                    logger.warn("connection db failed");
//                    return APIResult.newFailResult(5000, "connection db failed");
//                }
//                Result bindNode = (Result) nodeService.stop(mapCacheUtil.getCacheItem("bindNode").toString());
//                if (bindNode.getCode() == 5000) {
//                    logger.info("stop node failed");
//                    return APIResult.newFailResult(5000, "stop node failed");
//                }
//                if (bindNode.getCode() == 2000) {
//                    logger.info("stop node success");
//                    DBUtil util = DBUtil.builder()
//                            .host(dbIp)
//                            .port(Integer.valueOf(dbPort))
//                            .username(dbusername)
//                            .password(dbpassword)
//                            .database(dbname)
//                            .leveldbDirectory(levelDbPath)
//                            .build();
//                    util.removeBlocksAfter(Long.valueOf(height));
//                    Result bindNode2 = (Result) nodeService.restart(mapCacheUtil.getCacheItem("bindNode").toString());
//                    if (bindNode2.getCode() == 2000) {
//                        logger.info("restart node success");
//                    } else {
//                        logger.info("restart node failed");
//                        return bindNode2;
//                    }
//                    boolean ismail = false;
//                    if (levelDb.get("mail".getBytes(StandardCharsets.UTF_8)).isPresent()){
//                        ismail = true;
//                    }
//                    if (ismail) {
//                        StringBuffer messageText = new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
//                        messageText.append("<span>通知:</span></br>");
//                        messageText.append("<span>您绑定的节点出现分叉，分叉区块已修复！</span></br>");
//                        new SendMailUtil().sendMailOutLook("通知", messageText.toString());
//                    }
//                } else {
//                    logger.warn("stop node failed,System_Password is error");
//                    return APIResult.newFailResult(5000,"stop node failed,System_Password is error");
//                }
//            }else {
//                return APIResult.newFailResult(5000,"Please bind node");
//            }
//
//
//        } catch (SQLException e) {
//            logger.error("error when removeBlocksAfter",e.getMessage());
//            return APIResult.newFailResult(5000, "error when removeBlocksAfter");
//        } catch (IOException e) {
//            logger.error("error when removeBlocksAfter",e.getMessage());
//            return APIResult.newFailResult(5000, e.getMessage());
//        } catch (Exception e) {
//            logger.error("error when removeBlocksAfter",e.getMessage());
//            return APIResult.newFailResult(5000, e.getMessage());
//        }
//        return APIResult.newSuccess(2000);
//    }
}
