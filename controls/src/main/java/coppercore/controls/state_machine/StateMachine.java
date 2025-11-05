package coppercore.controls.state_machine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StateMachine<StateKey extends Enum> {
    
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
        StateKey nextState = state.checkTransitions();
        if (nextState != null) {
            state._onExit();
        }
        setState(nextState);
        state._onEntry();
    }

    public void periodic() {
        state._periodic();
    }

}
