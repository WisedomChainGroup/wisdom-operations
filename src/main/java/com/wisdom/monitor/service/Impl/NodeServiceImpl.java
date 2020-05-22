package com.wisdom.monitor.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.leveldb.Leveldb;
import com.wisdom.monitor.model.Nodes;
import com.wisdom.monitor.model.Result;
import com.wisdom.monitor.model.ResultCode;
import com.wisdom.monitor.service.NodeService;
import com.wisdom.monitor.utils.ConnectionDbUtil;
import com.wisdom.monitor.utils.ConnectionUtil;
import com.wisdom.monitor.utils.MapCacheUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NodeServiceImpl implements NodeService {
    @Value("${Image}")
    private String image;
    private static final Logger logger = LoggerFactory.getLogger(NodeServiceImpl.class);
    @Override
    public Object stop(String ipPort) {
        List<String> strList = new ArrayList<String>();
        Result result = new Result();
        try {
            GetNodeinfo getNodeinfo = new GetNodeinfo(ipPort).invoke();
            String username = getNodeinfo.getUsername();
            String usepassword = getNodeinfo.getUsepassword();
            String ip = getNodeinfo.getIp();
            if(username == null || usepassword == null){
                result.setMessage("失败，请完善远程连接信息。");
                result.setCode(ResultCode.FAIL);
                return result;
            }
            ConnectionUtil connectionUtil = new ConnectionUtil(ip,username,usepassword);
            if(connectionUtil.login()){

                if(connectionUtil.login()){
                    String shellResult = connectionUtil.executeSuccess("echo "+usepassword+" |sudo -S docker stop "+image);
                    if(StringUtils.isBlank(shellResult)){
                        result.setMessage("停止失败");
                        result.setCode(ResultCode.FAIL);
                        return result;
                    }
                    result.setMessage("成功");
                    result.setCode(ResultCode.SUCCESS);
                    return result;
                }else {
                    result.setMessage("连接失败");
                    result.setCode(ResultCode.FAIL);
                    return result;
                }
            }


        } catch (Exception e) {
            result.setMessage("错误");
            result.setCode(ResultCode.FAIL);
            return result;
        }
        result.setMessage("失败");
        result.setCode(ResultCode.FAIL);
        return result;
    }

    @Override
    public Object restart(String ipPort) {
        List<String> strList = new ArrayList<String>();
        Result result = new Result();
        try {
            GetNodeinfo getNodeinfo = new GetNodeinfo(ipPort).invoke();
            String username = getNodeinfo.getUsername();
            String usepassword = getNodeinfo.getUsepassword();
            String ip = getNodeinfo.getIp();
            if(username == null || usepassword == null){
                result.setMessage("失败，请完善远程连接信息。");
                result.setCode(ResultCode.FAIL);
                return result;
            }
            ConnectionUtil connectionUtil = new ConnectionUtil(ip,username,usepassword);
            if(connectionUtil.login()){

                if(connectionUtil.login()){
                    String shellResult = connectionUtil.executeSuccess("echo "+usepassword+" |sudo -S docker restart "+image);
                    if(StringUtils.isBlank(shellResult)){
                        result.setMessage("重启失败");
                        result.setCode(ResultCode.FAIL);
                        return result;
                    }
                    result.setMessage("成功");
                    result.setCode(ResultCode.SUCCESS);
                    return result;
                }else {
                    result.setMessage("连接失败");
                    result.setCode(ResultCode.FAIL);
                    return result;
                }
            }


        } catch (Exception e) {
            result.setMessage("错误");
            result.setCode(ResultCode.FAIL);
            return result;
        }
        result.setMessage("失败");
        result.setCode(ResultCode.FAIL);
        return result;
    }

    @Override
    public Object deleteData(long height) {
        Result result = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            try {
                GetNodeinfo getNodeinfo = new GetNodeinfo(mapCacheUtil.getCacheItem("bindNode").toString()).invoke();
                ConnectionDbUtil connectionDbUtil = new ConnectionDbUtil(getNodeinfo.getDbip()+":"+getNodeinfo.getDbPort(),getNodeinfo.getDbname(),getNodeinfo.getDbusername(),getNodeinfo.getDbpassword());
                Result connectResult = (Result) connectionDbUtil.login();
                if (connectResult.getCode() == 2000){
                    String sql = "delete from transaction t where t.tx_hash in(select h.tx_hash from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height>=" + height + "))";
                    connectionDbUtil.getStatement().executeUpdate(sql);
                    String sql2 = "delete from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height>=" + height + ")";
                    connectionDbUtil.getStatement().executeUpdate(sql2);
                    String sql3 = "delete from header h where h.height>=" + height;
                    connectionDbUtil.getStatement().executeUpdate(sql3);
                    result.setMessage("成功");
                    result.setCode(ResultCode.SUCCESS);
                    return result;
                }else {
                    logger.error("deleteData warn:connection failed");
                    result.setMessage("数据库连接失败");
                    result.setCode(ResultCode.FAIL);
                    return result;
                }

            } catch (IOException e) {
                logger.error("deleteData error:"+e.getMessage());
                result.setMessage("失败");
                result.setCode(ResultCode.FAIL);
                return result;
            } catch (SQLException e) {
                logger.error("deleteData SQL error:"+e.getMessage());
                result.setMessage("失败");
                result.setCode(ResultCode.FAIL);
                return result;
            }
        }

        result.setMessage("请绑定节点");
        result.setCode(ResultCode.FAIL);
        logger.warn("deleteData warn:please Bind node");
        return result;
    }


    private class GetNodeinfo {
        private String ipPort;
        private String username;
        private String usepassword;
        private String ip;
        private String dbip;
        private String dbPort;
        private String dbname;
        private String dbusername;
        private String dbpassword;

        public GetNodeinfo(String ipPort) {
            this.ipPort = ipPort;
        }

        public String getUsername() {
            return username;
        }

        public String getUsepassword() {
            return usepassword;
        }

        public String getIp() {
            return ip;
        }

        public String getIpPort() {
            return ipPort;
        }

        public String getDbip() {
            return dbip;
        }

        public String getDbPort() {
            return dbPort;
        }

        public String getDbname() {
            return dbname;
        }

        public String getDbusername() {
            return dbusername;
        }

        public String getDbpassword() {
            return dbpassword;
        }

        public GetNodeinfo invoke() throws IOException {
            Leveldb leveldb = new Leveldb();
            Object read = JSONObject.parseArray(leveldb.readAccountFromSnapshot("node"), Nodes.class);
            List<Nodes> nodeList = new ArrayList<Nodes>();
            ip = null;
            if (read != null) {
                nodeList = (List<Nodes>) read;
                for (int i = 0; i < nodeList.size(); i++) {
                    if (nodeList.get(i).getNodeIP().equals(ipPort.split(":")[0]) && nodeList.get(i).getNodePort().equals(ipPort.split(":")[1])) {
                        ip = nodeList.get(i).getNodeIP();
                        username = nodeList.get(i).getUserName();
                        usepassword = nodeList.get(i).getPassword();
                        dbip = nodeList.get(i).getDbIP();
                        dbPort = nodeList.get(i).getDbPort();
                        dbname = nodeList.get(i).getDatabaseName();
                        dbusername = nodeList.get(i).getDbUsername();
                        dbpassword = nodeList.get(i).getDbPassword();
                        break;
                    }
                }
            }
            return this;
        }
    }
}
