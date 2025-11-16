package coppercore.controls.state_machine;

import java.util.HashMap;
import java.util.Map;

// Note: Some parts of the javadoc were written using Copilot

/** A simple state machine implementation. */
public class StateMachine<StateKey extends Enum<StateKey>> {

    private State<StateKey> state;
    private StateKey stateKey;
    private final Map<StateKey, State<StateKey>> states;

    /** Constructs a new StateMachine. */
    public StateMachine() {
        this.states = new HashMap<>();
    }

    /**
     * Adds a new functional state to the state machine.
     *
     * @param state StateKey of the new state
     * @param periodic The periodic function to be called while in this state
     * @return The newly created state
     */
    public State<StateKey> addState(StateKey state, Runnable periodic) {
        State<StateKey> newState = new FunctionalState<>(periodic);
        states.put(state, newState);
        return newState;
    }

    /**
     * Registers a new state to the state machine.
     *
     * @param stateKey StateKey of the new state
     * @param state The state to be registered
     * @return The registered state
     */
    public State<StateKey> registerState(StateKey stateKey, State<StateKey> state) {
        states.put(stateKey, state);
        return state;
    }

    /**
     * Sets the current state of the state machine.
     * This will override defined transitions.
     * @param newState The StateKey of the new state
     */
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

    /**
     * Gets the current state key of the state machine.
     *
     * @return The current StateKey
     */
    public StateKey getCurrentStateKey() {
        return stateKey;
    }

    /**
     * Gets the current state of the state machine.
     *
     * @return The current State
     */
    public State<StateKey> getCurrentState() {
        return state;
    }

    /** Updates the state machine, transitioning to the next state if conditions are met. */
    public void updateStates() {
        if (state == null) {
            return;
        }
        setState(state.getNextState());
    }

    /** Calls the periodic function of the current state. */
    public void periodic() {
        if (state != null) {
            state._periodic();
        }
    }
}
