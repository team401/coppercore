package coppercore.geometry;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.math.geometry.Translation2d;
import org.junit.jupiter.api.Test;

// This was written by Codex 5.5.
class RectangleTest {
    private static final double DELTA = 1e-12;

    @Test
    void containsIncludesInteriorAndEdges() {
        Rectangle rectangle = new Rectangle(new Translation2d(1.0, 2.0), 3.0, 4.0);

        assertAll(
                () -> assertTrue(rectangle.contains(new Translation2d(2.0, 3.0))),
                () -> assertTrue(rectangle.contains(new Translation2d(1.0, 2.0))),
                () -> assertTrue(rectangle.contains(new Translation2d(4.0, 6.0))),
                () -> assertTrue(rectangle.contains(new Translation2d(1.0, 4.0))),
                () -> assertTrue(rectangle.contains(new Translation2d(3.0, 6.0))));
    }

    @Test
    void containsRejectsPointsOutsideBounds() {
        Rectangle rectangle = new Rectangle(new Translation2d(1.0, 2.0), 3.0, 4.0);

        assertAll(
                () -> assertFalse(rectangle.contains(new Translation2d(0.999, 3.0))),
                () -> assertFalse(rectangle.contains(new Translation2d(4.001, 3.0))),
                () -> assertFalse(rectangle.contains(new Translation2d(2.0, 1.999))),
                () -> assertFalse(rectangle.contains(new Translation2d(2.0, 6.001))));
    }

    @Test
    void fromCenterBuildsRectangleAroundCenter() {
        Rectangle rectangle = Rectangle.fromCenter(new Translation2d(5.0, 7.0), 4.0, 6.0);

        assertAll(
                () -> assertTrue(rectangle.contains(new Translation2d(3.0, 4.0))),
                () -> assertTrue(rectangle.contains(new Translation2d(7.0, 10.0))),
                () -> assertTrue(rectangle.contains(new Translation2d(5.0, 7.0))),
                () -> assertFalse(rectangle.contains(new Translation2d(2.999, 7.0))),
                () -> assertFalse(rectangle.contains(new Translation2d(7.001, 7.0))));
    }

    @Test
    void getOutlineReturnsClosedClockwisePathFromBottomLeft() {
        Rectangle rectangle = new Rectangle(new Translation2d(-2.0, 1.0), 5.0, 3.0);

        Translation2d[] outline = rectangle.getOutline();

        assertArrayEquals(
                new Translation2d[] {
                    new Translation2d(-2.0, 1.0),
                    new Translation2d(3.0, 1.0),
                    new Translation2d(3.0, 4.0),
                    new Translation2d(-2.0, 4.0),
                    new Translation2d(-2.0, 1.0),
                },
                outline);
        assertTranslationEquals(outline[0], outline[outline.length - 1]);
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
