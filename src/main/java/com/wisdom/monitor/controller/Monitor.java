package com.wisdom.monitor.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.leveldb.Leveldb;
import com.wisdom.monitor.model.Mail;
import com.wisdom.monitor.model.User;
import com.wisdom.monitor.service.TransactionService;
import com.wisdom.monitor.utils.*;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class Monitor {
    private static final Logger logger = LoggerFactory.getLogger(Monitor.class);
    @Autowired
    private Leveldb leveldb;
    @Autowired
    TransactionService transactionService;
    @Value("${password}")
    private String password;
    @Value("${imageVersion}")
    private String imageVersion;
    @Autowired
    private JdbcTemplate tmpl;

    //分叉监测
    public boolean checkBifurcate(){
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
                            return true;
                    }
                }
            }
            return false;
    }

    //分叉修复
    public  void recoveryBifurcate(boolean ismail){
        try {
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            if (mapCacheUtil.getCacheItem("bindNode") != null) {
                String ip = mapCacheUtil.getCacheItem("bindNode").toString();
                //获取当前高度
                String heightUrl = "http://"+ip+"/height";
                JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl,""));
                Long nHeight = 0L;
                if(result != null) {
                    nHeight  = result.getLong("data");
                }
                if(checkBifurcate()){
                    if (ismail){
                        StringBuffer messageText=new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
                        messageText.append("<span>警告:</span></br>");
                        messageText.append("<span>您绑定的节点出现分叉！</span></br>");
                        SendMailUtil.sendMailOutLook("通知",messageText.toString());
                    }
                    logger.info("Wrong Block Height: "+ nHeight);
                    String stopShell = "echo "+password+" |sudo -S docker stop wdc_core_v"+imageVersion;
                    JavaShellUtil.executeShell(stopShell);
                    Thread.sleep(3000);
                    //删数据
                    tmpl.batchUpdate("delete from incubator_state i where i.height>=" + nHeight,
                            "delete from account a where a.blockheight>=" + nHeight,
                            "delete from transaction t where t.tx_hash in(select h.tx_hash from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height>=" + nHeight + "))",
                            "delete from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height>=" + nHeight + ")",
                            "delete from header h where h.height>=" + nHeight);
                    //重启容器
                    String restartShell = "echo "+password+" |sudo -S docker restart wdc_core_v"+imageVersion;
                    JavaShellUtil.executeShell(restartShell);
                    if (ismail){
                        StringBuffer messageText=new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
                        messageText.append("<span>通知:</span></br>");
                        messageText.append("<span>您的分叉区块已修复！</span></br>");
                        SendMailUtil.sendMailOutLook("通知",messageText.toString());
                    }
                }
            }
        }catch (InterruptedException e) {
            logger.error("InterruptedException when recoveryBifurcate",e);
        } catch (IOException e) {
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
                JSONArray peersArray = data.getJSONArray("blockList");
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

    public int divisionRoundingUp(int divisor,int dividend){
        float number = divisor/dividend;
        return (int)Math.ceil(number);

    }

    public static void main(String[] args) throws SigarException, IOException {
        Leveldb leveldb = new Leveldb();
        List<User> userList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("user"), User.class);
        System.out.println(userList);


    }
}
