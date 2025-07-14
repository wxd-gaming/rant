package wxdgaming.spring.boot.rant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import wxdgaming.spring.boot.starter.batis.sql.DataJdbcScan;
import wxdgaming.spring.boot.starter.core.CoreScan;
import wxdgaming.spring.boot.starter.net.NetScan;
import wxdgaming.spring.boot.starter.net.httpclient.HttpClientPoolScan;

@Slf4j
@EnableScheduling
@EntityScan("wxdgaming.spring.boot.rant.entity.bean")
@SpringBootApplication(
        scanBasePackageClasses = {
                CoreScan.class,
                DataJdbcScan.class,
                NetScan.class,
                HttpClientPoolScan.class,
                RantMain.class,
        },
        exclude = {
                DataSourceAutoConfiguration.class,
                MongoAutoConfiguration.class
        }
)
public class RantMain {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(RantMain.class);
        run.getBean(RantSpringReflect.class).getSpringReflectContent().executorAppStartMethod();
    }
}