package com.wisdom.monitor.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.model.Block;
import com.wisdom.monitor.service.TransactionService;
import com.wisdom.monitor.utils.ApiResult.APIResult;
import com.wisdom.monitor.utils.HttpRequestUtil;
import com.wisdom.monitor.utils.JavaShellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@EnableScheduling
public class Monitor {
    private static final Logger logger = LoggerFactory.getLogger(Monitor.class);
    @Autowired
    TransactionService transactionService;

    @Value("${node}")
    private String ip;
    @Value("${password}")
    private String password;
    @Value("${imageVersion}")
    private String imageVersion;
    @Autowired
    private JdbcTemplate tmpl;

    @Scheduled(fixedRate=10000)
    public void checkBlock(){
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
                    if (divisionRoundingUp(proposersList.size() * 2, 3) > confirmNum) {
                        logger.info("Wrong Block Height: "+ nHeight);
//                        String stopShell = "echo "+password+" |sudo -S docker stop wdc_core_v"+imageVersion;
//                        JavaShellUtil.executeShell(stopShell);
//                        Thread.sleep(3000);
//                        //删数据
//                        tmpl.batchUpdate("delete from incubator_state i where i.height>=" + nHeight,
//                                "delete from account a where a.blockheight>=" + nHeight,
//                                "delete from transaction t where t.tx_hash in(select h.tx_hash from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height>=" + nHeight + "))",
//                                "delete from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height>=" + nHeight + ")",
//                                "delete from header h where h.height>=" + nHeight);
//                        System.out.println("----------------------2--------------");
//                        //重启容器
//                        String restartShell = "echo "+password+" |sudo -S docker restart wdc_core_v"+imageVersion;
//                        JavaShellUtil.executeShell(restartShell);

                    }
                }
            }
    }

    public  List<String> getPeers(){
        String url = "http://"+ip+"/peers/status";
        JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(url,""));
        List<String> list = new ArrayList<>();
        if(result != null){
            JSONObject data = result.getJSONObject("data");
            JSONArray peersArray = data.getJSONArray("peers");
            if(peersArray.size()>0){
                for(Object s:peersArray){
                    list.add(s.toString().substring(s.toString().indexOf("@")+1,s.toString().lastIndexOf('"')-5)+":19585");
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
        int renewNum = (int)Math.ceil(number);
        return renewNum;

    }
}
