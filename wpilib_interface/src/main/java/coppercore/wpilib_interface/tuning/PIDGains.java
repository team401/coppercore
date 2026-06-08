package coppercore.wpilib_interface.tuning;

import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.Slot1Configs;
import com.ctre.phoenix6.configs.Slot2Configs;
import coppercore.wpilib_interface.subsystems.motors.MotorIO;
import edu.wpi.first.math.controller.PIDController;

/** PID and feedforward gains used by motor controllers. */
public record PIDGains(
        double kP, double kI, double kD, double kS, double kG, double kV, double kA) {

    /**
     * Creates PID gains with zero feedforward terms.
     *
     * @param kP proportional gain
     * @param kI integral gain
     * @param kD derivative gain
     * @return PID gains
     */
    public static PIDGains kPID(double kP, double kI, double kD) {
        return new PIDGains(kP, kI, kD, 0.0, 0.0, 0.0, 0.0);
    }

    /**
     * Creates PID gains with static, gravity, velocity, and acceleration feedforward.
     *
     * @param kP proportional gain
     * @param kI integral gain
     * @param kD derivative gain
     * @param kS static feedforward gain
     * @param kG gravity feedforward gain
     * @param kV velocity feedforward gain
     * @param kA acceleration feedforward gain
     * @return full PID/feedforward gains
     */
    public static PIDGains kPIDSGVA(
            double kP, double kI, double kD, double kS, double kG, double kV, double kA) {
        return new PIDGains(kP, kI, kD, kS, kG, kV, kA);
    }

    /**
     * Creates PID gains with static, velocity, and acceleration feedforward.
     *
     * @param kP proportional gain
     * @param kI integral gain
     * @param kD derivative gain
     * @param kS static feedforward gain
     * @param kV velocity feedforward gain
     * @param kA acceleration feedforward gain
     * @return PID/feedforward gains with kG set to zero
     */
    public static PIDGains kPIDSVA(
            double kP, double kI, double kD, double kS, double kV, double kA) {
        return new PIDGains(kP, kI, kD, kS, 0.0, kV, kA);
    }

    /**
     * Gets only the PID terms.
     *
     * @return kP, kI, and kD
     */
    public double[] asArrayWithoutFeedForward() {
        return new double[] {kP, kI, kD};
    }

    /**
     * Gets all PID/feedforward terms.
     *
     * @return kP, kI, kD, kS, kG, kV, and kA
     */
    public double[] asArray() {
        return new double[] {kP, kI, kD, kS, kG, kV, kA};
    }

    /**
     * Applies these gains to a Phoenix slot 0 config.
     *
     * @param slot0Configs config to mutate
     * @return mutated config
     */
    public Slot0Configs applyToSlot0Config(Slot0Configs slot0Configs) {
        slot0Configs.withKP(kP).withKI(kI).withKD(kD).withKS(kS).withKV(kV).withKA(kA).withKG(kG);
        return slot0Configs;
    }

    /**
     * Applies these gains to a Phoenix slot 1 config.
     *
     * @param slot1Configs config to mutate
     * @return mutated config
     */
    public Slot1Configs applyToSlot1Config(Slot1Configs slot1Configs) {
        slot1Configs.withKP(kP).withKI(kI).withKD(kD).withKS(kS).withKV(kV).withKA(kA).withKG(kG);
        return slot1Configs;
    }

    /**
     * Applies these gains to a Phoenix slot 2 config.
     *
     * @param slot2Configs config to mutate
     * @return mutated config
     */
    public Slot2Configs applyToSlot2Config(Slot2Configs slot2Configs) {
        slot2Configs.withKP(kP).withKI(kI).withKD(kD).withKS(kS).withKG(kG).withKV(kV).withKA(kA);
        return slot2Configs;
    }

    /**
     * Creates a Phoenix slot 0 config from these gains.
     *
     * @return configured slot 0 config
     */
    public Slot0Configs toSlot0Config() {
        Slot0Configs slot0Configs = new Slot0Configs();
        applyToSlot0Config(slot0Configs);
        return slot0Configs;
    }

    /**
     * Creates a Phoenix slot 1 config from these gains.
     *
     * @return configured slot 1 config
     */
    public Slot1Configs toSlot1Config() {
        Slot1Configs slot1Configs = new Slot1Configs();
        applyToSlot1Config(slot1Configs);
        return slot1Configs;
    }

    /**
     * Creates a Phoenix slot 2 config from these gains.
     *
     * @return configured slot 2 config
     */
    public Slot2Configs toSlot2Config() {
        Slot2Configs slot2Configs = new Slot2Configs();
        applyToSlot2Config(slot2Configs);
        return slot2Configs;
    }

    /**
     * Applies these gains to a motor IO.
     *
     * @param motorIO motor IO to update
     */
    public void applyToMotorIO(MotorIO motorIO) {
        motorIO.setGains(kP, kI, kD, kS, kG, kV, kA);
    }

    /**
     * Creates a WPILib PIDController from the PID terms.
     *
     * @return PID controller using kP, kI, and kD
     */
    public PIDController toPIDController() {
        return new PIDController(kP, kI, kD);
    }
}
