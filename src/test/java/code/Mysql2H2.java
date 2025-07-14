package code;

import com.alibaba.druid.pool.DruidDataSource;
import jakarta.persistence.EntityManager;
import org.junit.Test;
import wxdgaming.spring.boot.rant.entity.bean.GlobalData;
import wxdgaming.spring.boot.rant.entity.bean.RantInfo;
import wxdgaming.spring.boot.rant.entity.bean.ReplyInfo;
import wxdgaming.spring.boot.starter.batis.sql.DruidSourceConfig;
import wxdgaming.spring.boot.starter.batis.sql.JdbcContext;

import java.util.List;
import java.util.Map;

public class Mysql2H2 {

    JdbcContext h2jdbcContext;
    JdbcContext mysqljdbcContext;

    void createFileDriver() {
        DruidSourceConfig dataSourceConfig = new DruidSourceConfig();
        dataSourceConfig.setUrl("jdbc:h2:file:./database/rant");
        dataSourceConfig.setUsername("root");
        dataSourceConfig.setPassword("test");
        dataSourceConfig.setDriverClassName("org.h2.Driver");
        dataSourceConfig.setPackageNames(new String[]{GlobalData.class.getPackageName()});
        dataSourceConfig.setBatchInsert(true);
        dataSourceConfig.setDdlAuto("create");

        DruidDataSource dataSource = dataSourceConfig.toDataSource();
        EntityManager entityManager = dataSourceConfig.entityManager(dataSource, Map.of());

        h2jdbcContext = new JdbcContext(dataSource, entityManager);
    }

    void createMysqlDriver() {
        DruidSourceConfig dataSourceConfig = new DruidSourceConfig();
        dataSourceConfig.setUrl("jdbc:mysql://192.168.137.10:3306/rant?serverTimezone=UTC&autoReconnect=true&useUnicode=true&characterEncoding=utf8&useSSL=true&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&rewriteBatchedStatements=true");
        dataSourceConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceConfig.setUsername("root");
        dataSourceConfig.setPassword("test");
        dataSourceConfig.setPackageNames(new String[]{GlobalData.class.getPackageName()});
        dataSourceConfig.setBatchInsert(true);

        DruidDataSource dataSource = dataSourceConfig.toDataSource();
        EntityManager entityManager = dataSourceConfig.entityManager(dataSource, Map.of());

        mysqljdbcContext = new JdbcContext(dataSource, entityManager);
    }

    @Test
    public void importDb() {
        createFileDriver();
        createMysqlDriver();

        List<GlobalData> all = mysqljdbcContext.findAll(GlobalData.class);
        h2jdbcContext.batchSave(all);
        List<RantInfo> allRants = mysqljdbcContext.findAll(RantInfo.class);
        h2jdbcContext.batchSave(allRants);

        List<ReplyInfo> replyInfos = mysqljdbcContext.findAll(ReplyInfo.class);
        h2jdbcContext.batchSave(replyInfos);

    }

}
