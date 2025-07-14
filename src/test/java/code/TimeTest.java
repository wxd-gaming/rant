package code;

import org.junit.Test;
import wxdgaming.spring.boot.starter.core.timer.MyClock;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-10-28 18:33
 **/
public class TimeTest {

    @Test
    public void t0() {
        System.out.println(MyClock.formatDate("MM/dd HH:mm", MyClock.millis()));
    }
}
