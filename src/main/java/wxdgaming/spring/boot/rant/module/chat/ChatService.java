package wxdgaming.spring.boot.rant.module.chat;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wxdgaming.spring.boot.core.ann.Start;
import wxdgaming.spring.boot.core.lang.RunResult;
import wxdgaming.spring.boot.core.threading.LogicExecutor;
import wxdgaming.spring.boot.core.timer.MyClock;
import wxdgaming.spring.boot.core.util.HtmlDecoder;
import wxdgaming.spring.boot.core.util.StringsUtil;
import wxdgaming.spring.boot.net.SessionHandler;
import wxdgaming.spring.boot.net.SocketSession;
import wxdgaming.spring.boot.net.server.SocketService;
import wxdgaming.spring.boot.webclient.HttpClientService;
import wxdgaming.spring.boot.webclient.IPInfo;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 聊天服务
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-11-27 20:52
 **/
@Slf4j
@Service
public class ChatService implements SessionHandler {

    final String BindKey = "__bind_key";

    final LogicExecutor logicExecutor;
    SocketService socketService;
    final HttpClientService httpClientService;
    private final ReentrantLock roomLock = new ReentrantLock();
    /** 历史聊天记录 */
    LinkedList<RunResult> history = new LinkedList<>();

    public ChatService(HttpClientService httpClientService, LogicExecutor logicExecutor) {
        this.httpClientService = httpClientService;
        this.logicExecutor = logicExecutor;
    }

    @Start
    public void start(SocketService socketService) {
        this.socketService = socketService;
        this.socketService.getSocketServerDeviceHandler().setSessionHandler(this);
    }

    public void onReceive(SocketSession session, JSONObject jsonObject) {
        String cmd = jsonObject.getString("cmd");
        RunResult runResult = RunResult.ok();
        runResult.fluentPut("cmd", cmd);
        switch (cmd) {
            case "ping":
                session.writeAndFlush(runResult.toJSONString());
                return;
            case "chat-img":
            case "chat": {
                ChatLoginInfo chatLoginInfo = session.attribute(BindKey);
                if (chatLoginInfo == null) {
                    /*尚未登录不允许发言*/
                    return;
                }
                runResult.put("nick", chatLoginInfo.getNickName());
                runResult.put("time", MyClock.nowString());
                runResult.put("address", chatLoginInfo.getIpAddress());
                String content = jsonObject.getString("content");
                if (StringsUtil.emptyOrNull(content)) {
                    return;
                }
                content = content.trim();
                if (content.length() > 400)
                    content = content.substring(0, 400) + "...";
                runResult.put("content", content);
                roomLock.lock();
                try {
                    history.add(runResult);
                    if (history.size() > 1000) {
                        history.removeFirst();
                    }
                } finally {
                    roomLock.unlock();
                }
            }
            break;
            case "login": {
                ChatLoginInfo chatLoginInfo = new ChatLoginInfo();
                String nick = String.valueOf(jsonObject.getOrDefault("nick", "匿名"));
                if (nick.length() > 20) {
                    nick = nick.substring(0, 20) + "...";
                }
                nick = HtmlDecoder.escapeHtml3(nick);
                chatLoginInfo.setNickName(nick);
                logicExecutor.execute(() -> {
                    try {
                        if ("127.0.0.1".equals(session.getIP())
                            || "localhost".equalsIgnoreCase(session.getIP())
                            || session.getIP().startsWith("192.168")) {
                            chatLoginInfo.setIpAddress("内网");
                        } else {
                            IPInfo city4Ip = httpClientService.getCity4Ip(session.getIP());
                            chatLoginInfo.setIpAddress(city4Ip.getRegionName() + "." + city4Ip.getCity());
                        }
                    } catch (Exception ignore) {
                        chatLoginInfo.setIpAddress("外星球");
                        log.warn("ip查询失败{}", ignore.toString());
                    }
                });
                session.attribute(BindKey, chatLoginInfo);
                String jsonString;
                roomLock.lock();
                try {
                    runResult.put("nick", nick);
                    RunResult logined = RunResult.ok().fluentPut("cmd", "logined");
                    logined.fluentPut("history", history);
                    jsonString = logined.toJSONString();
                } finally {
                    roomLock.unlock();
                }
                /*回复自己登录成功*/
                session.writeAndFlush(jsonString);
            }
            break;
            default:
                log.warn("未知命令{}", cmd);
                return;
        }
        socketService.writeAndFlush(runResult.toJSONString());
        log.info("action:{}", jsonObject);
    }

}
