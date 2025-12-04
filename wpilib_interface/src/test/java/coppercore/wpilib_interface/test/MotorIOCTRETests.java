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
import coppercore.wpilib_interface.subsystems.sim.ElevatorSimAdapter;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.PerUnit;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.ElevatorSim;
import org.junit.jupiter.api.Assertions;
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

    /**
     * Emulate the "periodic" loop of the robot by calling `loop` and incrementing the WPIUtilJNI
     * mock time by 20ms until `timeSeconds` seconds have elapsed.
     *
     * @param timeSeconds The duration to simulate. If this is not a multiple of 20 milliseconds,
     *     the method will round up.
     * @param loop A Runnable to call before each increment of time and once at the very end.
     */
    void loopForTime(double timeSeconds, Runnable loop) {
        double timeElapsed = 0.00;
        Timer loopTimer = new Timer();
        while (timeElapsed < timeSeconds || (timeSeconds == 0 && timeElapsed == 0)) {
            loopTimer.restart();
            if (DriverStation.isEnabled()) {
                Unmanaged.feedEnable(20);
            }
            loop.run();

            // Wait for the loop to be exactly 0.02 seconds
            if (!loopTimer.hasElapsed(0.02)) {
                Timer.delay(0.02 - loopTimer.get());
            }

            timeElapsed += loopTimer.get();
        }
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
                                        .withKV(0.0)
                                        .withKA(0.5)
                                        .withKG(0.0)
                                        .withKP(0.1)
                                        .withKI(0.0)
                                        .withKD(0.0))
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
                                        .withMotionMagicExpo_kA(1)
                                        .withMotionMagicExpo_kV(1));

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
                    // Ensure that the sim has properly started up before asserting that the
                    // positions match.
                    if (cancoderInputs.positionRadians != 0.0
                            && leadMotorInputs.positionRadians != 0.0) {
                        Assertions.assertEquals(
                                cancoderInputs.positionRadians,
                                leadMotorInputs.positionRadians,
                                Math.PI / 2, // Large delta to account for +/- 1 cycle of CAN delay
                                "Encoder and lead motor mechanism position should match at all"
                                        + " times.");
                    }
                    // This line of logging is incredibly useful for tuning, but produces a ton of
                    // output.
                    System.out.println(
                            cancoderInputs.positionRadians
                                    + " -> "
                                    + leadMotorInputs.closedLoopReference
                                    + " ("
                                    + leadMotorInputs.closedLoopReferenceSlope
                                    + "/sec) outputting "
                                    + leadMotorInputs.closedLoopOutput
                                    + " @ "
                                    + leadMotorInputs.appliedVolts
                                    + "v ("
                                    + DriverStation.isEnabled()
                                    + ")");
                };

        // Wait for library initialization to take place. As soon as a real value is read, it will
        // be around 51.7, not 0.0.
        while (cancoderInputs.positionRadians == 0.0) {
            loopForTime(0, loop);
        }

        // Should be around 51.7 radians, but an unknown amount of time has passed during
        // library startup so we have a big delta here. We also round down in case the physics sim
        // has let it fall down at all.
        Assertions.assertEquals(cancoderInputs.positionRadians, 51, 5);

        // Enabling!
        DriverStationSim.setEnabled(true);
        DriverStationSim.notifyNewData();

        leadMotor.controlToPositionExpoProfiled(Rotations.zero());

        // Drive to 0 and make sure it gets there
        loopForTime(3.0, loop); // Give it enough time to drive to zero
        Assertions.assertEquals(
                0.0,
                cancoderInputs.positionRadians,
                1); // Give it a decent delta to account for slight oscillation

        // Drive to 10.0 radians and make sure it gets there.
        leadMotor.controlToPositionExpoProfiled(Radians.of(10.0));
        loopForTime(1.0, loop);
        Assertions.assertEquals(10.0, cancoderInputs.positionRadians, 1);
    }
}
