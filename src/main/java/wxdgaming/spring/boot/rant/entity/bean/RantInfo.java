package wxdgaming.spring.boot.rant.entity.bean;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.spring.boot.data.EntityBase;

/**
 * 吐槽墙
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-10-27 18:06
 */
@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(indexes = {
        @Index(columnList = "accountId"),
        @Index(columnList = "ip"),
        @Index(columnList = "ipAddress"),
})
public class RantInfo extends EntityBase<Long> {

    @Column(nullable = false, columnDefinition = "varchar(128)")
    private String ip;
    @Column(nullable = false, columnDefinition = "varchar(128)")
    private String ipAddress;
    private int accountId;
    private boolean showNick = false;
    private String nickName;
    @Column(columnDefinition = "varchar(1024)")
    private String content;
    @Column(columnDefinition = "int")
    private int replyCount=0;
    @Column(columnDefinition = "bigint")
    private long lastReplyTime=0;
    @Column(columnDefinition = "int")
    private long likeCount;
    @Column(columnDefinition = "int")
    private long dislikeCount;
}
