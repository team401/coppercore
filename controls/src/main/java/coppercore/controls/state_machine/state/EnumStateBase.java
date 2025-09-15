package coppercore.controls.state_machine.state;

import coppercore.controls.state_machine.transition.TransitionBase;

/** State Base for Enum Containing States */
public interface EnumStateBase<State, Trigger> extends StateBase<State, Trigger> {

    /**
     * Method to get State
     *
     * @return held state
     */
    @Override
    public StateBase<State, Trigger> getState();

    @Override
    public default void onEntry(TransitionBase<State, Trigger> transition) {
        getState().onEntry(transition);
    }

    @Override
    public default void onExit(TransitionBase<State, Trigger> transition) {
        getState().onExit(transition);
    }

    @Override
    public default void periodic() {
        getState().periodic();
    }
}
