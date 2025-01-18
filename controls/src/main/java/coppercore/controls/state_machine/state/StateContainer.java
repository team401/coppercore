package coppercore.controls.state_machine.state;

/** State Container Base */
public interface StateContainer {
    /**
     * Method to get Containted State
     *
     * @return held state
     */
    public StateInterface getState();
}
