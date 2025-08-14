package coppercore.controls.state_machine.state;

import coppercore.controls.state_machine.transition.TransitionBase;

public interface StateBase<State, Trigger> {
    
    public default StateBase<State, Trigger> getState() {
        return this;
    };

    /**
     * Define statemachine action to be run when entering state
     *
     * @param transition transition event used
     */
    public default void onEntry(TransitionBase<State, Trigger> transition) {}

    /**
     * Define statemachine action to be run when exiting state
     *
     * @param transition transition event used
     */
    public default void onExit(TransitionBase<State, Trigger> transition) {}

    public default void periodic() {}
}
