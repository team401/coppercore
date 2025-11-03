package coppercore.wpilib_interface.subsystems;

import static edu.wpi.first.units.Units.Rotations;

import java.util.Arrays;
import java.util.stream.Collectors;

import coppercore.wpilib_interface.subsystems.configs.PositionMechanismConfig;
import coppercore.wpilib_interface.subsystems.motors.MotorIO;
import coppercore.wpilib_interface.subsystems.motors.MotorInputsAutoLogged;
import edu.wpi.first.units.measure.MutAngle;

/**
 * A base mechanism for position control.
 *
 * <p>Supports 1 lead motor IO and any number (including 0) of followers
 */
public class PositionMechanism<V extends MotorInputsAutoLogged, T extends MotorIO> {
    protected final PositionMechanismConfig config;

    protected V inputs;
    protected V[] followerInputs;
    protected T io;
    protected T[] followerIos;

    protected MutAngle positionSetpoint = Rotations.mutable(0.0);

    /**
     * Create a new mechanism for position-based control
     *
     * @param config The mechanism's configuration
     * @param inputs An inputs object
     * @param followers A set of IO objects, inputs objects, and motor inverts specifying each follower. Can be empty for no followers.
     */
    public PositionMechanism(
            PositionMechanismConfig config,
            V inputs,
            T io,
            Follower<V, T>[] followers) {
        this.config = config;
        this.inputs = inputs;
        this.io = io;

        // TODO: Correctly find ios and inputs
        this.followerIos = Arrays.stream(followers).map(f -> f.io()).collect(Collectors.toList()).toArray();
        this.followerInputs = followerInputs;

        if (!(followerIos.length == invertFollowers.length && followerIos.length == followerInputs.length)) {
            throw new IllegalArgumentException(
                    "followerIos must contain the same number of elements as invertFollowers and followerInputs");
        }

        // Config all followers to follow leader motor
        for (int i = 0; i < followerIos.length; i++) {
            T follower = followerIos[i];
            boolean invert = invertFollowers[i];
            follower.follow(config.leadMotorId, invert);
        }
    }

    /**
     * Poll inputs and log them.
     * 
     * <p>This method will not run automatically, it must be called by a subsystem or other periodic.
     */
    public void periodic() {
        io.updateInputs(inputs);

        for (int i = 0; i < followerIos.length; i++) {
        }
    }
}
