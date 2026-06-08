package coppercore.geometry;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import org.junit.jupiter.api.Test;

// This was written by Codex 5.5.
class EnhancedLine2dTest {
    private static final double DELTA = 1e-12;

    @Test
    void intersectsReturnsTrueWhenLineSegmentsCross() {
        EnhancedLine2d first =
                new EnhancedLine2d(new Translation2d(0.0, 0.0), new Translation2d(4.0, 4.0));
        EnhancedLine2d second =
                new EnhancedLine2d(new Translation2d(0.0, 4.0), new Translation2d(4.0, 0.0));

        assertTrue(first.intersects(second));
        assertTrue(second.intersects(first));
    }

    @Test
    void intersectsIncludesSharedEndpoints() {
        EnhancedLine2d first =
                new EnhancedLine2d(new Translation2d(0.0, 0.0), new Translation2d(2.0, 2.0));
        EnhancedLine2d second =
                new EnhancedLine2d(new Translation2d(2.0, 2.0), new Translation2d(4.0, 1.0));

        assertTrue(first.intersects(second));
        assertTrue(second.intersects(first));
    }

    @Test
    void intersectsReturnsFalseForParallelSegments() {
        EnhancedLine2d first =
                new EnhancedLine2d(new Translation2d(0.0, 0.0), new Translation2d(4.0, 0.0));
        EnhancedLine2d second =
                new EnhancedLine2d(new Translation2d(0.0, 1.0), new Translation2d(4.0, 1.0));

        assertFalse(first.intersects(second));
        assertFalse(second.intersects(first));
    }

    @Test
    void intersectsReturnsFalseWhenInfiniteLinesCrossOutsideSegments() {
        EnhancedLine2d first =
                new EnhancedLine2d(new Translation2d(0.0, 0.0), new Translation2d(1.0, 1.0));
        EnhancedLine2d second =
                new EnhancedLine2d(new Translation2d(2.0, 0.0), new Translation2d(2.0, 3.0));

        assertFalse(first.intersects(second));
        assertFalse(second.intersects(first));
    }

    @Test
    void midPointReturnsPointHalfwayBetweenStartAndEnd() {
        EnhancedLine2d line =
                new EnhancedLine2d(new Translation2d(-2.0, 3.0), new Translation2d(6.0, -1.0));

        assertTranslationEquals(new Translation2d(2.0, 1.0), line.midPoint());
    }

    @Test
    void getTrajectoryReturnsStartAndEnd() {
        Translation2d start = new Translation2d(1.0, 2.0);
        Translation2d end = new Translation2d(5.0, -3.0);
        EnhancedLine2d line = new EnhancedLine2d(start, end);

        assertArrayEquals(new Translation2d[] {start, end}, line.getTrajectory());
    }

    @Test
    void translation3dConstructorProjectsOntoXYPlane() {
        EnhancedLine2d line =
                new EnhancedLine2d(
                        new Translation3d(1.0, 2.0, 100.0), new Translation3d(5.0, 8.0, -100.0));

        assertAll(
                () -> assertTranslationEquals(new Translation2d(3.0, 5.0), line.midPoint()),
                () ->
                        assertArrayEquals(
                                new Translation2d[] {
                                    new Translation2d(1.0, 2.0), new Translation2d(5.0, 8.0),
                                },
                                line.getTrajectory()));
    }

    private static void assertTranslationEquals(Translation2d expected, Translation2d actual) {
        assertAll(
                () ->
                        org.junit.jupiter.api.Assertions.assertEquals(
                                expected.getX(), actual.getX(), DELTA),
                () ->
                        org.junit.jupiter.api.Assertions.assertEquals(
                                expected.getY(), actual.getY(), DELTA));
    }
}
