package coppercore.wpilib_interface.subsystems.motors;

import coppercore.wpilib_interface.subsystems.motors.profile.MotionProfileConfig;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Velocity;
import edu.wpi.first.units.measure.Voltage;

/**
 * The MotorIOReplay class implements MotorIO with dummy methods (methods that have no effect). This
 * provides an IO which can be used in replay without interfering with the inputs provided from
 * replay.
 *
 * <p>This class exists to avoid making all methods of MotorIO have a default (empty)
 * implementation, as is seen in other projects. This is because, providing a default implementation
 * of all methods makes it possible for an IO not to implement certain methods of the IO without
 * causing compiler errors, which can lead to bugs.
 */
public class MotorIOReplay implements MotorIO {
    public void updateInputs(MotorInputs inputs) {}

    public void controlToPositionUnprofiled(Angle positionSetpoint) {}

    public void controlToPositionProfiled(Angle positionSetpoint) {}

    public void controlToPositionProfiled(
            Angle positionSetpoint,
            AngularVelocity maxVelocity,
            AngularAcceleration maxAcceleration,
            Velocity<AngularAccelerationUnit> maxJerk,
            double expoKv,
            double expoKa) {}

    public void controlToPositionProfiled(
            Angle positionSetpoint, MotionProfileConfig profileConfig) {}

    public void controlToPositionExpoProfiled(Angle positionSetpoint) {}

    public void controlToVelocityUnprofiled(AngularVelocity velocitySetpoint) {}

    public void controlToVelocityProfiled(AngularVelocity velocitySetpoint) {}

    public void controlOpenLoopVoltage(Voltage voltage) {}

    public void controlOpenLoopCurrent(Current current) {}

    public void follow(int leaderId, boolean opposeLeaderDirection) {}

    public void setProfileConstraints(MotionProfileConfig profileConfig) {}

    public void setGains(
            double kP, double kI, double kD, double kS, double kG, double kV, double kA) {}

    public void setNeutralMode(NeutralMode neutralMode) {}

    public void setCurrentPosition(Angle position) {}
}
