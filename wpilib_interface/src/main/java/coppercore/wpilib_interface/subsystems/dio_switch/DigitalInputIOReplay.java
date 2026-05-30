package coppercore.wpilib_interface.subsystems.dio_switch;

/**
 * DigitalInputIOReplay allows DigitalInputIO to be used in replay by implementing methods with
 * no-op implementations.
 */
public class DigitalInputIOReplay implements DigitalInputIO {
    /** {@inheritDoc} */
    @Override
    public void updateInputs(DigitalInputInputs inputs) {}
}
