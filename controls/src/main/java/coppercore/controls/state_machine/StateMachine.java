package coppercore.controls.state_machine;

import java.util.HashMap;
import java.util.Map;

public class StateMachine<StateKey extends Enum<StateKey>> {

    private State<StateKey> state;
    private StateKey stateKey;
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
        if (newState == null) {
            return;
        }
        if (state != null) {
            state._onExit();
        }
        state = states.get(newState);
        stateKey = newState;
        if (state != null) {
            state._onEntry();
        }
    }

    public StateKey getCurrentStateKey() {
        return stateKey;
    }

    public State getCurrentState() {
        return state;
    }

    public void updateStates() {
        if (state == null) {
            return;
        }
        setState(state.checkTransitions());
    }

    public void periodic() {
        if (state != null) {
            state._periodic();
        }
    }
}
