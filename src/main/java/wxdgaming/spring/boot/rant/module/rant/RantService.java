package wxdgaming.spring.boot.rant.module.rant;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wxdgaming.spring.boot.core.InitPrint;
import wxdgaming.spring.boot.core.system.DumpUtil;
import wxdgaming.spring.boot.core.timer.MyClock;
import wxdgaming.spring.boot.core.util.StringsUtil;
import wxdgaming.spring.boot.rant.entity.bean.GlobalData;
import wxdgaming.spring.boot.rant.entity.bean.RantInfo;
import wxdgaming.spring.boot.rant.entity.store.GlobalRepository;
import wxdgaming.spring.boot.rant.entity.store.RantRepository;
import wxdgaming.spring.boot.webclient.HttpClientService;
import wxdgaming.spring.boot.webclient.IPInfo;

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

    final GlobalRepository globalRepository;
    final RantRepository rantRepository;
    private final HttpClientService httpClientService;
    private GlobalData globalData;

    public RantService(RantRepository rantRepository, GlobalRepository globalRepository, HttpClientService httpClientService) {
        this.rantRepository = rantRepository;
        this.globalRepository = globalRepository;
        this.httpClientService = httpClientService;
    }

    @PostConstruct
    public void initialize() {
        Optional<GlobalData> byId = globalRepository.findById(1);
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
        globalRepository.saveAndFlush(globalData);
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

    @Scheduled(cron = "0 */5 * * * ?")
    public void actionIp() {
        List<RantInfo> allNullIp = rantRepository.findAllNullIp();
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
                rantRepository.saveAndFlush(rantInfo);
            }
        }
    }

}
