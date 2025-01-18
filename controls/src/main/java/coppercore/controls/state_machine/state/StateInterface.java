package coppercore.controls.state_machine.state;

import coppercore.controls.state_machine.transition.Transition;

/** State Implementation Base */
public interface StateInterface {

    /**
     * Define statemachine action to be run when entering state
     *
     * @param transition transition event used
     */
    public default void onEntry(Transition transition) {}

    /**
     * Define statemachine action to be run when exiting state
     *
     * @param transition transition event used
     */
    public default void onExit(Transition transition) {}
}
