package wxdgaming.spring.boot.rant;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wxdgaming.spring.boot.core.InitPrint;
import wxdgaming.spring.boot.core.timer.MyClock;
import wxdgaming.spring.boot.rant.entity.bean.GlobalData;
import wxdgaming.spring.boot.rant.entity.store.GlobalRepository;

import java.io.Closeable;
import java.util.Optional;

/**
 * 后台服务中心
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-10-18 11:39
 **/
@Getter
@Service
public class RantService implements InitPrint, AutoCloseable, Closeable {

    @Autowired GlobalRepository globalRepository;
    private GlobalData globalData;

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

}
