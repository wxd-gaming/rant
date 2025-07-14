package wxdgaming.spring.boot.rant.module.chat;

import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wxdgaming.spring.boot.starter.core.ann.AppStart;
import wxdgaming.spring.boot.starter.core.format.HexId;
import wxdgaming.spring.boot.starter.core.json.FastJsonUtil;
import wxdgaming.spring.boot.starter.core.lang.RunResult;
import wxdgaming.spring.boot.starter.core.threading.ExecutorUtil;
import wxdgaming.spring.boot.starter.core.threading.IExecutorServices;
import wxdgaming.spring.boot.starter.core.timer.MyClock;
import wxdgaming.spring.boot.starter.core.util.HtmlDecoder;
import wxdgaming.spring.boot.starter.core.util.JwtUtils;
import wxdgaming.spring.boot.starter.core.util.StringsUtil;
import wxdgaming.spring.boot.starter.net.SocketSession;
import wxdgaming.spring.boot.starter.net.httpclient.HttpClientBuilder;
import wxdgaming.spring.boot.starter.net.httpclient.IPInfo;
import wxdgaming.spring.boot.starter.net.pojo.IWebSocketStringListener;
import wxdgaming.spring.boot.starter.net.server.SocketServer;

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
public class ChatService implements IWebSocketStringListener {

    final String BindKey = "__bind_key";
    final String jwtKEy = "__jwt_keysdfsgewgwegfhsodifjwsoeitgjwegsogiwegweg";
    final HexId hexId = new HexId(1);
    final IExecutorServices logicExecutor;
    SocketServer socketService;
    final HttpClientBuilder httpClientBuilder;
    private final ReentrantLock roomLock = new ReentrantLock();
    /** 历史聊天记录 */
    LinkedList<RunResult> history = new LinkedList<>();

    public ChatService(HttpClientBuilder httpClientBuilder, ExecutorUtil executorUtil) {
        this.httpClientBuilder = httpClientBuilder;
        this.logicExecutor = executorUtil.getLogicExecutor();
    }

    @AppStart
    public void start(SocketServer socketService) {
        this.socketService = socketService;
    }

    @Override public void onMessage(SocketSession socketSession, String message) {
        JSONObject jsonObject = FastJsonUtil.parse(message);
        onReceive(socketSession, jsonObject);
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
                runResult.put("uid", chatLoginInfo.getUid());
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
                String token = jsonObject.getString("token");
                try {
                    if (StringsUtil.notEmptyOrNull(token)) {
                        Jws<Claims> claimsJws = JwtUtils.parseJWT(jwtKEy, token);
                        String uid = claimsJws.getPayload().get("uid", String.class);
                        chatLoginInfo.setUid(uid);
                    }
                } catch (Exception e) {
                    log.error("token {}", token, e);
                }
                if (StringsUtil.emptyOrNull(chatLoginInfo.getUid())) {
                    chatLoginInfo.setUid(String.valueOf(hexId.newId()));
                }
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
                            IPInfo city4Ip = httpClientBuilder.getCity4Ip(session.getIP());
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
                    runResult.put("nick", chatLoginInfo.getNickName());

                    JwtBuilder jwt = JwtUtils.createJwtBuilder(jwtKEy);
                    jwt.claim("uid", chatLoginInfo.getUid());
                    RunResult logined = RunResult.ok()
                            .fluentPut("cmd", "logined")
                            .fluentPut("token", jwt.compact())
                            .fluentPut("uid", chatLoginInfo.getUid())
                            .fluentPut("history", history);
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
        socketService.getSessionGroup().writeAndFlush(runResult.toJSONString());
        log.info("action:{}", jsonObject);
    }

}
