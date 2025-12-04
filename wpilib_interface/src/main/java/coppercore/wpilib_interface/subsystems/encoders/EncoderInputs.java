package coppercore.wpilib_interface.subsystems.encoders;

import org.littletonrobotics.junction.AutoLog;

/**
 * A generic set of inputs for an encoder.
 *
 * <p>
 */
@AutoLog
public class EncoderInputs {
    /**
     * Tracks whether, the last time inputs were updated, all values successfully refreshed from the
     * device. If any value fails to refresh, this likely indicates a disconnected motor and
     * connected will be set to false until the next update.
     *
     * <p>The methods updating this value do not handle any debouncing, nor do they refer to past
     * values to make a determination of connectivity. It is merely an indicator of the connectivity
     * state at the instant of the most recent update. To more accurately filter out momentary
     * issues, a debouncer is recommended before using this value to disable subsystems.
     */
    public boolean connected = false;

    /**
     * The current absolute position of the encoder, in radians. This will be a value from 0 to
     * 2*pi. This is affected by encoder offset.
     */
    public double absolutePositionRadians = 0.0;

    /**
     * The current position of the encoder, in radians. This is affected by setPosition and encoder
     * offset.
     */
    public double positionRadians = 0.0;

    /** The current velocity of the encoder, in radians per second. */
    public double velocityRadiansPerSecond = 0.0;
}
