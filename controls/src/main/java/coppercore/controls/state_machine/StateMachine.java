package coppercore.controls.state_machine;

import java.util.HashMap;
import java.util.Map;

public class StateMachine<StateKey extends Enum<StateKey>> {

    private State<StateKey> state;
    private final Map<StateKey, State<StateKey>> states;

    public StateMachine() {
        this.states = new HashMap<>();
    }

    public State<StateKey> addState(StateKey state, Runnable periodic) {
        State<StateKey> newState = new FunctionalState<>(periodic);
        states.put(state, newState);
        return newState;
    }

    public State<StateKey> registerState(StateKey stateKey, State<StateKey> state) {
        states.put(stateKey, state);
        return state;
    }

    public void setState(StateKey newState) {
        state = states.get(newState);
    }

    public void updateStates() {
        if (state == null) {
            return;
        }
        StateKey nextState = state.checkTransitions();
        if (nextState == null) {
            return;
        }
        state._onExit();
        setState(nextState);
        if (state != null) {
            state._onEntry();
        }
    }

    public void periodic() {
        if (state != null) {
            state._periodic();
        }
    }
}
