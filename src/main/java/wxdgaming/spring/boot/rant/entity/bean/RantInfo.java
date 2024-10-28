package wxdgaming.spring.boot.rant.entity.bean;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.spring.boot.data.batis.EntityBase;

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
        @Index(columnList = "accountId")
})
public class RantInfo extends EntityBase<Long> {

    private String ip;
    private String ipAddress;
    private int accountId;
    private boolean showNick = false;
    @Column(columnDefinition = "varchar(1024) comment '吐槽内容，不得超过1000个字'")
    private String content;

}
