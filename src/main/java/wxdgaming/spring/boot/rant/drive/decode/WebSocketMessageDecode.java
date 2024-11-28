package wxdgaming.spring.boot.rant.drive.decode;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wxdgaming.spring.boot.core.InitPrint;
import wxdgaming.spring.boot.core.ann.Start;
import wxdgaming.spring.boot.net.BootstrapBuilder;
import wxdgaming.spring.boot.net.MessageDispatcher;
import wxdgaming.spring.boot.net.SocketSession;
import wxdgaming.spring.boot.net.server.ServerMessageDecode;
import wxdgaming.spring.boot.rant.module.chat.ChatService;

/**
 * 实现websocket解码器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-11-13 21:10
 **/
@Getter
@Service
@ChannelHandler.Sharable
public class WebSocketMessageDecode extends ServerMessageDecode implements InitPrint {

    ChatService chatService;

    @Autowired
    public WebSocketMessageDecode(BootstrapBuilder bootstrapBuilder, MessageDispatcher dispatcher) {
        super(bootstrapBuilder, dispatcher);
    }

    @Start
    public void start(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override protected void action(SocketSession socketSession, String message) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(message);
        chatService.onReceive(socketSession, jsonObject);
    }
}
