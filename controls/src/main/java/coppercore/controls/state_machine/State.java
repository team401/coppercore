package coppercore.controls.state_machine;

import static edu.wpi.first.units.Units.Seconds;

import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

// TODO: Add missing javadocs

/**
 * An abstract class representing a state in a state machine.
 *
 * <p>It supports defining transitions to other states based on conditions, as well as handling
 * entry, exit, finish, and periodic behavior.
 *
 * <p>If a state is marked as finished, it can be used in transition conditions. When a state is
 * entered, its onEntry method is called. When a state is exited, its onExit method is called. When
 * a state is finished, its onFinish method is called. The periodic method is called periodically
 * while in this state. Transitions can be defined using conditions, including timeouts and
 * requested transitions.
 *
 * <p>Each state has a name for identification. If the state is anonymous, it must be given a name
 * using the constructor that takes a name parameter. Otherwise, the simple name of the class is
 * used as the state's name. Currently there is no checking for duplicate state names.
 *
 * @param <World> The type of the world in which this state lives.
 */
public abstract class State<World> {

    /** Flag that indicates whether the state has finished its operation */
    protected boolean finished = false;

    /** List of transitions for the state */
    protected final List<Transition> transitions;

    /** Name of the state. Is used as identifier in State Machine. */
    protected final String name;

    private Optional<Timer> timer = Optional.empty();
    private double timeout;
    private Supplier<State<World>> requestedStateSupplier = () -> null;

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
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("State name cannot be null or empty");
        }
        this.name = name;
        this.transitions = new ArrayList<>();
    }

    /**
     * Sets the supplier for the requested state.
     *
     * @param supplier The supplier that provides the requested state
     */
    protected void setRequestedStateSupplier(Supplier<State<World>> supplier) {
        // Prevent null supplier
        Objects.requireNonNull(supplier, "Requested state supplier cannot be null");
        this.requestedStateSupplier = supplier;
    }

    /**
     * Get the supplier for the requested state.
     *
     * @return supplier
     */
    protected Supplier<State<World>> getRequestedStateSupplier() {
        return this.requestedStateSupplier;
    }

    /**
     * Get the requested state.
     *
     * @return requested state
     */
    protected State<World> getRequestedState() {
        return this.requestedStateSupplier.get();
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
     * @param world The current world state
     * @return An optional containing the next state, or empty if no transition is taken
     */
    protected final Optional<State<World>> getNextState(World world) {
        for (Transition transition : transitions) {
            if (transition.whenCondition.test(world)) {
                return Optional.of(transition.toState);
            }
        }
        return Optional.empty();
    }

    /**
     * Called when the state is entered. This is an internal method; use onEntry() to override
     * behavior.
     *
     * @param stateMachine The state machine this state belongs to
     * @param world The current world state
     */
    protected final void _onEntry(StateMachine<World> stateMachine, World world) {
        finished = false;
        timer.ifPresent(t -> t.restart());
        onEntry(stateMachine, world);
    }

    /**
     * Called when the state is exited. This is an internal method; use onExit() to override
     * behavior.
     *
     * @param stateMachine The state machine this state belongs to
     * @param world The current world state
     */
    protected final void _onExit(StateMachine<World> stateMachine, World world) {
        onExit(stateMachine, world);
    }

    /**
     * Called when the state is finished. This is an internal method; use onFinish() to override
     * behavior.
     */
    protected final void _onFinish() {
        onFinish();
    }

    /**
     * Called periodically while in this state. This is an internal method; use periodic() to
     * override behavior.
     *
     * @param stateMachine The state machine this state belongs to
     * @param world The current world state
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
     * finish() is called, the onFinish() method is also called. If there are multiple calls to
     * finish, onFinish() is called only once.
     */
    protected final void finish() {
        if (!finished) {
            finished = true;
            _onFinish();
        }
    }

    /**
     * Called when the state is entered. This method is called after a state transition. So it is
     * the first chance to perform any setup or initialization for this state.
     *
     * @param stateMachine The state machine this state belongs to
     * @param world The current world state
     */
    protected void onEntry(StateMachine<World> stateMachine, World world) {}

    /**
     * Called when the state is exited. This method is called before a state transition. So it is
     * the last chance to perform any cleanup or final actions in this state.
     *
     * @param stateMachine The state machine this state belongs to
     * @param world The current world state
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
     *
     * @param stateMachine The state machine this state belongs to
     * @param world The current world state
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
            Objects.requireNonNull(condition, "Condition cannot be null");
            Objects.requireNonNull(description, "Description cannot be null");
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
            Objects.requireNonNull(nextCondition, "Next condition cannot be null");
            Objects.requireNonNull(label, "Label cannot be null");
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
            Objects.requireNonNull(toState, "Target state cannot be null");
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

    private boolean hasTimedOut() {
        return timer.map(t -> t.hasElapsed(timeout)).orElse(false);
    }

    /**
     * Creates a transition condition builder that triggers a transition upon a timeout.
     *
     * <p>Each State may be, optionally, associated with exactly one transition that fires on
     * timeout. The timeout is rearmed every time the state is entered.
     *
     * @param durationAfterInit timeout, duration starting when state is entered.
     * @return The transition condition builder
     */
    public TransitionConditionBuilder whenTimeout(Time durationAfterInit) {
        if (timer.isPresent())
            throw new UnsupportedOperationException(
                    "only one timeout transition per state supported.");
        timer = Optional.of(new Timer());
        timeout = durationAfterInit.in(Seconds);
        return when((world) -> hasTimedOut(), "timeout");
    }

    /**
     * Creates a transition condition builder that triggers when the specified state is requested.
     *
     * @param requestedState The requested state to check for
     * @param description A description for the transition condition
     */
    public void whenRequestedTransitionTo(State<World> requestedState, String description) {
        transitions.add(
                new Transition(
                        requestedState,
                        (world) -> requestedState.equals(getRequestedState()),
                        description));
    }

    /**
     * Creates a transition condition builder that triggers when the specified state is requested.
     *
     * @param requestedState The requested state to check for
     */
    public void whenRequestedTransitionTo(State<World> requestedState) {
        Objects.requireNonNull(requestedState, "Requested state cannot be null");
        whenRequestedTransitionTo(requestedState, "When " + requestedState.name + " requested");
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
