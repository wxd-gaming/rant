package wxdgaming.spring.boot.rant.module.rant.spi;

import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wxdgaming.spring.boot.rant.entity.bean.RantInfo;
import wxdgaming.spring.boot.rant.entity.bean.ReplyInfo;
import wxdgaming.spring.boot.rant.module.rant.RantService;
import wxdgaming.spring.boot.starter.batis.sql.JdbcContext;
import wxdgaming.spring.boot.starter.core.SpringUtil;
import wxdgaming.spring.boot.starter.core.lang.RunResult;
import wxdgaming.spring.boot.starter.core.timer.MyClock;
import wxdgaming.spring.boot.starter.core.util.HtmlDecoder;
import wxdgaming.spring.boot.starter.core.util.StringsUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 吐槽接口
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-10-27 18:15
 **/
@RestController
@RequestMapping("/reply")
public class ReplyController {

    final JdbcContext jdbcContext;
    final RantService robotService;
    final RantController rantController;

    public ReplyController(JdbcContext jdbcContext, RantService robotService, RantController rantController) {
        this.jdbcContext = jdbcContext;
        this.robotService = robotService;
        this.rantController = rantController;
    }

    @RequestMapping("/get")
    public RunResult get(@RequestParam(name = "rantId") long rantId) {

        List<ReplyInfo> allByRantId = jdbcContext.findAll("select t from ReplyInfo t where t.rantId = ?1", ReplyInfo.class, rantId);
        Optional<RantInfo> byId = Optional.ofNullable(jdbcContext.find(RantInfo.class, rantId));
        if (byId.isEmpty()) {
            return RunResult.error("找不到记录");
        }
        List<JSONObject> list = allByRantId.stream().map(this::convert).collect(Collectors.toList());
        Collections.reverse(list);
        return RunResult.ok()
                .fluentPut("rant", rantController.convert(byId.get()))
                .fluentPut("replyList", list)
                .fluentPut("dataSize", allByRantId.size());
    }

    JSONObject convert(ReplyInfo rantInfo) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uid", rantInfo.getUid());
        jsonObject.put("rantId", rantInfo.getRantId());
        jsonObject.put("replyId", rantInfo.getReplyId());
        jsonObject.put("address", StringsUtil.emptyOrNull(rantInfo.getIpAddress()) ? "外星球" : rantInfo.getIpAddress());
        jsonObject.put("content", rantInfo.getContent());
        jsonObject.put("time", MyClock.formatDate("MM/dd HH:mm:ss", rantInfo.getCreatedTime()));
        jsonObject.put("likeCount", rantInfo.getLikeCount());
        jsonObject.put("dislikeCount", rantInfo.getDislikeCount());
        return jsonObject;
    }

    @RequestMapping("/push")
    public RunResult push(HttpServletRequest request,
                          @RequestParam(name = "rantId") Long rantId,
                          @RequestParam(name = "replyId", required = false, defaultValue = "0") Long replyId,
                          @RequestParam(name = "content") String content) {
        synchronized (rantId.toString().intern()) {
            content = HtmlDecoder.escapeHtml3(content);
            if (StringsUtil.emptyOrNull(content)) {
                return RunResult.error("内容不能空");
            }
            if (content.trim().length() > 1024) {
                return RunResult.error("内容应该小于1000字");
            }

            Optional<RantInfo> byId = jdbcContext.findNullable(RantInfo.class, rantId);
            if (byId.isEmpty()) {
                return RunResult.error("找不到记录");
            }
            if (replyId > 0) {
                if (jdbcContext.findNullable(ReplyInfo.class, replyId).isEmpty()) {
                    return RunResult.error("找不到回复记录");
                }
            }

            byId.get().setReplyCount(Math.addExact(byId.get().getReplyCount(), 1));
            byId.get().setLastReplyTime(MyClock.millis());

            String clientIp = SpringUtil.getClientIp();
            ReplyInfo replyInfo = new ReplyInfo()
                    .setRantId(rantId)
                    .setReplyId(replyId)
                    .setIp(clientIp)
                    .setIpAddress("")
                    .setContent(content.trim());
            replyInfo.setUid(robotService.getGlobalData().replyNewId());
            replyInfo.setCreatedTime(MyClock.millis());
            jdbcContext.save(replyInfo);
            jdbcContext.save(byId.get());
            robotService.saveAndFlush();
            return RunResult.ok().data(byId.get().getReplyCount());
        }
    }

    @RequestMapping("/like")
    public RunResult like(@RequestParam(name = "uid") Long uid) {
        ReplyInfo rantInfo = jdbcContext.findNullable(ReplyInfo.class, uid).orElse(null);
        if (rantInfo == null) {
            return RunResult.error("找不到记录");
        }
        synchronized (rantInfo.getUid().toString().intern()) {
            rantInfo.setLikeCount(Math.addExact(rantInfo.getLikeCount(), 1));
            jdbcContext.save(rantInfo);
        }
        return RunResult.ok().data(rantInfo.getLikeCount());
    }

    @RequestMapping("/dislike")
    public RunResult dislike(@RequestParam(name = "uid") Long uid) {
        ReplyInfo rantInfo = jdbcContext.findNullable(ReplyInfo.class, uid).orElse(null);
        if (rantInfo == null) {
            return RunResult.error("找不到记录");
        }
        synchronized (rantInfo.getUid().toString().intern()) {
            rantInfo.setDislikeCount(Math.addExact(rantInfo.getDislikeCount(), 1));
            jdbcContext.save(rantInfo);
        }
        return RunResult.ok().data(rantInfo.getDislikeCount());
    }

}
