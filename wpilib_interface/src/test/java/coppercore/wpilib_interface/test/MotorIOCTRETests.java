package coppercore.wpilib_interface.test;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Kilograms;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.Pounds;
import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.Rotations;

import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.FeedbackConfigs;
import com.ctre.phoenix6.configs.MagnetSensorConfigs;
import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.GravityTypeValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.signals.SensorDirectionValue;
import com.ctre.phoenix6.unmanaged.Unmanaged;
import coppercore.wpilib_interface.subsystems.configs.CANDeviceID;
import coppercore.wpilib_interface.subsystems.configs.ElevatorMechanismConfig;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig.GravityFeedforwardType;
import coppercore.wpilib_interface.subsystems.encoders.EncoderIOCANCoderPositionSim;
import coppercore.wpilib_interface.subsystems.encoders.EncoderInputs;
import coppercore.wpilib_interface.subsystems.motors.MotorInputs;
import coppercore.wpilib_interface.subsystems.motors.talonfx.MotorIOTalonFXPositionSim;
import coppercore.wpilib_interface.subsystems.sim.DummySimAdapter;
import coppercore.wpilib_interface.subsystems.sim.ElevatorSimAdapter;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.PerUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.ElevatorSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The MotorIOCTRETests class contains tests for TalonFX motor IOs and CANCoder encoderIOs using
 * simulation.
 *
 * <p>Most of the constants and gains found here are from
 * https://github.com/team401/2025-Robot-Code.
 */
public class MotorIOCTRETests {
    private static String canbus = "canivore";

    public static CANDeviceID encoderId = new CANDeviceID(canbus, 52);

    private static double ELEVATOR_MOTOR_REDUCTION = 5.0;

    private static ElevatorMechanismConfig elevatorMechanismConfig =
            ElevatorMechanismConfig.builder()
                    .withName("coppervator")
                    .withEncoderToMechanismRatio(1.0)
                    .withMotorToEncoderRatio(ELEVATOR_MOTOR_REDUCTION)
                    .withLeadMotorId(new CANDeviceID(canbus, 50))
                    .withGravityFeedforwardType(GravityFeedforwardType.STATIC_ELEVATOR)
                    .withElevatorToMechanismRatio(Inches.of(4.724).div(Rotations.of(1)))
                    .addFollower(new CANDeviceID(canbus, 51), true)
                    .build();

    private static Supplier<TalonFXConfiguration> generateElevatorTalonFXConfig =
            () ->
                    new TalonFXConfiguration()
                            .withFeedback(
                                    new FeedbackConfigs()
                                            .withFeedbackSensorSource(
                                                    FeedbackSensorSourceValue.RemoteCANcoder)
                                            .withFeedbackRemoteSensorID(encoderId.id())
                                            .withSensorToMechanismRatio(1.0)
                                            .withRotorToSensorRatio(ELEVATOR_MOTOR_REDUCTION))
                            .withMotorOutput(
                                    new MotorOutputConfigs()
                                            .withNeutralMode(NeutralModeValue.Coast))
                            .withCurrentLimits(
                                    new CurrentLimitsConfigs()
                                            .withStatorCurrentLimitEnable(true)
                                            .withStatorCurrentLimit(Amps.of(80.0)))
                            .withSlot0(
                                    new Slot0Configs()
                                            .withGravityType(GravityTypeValue.Elevator_Static)
                                            .withKS(0.0)
                                            .withKV(0.0)
                                            .withKA(0.0)
                                            .withKG(0.0)
                                            .withKP(1.0)
                                            .withKI(0.0)
                                            .withKD(0.0))
                            .withMotionMagic(
                                    new MotionMagicConfigs()
                                            .withMotionMagicCruiseVelocity(
                                                    // This very convoluted conversion turns 3.0 m/s
                                                    // goal velocity into a value in radians per
                                                    // second
                                                    // by converting meters to radians using the
                                                    // 4.724
                                                    // inches of height per spool rotation based on
                                                    // the
                                                    // drum radius.
                                                    RadiansPerSecond.of(
                                                            3.0 // Meters per second
                                                                    / Inches.of(4.724)
                                                                            .div(Rotations.of(1))
                                                                            .in(
                                                                                    PerUnit.combine(
                                                                                            Meters,
                                                                                            Radians))))
                                            .withMotionMagicExpo_kA(1)
                                            .withMotionMagicExpo_kV(0.0));

    @BeforeEach
    void initializeSimFeatures() {
        HAL.initialize(500, 2);
        SimHooks.setHALRuntimeType(2);
        SimHooks.setProgramStarted();
    }

    /**
     * Emulate the "periodic" loop of the robot by calling `loop` and incrementing the WPIUtilJNI
     * mock time by 20ms until `timeSeconds` seconds have elapsed.
     *
     * @param timeSeconds The duration to simulate. If this is not a multiple of 20 milliseconds,
     *     the method will round up.
     * @param loop A Runnable to call before each increment of time and once at the very end.
     */
    void loopForTime(double timeSeconds, Runnable loop) {
        double timeElapsed = 0.0;
        SimHooks.pauseTiming();
        while (timeElapsed < timeSeconds || (timeSeconds == 0.0 && timeElapsed == 0.0)) {
            SimHooks.stepTiming(0.02);
            if (DriverStation.isEnabled()) {
                Unmanaged.feedEnable(100);
            }
            loop.run();

            Timer.delay(0.1); // Allow phoenix sim thread to run

            timeElapsed += 0.02;
        }
        SimHooks.resumeTiming();
    }

    /** Tests a TalonFX elevator mechanism with a CANcoder and an elevator motor. */
    @Test
    public void talonFXElevatorWithFollower() {
        // Define configs
        ElevatorMechanismConfig mechanismConfig = elevatorMechanismConfig;
        TalonFXConfiguration talonFXConfigs = generateElevatorTalonFXConfig.get();

        var cancoderConfig =
                new CANcoderConfiguration()
                        .withMagnetSensor(
                                new MagnetSensorConfigs()
                                        .withSensorDirection(
                                                SensorDirectionValue.Clockwise_Positive));

        var elevatorSim =
                new ElevatorSim(
                        DCMotor.getKrakenX60Foc(2),
                        5.0,
                        Pounds.of(20.0).in(Kilograms),
                        Inches.of(0.7515).in(Meters),
                        0.0,
                        1.9,
                        false,
                        1.0,
                        0.0,
                        0.0);

        var simAdapter = new DummySimAdapter(new ElevatorSimAdapter(mechanismConfig, elevatorSim));
        // previous commits when this was an actual physics sim.

        var leadMotor =
                MotorIOTalonFXPositionSim.newLeader(mechanismConfig, talonFXConfigs, simAdapter);
        leadMotor.enableUnitTestMode();
        var followerMotor =
                MotorIOTalonFXPositionSim.newFollower(
                        mechanismConfig, 0, talonFXConfigs, simAdapter);
        followerMotor.enableUnitTestMode();
        var cancoder = new EncoderIOCANCoderPositionSim(encoderId, cancoderConfig, simAdapter);

        var leadMotorInputs = new MotorInputs();
        var followerMotorInputs = new MotorInputs();
        var cancoderInputs = new EncoderInputs();

        Runnable loop =
                () -> {
                    leadMotor.updateInputs(leadMotorInputs);
                    followerMotor.updateInputs(followerMotorInputs);
                    cancoder.updateInputs(cancoderInputs);
                };

        // Begin actually testing by setting a position
        simAdapter.setState(Radians.of(5.0), RadiansPerSecond.zero());

        // Wait for library initialization to take place. As soon as a real value is
        // read, it will be ~1, not 0.0
        while (cancoderInputs.positionRadians == 0.0
                || leadMotorInputs.positionRadians == 0.0
                || followerMotorInputs.positionRadians == 0.0) {
            leadMotor.controlNeutral();
            loopForTime(0.02, loop);
        }

        Assertions.assertEquals(
                leadMotorInputs.positionRadians,
                followerMotorInputs.positionRadians,
                1e-2,
                "Lead motor and follower mechanism position should match at all times.");

        Assertions.assertEquals(
                leadMotorInputs.positionRadians,
                leadMotorInputs.rawRotorPositionRadians / ELEVATOR_MOTOR_REDUCTION,
                1e-2,
                "Rotor position ("
                        + leadMotorInputs.rawRotorPositionRadians
                        + ") should be mechanism position ("
                        + leadMotorInputs.positionRadians
                        + ") * reduction ("
                        + ELEVATOR_MOTOR_REDUCTION
                        + ").");

        Assertions.assertEquals(
                0.0,
                leadMotorInputs.appliedVolts,
                1e-2,
                "Motor should apply zero volts when commanding a neutral output");

        Assertions.assertEquals(1.0, leadMotorInputs.positionRadians, 1e-1);
        Assertions.assertEquals(5.0, leadMotorInputs.rawRotorPositionRadians, 1e-1);
        Assertions.assertEquals(1.0, cancoderInputs.positionRadians, 1e-1);

        simAdapter.setState(Radians.of(10.0), RadiansPerSecond.zero());

        // Enabling!
        DriverStationSim.setEnabled(true);
        DriverStationSim.notifyNewData();

        // As per
        // https://v6.docs.ctr-electronics.com/en/latest/docs/api-reference/wpilib-integration/unit-testing.html
        // delay ~100ms so the devices can start up and enable
        Timer.delay(0.100);

        assert DriverStation.isEnabled();

        // Give it a couple cycles to let data propagate into the IOs
        leadMotor.controlToPositionUnprofiled(Radians.zero());
        double startTime = Timer.getFPGATimestamp();
        loopForTime(
                2.00,
                () -> {
                    loop.run();
                    System.err.println(
                            "leadMotorInputs.positionRadians is "
                                    + leadMotorInputs.positionRadians
                                    + ", leadMotorInputs.appliedVolts is "
                                    + leadMotorInputs.appliedVolts
                                    + ", current time since start of loop is "
                                    + (Timer.getFPGATimestamp() - startTime));
                });

        Assertions.assertEquals(
                0.0,
                simAdapter.getEncoderPosition().in(Radians),
                1e-1); // Give it a decent delta to account for slight oscillation

        // Now tell it to control back up and ensure that it both commands upward movement and
        // successfully moves the sim updward.
        leadMotor.controlToPositionUnprofiled(Radians.of(1.0));
        loopForTime(0.1, loop); // Give it a few cycles for the sim thread to update
        System.err.println("leadMotorInputs.appliedVolts is " + leadMotorInputs.appliedVolts);
        Assertions.assertEquals(
                1.0,
                Math.signum(leadMotorInputs.appliedVolts),
                "Motor should apply a negative voltage when controlling to a position below its"
                        + " current position");

        Angle startPosition = simAdapter.getMotorPosition();
        loopForTime(0.5, loop);
        Angle endPosition = simAdapter.getMotorPosition();

        Assertions.assertTrue(
                endPosition.gt(startPosition),
                "Mechanism position at the end of 1.0 seconds ("
                        + endPosition.in(Radians)
                        + "radians) should be greater than starting position ("
                        + startPosition.in(Radians)
                        + " radians)");
    }
}
