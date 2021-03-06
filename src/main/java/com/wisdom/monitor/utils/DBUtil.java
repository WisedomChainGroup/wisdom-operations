package com.wisdom.monitor.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.tdf.common.store.DBSettings;
import org.tdf.common.store.LevelDb;

import javax.mail.MailSessionDefinition;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DBUtil {
    private static final String HASH_QUERY = "select h.block_hash from header h where h.height >= ?";

    private static final String SQL_0 = "delete from transaction t where t.tx_hash in(select h.tx_hash from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height >= ? ))";
    private static final String SQL_1 = "delete from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height >= ? )";
    private static final String SQL_2 = "delete from header h where h.height >= ?";

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
    public void removeBlocksAfter(long height) throws SQLException {
        JdbcTemplate template = template();
        List<byte[]> hashes =
                template.query(HASH_QUERY, new Object[]{height}, new SingleColumnRowMapper<>(byte[].class));
        template.update(SQL_0, height);
        template.update(SQL_1, height);
        template.update(SQL_2, height);

        DBFactory factory =
                levelDBType == LevelDBType.IQ80 ?
                        Iq80DBFactory.factory :
                        JniDBFactory.factory;


        List<LevelDb> levelDbs =
                Stream.of("account", "asset-code", "candidate", "lock-gettransfer", "validator")
                        .map(n -> new LevelDb(factory, leveldbDirectory, n + "-trie-roots"))
                        .collect(Collectors.toList());

        for (LevelDb db : levelDbs) {
            db.init(DBSettings.newInstance()
                    .withMaxOpenFiles(leveldbMaxFiles)
                    .withMaxThreads(Math.max(1, Runtime.getRuntime().availableProcessors() / 2)));
            for (byte[] hash : hashes) {
                db.remove(hash);
            }
        }

        levelDbs.forEach(LevelDb::close);
        template.getDataSource().getConnection().close();
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
