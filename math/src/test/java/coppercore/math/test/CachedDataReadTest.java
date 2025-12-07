package coppercore.math.test;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Millimeters;

import coppercore.math.CachedDataRead;
import edu.wpi.first.units.measure.Distance;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CachedDataReadTest {
    @BeforeEach
    @SuppressWarnings("unused")
    @Test
    public void isStaleTest1() {
        CachedDataRead<Integer> data = new CachedDataRead<>(5);
        for (int i = 0; i < 5; i++) {
            Assertions.assertFalse(data.isStale());
            data.read();
        }
        data.read();
        Assertions.assertTrue(data.isStale());

        // Second Test
        data = new CachedDataRead<>(20);
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

    @SuppressWarnings({"rawtypes"})
    @Test
    public void readTestTrue() {
        CachedDataRead<String> stringData = new CachedDataRead<>(10);
        stringData.write("real");
        Assertions.assertEquals(stringData.read(), "real");
        stringData.write("fake");
        Assertions.assertEquals(stringData.read(), "fake");
        CachedDataRead<Integer> integerData = new CachedDataRead<>(10);
        integerData.write(100);
        Assertions.assertEquals(integerData.read(), 100);
        CachedDataRead<Boolean> booleanData = new CachedDataRead<>(10);
        booleanData.write(true);
        Assertions.assertEquals(booleanData.read(), true);
        booleanData.write(false);
        Assertions.assertEquals(booleanData.read(), false);
        CachedDataRead<Distance> distanceData = new CachedDataRead<>(10);
        distanceData.write(Millimeters.of(5));
        Assertions.assertEquals(distanceData.read(), Millimeters.of(5));
        distanceData.write(Inches.of(5));
        Assertions.assertEquals(distanceData.read(), Inches.of(5));
        CachedDataRead<List> listData = new CachedDataRead<>(10);
        listData.write(List.of(1, 0, 0, 0, 1, -1));
        Assertions.assertEquals(listData.read(), List.of(1, 0, 0, 0, 1, -1));
        integerData.write(-100);
        Assertions.assertEquals(integerData.read(), -100);
        integerData.write(0);
        Assertions.assertEquals(integerData.read(), 0);
    }

    @Test
    public void readTestNull() {
        CachedDataRead<Integer> integerData = new CachedDataRead<>(10);
        integerData.write(100);
        for (int i = 0; i < 10; i++) {
            Assertions.assertEquals(integerData.read(), 100);
        }
        Assertions.assertEquals(integerData.read(), null);
    }
}
