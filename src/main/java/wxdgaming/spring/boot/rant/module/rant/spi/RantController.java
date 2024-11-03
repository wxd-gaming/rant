package wxdgaming.spring.boot.rant.module.rant.spi;

import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wxdgaming.spring.boot.core.SpringUtil;
import wxdgaming.spring.boot.core.lang.RunResult;
import wxdgaming.spring.boot.core.timer.MyClock;
import wxdgaming.spring.boot.core.util.HtmlDecoder;
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
    public RunResult list(@RequestParam(name = "sort", required = false, defaultValue = "随机") String sort) {
        sort = HtmlDecoder.escapeHtml3(sort);
        List<RantInfo> all = rantRepository.findAll();
        if ("发布倒序".equals(sort)) {
            all.sort((o1, o2) -> {
                if (!o2.getCreatedTime().equals(o1.getCreatedTime())) {
                    return Long.compare(o2.getCreatedTime(), o1.getCreatedTime());
                }
                return Long.compare(o2.getUid(), o1.getUid());
            });
        } else if ("发布正序".equals(sort)) {
            all.sort((o1, o2) -> {
                if (!o1.getCreatedTime().equals(o2.getCreatedTime())) {
                    return Long.compare(o1.getCreatedTime(), o2.getCreatedTime());
                }
                return Long.compare(o1.getUid(), o2.getUid());
            });
        } else if ("最多点赞".equals(sort)) {
            all.sort((o1, o2) -> {
                if (o2.getLikeCount() != o1.getLikeCount()) {
                    return Long.compare(o2.getLikeCount(), o1.getLikeCount());
                }
                if (!o2.getCreatedTime().equals(o1.getCreatedTime())) {
                    return Long.compare(o2.getCreatedTime(), o1.getCreatedTime());
                }
                return Long.compare(o2.getUid(), o1.getUid());
            });
        } else if ("最多点踩".equals(sort)) {
            all.sort((o1, o2) -> {
                if (o2.getDislikeCount() != o1.getDislikeCount()) {
                    return Long.compare(o2.getDislikeCount(), o1.getDislikeCount());
                }
                if (!o2.getCreatedTime().equals(o1.getCreatedTime())) {
                    return Long.compare(o2.getCreatedTime(), o1.getCreatedTime());
                }
                return Long.compare(o2.getUid(), o1.getUid());
            });
        } else if ("最多评论".equals(sort)) {
            all.sort((o1, o2) -> {
                if (o2.getReplyCount() != o1.getReplyCount()) {
                    return Long.compare(o2.getReplyCount(), o1.getReplyCount());
                }
                if (!o2.getCreatedTime().equals(o1.getCreatedTime())) {
                    return Long.compare(o2.getCreatedTime(), o1.getCreatedTime());
                }
                return Long.compare(o2.getUid(), o1.getUid());
            });
        } else if ("最新评论".equals(sort)) {
            all.sort((o1, o2) -> {
                if (o2.getLastReplyTime() != o1.getLastReplyTime()) {
                    return Long.compare(o2.getLastReplyTime(), o1.getLastReplyTime());
                }
                if (!o2.getCreatedTime().equals(o1.getCreatedTime())) {
                    return Long.compare(o2.getCreatedTime(), o1.getCreatedTime());
                }
                return Long.compare(o2.getUid(), o1.getUid());
            });
        } else {
            Collections.shuffle(all);
        }
        List<JSONObject> list = all.stream().map(this::convert).limit(300).toList();
        return RunResult.ok().data(list).fluentPut("dataSize", all.size());
    }

    JSONObject convert(RantInfo rantInfo) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uid", rantInfo.getUid());
        jsonObject.put("address", StringsUtil.emptyOrNull(rantInfo.getIpAddress()) ? "外星球" : rantInfo.getIpAddress());
        jsonObject.put("content", rantInfo.getContent());
        jsonObject.put("time", MyClock.formatDate("MM/dd HH:mm:ss", rantInfo.getCreatedTime()));
        jsonObject.put("replyCount", rantInfo.getReplyCount());
        jsonObject.put("likeCount", rantInfo.getLikeCount());
        jsonObject.put("dislikeCount", rantInfo.getDislikeCount());
        return jsonObject;
    }

    @RequestMapping("/push")
    public RunResult push(HttpServletRequest request, @RequestParam String content) {
        content = HtmlDecoder.escapeHtml3(content);
        if (StringsUtil.emptyOrNull(content)) {
            return RunResult.error("内容不能空");
        }
        if (content.trim().length() > 1024) {
            return RunResult.error("内容应该小于1000字");
        }
        String clientIp = SpringUtil.getClientIp();
        RantInfo rantInfo = new RantInfo()
                .setIp(clientIp)
                .setIpAddress("")
                .setContent(content.trim());
        rantInfo.setUid(robotService.getGlobalData().rantNewId());
        rantInfo.setCreatedTime(MyClock.millis());
        rantRepository.save(rantInfo);
        robotService.saveAndFlush();
        return RunResult.ok().data(convert(rantInfo));
    }

    @RequestMapping("/like")
    public RunResult like(@RequestParam(name = "uid") Long uid) {
        RantInfo rantInfo = rantRepository.findById(uid).orElse(null);
        if (rantInfo == null) {
            return RunResult.error("找不到记录");
        }
        synchronized (rantInfo.getUid().toString().intern()) {
            rantInfo.setLikeCount(Math.addExact(rantInfo.getLikeCount(), 1));
            rantRepository.save(rantInfo);
        }
        return RunResult.ok().data(rantInfo.getLikeCount());
    }

    @RequestMapping("/dislike")
    public RunResult dislike(@RequestParam(name = "uid") Long uid) {
        RantInfo rantInfo = rantRepository.findById(uid).orElse(null);
        if (rantInfo == null) {
            return RunResult.error("找不到记录");
        }
        synchronized (rantInfo.getUid().toString().intern()) {
            rantInfo.setDislikeCount(Math.addExact(rantInfo.getDislikeCount(), 1));
            rantRepository.save(rantInfo);
        }
        return RunResult.ok().data(rantInfo.getDislikeCount());
    }

}
