package coppercore.wpilib_interface.subsystems.motors.sparkmax;

import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.RadiansPerSecond;
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
import java.util.function.Function;

/**
 * The MotorIOSparkMaxPositionSim class extends the MotorIOSparkMax class to provide simulation
 * capabilities for position-based mechanisms by using SparkMaxSim objects.
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

        int numMotors = 1 + config.followerMotorConfigs.length;
        sparkSim = new SparkMaxSim(sparkMax, motorFactory.apply(numMotors));

        this.physicsSimAdapter = physicsSimAdapter;

        deltaTimer.start();
        this.lastTimestamp = deltaTimer.get();
    }

    /**
     * Create a new simulated SparkMax IO for a follower motor, initializing a SparkMaxSim object.
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
        return newFollower(
                config, followerIndex, sparkMaxConfig, motorType, physicsSimAdapter, motorFactory);
    }

    @Override
    public void updateInputs(MotorInputs inputs) {
        updateSimState();

        super.updateInputs(inputs);
    }

    protected void updateSimState() {
        sparkSim.setBusVoltage(RobotController.getBatteryVoltage());

        if (!isFollower) {
            double timestamp = deltaTimer.get();

            double deltaTimeSeconds = timestamp - this.lastTimestamp;
            this.lastTimestamp = timestamp;

            physicsSimAdapter.update(
                    Volts.of(sparkSim.getAppliedOutput() * sparkSim.getBusVoltage()),
                    deltaTimeSeconds);
        }

        double invertMultiplier = invertSimRotation ? -1.0 : 1.0;

        sparkSim.setPosition(
                physicsSimAdapter.getMotorPosition().times(invertMultiplier).in(Radians));
        sparkSim.setVelocity(
                physicsSimAdapter
                        .getMotorAngularVelocity()
                        .times(invertMultiplier)
                        .in(RadiansPerSecond));
    }
}
