package coppercore.controls.state_machine.transition.transitions;

import coppercore.controls.state_machine.transition.TransitionBase;

public class Transition<State, Trigger> extends TransitionBase<State, Trigger> {
    
    public Transition(State source, State destination, Trigger trigger) {
        super(source, destination, trigger);
    }
    
}
