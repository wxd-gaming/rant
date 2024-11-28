package wxdgaming.spring.boot.rant.module.chat;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wxdgaming.spring.boot.core.ann.Start;
import wxdgaming.spring.boot.core.lang.RunResult;
import wxdgaming.spring.boot.core.threading.LogicExecutor;
import wxdgaming.spring.boot.core.timer.MyClock;
import wxdgaming.spring.boot.net.SessionHandler;
import wxdgaming.spring.boot.net.SocketSession;
import wxdgaming.spring.boot.net.server.SocketService;
import wxdgaming.spring.boot.webclient.HttpClientService;
import wxdgaming.spring.boot.webclient.IPInfo;

/**
 * 聊天服务
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-11-27 20:52
 **/
@Slf4j
@Service
public class ChatService implements SessionHandler {

    final String NICK_KEY = "NICK_KEY";
    final String ADDRESS_KEY = "ADDRESS_KEY";

    final LogicExecutor logicExecutor;
    SocketService socketService;
    final HttpClientService httpClientService;

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
                break;
            case "chat": {
                runResult.put("nick", session.attribute(NICK_KEY));
                runResult.put("time", MyClock.nowString());
                runResult.put("address", session.attribute(ADDRESS_KEY));
                runResult.put("content", jsonObject.getString("content"));
            }
            break;
            case "login": {
                String nick = String.valueOf(jsonObject.getOrDefault("nick", "匿名"));
                session.attribute(NICK_KEY, nick);
                logicExecutor.execute(() -> {
                    try {
                        if ("127.0.0.1".equals(session.getIP())
                            || "localhost".equalsIgnoreCase(session.getIP())
                            || session.getIP().startsWith("192.168")) {
                            session.attribute(ADDRESS_KEY, "内网");
                        } else {
                            IPInfo city4Ip = httpClientService.getCity4Ip(session.getIP());
                            session.attribute(ADDRESS_KEY, city4Ip.getRegionName() + "." + city4Ip.getCity());
                        }
                    } catch (Exception ignore) {
                        session.attribute(ADDRESS_KEY, "外星球");
                        log.warn("ip查询失败{}", ignore.toString());
                    }
                });
                runResult.put("nick", nick);
                socketService.writeAndFlush(RunResult.ok().fluentPut("cmd", "logined").toJSONString());
            }
            break;
        }
        socketService.writeAndFlush(runResult.toJSONString());
        log.info("action:{}", jsonObject);
    }

}
