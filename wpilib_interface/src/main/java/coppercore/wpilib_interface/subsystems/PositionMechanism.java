package coppercore.wpilib_interface.subsystems;

import static edu.wpi.first.units.Units.Rotations;

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
    protected T io;
    protected T[] followerIos;

    protected MutAngle positionSetpoint = Rotations.mutable(0.0);

    /**
     * Create a new mechanism for position-based control
     *
     * @param config The mechanism's configuration
     * @param inputs An inputs object
     * @param io An IO object, either for a real, sim, or replay/dummy IO implementation
     * @param followerIos An array of IOs for each follower
     * @param invertFollowers An array of booleans specifying whether or not to invert the direction
     *     for each follower
     */
    public PositionMechanism(
            PositionMechanismConfig config,
            V inputs,
            T io,
            T[] followerIos,
            boolean[] invertFollowers) {
        this.config = config;
        this.inputs = inputs;
        this.io = io;

        this.followerIos = followerIos;

        if (followerIos.length != invertFollowers.length) {
            throw new IllegalArgumentException(
                    "followerIos must contain the same number of elements as invertFollowers");
        }

        // Config all followers to follow leader motor
        for (int i = 0; i < followerIos.length; i++) {
            T follower = followerIos[i];
            boolean invert = invertFollowers[i];
            follower.follow(config.leadMotorId, invert);
        }
    }
}
