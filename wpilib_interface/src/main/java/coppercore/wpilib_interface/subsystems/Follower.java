package coppercore.wpilib_interface.subsystems;

import coppercore.wpilib_interface.subsystems.motors.MotorIO;
import coppercore.wpilib_interface.subsystems.motors.MotorInputsAutoLogged;

/**
 * A record to configure follower motors
 * 
 * @param <V> The inputs type
 * @param <T> The IO type
 * @param io IO object to use for this follower
 * @param inputs Inputs object to pass to the mechanism
 */
public record Follower<V extends MotorInputsAutoLogged, T extends MotorIO>(
    T io,
    V inputs,
    boolean invert
) {}