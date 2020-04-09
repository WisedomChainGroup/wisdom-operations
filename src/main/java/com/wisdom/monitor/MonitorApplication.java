package com.wisdom.monitor;

import com.wisdom.monitor.model.ConfigBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

//region zhangtong code
/*@SpringBootApplication
public class MonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);
    }

}*/
//endregion
//@EnableConfigurationProperties({ConfigBean.class})
@SpringBootApplication
public class MonitorApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MonitorApplication.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MonitorApplication.class, args);
    }
}

