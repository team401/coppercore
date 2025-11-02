package coppercore.wpilib_interface.subsystems.motors;

import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.wpilibj.Alert;

/**
 * A base motor IO that implements closed-loop control for a TalonFX-supporting motor using
 * MotionMagicExpo, MotionMagicVelocity, and TorqueCurrentFOC wherever possible.
 */
public class MotorIOTalonFX implements MotorIO {
    private TalonFX talon;

    private final Alert configFailedToApplyAlert;

    public MotorIOTalonFX() {}
}
