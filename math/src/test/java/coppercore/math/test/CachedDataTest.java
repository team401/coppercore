package coppercore.math.test;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Millimeters;

import coppercore.math.CachedData;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CachedDataTest {
    @Test
    public void isStaleTest() {
        CachedData<Integer> data = new CachedData<>(1.0);
        try {
            for (int i = 0; i<7; i++){
                Thread.sleep(100);
                Assertions.assertFalse(data.isStale());
            }
            Thread.sleep(500); // Room for error in Thread.sleep
            Assertions.assertTrue(data.isStale());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void isStaleTest2() {
        CachedData<Integer> data = new CachedData<>(5);
        for (int i = 0; i<5; i++){
            Assertions.assertFalse(data.isStale());
            data.read();
        }
        date.read();
        Assertions.assertTrue(data.isStale());
        
        // Second Test
        data = new CachedData<>(20);
        // 20 reads
        for (int i = 0; i<20; i++){
            Assertions.assertFalse(data.isStale());
            data.read();
        }
        data.read();
        Assertions.assertTrue(data.isStale());
        //Ensure stale remains true
        for (int i = 0; i<10; i++){
            data.read();
            Assertions.assertTrue(data.isStale());
        }
    }

    @Test
    public void isStaleTest3() {
        CachedData<Integer> data = new CachedData<>(0.5);
        try {
            for (int i = 0; i<5; i++){
                Thread.sleep(50);
                Assertions.assertFalse(data.isStale());
            }
            Thread.sleep(500); // Room for error in Thread.sleep
            Assertions.assertTrue(data.isStale());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    public void readTest() {
        CachedData data = new CachedData(10);
        data.write("real");
        Assertions.assertEquals(data.read(), "real");
        data.write("fake");
        Assertions.assertEquals(data.read(), "fake");
        data.write(100);
        Assertions.assertEquals(data.read(), 100);
        data.write(true);
        Assertions.assertEquals(data.read(), true);
        data.write(false);
        Assertions.assertEquals(data.read(), false);
        data.write(Millimeters.of(5));
        Assertions.assertEquals(data.read(), Millimeters.of(5));
        data.write(Inches.of(5));
        Assertions.assertEquals(data.read(), Inches.of(5));
        data.write(List.of(1, 0, 0, 0, 1, -1));
        Assertions.assertEquals(data.read(), List.of(1, 0, 0, 0, 1, -1));
        data.write(-100);
        Assertions.assertEquals(data.read(), -100);
        data.write(0);
        Assertions.assertEquals(data.read(), 0);
        data.write(100);
        Assertions.assertEquals(data.read(), 100);
        Assertions.assertEquals(data.read(), 100);
        Assertions.assertEquals(data.read(), 100);
        Assertions.assertEquals(data.read(), 100);
        Assertions.assertEquals(data.read(), 100);
        Assertions.assertEquals(data.read(), 100);
        Assertions.assertEquals(data.read(), 100);
        Assertions.assertEquals(data.read(), 100);
        Assertions.assertEquals(data.read(), 100);
        Assertions.assertEquals(data.read(), 100);
        Assertions.assertEquals(data.read(), null);
    }

    @Test
    public void readTest2() {
        CachedData data = new CachedData(1, 0.0);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(data.read(), null);
    }
}
