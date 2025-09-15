package coppercore.controls.state_machine.state;

import coppercore.controls.state_machine.transition.TransitionBase;

/** State Base for Instanced States */
public abstract class InstancedStateBase<State, Trigger, S> implements StateBase<State, Trigger> {
    protected S instance;
    protected boolean initialized = false;

    public void setInstance(S instance) {
        this.instance = instance;
    }

    public abstract void onEntryWithInstance();

    public abstract void periodicWithInstance();

    public abstract void onExitWithInstance();

    @Override
    public void onEntry(TransitionBase<State, Trigger> transition) {
        if (initialized) {
            onEntryWithInstance();
        }
    }

    @Override
    public void periodic() {
        if (initialized) {
            periodicWithInstance();
        }
    }

    @Override
    public void onExit(TransitionBase<State, Trigger> transition) {
        if (initialized) {
            onExitWithInstance();
        }
    }
}
