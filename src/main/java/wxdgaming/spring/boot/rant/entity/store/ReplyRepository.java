package wxdgaming.spring.boot.rant.entity.store;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import wxdgaming.spring.boot.rant.entity.bean.ReplyInfo;
import wxdgaming.spring.boot.starter.batis.sql.BaseRepository;

import java.util.List;

/**
 * 吐槽墙
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-10-14 13:53
 **/
@Repository
public interface ReplyRepository extends BaseRepository<ReplyInfo, Long> {

    @Query(value = "select t from ReplyInfo t where t.ipAddress is null or t.ipAddress=''")
    List<ReplyInfo> findAllNullIp();

    @Query(value = "select t from ReplyInfo t where t.rantId=?1")
    List<ReplyInfo> findAllByRantId(long rantId);

}
