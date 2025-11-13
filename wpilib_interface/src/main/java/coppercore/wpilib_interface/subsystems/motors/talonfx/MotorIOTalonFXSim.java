package coppercore.wpilib_interface.subsystems.motors.talonfx;

import java.util.Optional;

import com.ctre.phoenix6.configs.TalonFXConfiguration;

import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.wpilibj.simulation.LinearSystemSim;

public class MotorIOTalonFXSim extends MotorIOTalonFX {
    LinearSystemSim<N2, N1, N2> mechanismPhysicsSim;
   
    /**
     * Create a new Simulated TalonFX IO, initializing a TalonFX and all required StatusSignals
     *
     * @param config A MechanismConfig config to use for CAN IDs
     * @param followerIndex An Optional containing either the index of the follower motor (what
     *     position in config.followerIds this motor is) or None if this is the lead motor. If
     *     followerIndex is not None, this IO will automatically follow the lead motor at the end of
     *     its constructor.
     * @param talonFXConfig A TalonFXConfiguration to apply to the motor. This config will not be
     *     modified by this IO, so there's no need to copy it.
     */ 
    public MotorIOTalonFXSim(MechanismConfig config, Optional<Integer> followerIndex, TalonFXConfiguration talonFXConfig, LinearSystemSim<N2, N1, N2> sim) {
        super(config, followerIndex, talonFXConfig);
    }
}
