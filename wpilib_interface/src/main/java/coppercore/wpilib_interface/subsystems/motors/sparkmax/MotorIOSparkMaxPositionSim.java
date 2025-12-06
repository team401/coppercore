package coppercore.wpilib_interface.subsystems.motors.sparkmax;

import java.util.function.Function;

import com.revrobotics.sim.SparkMaxSim;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;

import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import coppercore.wpilib_interface.subsystems.sim.PositionSimAdapter;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.Timer;

/**
 * The MotorIOSparkMaxPositionSim class extends the MotorIOSparkMax class to provide simulation capabilities for position-based mechanisms by using SparkMaxSim objects.
 */
public class MotorIOSparkMaxPositionSim extends MotorIOSparkMax {
    private final PositionSimAdapter physicsSimAdapter;

    private final SparkMaxSim sparkSim;

    private final Timer deltaTimer = new Timer();
    private double lastTimestamp;

    private final boolean isFollower;

    /**
     * Track whether or not to invert the sim rotation based on an being inverted follower in the
     * config.
     */
    private final boolean invertSimRotation;

    /**
     * Create a new simulated SparkMax IO for a lead motor, initializing a SparkMaxSim object.
     * 
     * @param config A MechanismConfig to use for CAN IDs and ratios
     * @param sparkMaxConfig A SparkMaxConfig to apply to the simulated SparkMax
     * @param motorType The motor type, either brushless or brushed
     * @param physicsSimAdapter An ElevatorSimAdapter or SingleJointedArmSimAdapter to use for simulation values
     * @param motorFactory A method to create the gearbox passed to the SparkMaxSim, given a number of motors. For example {@link edu.wpi.first.math.system.plant.DCMotor#getNEO(int)}
     */
    public MotorIOSparkMaxPositionSim(
        MechanismConfig config,
        SparkMaxConfig sparkMaxConfig,
        MotorType motorType,
        PositionSimAdapter physicsSimAdapter,
        Function<Integer, DCMotor> motorFactory
    ) {
        super(config, sparkMaxConfig, motorType);

        this.isFollower = false;

        this.invertSimRotation = false;

        int numMotors = 1 + config.followerMotorConfigs.length;
        sparkSim = new SparkMaxSim(sparkMax, motorFactory.apply(numMotors));

        this.physicsSimAdapter = physicsSimAdapter;

        deltaTimer.start();
        this.lastTimestamp = deltaTimer.get();
    }

    /**
     * Create a new simulated SparkMax IO for a lead motor, initializing a SparkMaxSim object.
     *
     * @param config A MechanismConfig to use for CAN IDs and ratios
     * @param sparkMaxConfig A SparkMaxConfig to apply to the simulated SparkMax
     * @param motorType The motor type, either brushless or brushed
     * @param physicsSimAdapter An ElevatorSimAdapter or SingleJointedArmSimAdapter to use for simulation values
     * @param motorFactory A method to create the gearbox passed to the SparkMaxSim, given a number of motors. For example {@link edu.wpi.first.math.system.plant.DCMotor#getNEO(int)}
     * @return a new MotorIOSparkMaxPositionSim configured as the lead motor
     */
    public static MotorIOSparkMaxPositionSim newLeader(
        MechanismConfig config,
        SparkMaxConfig sparkMaxConfig,
        MotorType motorType,
        PositionSimAdapter physicsSimAdapter,
        Function<Integer, DCMotor> motorFactory
    ) {
        return new MotorIOSparkMaxPositionSim(config, sparkMaxConfig, motorType, physicsSimAdapter, motorFactory);
    }
}

