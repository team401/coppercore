package coppercore.wpilib_interface.test;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Kilograms;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.Microseconds;
import static edu.wpi.first.units.Units.Pounds;
import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.Seconds;

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
import coppercore.wpilib_interface.subsystems.configs.CANDeviceID;
import coppercore.wpilib_interface.subsystems.configs.ElevatorMechanismConfig;
import coppercore.wpilib_interface.subsystems.configs.MechanismConfig.GravityFeedforwardType;
import coppercore.wpilib_interface.subsystems.encoders.EncoderIOCANCoderPositionSim;
import coppercore.wpilib_interface.subsystems.encoders.EncoderInputs;
import coppercore.wpilib_interface.subsystems.motors.MotorInputs;
import coppercore.wpilib_interface.subsystems.motors.talonfx.MotorIOTalonFXPositionSim;
import coppercore.wpilib_interface.subsystems.sim.ElevatorSimAdapter;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.PerUnit;
import edu.wpi.first.util.WPIUtilJNI;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.ElevatorSim;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The MotorIOTests class contains tests for motor IOs using simulation.
 *
 * <p>Most of the constants and gains found here are from
 * https://github.com/team401/2025-Robot-Code.
 */
public class MotorIOTests {
    private static String canbus = "sim";

    public static CANDeviceID encoderId = new CANDeviceID(canbus, 52);

    private double currentMockTimeSeconds = 0.0;

    /**
     * Set WPIUtilJni's mock time to timeSeconds
     *
     * <p>This exists as a wrapper because WPIUtilJNI.setMockTime expects the time as a long in
     * microseconds
     *
     * @param timeSeconds the time to set, in seconds
     */
    private void setMockTimeSeconds(double timeSeconds) {
        WPIUtilJNI.setMockTime((long) Seconds.of(timeSeconds).in(Microseconds));
    }

    /** Enable mock time and sets the time to 0.0 seconds */
    @BeforeEach
    void setUpMockTime() {
        assert HAL.initialize(500, 0);
        WPIUtilJNI.enableMockTime();
        setMockTimeSeconds(0.0);
        this.currentMockTimeSeconds = 0.0;
    }

    /** Disable mock time in case another test needs it to be off. */
    @AfterEach
    void disableMockTime() {
        WPIUtilJNI.disableMockTime();
    }

    /**
     * Emulate the "periodic" loop of the robot by calling `loop` and incrementing the WPIUtilJNI
     * mock time by 20ms utnil `timeSeconds` seconds have elapsed.
     *
     * @param timeSeconds The duration to simulate. If this is not a multiple of 20 milliseconds,
     *     the method will round up.
     * @param loop A Runnable to call before each increment of time and once at the very end.
     */
    void loopForTime(double timeSeconds, Runnable loop) {
        double timeElapsed = 0.0;
        while (timeElapsed < timeSeconds) {
            loop.run();
            timeElapsed += 0.02;
            currentMockTimeSeconds += 0.02;
            setMockTimeSeconds(currentMockTimeSeconds);
        }

        loop.run();
    }

    @Test
    public void talonFXElevator() {
        // Define configs

        ElevatorMechanismConfig mechanismConfig =
                ElevatorMechanismConfig.builder()
                        .withName("coppervator")
                        .withEncoderToMechanismRatio(1.0)
                        .withMotorToEncoderRatio(5.0)
                        .withLeadMotorId(new CANDeviceID(canbus, 50))
                        .withGravityFeedforwardType(GravityFeedforwardType.STATIC_ELEVATOR)
                        .withElevatorToMechanismRatio(Inches.of(4.724).div(Rotations.of(1)))
                        .addFollower(new CANDeviceID(canbus, 51), true)
                        .build();

        TalonFXConfiguration talonFXConfigs =
                new TalonFXConfiguration()
                        .withFeedback(
                                new FeedbackConfigs()
                                        .withFeedbackRemoteSensorID(encoderId.id())
                                        .withFeedbackSensorSource(
                                                FeedbackSensorSourceValue.FusedCANcoder)
                                        .withSensorToMechanismRatio(1.0)
                                        .withRotorToSensorRatio(1.0))
                        .withMotorOutput(
                                new MotorOutputConfigs().withNeutralMode(NeutralModeValue.Coast))
                        .withCurrentLimits(
                                new CurrentLimitsConfigs()
                                        .withStatorCurrentLimitEnable(true)
                                        .withStatorCurrentLimit(Amps.of(80.0)))
                        .withSlot0(
                                new Slot0Configs()
                                        .withGravityType(GravityTypeValue.Elevator_Static)
                                        .withKS(0.0)
                                        .withKV(2.0)
                                        .withKA(0.08)
                                        .withKG(13.0)
                                        .withKP(60.0)
                                        .withKI(0.0)
                                        .withKD(10.0))
                        .withMotionMagic(
                                new MotionMagicConfigs()
                                        .withMotionMagicCruiseVelocity(
                                                // This very convoluted conversion turns 3.0 m/s
                                                // goal velocity into a value in radians per second
                                                // by converting meters to radians using the 4.724
                                                // inches of height per spool rotation based on the
                                                // drum radius.
                                                RadiansPerSecond.of(
                                                        3.0 // Meters per second
                                                                / Inches.of(4.724)
                                                                        .div(Rotations.of(1))
                                                                        .in(
                                                                                PerUnit.combine(
                                                                                        Meters,
                                                                                        Radians))))
                                        .withMotionMagicExpo_kA(0.01)
                                        .withMotionMagicExpo_kV(0.1));

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
                        true,
                        1.0,
                        0.0,
                        0.0);

        var simAdapter = new ElevatorSimAdapter(mechanismConfig, elevatorSim);

        var leadMotor =
                MotorIOTalonFXPositionSim.newLeader(mechanismConfig, talonFXConfigs, simAdapter);
        var followerMotor =
                MotorIOTalonFXPositionSim.newFollower(
                        mechanismConfig, 0, talonFXConfigs, simAdapter);
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

        loopForTime(5.0, loop);
        DriverStationSim.setEnabled(true);
        DriverStationSim.notifyNewData();
        loopForTime(2.0, loop);
        leadMotor.controlToPositionExpoProfiled(Rotations.zero());

        System.err.println(cancoderInputs.positionRadians);
        Assertions.assertEquals(cancoderInputs.positionRadians, 0, 1e-2);
    }
}
