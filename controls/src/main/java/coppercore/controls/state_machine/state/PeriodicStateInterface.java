package coppercore.controls.state_machine.state;

public interface PeriodicStateInterface extends StateInterface {

    public default void periodic() {}
}
