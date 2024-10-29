package wxdgaming.spring.boot.rant.entity.bean;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.spring.boot.data.batis.EntityBase;

/**
 * 全局数据
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-10-18 13:12
 **/
@Getter
@Setter
@Accessors(chain = true)
@Entity
public class GlobalData extends EntityBase<Integer> {

    @Column(nullable = false, columnDefinition = "bigint default 10000")
    private long rantNewId = 10000;

    public GlobalData() {
        this.setUid(1);
    }

    public synchronized long rantNewId() {
        return ++rantNewId;
    }

}