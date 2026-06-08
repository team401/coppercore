package coppercore.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.math.geometry.Rotation2d;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// This file was written by Codex 5.5.
class AngleUtilTest {
    private static final double DELTA = 1e-12;

    @ParameterizedTest
    @MethodSource("headings")
    void normalizeHeadingReturnsCoterminalAngleInPositiveRange(
            double originalHeadingRadians, double expectedHeadingRadians) {
        double normalizedHeading = AngleUtil.normalizeHeading(originalHeadingRadians);

        assertEquals(expectedHeadingRadians, normalizedHeading, DELTA);
        assertTrue(normalizedHeading >= 0.0);
        assertTrue(normalizedHeading < AngleUtil.TAU);
    }

    @ParameterizedTest
    @MethodSource("rotationHeadings")
    void normalizeHeadingRotation2dReturnsCoterminalAngleInPositiveRange(
            Rotation2d originalHeading, double expectedHeadingRadians) {
        Rotation2d normalizedHeading = AngleUtil.normalizeHeading(originalHeading);

        assertEquals(expectedHeadingRadians, normalizedHeading.getRadians(), DELTA);
        assertTrue(normalizedHeading.getRadians() >= 0.0);
        assertTrue(normalizedHeading.getRadians() < AngleUtil.TAU);
    }

    private static Stream<Arguments> headings() {
        return Stream.of(
                Arguments.of(0.0, 0.0),
                Arguments.of(AngleUtil.TAU, 0.0),
                Arguments.of(-AngleUtil.TAU, 0.0),
                Arguments.of(Math.PI, Math.PI),
                Arguments.of(-Math.PI, Math.PI),
                Arguments.of(3.0 * Math.PI / 2.0, 3.0 * Math.PI / 2.0),
                Arguments.of(-Math.PI / 2.0, 3.0 * Math.PI / 2.0),
                Arguments.of(5.0 * AngleUtil.TAU + Math.PI / 4.0, Math.PI / 4.0),
                Arguments.of(-3.0 * AngleUtil.TAU + Math.PI / 6.0, Math.PI / 6.0));
    }

    private static Stream<Arguments> rotationHeadings() {
        return Stream.of(
                Arguments.of(new Rotation2d(-Math.PI / 2.0), 3.0 * Math.PI / 2.0),
                Arguments.of(new Rotation2d(AngleUtil.TAU), 0.0));
    }
}
