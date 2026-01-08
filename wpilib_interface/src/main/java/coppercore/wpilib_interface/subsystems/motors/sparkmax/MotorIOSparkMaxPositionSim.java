package coppercore.wpilib_interface.subsystems.motors.sparkmax;

import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Volts;

import com.revrobotics.sim.SparkMaxSim;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig;
import coppercore.wpilib_interface.subsystems.motors.MotorInputs;
import coppercore.wpilib_interface.subsystems.sim.PositionSimAdapter;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The MotorIOSparkMaxPositionSim class extends the MotorIOSparkMax class to provide simulation
 * capabilities for position-based mechanisms by using SparkMaxSim objects.
 *
 * @deprecated This class has not yet been fully implemented or tested, and does NOT function as
 *     intended. It has been left in coppercore to provide the opportunity for it to be fixed and
 *     iterated upon in the future without having to start from a blank slate.
 */
@Deprecated
public class MotorIOSparkMaxPositionSim extends MotorIOSparkMax {
    /** Sim adapter to read physics sim values from and update with simulated motor output */
    private final PositionSimAdapter physicsSimAdapter;

    /** SparkMaxSim handle to interface with the simulated motor */
    private final SparkMaxSim sparkSim;

    /** Timer to fetch timestamps to calculate delta time in sim */
    private final Timer deltaTimer = new Timer();

    /** Variable to keep track of the last timestamp, in seconds */
    private double lastTimestampSeconds;

    /** True if this motor is a follower, false if it is the leader */
    private final boolean isFollower;

    /**
     * Track whether or not to invert the sim rotation based on an being inverted follower in the
     * config.
     */
    private final boolean invertSimRotation;

    /**
     * The number of motors, calculated in the constructor. This value equals 1 plus the length of
     * the followerMotorConfigs field of the MechanismConfig passed to this simulated IO.
     */
    private final int numMotors;

    /**
     * A map to allow follower motors to access the same SparkMaxSim objects as their leaders. This
     * map uses the mechanism name as the key and the SparkMaxSim object as the value.
     */
    private static final Map<String, SparkMaxSim> sparkMaxSimMap = new HashMap<>();

    /**
     * Create a new simulated SparkMax IO for a lead motor, initializing a SparkMaxSim object.
     *
     * @param config A MechanismConfig to use for CAN IDs and ratios
     * @param sparkMaxConfig A SparkMaxConfig to apply to the simulated SparkMax
     * @param motorType The motor type, either brushless or brushed
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for simulation values
     * @param motorFactory A method to create the gearbox passed to the SparkMaxSim, given a number
     *     of motors. For example {@link edu.wpi.first.math.system.plant.DCMotor#getNEO(int)}
     */
    public MotorIOSparkMaxPositionSim(
            MechanismConfig config,
            SparkMaxConfig sparkMaxConfig,
            MotorType motorType,
            PositionSimAdapter physicsSimAdapter,
            Function<Integer, DCMotor> motorFactory) {
        super(config, sparkMaxConfig, motorType);

        this.isFollower = false;

        this.invertSimRotation = false;

        this.numMotors = 1 + config.followerMotorConfigs.length;
        sparkSim = new SparkMaxSim(sparkMax, motorFactory.apply(this.numMotors));
        sparkMaxSimMap.put(config.name, sparkSim);

        this.physicsSimAdapter = physicsSimAdapter;

        this.deltaTimer.start();
        this.lastTimestampSeconds = deltaTimer.get();
    }

    /**
     * Create a new simulated SparkMax IO for a lead motor, initializing a SparkMaxSim object.
     *
     * @param config A MechanismConfig to use for CAN IDs and ratios
     * @param sparkMaxConfig A SparkMaxConfig to apply to the simulated SparkMax
     * @param motorType The motor type, either brushless or brushed
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for simulation values
     * @param motorFactory A method to create the gearbox passed to the SparkMaxSim, given a number
     *     of motors. For example {@link edu.wpi.first.math.system.plant.DCMotor#getNEO(int)}
     * @return a new MotorIOSparkMaxPositionSim configured as the lead motor
     */
    public static MotorIOSparkMaxPositionSim newLeader(
            MechanismConfig config,
            SparkMaxConfig sparkMaxConfig,
            MotorType motorType,
            PositionSimAdapter physicsSimAdapter,
            Function<Integer, DCMotor> motorFactory) {
        return new MotorIOSparkMaxPositionSim(
                config, sparkMaxConfig, motorType, physicsSimAdapter, motorFactory);
    }

    /**
     * Create a new simulated SparkMax IO for a follower motor, initializing a SparkMaxSim object.
     *
     * <p>Follower motors must be instantiated after the leader, as they depend on the leader for
     * some sim initialization tasks.
     *
     * @param config A MechanismConfig to use for CAN IDs and ratios
     * @param followerIndex An int specifying the index of the follower motor (what index in
     *     config.followerConfigs)
     * @param sparkMaxConfig A SparkMaxConfig to apply to the simulated SparkMax
     * @param motorType The motor type, either brushless or brushed
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for simulation values
     * @param motorFactory A method to create the gearbox passed to the SparkMaxSim, given a number
     *     of motors. For example {@link edu.wpi.first.math.system.plant.DCMotor#getNEO(int)}
     */
    public MotorIOSparkMaxPositionSim(
            MechanismConfig config,
            int followerIndex,
            SparkMaxConfig sparkMaxConfig,
            MotorType motorType,
            PositionSimAdapter physicsSimAdapter,
            Function<Integer, DCMotor> motorFactory) {
        super(config, followerIndex, sparkMaxConfig, motorType);

        this.isFollower = true;
        this.invertSimRotation = config.followerMotorConfigs[followerIndex].invert();

        this.numMotors = 1 + config.followerMotorConfigs.length;
        sparkSim = sparkMaxSimMap.get(config.name);

        this.physicsSimAdapter = physicsSimAdapter;

        this.deltaTimer.start();
        this.lastTimestampSeconds = deltaTimer.get();
    }

    /**
     * Create a new simulated SparkMax IO for a follower motor, initializing a SparkMaxSim object.
     *
     * <p>Follower motors must be instantiated after the leader, as they depend on the leader for
     * some sim initialization tasks.
     *
     * @param config A MechanismConfig to use for CAN IDs and ratios
     * @param followerIndex An int specifying the index of the follower motor (what index in
     *     config.followerConfigs)
     * @param sparkMaxConfig A SparkMaxConfig to apply to the simulated SparkMax
     * @param motorType The motor type, either brushless or brushed
     * @param physicsSimAdapter An ElevatorSimAdapter or ArmSimAdapter to use for simulation values
     * @param motorFactory A method to create the gearbox passed to the SparkMaxSim, given a number
     *     of motors. For example {@link edu.wpi.first.math.system.plant.DCMotor#getNEO(int)}
     * @return a new MotorIOSparkMaxPositionSim configured as the follower motor
     */
    public static MotorIOSparkMaxPositionSim newFollower(
            MechanismConfig config,
            int followerIndex,
            SparkMaxConfig sparkMaxConfig,
            MotorType motorType,
            PositionSimAdapter physicsSimAdapter,
            Function<Integer, DCMotor> motorFactory) {
        return new MotorIOSparkMaxPositionSim(
                config, followerIndex, sparkMaxConfig, motorType, physicsSimAdapter, motorFactory);
    }

    @Override
    public void updateInputs(MotorInputs inputs) {
        updateSimState();

        super.updateInputs(inputs);
    }

    /**
     * Update the state of the physics sim, by:
     *
     * <ul>
     *   <li>Calculating delta time
     *   <li>If this IO is the leader motor, updating the physics sim adapter with the correct
     *       applied output based on the SparkMaxSim.
     *   <li>Updating the SparkMaxSim's position, velocity, and current draw based on the sim
     *       adapter
     * </ul>
     */
    protected void updateSimState() {
        // TODO: Write a system to simulate current draw from multiple subsystems across a project.
        if (isFollower) {
            return;
        }

        sparkSim.setBusVoltage(RobotController.getBatteryVoltage());

        double timestamp = deltaTimer.get();
        double deltaTimeSeconds = timestamp - this.lastTimestampSeconds;
        this.lastTimestampSeconds = timestamp;

        physicsSimAdapter.update(
                Volts.of(sparkMax.getAppliedOutput() * RobotController.getBatteryVoltage()),
                deltaTimeSeconds);

        sparkSim.iterate(
                physicsSimAdapter.getMotorAngularVelocity().in(RPM),
                RobotController.getBatteryVoltage(),
                deltaTimeSeconds);
    }
}
