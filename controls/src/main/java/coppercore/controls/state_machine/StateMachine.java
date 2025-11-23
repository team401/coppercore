package coppercore.controls.state_machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Add missing javadocs

// Note: Some parts of the javadoc were written using Copilot

/** A simple state machine implementation. */
public class StateMachine<World> {

    private State<World> currentState;
    private final Map<String, State<World>> states;
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
    public State<World> registerState(String stateName, State<World> state) {
        states.put(stateName, state);
        return state;
    }

    public State<World> getStateByName(String stateName) {
        return states.get(stateName);
    }

    /**
     * Sets the current state of the state machine.
     * This will override defined transitions.
     * @param newState The StateKey of the new state
     */
    public void setState(State<World> newState) {
        if (newState == null) {
            return;
        }
        if (currentState != null) {
            currentState._onExit(world);
        }
        currentState = newState;
        if (currentState != null) {
            currentState._onEntry(world);
        }
    }

    /**
     * Gets the current state of the state machine.
     *
     * @return The current State
     */
    public State<World> getCurrentState() {
        return currentState;
    }

    /** Updates the state machine, transitioning to the next state if conditions are met. */
    public void updateStates() {
        if (currentState == null) {
            return;
        }
        setState(currentState.getNextState(world));
    }

    /** Calls the periodic function of the current state. */
    public void periodic() {
        if (currentState != null) {
            currentState._periodic(world);
        }
    }
}
