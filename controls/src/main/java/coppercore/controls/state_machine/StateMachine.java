package coppercore.controls.state_machine;

import java.util.HashMap;
import java.util.Map;

// Note: Some parts of the javadoc were written using Copilot

/** A simple state machine implementation. */
public class StateMachine<StateKey extends Enum<StateKey>, World> {

    private State<StateKey, World> state;
    private StateKey stateKey;
    private final Map<StateKey, State<StateKey, World>> states;
    private World world;

    /** Constructs a new StateMachine. */
    public StateMachine(World world) {
        this.states = new HashMap<>();
        this.world = world;
    }

    // /**
    //  * Adds a new functional state to the state machine.
    //  *
    //  * @param state StateKey of the new state
    //  * @param periodic The periodic function to be called while in this state
    //  * @return The newly created state
    //  */
    // public State<StateKey, World> addState(StateKey state, Runnable periodic) {
    //     State<StateKey, World> newState = new FunctionalState<>(periodic);
    //     states.put(state, newState);
    //     return newState;
    // }

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
            state._periodic();
        }
    }

    public TransitionFrom from(StateKey stateKey) {
        if (!states.containsKey(stateKey)) {
            throw new IllegalArgumentException("StateMachine does not contain state: " + stateKey);
        }
        State<StateKey, World> fromState = states.get(stateKey);
        if (fromState == null) {
            throw new IllegalArgumentException("StateMachine does not contain state: " + stateKey);
        }
        return new TransitionFrom(fromState);
    }

    public class TransitionFrom {
        State<StateKey, World> fromState;

        TransitionFrom(State<StateKey, World> fromState) {
            this.fromState = fromState;
        }

        public TransitionConditionBuilder when(Condition<World> condition) {
            return new TransitionConditionBuilder(condition);
        }

        public TransitionConditionBuilder whenFinished() {
            return when((world) -> fromState.isFinished());
        }

        public TransitionConditionBuilder whenRequested(StateKey requestedState) {
            return when((world) -> fromState.requestedState != null && fromState.requestedState.equals(requestedState));
        }

    
        public class TransitionConditionBuilder {
            Condition<World> condition;
    
            TransitionConditionBuilder(Condition<World> condition) {
                this.condition = condition;
            }
    
            public TransitionConditionBuilder andWhen(Condition<World> nextCondition) {
                this.condition = (world) -> this.condition.isFulfilledFor(world) && nextCondition.isFulfilledFor(world);
                return this;    
            }
    
            public TransitionFrom transitionTo(StateKey toState) {
                fromState.addTransition(toState, condition);
                return TransitionFrom.this;
            }
        }

    }

    @FunctionalInterface
    public interface Condition<World> {
        public boolean isFulfilledFor(World world);
    }

}
