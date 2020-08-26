package com.wisdom.monitor.service.Impl;

import com.wisdom.monitor.dao.NodeDao;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tdf.common.store.LevelDb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NodeServiceImpl implements NodeService {
    @Value("${Image}")
    private String image;
    private static final Logger logger = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private NodeDao nodeDao;


    @Autowired
    private LevelDb levelDb;

    @Override
    public Object stop(String ipPort) {
        List<String> strList = new ArrayList<String>();
        Result result = new Result();
        try {
            Nodes node = nodeDao.findNodesByNodeIPAndNodePort(ipPort.split(":")[0],ipPort.split(":")[1]).get();
            String username = node.getUserName();
            String usepassword = node.getPassword();
            String ip = node.getNodeIP();
            if (username == null || usepassword == null) {
                result.setMessage("失败，请完善远程连接信息。");
                result.setCode(ResultCode.FAIL);
                return result;
            }
            ConnectionUtil connectionUtil = new ConnectionUtil(ip, username, usepassword);
            if (connectionUtil.login()) {

                if (connectionUtil.login()) {
                    String shellResult = connectionUtil.executeSuccess("echo " + usepassword + " |sudo -S docker stop " + image);
                    if (StringUtils.isBlank(shellResult)) {
                        result.setMessage("停止失败");
                        result.setCode(ResultCode.FAIL);
                        return result;
                    }
                    result.setMessage("成功");
                    result.setCode(ResultCode.SUCCESS);
                    return result;
                } else {
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
            Nodes node = nodeDao.findNodesByNodeIPAndNodePort(ipPort.split(":")[0],ipPort.split(":")[1]).get();
            String username = node.getUserName();
            String usepassword = node.getPassword();
            String ip = node.getNodeIP();
            if (username == null || usepassword == null) {
                result.setMessage("失败，请完善远程连接信息。");
                result.setCode(ResultCode.FAIL);
                return result;
            }
            ConnectionUtil connectionUtil = new ConnectionUtil(ip, username, usepassword);
            if (connectionUtil.login()) {

                if (connectionUtil.login()) {
                    String shellResult = connectionUtil.executeSuccess("echo " + usepassword + " |sudo -S docker restart " + image);
                    if (StringUtils.isBlank(shellResult)) {
                        result.setMessage("重启失败");
                        result.setCode(ResultCode.FAIL);
                        return result;
                    }
                    result.setMessage("成功");
                    result.setCode(ResultCode.SUCCESS);
                    return result;
                } else {
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
        if (mapCacheUtil.getCacheItem("bindNode") != null) {
            try {
                String ipPort = mapCacheUtil.getCacheItem("bindNode").toString();
                Nodes node = nodeDao.findNodesByNodeIPAndNodePort(ipPort.split(":")[0],ipPort.split(":")[1]).get();
                ConnectionDbUtil connectionDbUtil = new ConnectionDbUtil(node.getDbIP() + ":" + node.getDbPort(), node.getDatabaseName(), node.getDbUsername(), node.getDbPassword());
                Result connectResult = (Result) connectionDbUtil.login();
                if (connectResult.getCode() == 2000) {
                    String sql = "delete from transaction t where t.tx_hash in(select h.tx_hash from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height>=" + height + "))";
                    connectionDbUtil.getStatement().executeUpdate(sql);
                    String sql2 = "delete from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height>=" + height + ")";
                    connectionDbUtil.getStatement().executeUpdate(sql2);
                    String sql3 = "delete from header h where h.height>=" + height;
                    connectionDbUtil.getStatement().executeUpdate(sql3);
                    result.setMessage("成功");
                    result.setCode(ResultCode.SUCCESS);
                    return result;
                } else {
                    logger.error("deleteData warn:connection failed");
                    result.setMessage("数据库连接失败");
                    result.setCode(ResultCode.FAIL);
                    return result;
                }

            } catch (SQLException e) {
                logger.error("deleteData SQL error:" + e.getMessage());
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

    @Override
    public Nodes searchNode(String ipPort) {
        if (nodeDao.findNodesByNodeIPAndNodePort(ipPort.split(":")[0],ipPort.split(":")[1]).isPresent()){
            return nodeDao.findNodesByNodeIPAndNodePort(ipPort.split(":")[0],ipPort.split(":")[1]).get();
        }
        return new Nodes();
    }

    @Override
    public boolean updateNode(Nodes nodes) {
        boolean t = false;
        if (nodeDao.findNodesByNodeIPAndNodePort(nodes.getNodeIP(),nodes.getNodePort()).isPresent()){
            nodes.setId(nodeDao.findNodesByNodeIPAndNodePort(nodes.getNodeIP(),nodes.getNodePort()).get().getId());
            nodeDao.save(nodes);
            t = true;
        }
        return t;
    }

}
