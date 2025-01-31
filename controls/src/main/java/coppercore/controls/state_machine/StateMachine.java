package coppercore.controls.state_machine;

import coppercore.controls.state_machine.state.PeriodicStateInterface;
import coppercore.controls.state_machine.state.StateConfiguration;
import coppercore.controls.state_machine.state.StateContainer;
import coppercore.controls.state_machine.state.StateInterface;
import coppercore.controls.state_machine.transition.Transition;
import coppercore.controls.state_machine.transition.TransitionInfo;
import java.util.Optional;

/** Generic State Machine */
public class StateMachine<State, Trigger> {
    private final StateMachineConfiguration<State, Trigger> configuration;
    private TransitionInfo<State, Trigger> transitionInfo;
    private State currentState;

    /**
     * Creates a StateMachine in the given state with the given configuration
     *
     * @param config       The state machine configuration
     * @param initialState default state
     */
    public StateMachine(StateMachineConfiguration<State, Trigger> config, State initialState) {
        configuration = config;
        currentState = initialState;
    }

    /**
     * Method to transition States based on given trigger
     *
     * @param trigger Trigger event to run
     */
    public void fire(Trigger trigger) {
        transitionInfo = new TransitionInfo<>(currentState, trigger);
        Optional<Transition<State, Trigger>> transitionOptional = configuration.getTransition(currentState, trigger);
        if (transitionOptional.isEmpty()) {
            transitionInfo.fail();
            return;
        }
        Transition<State, Trigger> transition = transitionOptional.get();
        if (!transition.canTransition()) {
            transitionInfo.fail();
            return;
        }
        transitionInfo.setTransition(transition);
        if (!transition.isInternal()) {
            Optional<StateConfiguration<State, Trigger>> currentStateConfigurationOptional = configuration
                    .getStateConfiguration(currentState);
            Optional<StateConfiguration<State, Trigger>> nextStateConfigurationOptional = configuration
                    .getStateConfiguration(transition.getDestination());
            if (currentStateConfigurationOptional.isPresent()) {
                StateConfiguration<State, Trigger> config = currentStateConfigurationOptional.get();
                if (config.doRunDefaultExitAction()) {
                    configuration.runOnExit(transition);
                } else if (config.hasExitAction()) {
                    config.runOnExit(transition);
                } else {
                    runOnExit(transition);
                }
            } else {
                configuration.runOnExit(transition);
            }
            transition.runAction();
            currentState = transition.getDestination();
            if (nextStateConfigurationOptional.isPresent()) {
                StateConfiguration<State, Trigger> config = nextStateConfigurationOptional.get();
                if (config.doRunDefaultExitAction()) {
                    configuration.runOnEntry(transition);
                } else if (config.hasEntryAction()) {
                    config.runOnEntry(transition);
                } else {
                    runOnEntry(transition);
                }
            } else {
                configuration.runOnEntry(transition);
            }
        } else {
            currentState = transition.getDestination();
        }
    }

    /**
     * Returns current state
     *
     * @return current state
     */
    public State getCurrentState() {
        return currentState;
    }

    /** Runs states Period if is periodic */
    public void periodic() {
        if (currentState instanceof PeriodicStateInterface) {
            ((PeriodicStateInterface) currentState).periodic();
        } else {
            periodicContainer();
        }
    }

    /**
     * Runs states Period if is periodic (This method is for if state is in
     * Container)
     */
    public void periodicContainer() {
        if (currentState instanceof StateContainer) {
            StateInterface state = ((StateContainer) currentState).getState();
            if (state instanceof PeriodicStateInterface) {
                ((PeriodicStateInterface) state).periodic();
            }
        }
    }

    private void runOnEntry(Transition transition) {
        if (currentState instanceof StateInterface) {
            ((StateInterface) currentState).onEntry(transition);
        } else {
            runOnEntryContainer(transition);
        }
    }

    private void runOnEntryContainer(Transition transition) {
        if (currentState instanceof StateContainer) {
            StateInterface state = ((StateContainer) currentState).getState();
            if (state instanceof StateInterface) {
                ((StateInterface) state).onEntry(transition);
            }
        }
    }

    private void runOnExit(Transition transition) {
        if (currentState instanceof StateInterface) {
            ((StateInterface) currentState).onEntry(transition);
        } else {
            runOnExitContainer(transition);
        }
    }

    private void runOnExitContainer(Transition transition) {
        if (currentState instanceof StateContainer) {
            StateInterface state = ((StateContainer) currentState).getState();
            if (state instanceof StateInterface) {
                ((StateInterface) state).onExit(transition);
            }
        }
    }

    /**
     * Returns if last transition was successful
     *
     * @return success
     */
    public boolean successfulTransition() {
        return !transitionInfo.wasFail();
    }

    /**
     * Returns infomation about last transtion
     *
     * @return information of last transiton
     */
    public TransitionInfo<State, Trigger> getTransitionInfo() {
        return transitionInfo;
    }

    /**
     * Tests if in state
     *
     * @param state target state
     * @return if in state
     */
    public boolean inState(State state) {
        return currentState.equals(state);
    }
}
