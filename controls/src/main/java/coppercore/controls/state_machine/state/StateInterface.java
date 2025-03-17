package coppercore.controls.state_machine.state;

import coppercore.controls.state_machine.transition.Transition;

/** State Implementation Base */
public interface StateInterface<State, Trigger> {

    /**
     * Define statemachine action to be run when entering state
     *
     * @param transition transition event used
     */
    public default void onEntry(Transition<State, Trigger> transition) {}

    /**
     * Define statemachine action to be run when exiting state
     *
     * @param transition transition event used
     */
    public default void onExit(Transition<State, Trigger> transition) {}
}
