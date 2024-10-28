package wxdgaming.spring.boot.rant.entity.store;

import org.springframework.stereotype.Repository;
import wxdgaming.spring.boot.data.batis.BaseJpaRepository;
import wxdgaming.spring.boot.rant.entity.bean.GlobalData;

/**
 * 门店
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-10-14 13:53
 **/
@Repository
public interface GlobalRepository extends BaseJpaRepository<GlobalData, Integer> {


}
