package coppercore.controls.state_machine;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

// TODO: Add missing javadocs
// TODO: Clear requestedState after a transition or periodic call?

// Note: Some parts of the javadoc were written using Copilot

/** A simple state machine implementation. */
public class StateMachine<World> {

    private State<World> currentState;
    private State<World> requestedState;
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
     * @param state The state to be registered
     * @return The registered state
     */
    public State<World> registerState(State<World> state) {
        states.put(state.name, state);
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
     * Sets the current state of the state machine. This will override defined transitions.
     *
     * @param newState The new state
     */
    public void setState(State<World> newState) {
        if (newState == null) {
            return;
        }
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

    /** Updates the state machine, transitioning to the next state if conditions are met. */
    public void updateStates() {
        if (currentState == null) {
            return;
        }
        setState(currentState.getNextState(world));
        // Clear requested state after processing
        this.requestedState = null;
    }

    /** Calls the periodic function of the current state. */
    public void periodic() {
        if (currentState != null) {
            currentState._periodic(this, world);
        }
    }

    /**
     * Requests a state change to the specified state.
     *
     * @param state The requested state
     */
    public void requestState(State<World> state) {
        this.requestedState = state;
    }

    /**
     * Write a state machine configuration in graphviz format
     *
     * @param pw The PrintWriter to write to
     */
    public void writeGraphvizFile(PrintWriter pw) {
        pw.println("digraph {");
        pw.println();
        pw.println("  // Default Graphviz settings");
        pw.println();
        pw.println(
                "  rankdir=LR;\r\n"
                        + "\r\n"
                        + "  node [\r\n"
                        + "    shape=box,\r\n"
                        + "    style=rounded\r\n"
                        + "  ];\r\n"
                        + "\r\n"
                        + "  edge [\r\n"
                        + "    fontsize=10\r\n"
                        + "  ];");
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
}
