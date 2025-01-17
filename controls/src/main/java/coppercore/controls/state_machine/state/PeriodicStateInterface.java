package coppercore.controls.state_machine.state;

public interface PeriodicStateInterface extends StateInterface {

    /** Method run on subsystem periodics (Does not get run automaticly) */
    public default void periodic() {}
}
