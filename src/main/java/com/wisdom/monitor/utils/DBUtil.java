package com.wisdom.monitor.utils;

import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.dao.NodeDao;
import com.wisdom.monitor.leveldb.Leveldb;
import com.wisdom.monitor.model.Nodes;
import com.wisdom.monitor.service.Impl.NodeServiceImpl;
import com.wisdom.monitor.service.NodeService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Component;

import javax.mail.MailSessionDefinition;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Component
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DBUtil {
    private static final String HASH_QUERY = "select h.block_hash from header h where h.height >= ?";

    private static final String SQL_0 = "delete from transaction t where t.tx_hash in(select h.tx_hash from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height >= ? ))";
    private static final String SQL_1 = "delete from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height >= ? )";
    private static final String SQL_2 = "delete from header h where h.height >= ?";

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    public NodeServiceImpl nodeService;

    @Builder.Default
    private String host = "localhost";

    @Builder.Default
    private int port = 5432;

    @Builder.Default
    private String username = "postgres";

    @Builder.Default
    private String password = "postgres";

    @Builder.Default
    private String database = "postgres";

    @Builder.Default
    private String leveldbDirectory = "/opt/leveldb";

    @Builder.Default
    private int leveldbMaxFiles = 512;

    @Builder.Default
    private LevelDBType levelDBType = LevelDBType.JNI;

    @Value("${Image}")
    private String image;



    enum LevelDBType {
        IQ80,
        JNI
    }

    /**
     * remove blocks whose height greater than or equals to @param height
     *
     * @param height block height
     */
    @SneakyThrows
    public void removeBlocksAfter(long height) throws Exception{
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        JdbcTemplate template = template();
        List<byte[]> hashes =
                template.query(HASH_QUERY, new Object[]{height}, new SingleColumnRowMapper<>(byte[].class));
        template.update(SQL_0, height);
        template.update(SQL_1, height);
        template.update(SQL_2, height);
        String ipPort = mapCacheUtil.getCacheItem("bindNode").toString();
        Nodes node = nodeDao.findNodesByNodeIPAndNodePort(ipPort.split(":")[0],ipPort.split(":")[1]).get();
        ConnectionDbUtil connectionDbUtil = new ConnectionDbUtil(node.getDbIP() + ":" + node.getDbPort(), node.getDatabaseName(), node.getDbUsername(), node.getDbPassword());
        String ssh_username = node.getUserName();
        String ssh_usepassword = node.getPassword();
        String ssh_ip = node.getNodeIP();
        ConnectionUtil connectionUtil = new ConnectionUtil(ssh_ip, ssh_username, ssh_usepassword);
        String shellResult = connectionUtil.executeSuccess("echo " + ssh_usepassword + " |sudo -S rm -rf "+ leveldbDirectory);
//        DBFactory factory =
//                levelDBType == LevelDBType.IQ80 ?
//                        Iq80DBFactory.factory :
//                        JniDBFactory.factory;
//
//
//        List<LevelDb> levelDbs =
//                Stream.of("account", "asset-code", "candidate", "lock-gettransfer", "validator")
//                        .map(n -> new LevelDb(factory, leveldbDirectory, n + "-trie-roots"))
//                        .collect(Collectors.toList());
//
//        for (LevelDb db : levelDbs) {
//            db.init(DBSettings.newInstance()
//                    .withMaxOpenFiles(leveldbMaxFiles)
//                    .withMaxThreads(Math.max(1, Runtime.getRuntime().availableProcessors() / 2)));
//            for (byte[] hash : hashes) {
//                db.remove(hash);
//            }
//        }
////
//        levelDbs.forEach(LevelDb::close);
//        template.getDataSource().getConnection().close();
    }

    private JdbcTemplate template(){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", host, port, database));
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        HikariDataSource ds = new HikariDataSource(config);
        return
                new JdbcTemplate(ds);
    }

//    public static void main(String[] args) {
//        DBUtil util = DBUtil
//                .builder()
//                .host("192.168.1.123")
//                .port(5433)
//                .username("postgres")
//                .password("password")
//                .database("ddl")
//                .leveldbDirectory("/opt/leveldb_1")
//                .build();
//
//        // remove blocks whose height >= 12345678 (inclusive)
//        util.removeBlocksAfter(12345678);
//    }

}
