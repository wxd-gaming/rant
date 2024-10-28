package wxdgaming.spring.boot.rant.module.rant.spi;

import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wxdgaming.spring.boot.core.SpringUtil;
import wxdgaming.spring.boot.core.lang.RunResult;
import wxdgaming.spring.boot.core.timer.MyClock;
import wxdgaming.spring.boot.core.util.StringsUtil;
import wxdgaming.spring.boot.rant.entity.bean.RantInfo;
import wxdgaming.spring.boot.rant.entity.store.RantRepository;
import wxdgaming.spring.boot.rant.module.rant.RantService;

import java.util.Collections;
import java.util.List;

/**
 * 吐槽接口
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-10-27 18:15
 **/
@RestController
@RequestMapping("/rant")
public class RantController {

    final RantRepository rantRepository;
    final RantService robotService;

    public RantController(RantRepository rantRepository, RantService robotService) {
        this.rantRepository = rantRepository;
        this.robotService = robotService;
    }

    @RequestMapping("/list")
    public RunResult list() {
        List<RantInfo> all = rantRepository.findAll();
        Collections.shuffle(all);
        List<JSONObject> list = all.stream().map(this::convert).limit(300).toList();
        return RunResult.ok().data(list).fluentPut("dataSize", all.size());
    }

    JSONObject convert(RantInfo rantInfo) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uid", rantInfo.getUid());
        jsonObject.put("address", StringsUtil.emptyOrNull(rantInfo.getIpAddress()) ? "外星球" : rantInfo.getIpAddress());
        jsonObject.put("content", rantInfo.getContent());
        jsonObject.put("time", MyClock.formatDate("MM/dd HH:mm", rantInfo.getCreatedTime()));
        return jsonObject;
    }

    @RequestMapping("/push")
    public RunResult push(HttpServletRequest request, @RequestParam String content) {
        if (StringsUtil.emptyOrNull(content)) {
            return RunResult.error("内容不能空");
        }
        if (content.trim().length() > 1024) {
            return RunResult.error("内容应该小于1000字");
        }
        String clientIp = SpringUtil.getClientIp();
        RantInfo rantInfo = new RantInfo()
                .setIp(clientIp)
                .setContent(content.trim());
        rantInfo.setUid(robotService.getGlobalData().rantNewId());
        rantInfo.setCreatedTime(MyClock.millis());
        rantRepository.save(rantInfo);
        robotService.saveAndFlush();
        return RunResult.ok().data(convert(rantInfo));
    }

}
