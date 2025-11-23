package coppercore.controls.state_machine;

import java.util.HashMap;
import java.util.Map;

// Note: Some parts of the javadoc were written using Copilot

/** A simple state machine implementation. */
public class StateMachine<StateKey extends Enum<StateKey>, World> {

    private State<StateKey, World> state;
    private StateKey stateKey;
    private final Map<StateKey, State<StateKey, World>> states;
    private final World world;

    /** Constructs a new StateMachine. */
    public StateMachine(World world) {
        this.states = new HashMap<>();
        this.world = world;
    }

    /**
     * Registers a new state to the state machine.
     *
     * @param stateKey StateKey of the new state
     * @param state The state to be registered
     * @return The registered state
     */
    public State<StateKey, World> registerState(StateKey stateKey, State<StateKey, World> state) {
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
            state._onExit(world);
        }
        state = states.get(newState);
        stateKey = newState;
        if (state != null) {
            state._onEntry(world);
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
    public State<StateKey, World> getCurrentState() {
        return state;
    }

    /** Updates the state machine, transitioning to the next state if conditions are met. */
    public void updateStates() {
        if (state == null) {
            return;
        }
        setState(state.getNextState(world));
    }

    /** Calls the periodic function of the current state. */
    public void periodic() {
        if (state != null) {
            state._periodic(world);
        }
    }
}
