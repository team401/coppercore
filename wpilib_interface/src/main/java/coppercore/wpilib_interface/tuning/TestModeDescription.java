package coppercore.wpilib_interface.tuning;

/*
 * All TestMode enum classes must implement this interface.
 */
public interface TestModeDescription {
    /**
     * @return a description of a test mode
     */
    public String getDescription();
}
