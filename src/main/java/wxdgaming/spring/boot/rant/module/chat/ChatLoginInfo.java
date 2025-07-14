package wxdgaming.spring.boot.rant.module.chat;

import lombok.Getter;
import lombok.Setter;
import wxdgaming.spring.boot.starter.core.lang.ObjectBase;

/**
 * 当前聊天室登录情况
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-11-29 13:53
 **/
@Getter
@Setter
public class ChatLoginInfo extends ObjectBase {

    private String uid;
    private String nickName;
    private String ipAddress;
    private String loginTime;

}
