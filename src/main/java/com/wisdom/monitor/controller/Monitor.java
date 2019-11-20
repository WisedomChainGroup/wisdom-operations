//package com.wisdom.monitor.controller;
//
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.wisdom.monitor.model.Block;
//import com.wisdom.monitor.utils.HttpRequestUtil;
//import com.wisdom.monitor.utils.JavaShellUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import java.util.ArrayList;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//@Component
//@EnableScheduling
//public class Monitor {
//    @Value("${node}")
//    private String ip;
//    @Value("${unconfirmedBlocks}")
//    private Integer unconfirmedBlocks;
//    @Value("${maxControlBlocks}")
//    private Integer maxControlBlocks;
//    @Autowired
//    private JdbcTemplate tmpl;
//
////    @Scheduled(fixedRate=10000)
////    public void checkBlock(){
////        try {
////            String url = "http://"+ip+"/blocks/unconfirmed";
////            String result = HttpRequestUtil.sendGet(url,"");
////            if (result == null) return;
////            List<Block> blockList = JSONArray.parseArray(result,Block.class);
////            Collections.sort(blockList);
////            if(blockList.size() >= unconfirmedBlocks){
////            List<String> proposersList = getPeers();
////            Long blockHeight = 0L;
////            //对比所有的邻居借点
////            for(int i=1;i<=maxControlBlocks;i++){
////                blockHeight  = blockList.get(0).getnHeight()-i;
////                int confirmNum = 0;
////                for (String str : proposersList){
////                    String proposersBlockHash = getBlockHash(ip,blockHeight);
////                    if (!proposersBlockHash.equals(proposersBlockHash)){
////                        confirmNum++;
////                    }
////                }
////                //发现正确的则跳出循环
////                if(divisionRoundingUp(proposersList.size()*2,3)<confirmNum){
////                    break;
////                }
////            }
////            if(blockList.get(0).getnHeight()-1>blockHeight){
////                //关闭容器
////                JavaShellUtil.executeShell("docker-compose -f wdc.yml down");
////
////                //删数据
////
////                //重启容器
////                JavaShellUtil.executeShell("docker-compose -f wdc.yml up -d");
////            }
////
////            }
////        }catch (Exception e){
////            e.printStackTrace();
////        }
////
////
////    }
//
//    public  List<String> getPeers(){
////        String url = "http://"+ip+"/proposers";
//        String url = "http://"+"192.168.1.11:19585"+"/peers/status";
//        JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(url,""));
//        JSONObject data = result.getJSONObject("data");
//        JSONArray peersArray = data.getJSONArray("peers");
//        List<String> list = new ArrayList<>();
//        if(peersArray.size()>0){
//            for(Object s:peersArray){
//                list.add(s.toString().substring(s.toString().indexOf("@")+1,s.toString().lastIndexOf('"')-5)+":19585");
//            }
//        }
//        return list;
//    }
//
//    public String getBlockHash(String ip,Long height){
//        String blockHash = null;
//        try {
//            String blockUrl = "http://"+ip+"/block/"+height;
//            blockHash = (String) JSON.parseObject(HttpRequestUtil.sendGet(blockUrl,"")).get("blockHash");
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        return blockHash;
//    }
//
//    public int divisionRoundingUp(int divisor,int dividend){
//        float number = divisor/dividend;
//        int renewNum = (int)Math.ceil(number);
//        return renewNum;
//
//    }
//
//
//}
