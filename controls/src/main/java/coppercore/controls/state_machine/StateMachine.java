package coppercore.controls.state_machine;

import coppercore.controls.state_machine.state.StateConfiguration;
import coppercore.controls.state_machine.transition.Transition;
import coppercore.controls.state_machine.transition.TransitionInfo;
import java.util.Optional;

public class StateMachine<State, Trigger> {
    private final StateMachineConfiguration<State, Trigger> configuration;
    private TransitionInfo<State, Trigger> transitionInfo;
    private State currentState;

    public StateMachine(StateMachineConfiguration<State, Trigger> config, State initialState) {
        configuration = config;
        currentState = initialState;
    }

    /**
     * Method to transition States based on given trigger.
     *
     * @param trigger
     */
    public void fire(Trigger trigger) {
        transitionInfo = new TransitionInfo<>(currentState, trigger);
        Optional<Transition<State, Trigger>> transitionOptional =
                configuration.getTransition(currentState, trigger);
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
            // TODO: Make use set actions
            Optional<StateConfiguration<State, Trigger>> currentStateConfigurationOptional =
                    configuration.getStateConfiguration(currentState);
            Optional<StateConfiguration<State, Trigger>> nextStateConfigurationOptional =
                    configuration.getStateConfiguration(transition.getDestination());
            if (currentStateConfigurationOptional.isPresent()) {
                currentStateConfigurationOptional.get().runOnEntry(transition);
            }
            transition.runAction();
            if (nextStateConfigurationOptional.isPresent()) {
                nextStateConfigurationOptional.get().runOnExit(transition);
            }
        }
        currentState = transition.getDestination();
    }

    public State getCurrentState() {
        return currentState;
    }

    public boolean successfulTransition() {
        return !transitionInfo.wasFail();
    }

    public TransitionInfo<State, Trigger> getTransitionInfo() {
        return transitionInfo;
    }

    public boolean inState(State state) {
        return currentState.equals(state);
    }
}
