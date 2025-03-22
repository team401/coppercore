package coppercore.math.test;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Millimeters;

import coppercore.math.CachedData;
import edu.wpi.first.units.measure.Distance;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CachedDataTest {
    @Test
    public void isStaleTest() {
        CachedData<Integer> data = new CachedData<>(1.5);
        for (int i = 0; i < 7; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Assertions.assertFalse(data.isStale());
        }
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // Room for error in Thread.sleep
        Assertions.assertTrue(data.isStale());
    }

    @Test
    public void isStaleTest2() {
        CachedData<Integer> data = new CachedData<>(5);
        for (int i = 0; i < 5; i++) {
            Assertions.assertFalse(data.isStale());
            data.read();
        }
        data.read();
        Assertions.assertTrue(data.isStale());

        // Second Test
        data = new CachedData<>(20);
        // 20 reads
        for (int i = 0; i < 20; i++) {
            Assertions.assertFalse(data.isStale());
            data.read();
        }
        data.read();
        Assertions.assertTrue(data.isStale());
        // Ensure stale remains true
        for (int i = 0; i < 10; i++) {
            data.read();
            Assertions.assertTrue(data.isStale());
        }
    }

    @Test
    public void isStaleTest3() {
        CachedData<Integer> data = new CachedData<>(1.5);
        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Assertions.assertFalse(data.isStale());
        }
        try {
            Thread.sleep(1600);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // Room for error in Thread.sleep
        Assertions.assertTrue(data.isStale());
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    public void readTestTrue() {
        CachedData<String> stringData = new CachedData<>(10);
        stringData.write("real");
        Assertions.assertEquals(stringData.read(), "real");
        stringData.write("fake");
        Assertions.assertEquals(stringData.read(), "fake");
        CachedData<Integer> integerData = new CachedData<>(10);
        integerData.write(100);
        Assertions.assertEquals(integerData.read(), 100);
        CachedData<Boolean> booleanData = new CachedData<>(10);
        booleanData.write(true);
        Assertions.assertEquals(booleanData.read(), true);
        booleanData.write(false);
        Assertions.assertEquals(booleanData.read(), false);
        CachedData<Distance> distanceData = new CachedData<>(10);
        distanceData.write(Millimeters.of(5));
        Assertions.assertEquals(distanceData.read(), Millimeters.of(5));
        distanceData.write(Inches.of(5));
        Assertions.assertEquals(distanceData.read(), Inches.of(5));
        CachedData<List> listData = new CachedData<>(10);
        listData.write(List.of(1, 0, 0, 0, 1, -1));
        Assertions.assertEquals(listData.read(), List.of(1, 0, 0, 0, 1, -1));
        integerData.write(-100);
        Assertions.assertEquals(integerData.read(), -100);
        integerData.write(0);
        Assertions.assertEquals(integerData.read(), 0);
    }

    @Test
    public void readTestTrue2() {
        CachedData data = new CachedData(3);
        CachedData.write(-100);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(data.read(), -100);
    }
    @Test
    public void readTestNull() {
        CachedData data = new CachedData(1);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(data.read(), null);
    }
    @Test
    public void readTestNull2() {
        CachedData<String> stringData = new CachedData<>(10);
        integerData.write(100);
        for (int i = 0; i < 10; i++) {
            Assertions.assertEquals(integerData.read(), 100);
        }
        Assertions.assertEquals(integerData.read(), null);
    }
}
