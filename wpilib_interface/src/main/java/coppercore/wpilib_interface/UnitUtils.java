package coppercore.wpilib_interface;

import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.RotationsPerSecondPerSecond;
import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.AngularVelocityUnit;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.PerUnit;
import edu.wpi.first.units.Unit;
import edu.wpi.first.units.VoltageUnit;

/** Utilities for dealing with the WPILib units library. */
public final class UnitUtils {
    /**
     * Given a measurement, a minimum value, and a maximum value, return a clamped measurement
     * within those bounds.
     *
     * <p>Note: If for some reason min is greater than max, measure &lt; min takes precedence over
     * measure &lt; max, which takes precedence over min &lt; measure &lt; max. Therefore, when
     * debugging, if this function is strangely always returning min it is highly likely there is an
     * error in the generation of bounds.
     *
     * @param <M> A measure, the type of the measures being compoared. This should be able to be
     *     inferred automatically.
     * @param <U> The Unit measured by M. This should be able to be inferred automatically.
     * @param measure The measure being clamped. If it is greater than min and less than max, it
     *     will be the result.
     * @param min The minimum value. If measure &lt; min, min will be returned
     * @param max The maximum value. If measure &gt; max, max will be returned.
     * @return If measure is within the bounds, measure. If measure is less than min, min. If
     *     measure is greater than max, max.
     */
    public static final <M extends Measure<U>, U extends Unit> M clampMeasure(
            M measure, M min, M max) {
        if (measure.lt(min)) {
            return min;
        } else if (measure.gt(max)) {
            return max;
        }

        return measure;
    }

    /**
     * A unit for measuring angular mechanisms' feedforward voltages based on a model of the system
     * and a desired commanded angular velocity.
     *
     * <p>Defined as Volts / (Rotations / Second)
     */
    public static final PerUnit<VoltageUnit, AngularVelocityUnit> VoltsPerRotationPerSecond =
            Volts.per(RotationsPerSecond);

    /**
     * A unit for measuring angular mechanisms' feedforward voltages based on a model of the system
     * and a desired commanded angular acceleration.
     *
     * <p>Defined as Volts / ((Rotations / Second) / Second)
     */
    public static final PerUnit<VoltageUnit, AngularAccelerationUnit>
            VoltsPerRotationPerSecondSquared = Volts.per(RotationsPerSecondPerSecond);
}
