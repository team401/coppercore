package coppercore.controls.state_machine.transition.transitions;

import coppercore.controls.state_machine.transition.TransitionBase;

public class Transition<State, Trigger> extends TransitionBase<State, Trigger> {
    
    private boolean runOnEntry;
    private boolean runOnExit;

    public Transition(State source, State destination, Trigger trigger) {
        super(source, destination, trigger);
    }

    public Transition<State, Trigger> disableOnEntry(){
        this.runOnEntry = false;
        return this;
    }

    public Transition<State, Trigger> disableOnExit(){
        this.runOnExit = false;
        return this;
    }

    @Override
    public boolean runOnEntry(){
        return runOnEntry;
    }

    @Override
    public boolean runOnExit(){
        return runOnExit;
    }
    
}
