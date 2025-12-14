package coppercore.controls.state_machine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

// TODO: Add missing javadocs

/** An abstract class representing a state in a state machine. */
public abstract class State<World> {

    protected boolean finished = false;
    protected final List<Transition> transitions;
    protected Supplier<State<World>> requestedStateSupplier;
    protected String name;

    /**
     * This constructor sets the name of the state to the simple name of the class. It throws an
     * IllegalStateException if the class is anonymous, to enforce that anonymous classes must use
     * the constructor that takes a name parameter.
     */
    public State() {
        // Ensure that this constructor does not work if the class is anonymous?
        if (this.getClass().isAnonymousClass()) {
            throw new IllegalStateException(
                    "Cannot use zero-argument constructor for anonymous classes, use State(String"
                            + " name) instead");
        }
        this.name = this.getClass().getSimpleName();
        this.transitions = new ArrayList<>();
    }

    /**
     * Constructs a new State with the given name.
     *
     * @param name The name of the state
     */
    public State(String name) {
        this.name = name;
        this.transitions = new ArrayList<>();
    }

    /**
     * Sets the supplier for the requested state.
     *
     * @param supplier The supplier that provides the requested state
     */
    public void setRequestedStateSupplier(Supplier<State<World>> supplier) {
        this.requestedStateSupplier = supplier;
    }

    /**
     * Checks if the state has finished its operation. The finished state can be used in transition
     * conditions.
     *
     * @return true if the state is finished, false otherwise
     */
    protected final boolean isFinished() {
        return finished;
    }

    /**
     * Determines the next state based on the defined transitions. Multiple transitions may be
     * defined; the first whose condition is true is taken.
     *
     * @return The StateKey of the next state, or null if no transition is taken
     */
    protected final State<World> getNextState(World world) {
        for (Transition transition : transitions) {
            if (transition.whenCondition.test(world)) {
                return transition.toState;
            }
        }
        return null;
    }

    /**
     * Called when the state is entered. This is a internal method; use onEntry() to override
     * behavior.
     */
    protected final void _onEntry(StateMachine<World> stateMachine, World world) {
        finished = false;
        onEntry(stateMachine, world);
    }

    /**
     * Called when the state is exited. This is a internal method; use onExit() to override
     * behavior.
     */
    protected final void _onExit(StateMachine<World> stateMachine, World world) {
        onExit(stateMachine, world);
    }

    /**
     * Called when the state is finished. This is a internal method; use onFinish() to override
     * behavior.
     */
    protected final void _onFinish() {
        onFinish();
    }

    /**
     * Called periodically while in this state. This is a internal method; use periodic() to
     * override behavior.
     */
    protected final void _periodic(StateMachine<World> stateMachine, World world) {
        periodic(stateMachine, world);
    }

    /**
     * Marks the state as finished. This is different from requesting a transition to another state.
     * Because of this, calling finish() does not automatically cause a state transition. And the
     * state machine will only transition to another state if a transition condition is met, and the
     * state machine is updated. But it can be used in conjunction with transitionWhenFinished() to
     * trigger a transition. And it can be used to indicate that the state's work is done. When
     * finish() is called, the onFinish() method is also called.
     */
    protected final void finish() {
        finished = true;
        _onFinish();
    }

    /**
     * Called when the state is entered. This method is called after a state transition. So it is
     * the first chance to perform any setup or initialization for this state.
     */
    protected void onEntry(StateMachine<World> stateMachine, World world) {}

    /**
     * Called when the state is exited. This method is called before a state transition. So it is
     * the last chance to perform any cleanup or final actions in this state.
     */
    protected void onExit(StateMachine<World> stateMachine, World world) {}

    /**
     * Called when the state is finished. This method is called when finish() is invoked. So it is
     * not guaranteed to be called before a state transition.
     */
    protected void onFinish() {}

    /**
     * Called periodically while in this state. This method is called by the state machine's
     * periodic update.
     */
    protected abstract void periodic(StateMachine<World> stateMachine, World world);

    /** A class representing a transition from one state to another. */
    public class Transition {
        State<World> toState;
        Predicate<World> whenCondition;
        String description;

        /**
         * Constructor for Transition.
         *
         * @param toState The target state for the transition
         * @param whenCondition The condition under which the transition occurs
         * @param description A description of the transition
         */
        Transition(State<World> toState, Predicate<World> whenCondition, String description) {
            this.toState = toState;
            this.whenCondition = whenCondition;
            this.description = description;
        }
    }

    /** A builder class for defining transition conditions and target states. */
    public class TransitionConditionBuilder {
        Predicate<World> condition;
        // Need to make this more descriptive
        String description = "";

        /**
         * Constructor for TransitionConditionBuilder.
         *
         * @param condition The condition for the transition
         * @param description A description for the transition condition
         */
        TransitionConditionBuilder(Predicate<World> condition, String description) {
            this.condition = condition;
            this.description = description;
        }

        /**
         * Combines the current condition with another condition using logical AND.
         *
         * @param nextCondition The next condition to combine
         * @param label A label for the next condition
         * @return The updated TransitionConditionBuilder
         */
        public TransitionConditionBuilder andWhen(Predicate<World> nextCondition, String label) {
            this.condition = this.condition.and(nextCondition);
            this.description += " && " + label;
            return this;
        }

        /**
         * Defines the target state for the transition.
         *
         * @param toState The state to transition to
         */
        public void transitionTo(State<World> toState) {
            transitions.add(new Transition(toState, condition, description));
        }
    }

    /**
     * Creates a transition condition builder with the given condition.
     *
     * @param condition The condition for the transition
     * @param description A description for the transition condition
     * @return The transition condition builder
     */
    public TransitionConditionBuilder when(Predicate<World> condition, String description) {
        return new TransitionConditionBuilder(condition, description);
    }

    /**
     * Creates a transition condition builder that triggers when the state is finished.
     *
     * @param description A description for the transition condition
     * @return The transition condition builder
     */
    public TransitionConditionBuilder whenFinished(String description) {
        return when((world) -> isFinished(), description);
    }

    /**
     * Creates a transition condition builder that triggers when the state is finished.
     *
     * @return The transition condition builder
     */
    public TransitionConditionBuilder whenFinished() {
        return whenFinished("When " + this.name + " finished");
    }

    /**
     * Creates a transition condition builder that triggers when the specified state is requested.
     *
     * @param requestedState The requested state to check for
     * @param description A description for the transition condition
     * @return The transition condition builder
     */
    public TransitionConditionBuilder whenRequested(
            State<World> requestedState, String description) {
        return when(
                (world) ->
                        requestedState != null
                                && this.requestedStateSupplier != null
                                && requestedState.equals(this.requestedStateSupplier.get()),
                description);
    }

    /**
     * Creates a transition condition builder that triggers when the specified state is requested.
     *
     * @param requestedState The requested state to check for
     * @return The transition condition builder
     */
    public TransitionConditionBuilder whenRequested(State<World> requestedState) {
        return whenRequested(requestedState, "When " + requestedState.name + " requested");
    }

    /**
     * Gets the list of transitions defined for this state.
     *
     * @return The list of transitions
     */
    protected List<Transition> getTransitions() {
        return transitions;
    }

    /**
     * Gets the name of the state.
     *
     * @return The name of the state
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a string representation of the state.
     *
     * @return A string representing the state
     */
    @Override
    public String toString() {
        // Maybe change to include more info later
        return name;
    }
}
