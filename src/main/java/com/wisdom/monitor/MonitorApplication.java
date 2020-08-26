package com.wisdom.monitor;

import org.iq80.leveldb.impl.Iq80DBFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.tdf.common.store.DBSettings;
import org.tdf.common.store.LevelDb;

//region zhangtong code
/*@SpringBootApplication
public class MonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);
    }

}*/

@SpringBootApplication
@EnableJpaRepositories
public class MonitorApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MonitorApplication.class);
    }

    @Bean
    public LevelDb levelDb() {
        LevelDb db = new LevelDb(Iq80DBFactory.factory, "local/leveldb", "tmp");
        db.init(DBSettings.DEFAULT);
        return db;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MonitorApplication.class, args);

    }
}

