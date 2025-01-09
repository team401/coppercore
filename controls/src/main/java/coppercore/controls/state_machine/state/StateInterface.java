package coppercore.controls.state_machine.state;

import coppercore.controls.state_machine.transition.Transition;

public interface StateInterface {

    public default void onEntry(Transition transition) {}

    public default void onExit(Transition transition) {}
}
