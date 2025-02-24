package coppercore.math.test;

import coppercore.math.CachedData;
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

    @SuppressWarnings({"rawtypes"})
    @Test
    public void readTest() {
        CachedData data = new CachedData(10);
        data.write("real");
        Assertions.assertEquals(data.read(), "real");
        // data.write(null);
        // Assertions.assertEquals(data.read(), null);
        data.write("fake");
        Assertions.assertEquals(data.read(), "fake");
        data.write(100);
        Assertions.assertEquals(data.read(), 100);
        data.write(true);
        Assertions.assertEquals(data.read(), true);
    }
}
