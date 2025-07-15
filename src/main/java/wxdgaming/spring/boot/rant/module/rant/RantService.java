package wxdgaming.spring.boot.rant.module.rant;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wxdgaming.spring.boot.rant.entity.bean.GlobalData;
import wxdgaming.spring.boot.rant.entity.bean.RantInfo;
import wxdgaming.spring.boot.rant.entity.bean.ReplyInfo;
import wxdgaming.spring.boot.starter.batis.sql.JdbcContext;
import wxdgaming.spring.boot.starter.core.InitPrint;
import wxdgaming.spring.boot.starter.core.system.DumpUtil;
import wxdgaming.spring.boot.starter.core.timer.MyClock;
import wxdgaming.spring.boot.starter.core.util.StringsUtil;
import wxdgaming.spring.boot.starter.net.httpclient.HttpClientBuilder;
import wxdgaming.spring.boot.starter.net.httpclient.IPInfo;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;

/**
 * 数据服务
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-10-28 21:27
 */
@Slf4j
@Getter
@Service
public class RantService implements InitPrint, AutoCloseable, Closeable {

    final JdbcContext jdbcContext;
    final HttpClientBuilder httpClientService;
    GlobalData globalData;

    public RantService(HttpClientBuilder httpClientBuilder, JdbcContext jdbcContext) {
        this.httpClientService = httpClientBuilder;
        this.jdbcContext = jdbcContext;
    }

    @PostConstruct
    public void initialize() {
        Optional<GlobalData> byId = Optional.ofNullable(jdbcContext.find(GlobalData.class, 1));
        byId.ifPresentOrElse(
                find -> {
                    globalData = find;
                },
                () -> {
                    globalData = new GlobalData();
                    globalData.setCreatedTime(MyClock.millis());
                    saveAndFlush();
                }
        );
    }

    public void saveAndFlush() {
        jdbcContext.save(globalData);
    }

    @Override public void close() {
        saveAndFlush();
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void showMemory() {
        StringBuilder stringBuilder = new StringBuilder();
        DumpUtil.freeMemory(stringBuilder);
        log.info("{}", stringBuilder);
    }

    @Scheduled(cron = "0 */2 * * * ?")
    public void actionIp() {
        List<RantInfo> allNullIp = jdbcContext.findAll("select t from RantInfo t where t.ipAddress is null or t.ipAddress=''", RantInfo.class);
        for (RantInfo rantInfo : allNullIp) {
            if (StringsUtil.emptyOrNull(rantInfo.getIpAddress())) {
                try {
                    if ("127.0.0.1".equals(rantInfo.getIp())
                        || "localhost".equalsIgnoreCase(rantInfo.getIp())
                        || rantInfo.getIp().startsWith("192.168")) {
                        rantInfo.setIpAddress("内网");
                    } else {
                        IPInfo city4Ip = httpClientService.getCity4Ip(rantInfo.getIp());
                        rantInfo.setIpAddress(city4Ip.getRegionName() + "." + city4Ip.getCity());
                    }
                } catch (Exception ignore) {
                    rantInfo.setIpAddress("外星球");
                    log.warn("ip查询失败{}", ignore.toString());
                }
                jdbcContext.save(rantInfo);
            }
        }

        List<ReplyInfo> allNullIp1 = jdbcContext.findAll("select t from ReplyInfo t where t.ipAddress is null or t.ipAddress=''", ReplyInfo.class);
        for (ReplyInfo replyInfo : allNullIp1) {
            if (StringsUtil.emptyOrNull(replyInfo.getIpAddress())) {
                try {
                    if ("127.0.0.1".equals(replyInfo.getIp())
                        || "localhost".equalsIgnoreCase(replyInfo.getIp())
                        || replyInfo.getIp().startsWith("192.168")) {
                        replyInfo.setIpAddress("内网");
                    } else {
                        IPInfo city4Ip = httpClientService.getCity4Ip(replyInfo.getIp());
                        replyInfo.setIpAddress(city4Ip.getRegionName() + "." + city4Ip.getCity());
                    }
                } catch (Exception ignore) {
                    replyInfo.setIpAddress("外星球");
                    log.warn("ip查询失败{}", ignore.toString());
                }
                jdbcContext.save(replyInfo);
            }
        }
    }

}
