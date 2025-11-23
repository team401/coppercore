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
    @Override
    public void updateInputs(MotorInputs inputs) {}

    @Override
    public void controlToPositionUnprofiled(Angle positionSetpoint) {}

    @Override
    public void controlToPositionProfiled(Angle positionSetpoint) {}

    @Override
    public void controlToPositionProfiled(
            Angle positionSetpoint,
            AngularVelocity maxVelocity,
            AngularAcceleration maxAcceleration,
            Velocity<AngularAccelerationUnit> maxJerk,
            double expoKv,
            double expoKa) {}

    @Override
    public void controlToPositionProfiled(
            Angle positionSetpoint, MotionProfileConfig profileConfig) {}

    @Override
    public void controlToPositionExpoProfiled(Angle positionSetpoint) {}

    @Override
    public void controlToVelocityUnprofiled(AngularVelocity velocitySetpoint) {}

    @Override
    public void controlToVelocityProfiled(AngularVelocity velocitySetpoint) {}

    @Override
    public void controlOpenLoopVoltage(Voltage voltage) {}

    @Override
    public void controlOpenLoopCurrent(Current current) {}

    @Override
    public void follow(int leaderId, boolean opposeLeaderDirection) {}

    @Override
    public void setProfileConstraints(MotionProfileConfig profileConfig) {}

    @Override
    public void setGains(
            double kP, double kI, double kD, double kS, double kG, double kV, double kA) {}

    @Override
    public void setNeutralMode(NeutralMode neutralMode) {}

    @Override
    public void setCurrentPosition(Angle position) {}
}
