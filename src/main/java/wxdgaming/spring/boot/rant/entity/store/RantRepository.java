package wxdgaming.spring.boot.rant.entity.store;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import wxdgaming.spring.boot.data.batis.BaseRepository;
import wxdgaming.spring.boot.rant.entity.bean.RantInfo;

import java.util.List;

/**
 * 吐槽墙
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-10-14 13:53
 **/
@Repository
public interface RantRepository extends BaseRepository<RantInfo, Long> {

    @Query(value = "select t from RantInfo t where t.ipAddress is null or t.ipAddress=''")
    List<RantInfo> findAllNullIp();

}
