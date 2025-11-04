package coppercore.controls.state_machine;

import java.util.Optional;

import coppercore.controls.state_machine.state.StateBase;
import coppercore.controls.state_machine.transition.TransitionBase;
import coppercore.controls.state_machine.transition.TransitionInfo;

/** Generic State Machine */
public class StateMachine<State extends StateBase<State, Trigger>, Trigger> {
    private final StateMachineConfiguration<State, Trigger> configuration;
    private TransitionInfo<State, Trigger> transitionInfo;
    private State currentState;

    /**
     * Creates a StateMachine in the given state with the given configuration
     *
     * @param config The state machine configuration
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
        Optional<TransitionBase<State, Trigger>> transitionOptional =
                configuration.getTransition(currentState, trigger);
        // Fail the transition if no transition available
        if (transitionOptional.isEmpty()) {
            transitionInfo.fail();
            return;
        }
        TransitionBase<State, Trigger> transition = transitionOptional.get();
        // Fail the transition if is not a legal transition
        if (!transition.canTransition()) {
            transitionInfo.fail();
            return;
        }
        // TODO: Make the Transition generate the transition info.
        transitionInfo.setTransition(transition);
        if (transition.runOnExit()) {
            runOnExit(transition);
        }
        currentState = transition.getDestination();
        if (transition.runOnEntry()) {
            runOnEntry(transition);
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
        currentState.periodic();
    }

    private void runOnEntry(TransitionBase<State, Trigger> transition) {
        currentState.onEntry(transition);
    }

    private void runOnExit(TransitionBase<State, Trigger> transition) {
        currentState.onExit(transition);
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
     * Returns information about last transition
     *
     * @return information of last transition
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
