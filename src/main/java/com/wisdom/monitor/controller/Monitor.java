package com.wisdom.monitor.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.model.Block;
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
import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@EnableScheduling
public class Monitor {
    @Value("${node}")
    private String ip;
    @Value("${unconfirmedBlocks}")
    private Integer unconfirmedBlocks;
    @Value("${maxControlBlocks}")
    private Integer maxControlBlocks;
    @Autowired
    private JdbcTemplate tmpl;

    private static final Logger logger = LoggerFactory.getLogger(Monitor.class);
    @Scheduled(fixedRate=10000)
    public void checkBlock(){
        try {
            String url = "http://"+ip+"/blocks/unconfirmed";
            String result = HttpRequestUtil.sendGet(url,"");
            if (result == null) return;
            List<Block> blockList = JSONArray.parseArray(result,Block.class);
            Collections.sort(blockList);
            if(blockList.size() >= unconfirmedBlocks){
            List<String> proposersList = getPeers();
            Long blockHeight = 0L;
            //对比所有的邻居借点
            for(int i=1;i<=maxControlBlocks;i++){
                blockHeight  = blockList.get(0).getnHeight()-i;
                int confirmNum = 0;
                for (String str : proposersList){
                    String proposersBlockHash = getBlockHash(ip,blockHeight);
                    if (!proposersBlockHash.equals(proposersBlockHash)){
                        confirmNum++;
                    }
                }
                //发现正确的则跳出循环
                if(divisionRoundingUp(proposersList.size()*2,3)<confirmNum){
                    break;
                }
            }
            if(blockList.get(0).getnHeight()-1>blockHeight){
                //关闭容器
                JavaShellUtil.executeShell("docker-compose -f wdc.yml down");

                //删数据


                //重启容器
                JavaShellUtil.executeShell("docker-compose -f wdc.yml up -d");
            }

            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public  List<String> getPeers(){
//        String url = "http://"+ip+"/proposers";
        String url = "http://"+"192.168.1.11:19585"+"/peers/status";
        JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(url,""));
        JSONObject data = result.getJSONObject("data");
        JSONArray peersArray = data.getJSONArray("peers");
        List<String> list = new ArrayList<>();
        if(peersArray.size()>0){
            for(Object s:peersArray){
                list.add(s.toString().substring(s.toString().indexOf("@")+1,s.toString().lastIndexOf('"')-5)+":19585");
            }
        }
        return list;
    }

    public String getBlockHash(String ip,Long height){
        String blockHash = null;
        try {
            String blockUrl = "http://"+ip+"/block/"+height;
            blockHash = (String) JSON.parseObject(HttpRequestUtil.sendGet(blockUrl,"")).get("blockHash");
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
    public static void main(String[] args) {
//        String test = "[{,,,},{,,,,},{,,,,}]";
//        String t = test.substring(1,test.length()-1);
//        System.out.println(test.substring(test.indexOf("{"), test.indexOf("}")));
//        [ {  "blockSize" : 131454,  "blockHash" : "c0d273547cfa6095a11d0b546669c1f6fe485734a92d2227b602075d73189546",  "nVersion" : 0,  "hashPrevBlock" : "1007b5d4048bb4abc583ea87ecab681801cc13c660c664499e3d138cf2524cc6",  "hashMerkleRoot" : "9e736793e0e66a0d45fe3c963554381382becbf0c72300f117aa80af250ff6e9",  "hashMerkleState" : "0f5d23b03ec98b61088a3a77d3696258b1fc906c2b9900bc51b13f747cb759b1",  "hashMerkleIncubate" : "0000000000000000000000000000000000000000000000000000000000000000",  "nHeight" : 51976,  "nTime" : 1573722589,  "nBits" : "00001dc196c0249d4dbdc4c16f444989a4e14a3a473b36e5bdea2eb1bcebf7c8",  "nNonce" : "51cace10af69f1311c069a4c4b73ea95a060aed03ce254fb3c8c6b39865df4da",  "blockNotice" : null,  "body" : [ {    "transactionHash" : "0dcabbc6b8a15c1ce27dec098daea49b79329677e7acae08a363482d45c884f3",    "version" : 1,    "type" : 0,    "nonce" : 13325,    "from" : "0000000000000000000000000000000000000000000000000000000000000000",    "gasPrice" : 0,    "amount" : 666666666,    "payload" : null,    "to" : "c017fd8d81fb6e5bbe56dc549c33abcf4f397332",    "signature" : "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",    "blockHash" : "c0d273547cfa6095a11d0b546669c1f6fe485734a92d2227b602075d73189546",    "fee" : 0,    "blockHeight" : 51976  } ]}, {  "blockSize" : 131454,  "blockHash" : "2b8e64201f8de89885002cc3e2c101009167520375d0153dcdf4ac02a756ce5d",  "nVersion" : 0,  "hashPrevBlock" : "c0d273547cfa6095a11d0b546669c1f6fe485734a92d2227b602075d73189546",  "hashMerkleRoot" : "8ec5211cc8fc2753205d5f0724ea9344c24db4f27c35b4f6e56fb6c8e6cc5b18",  "hashMerkleState" : "e7fec515fffd4caf7dcda7f720c33cb30c6c69a81b8bc0466b6ccec3db294f52",  "hashMerkleIncubate" : "0000000000000000000000000000000000000000000000000000000000000000",  "nHeight" : 51977,  "nTime" : 1573722590,  "nBits" : "00001dc196c0249d4dbdc4c16f444989a4e14a3a473b36e5bdea2eb1bcebf7c8",  "nNonce" : "eaff6c91175981f5ca68cd5fad5947d3903c4215b24c529b204f0c48d9b05666",  "blockNotice" : null,  "body" : [ {    "transactionHash" : "0aa4337e682c37699a889badd737ea607144db98e80c8d8e212e3d11dda35a8e",    "version" : 1,    "type" : 0,    "nonce" : 11961,    "from" : "0000000000000000000000000000000000000000000000000000000000000000",    "gasPrice" : 0,    "amount" : 666666666,    "payload" : "",    "to" : "ee649fbd62ee91dce16017152c94acdaa11abe86",    "signature" : "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",    "blockHash" : "2b8e64201f8de89885002cc3e2c101009167520375d0153dcdf4ac02a756ce5d",    "fee" : 0,    "blockHeight" : 51977  } ]}, {  "blockSize" : 131454,  "blockHash" : "a245410eb5952a31836c22625e4ff9c757e9658816718b348e66feab432f18c2",  "nVersion" : 0,  "hashPrevBlock" : "2b8e64201f8de89885002cc3e2c101009167520375d0153dcdf4ac02a756ce5d",  "hashMerkleRoot" : "7846406ee78c1fbc96c9ddd143a701306b5eb6eb3d1283a8b2481e53460edf54",  "hashMerkleState" : "aa334b51f9c2eae9ead9db56a6914ebc5427dcffe20c1c4afc0379c328f5e64b",  "hashMerkleIncubate" : "0000000000000000000000000000000000000000000000000000000000000000",  "nHeight" : 51978,  "nTime" : 1573722610,  "nBits" : "00001dc196c0249d4dbdc4c16f444989a4e14a3a473b36e5bdea2eb1bcebf7c8",  "nNonce" : "2d8d47b9cfed882854e42d2819205272db55324b9e5a908b07c3ec0c127124ac",  "blockNotice" : null,  "body" : [ {    "transactionHash" : "f677219c20c2882d13b21f25ff46881058baf823be063076b6a337c6e99ebdbc",    "version" : 1,    "type" : 0,    "nonce" : 9088,    "from" : "0000000000000000000000000000000000000000000000000000000000000000",    "gasPrice" : 0,    "amount" : 666666666,    "payload" : "",    "to" : "c4061e318e4ce23ae37b56568a0d2edbe8d394fe",    "signature" : "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",    "blockHash" : "a245410eb5952a31836c22625e4ff9c757e9658816718b348e66feab432f18c2",    "fee" : 0,    "blockHeight" : 51978  } ]} ]
        Monitor m = new Monitor();
//        List<String> list = m.getProposers();
//        for (String l : list){
//            System.out.println(l);
//        }
//        int i = 10;
//        while (i<0){
//            i=i-1;
//            System.out.println(i);
//        }


//        String blockUrl = "http://"+"192.168.1.11:19585"+"/block/58450";
//        JSONObject json = JSON.parseObject(HttpRequestUtil.sendGet(blockUrl,""));
//        System.out.println(json.get("blockHash"));

//        int a = 0;
//        int b= 0 ;
//        boolean w =false;
//        for (int i=0;i<5;i++){
//            for(int j=0;j<5;j++){
//                System.out.println("------");
//                b++;
//                if(b>3){
//                    w = true;
//                    break;
//                }
//                a++;
//                System.out.println(a);
//            }
//
//
//        }
//        logger.error("post ");
//        for (int i=0;i<5;i++){
//            System.out.println("test");
//            if (i>3){
//                break;
//            }
//        }

//        List<String>list = m.getPeers();
//        System.out.println(list);
//        List<String> list = new ArrayList<>();
//        list.add("192.168.1.40:9585");
//        list.add("192.168.1.12:19585");
//        list.add("192.168.1.13:19585");
//        for (String ste:list){
//            String proposersBlockHash = m.getBlockHash(ste,100L);
//            System.out.println(proposersBlockHash);
//        }




     }


}
