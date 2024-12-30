package coppercore.controls;

import java.util.Optional;

public class StateMachine<State, Trigger> {
    private StateMachineConfiguration configuration;
    private TransitionInfo transitionInfo;
    private State currentState;

    public StateMachine(StateMachineConfiguration config, State initialState) {
        configuration = config;
        currentState = initialState;
    }

    public void fire(Trigger trigger) {
        transitionInfo = new TransitionInfo(currentState, trigger);
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
            Optional<StateConfiguration> currentStateConfigurationOptional =
                    configuration.getStateConfiguration(currentState);
            Optional<StateConfiguration> nextStateConfigurationOptional =
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

    public TransitionInfo getTransitionInfo() {
        return transitionInfo;
    }

    public boolean inState(State state){
        return currentState.equals(state);
    }
}
