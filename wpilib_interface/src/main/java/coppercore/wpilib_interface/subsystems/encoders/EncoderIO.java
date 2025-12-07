package coppercore.wpilib_interface.subsystems.encoders;

import edu.wpi.first.units.measure.Angle;

/**
 * A generic encoder IO. This is geared mostly towards use with a CANCoder. Contains methods to
 * update inputs and set/seed position.
 */
public interface EncoderIO {
    public void updateInputs(EncoderInputs inputs);

    /**
     * Set the current position of the encoder.
     *
     * <p>This does NOT move the system. It tells the encoder that it is now at `position`, updating
     * its offset accordingly.
     *
     * @param position The position to set as the current encoder position.
     */
    public void setCurrentPosition(Angle position);
}
