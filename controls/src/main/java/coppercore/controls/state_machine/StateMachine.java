package coppercore.controls.state_machine;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// TODO: Add missing javadocs
// TODO: Clear requestedState after a transition or periodic call?

// Note: Some parts of the javadoc were written using Copilot

/**
 * A simple state machine implementation.
 *
 * <p>It supports registering states, defining transitions between states based on conditions, and
 * updating the current state based on those transitions.
 *
 * <p>Before using the state machine, states must be registered and transitions defined. The state
 * machine must also be initialized with an initial state using setState.
 *
 * <p>The state machine can then be updated using the updateStates method, which will evaluate the
 * transitions and change the current state accordingly. The periodic method can be called to
 * execute any periodic actions defined in the current state.
 *
 * <p>It also does not support hierarchical states or parallel states.
 *
 * @warning Currently the state machine does not check for duplicate state names.
 * @param <World> The type of the world in which this state machine lives.
 */
public class StateMachine<World> {

    private State<World> currentState;
    private State<World> requestedState;
    private final Map<String, State<World>> states;
    private final World world;

    /**
     * Constructs a new StateMachine.
     *
     * @param world - the world in which this state machine lives.
     */
    public StateMachine(World world) {
        this.states = new HashMap<>();
        this.world = world;
    }

    /**
     * Registers a new state to the state machine.
     *
     * @param state The state to be registered
     * @return The registered state
     */
    public State<World> registerState(State<World> state) {
        states.put(state.getName(), state);
        state.setRequestedStateSupplier(() -> requestedState);
        return state;
    }

    /**
     * Gets a state by its name.
     *
     * @param stateName The name of the state
     * @return The state with the given name, or null if not found
     */
    public State<World> getStateByName(String stateName) {
        return states.get(stateName);
    }

    /**
     * Sets the current state of the state machine. This will override defined transitions. If
     * newState is equal to currentState, The onExit and onEntry methods will still be called. If
     * the current state is null / setting the initial state, only the onEntry method will be
     * called. If the current state is not null, its onExit method will be called before changing
     * states.
     *
     * @param newState The new state
     */
    public void setState(State<World> newState) {
        Objects.requireNonNull(newState, "Can't set state to null");
        if (currentState != null) {
            currentState._onExit(this, world);
        }
        currentState = newState;
        currentState._onEntry(this, world);
    }

    /**
     * Gets the current state of the state machine.
     *
     * @return The current State
     */
    public State<World> getCurrentState() {
        return currentState;
    }

    /**
     * Updates the state machine, transitioning to the next state if conditions are met. If the next
     * state is the same as the current state, onExit and onEntry will still be called. Raises an
     * exception if currentState is null.
     *
     * <p>After processing transitions, the requested state is cleared.
     */
    public void updateStates() {
        Objects.requireNonNull(
                currentState,
                "Can't call updateStates while the currentState is null, this is either a bug in"
                    + " the State Machine implementation or You forgot to give the state machine an"
                    + " initial state using setState");
        currentState.getNextState(world).ifPresent(this::setState);
        // Clear requested state after processing
        this.requestedState = null;
    }

    /**
     * Calls the periodic function of the current state. Raises an exception if currentState is
     * null.
     */
    public void periodic() {
        Objects.requireNonNull(
                currentState,
                "Can't call periodic while the currentState is null, this is either a bug in the"
                        + " State Machine implementation or You forgot to give the state machine an"
                        + " initial state using setState");
        currentState._periodic(this, world);
    }

    /**
     * Requests a state change to the specified state.
     *
     * <p>If multiple states are requested before the next updateStates call, the last requested
     * state will take precedence. And if the requested state is null, no state change will occur.
     *
     * <p>If the requested state is the same as the current state, onExit and onEntry will still be
     * called during the next updateStates call if the request is honored.
     *
     * <p>The requested state will be cleared after the next updateStates call.
     *
     * @param state The requested state
     */
    public void requestState(State<World> state) {
        requestedState = state;
    }

    // NOTE: Consider switching from a String to some other type for graph format
    /**
     * Write a state machine configuration in graphviz format with custom graph settings
     *
     * @param pw The PrintWriter to write to
     * @param graphFormat The graph format settings
     */
    public void writeGraphvizFileWithCustomGraphFormat(PrintWriter pw, String graphFormat) {
        pw.println("digraph {");
        pw.println();
        pw.println("  // Graphviz Format settings");
        pw.println();
        pw.println(graphFormat);
        pw.println();
        pw.println("  // States");
        pw.println();
        for (var state : states.entrySet()) {
            var stateName = state.getKey();
            pw.printf("  %s;%n", stateName);
        }
        pw.println();
        pw.println("  // Transitions");
        pw.println();
        for (var state : states.values()) {
            var stateName = state.name;
            var transitions = state.getTransitions();
            for (var transition : transitions) {
                var toState = transition.toState;
                var toStateName = toState.name;
                pw.printf(
                        "  %s -> %s [label=\"%s\"];%n",
                        stateName, toStateName, transition.description);
            }
            pw.println();
        }
        pw.println("}");
    }

    /**
     * Write a state machine configuration in graphviz format Use a custom default graph format.
     * Which is a left to right directed graph with rounded box nodes, and fontsize 10 for edges.
     *
     * @param pw The PrintWriter to write to
     */
    public void writeGraphvizFile(PrintWriter pw) {
        writeGraphvizFileWithCustomGraphFormat(
                pw,
                """
      rankdir=LR;
      node [
        shape=box,
        style=rounded
      ];

      edge [
        fontsize=10
      ];
    """);
    }
}
