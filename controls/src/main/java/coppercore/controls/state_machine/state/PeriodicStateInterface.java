package coppercore.controls.state_machine.state;

/** Periodic State Base */
public interface PeriodicStateInterface<State, Trigger> extends StateInterface<State, Trigger> {

    /** Method run on subsystem periodics (Does not get run automaticly) */
    public default void periodic() {}
}
