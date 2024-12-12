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
 * 回复详情
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-10-30 19:09
 **/
@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(indexes = {
        @Index(columnList = "rantId"),
        @Index(columnList = "replyId"),
        @Index(columnList = "ip"),
        @Index(columnList = "ipAddress"),
})
public class ReplyInfo extends EntityBase<Long> {
    /** 当前回复是那个贴贴下面的 */
    private long rantId;
    /** 可能是回复某个贴贴里面的回复 */
    private long replyId;
    @Column(columnDefinition = "varchar(64)")
    private String ip;
    @Column(columnDefinition = "varchar(128)")
    private String ipAddress;
    @Column(columnDefinition = "varchar(1024)")
    private String content;
    @Column(columnDefinition = "int")
    private long likeCount;
    @Column(columnDefinition = "int")
    private long dislikeCount;
}
