package coppercore.math.test;

import coppercore.math.CachedData;

import java.util.concurrent.ForkJoinPool.ManagedBlocker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CachedDataTest {
    @Test
    public void isStaleTest() {
        CachedData data = new CachedData(1, 0.0);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(data.isStale());
    }

    @Test
    public void isStaleTest2() {
        CachedData data = new CachedData(0);
        Assertions.assertTrue(data.isStale());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void readTest() {
        CachedData data= new CachedData(0);
        CachedData.write("ez");
        Assertions.assertEquals(data.read(), "Something");
    }
}
