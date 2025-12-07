package coppercore.math.test;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Microseconds;
import static edu.wpi.first.units.Units.Millimeters;
import static edu.wpi.first.units.Units.Seconds;

import coppercore.math.CachedDataTime;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.util.WPIUtilJNI;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CachedDataTimeTest {

    @BeforeEach
    @SuppressWarnings("unused")
    void setUpMockTime() {
        WPIUtilJNI.enableMockTime();
        setMockTimeSeconds(0.0);
    }

    public static void enableMockTime() {}

    public static void setMockTime(long time) {}

    /**
     * Set WPIUtilJni's mock time to timeSeconds
     *
     * <p>This exists as a wrapper because WPIUtilJNI.setMockTime expects the time as a long in
     * microseconds
     *
     * @param timeSeconds the time to set, in seconds
     */
    private void setMockTimeSeconds(double timeSeconds) {
        WPIUtilJNI.setMockTime((long) Seconds.of(timeSeconds).in(Microseconds));
    }

    public static void disableMockTime() {}

    @Test
    public void isStaleTest() {
        enableMockTime();
        CachedDataTime<Integer> data = new CachedDataTime<>(1.5);
        double staletimeTest = 0.1;
        for (int i = 0; i < 7; i++) {
            setMockTimeSeconds(staletimeTest + 0.1 * i);
            Assertions.assertFalse(data.isStale());
        }
        setMockTimeSeconds(1.5);
        Assertions.assertTrue(data.isStale());
        disableMockTime();
    }

    @Test
    public void isStaleTest2() {
        enableMockTime();
        CachedDataTime<Integer> data = new CachedDataTime<>(1.5);
        double staletimeTest = 0.05;
        for (int i = 0; i < 5; i++) {
            setMockTimeSeconds(staletimeTest + 0.05 * i);
            Assertions.assertFalse(data.isStale());
        }
        setMockTimeSeconds(1.5);
        Assertions.assertTrue(data.isStale());
        disableMockTime();
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    public void readTestTrue() {
        CachedDataTime<String> stringData = new CachedDataTime<>(10);
        stringData.write("real");
        Assertions.assertEquals(stringData.read(), "real");
        stringData.write("fake");
        Assertions.assertEquals(stringData.read(), "fake");
        CachedDataTime<Integer> integerData = new CachedDataTime<>(10);
        integerData.write(100);
        Assertions.assertEquals(integerData.read(), 100);
        CachedDataTime<Boolean> booleanData = new CachedDataTime<>(10);
        booleanData.write(true);
        Assertions.assertEquals(booleanData.read(), true);
        booleanData.write(false);
        Assertions.assertEquals(booleanData.read(), false);
        CachedDataTime<Distance> distanceData = new CachedDataTime<>(10);
        distanceData.write(Millimeters.of(5));
        Assertions.assertEquals(distanceData.read(), Millimeters.of(5));
        distanceData.write(Inches.of(5));
        Assertions.assertEquals(distanceData.read(), Inches.of(5));
        CachedDataTime<List> listData = new CachedDataTime<>(10);
        listData.write(List.of(1, 0, 0, 0, 1, -1));
        Assertions.assertEquals(listData.read(), List.of(1, 0, 0, 0, 1, -1));
        integerData.write(-100);
        Assertions.assertEquals(integerData.read(), -100);
        integerData.write(0);
        Assertions.assertEquals(integerData.read(), 0);
    }

    @Test
    public void readTestTrue2() {
        enableMockTime();
        CachedDataTime data = new CachedDataTime(3);
        data.write(-100);
        setMockTimeSeconds(2.0);
        Assertions.assertEquals(data.read(), -100);
        disableMockTime();
    }

    @Test
    public void readTestNull() {
        enableMockTime();
        CachedDataTime data = new CachedDataTime(1);
        setMockTimeSeconds(1.0);
        Assertions.assertEquals(data.read(), null);
        disableMockTime();
    }
}
