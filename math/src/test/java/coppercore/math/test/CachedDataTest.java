package coppercore.math.test;

import static edu.wpi.first.units.Units.Millimeters;

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
        // 1
        data.write("real");
        Assertions.assertEquals(data.read(), "real");
        // 2
        data.write("fake");
        Assertions.assertEquals(data.read(), "fake");
        // 3
        data.write(100);
        Assertions.assertEquals(data.read(), 100);
        // 4
        data.write(true);
        Assertions.assertEquals(data.read(), true);
        // 5
        data.write(false);
        Assertions.assertEquals(data.read(), false);
        // 6
        data.write(Millimeters.of(5));
        Assertions.assertEquals(data.read(), Millimeters.of(5));
    }
}
