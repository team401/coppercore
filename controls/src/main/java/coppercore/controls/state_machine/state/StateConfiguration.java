package coppercore.controls.state_machine.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import coppercore.controls.state_machine.transition.ConditinalTransition;
import coppercore.controls.state_machine.transition.Transition;

/** Configures State Machine State behavior */
public class StateConfiguration<State, Trigger> {

    private List<Transition<State, Trigger>> transitions;
    private State source;
    private Consumer<Transition> onEntryAction;
    private Consumer<Transition> onExitAction;
    private boolean runDefaultEntryAction = true;
    private boolean runDefaultExitAction = true;

    public boolean hasEntryAction() {
        return (this.onEntryAction != null);
    }

    public boolean hasExitAction() {
        return (this.onExitAction != null);
    }

    /**
     * Creates configuration for how state machine behaves in given State
     *
     * @param source Start state
     */
    public StateConfiguration(State source) {
        this.source = source;
        // Temp solution
        transitions = new ArrayList<>();
    }

    /**
     * Create Transition between States
     *
     * @param trigger trigger event
     * @param destination end state
     * @return configuration
     */
    public StateConfiguration<State, Trigger> permit(Trigger trigger, State destination) {
        if (getFilteredTransition(trigger, true).isEmpty()) {
            transitions.add(new Transition<>(source, destination, trigger, false));
        }
        return this;
    }

    /**
     * Create Transistion between states without trigger the enter or exit functions.
     *
     * @param trigger trigger event
     * @param destination end state
     * @return configuration
     */
    public StateConfiguration<State, Trigger> permitInternal(Trigger trigger, State destination) {
        if (getFilteredTransition(trigger, true).isEmpty()) {
            transitions.add(new Transition<>(source, destination, trigger, true));
        }
        return this;
    }

    /**
     * Creates a Conditional Transition that only fires if both the right Trigger is fired and the
     * check lambda evaluates to true.
     *
     * @param trigger trigger event
     * @param destination end state
     * @param check condition
     * @return configuration
     */
    public StateConfiguration<State, Trigger> permitIf(
            Trigger trigger, State destination, BooleanSupplier check) {
        if (getFilteredTransition(trigger, true, true).isEmpty()) {
            transitions.add(new ConditinalTransition<>(source, destination, trigger, check, false));
        }
        return this;
    }

    /**
     * Creates a Conditional Internal Transition that only fires if both the right Trigger is fired
     * and the check lambda evaluates to true. This transition will not trigger the enter or exit
     * functions.
     *
     * @param trigger trigger event
     * @param destination end state
     * @param check condition
     * @return configuration
     */
    public StateConfiguration<State, Trigger> permitInternalIf(
            Trigger trigger, State destination, BooleanSupplier check) {
        if (getFilteredTransition(trigger, true, true).isEmpty()) {
            transitions.add(new ConditinalTransition<>(source, destination, trigger, check, true));
        }
        return this;
    }

    /**
     * Returns Transitions that use the trigger event
     *
     * @param trigger trigger event
     * @return list of transitions
     */
    public List<Transition<State, Trigger>> getTransitions(Trigger trigger) {
        List<Transition<State, Trigger>> matchedTransitions = new ArrayList<>();
        if (trigger == null) return matchedTransitions;
        for (Transition<State, Trigger> transition : transitions) {
            if (trigger.equals(transition.getTrigger())) {
                matchedTransitions.add(transition);
            }
        }
        return matchedTransitions;
    }

    /**
     * Filters Transitions
     *
     * @param transitions list of transtions
     * @return filtered transition
     */
    private Optional<Transition<State, Trigger>> filterTransitions(
            List<Transition<State, Trigger>> transitions, boolean excludeConditionals, boolean excludeNormal) {
        Optional<Transition<State, Trigger>> returnOptional = Optional.empty();
        boolean conditinal = false;
        for (Transition<State, Trigger> transition : transitions) {
            if (transition instanceof ConditinalTransition) {
                if (excludeConditionals){
                    continue;
                }
                if (conditinal
                        && ((ConditinalTransition<State, Trigger>) transition).isCheckTrue()) {
                    return Optional.empty();
                } else if (((ConditinalTransition<State, Trigger>) transition).isCheckTrue()) {
                    returnOptional = Optional.of(transition);
                    conditinal = true;
                }
            } else if (!conditinal) {
                if (excludeNormal){
                    continue;
                }
                returnOptional = Optional.of(transition);
            }
        }
        return returnOptional;
    }




    /**
     * Gets filtered transition
     *
     * @param trigger trigger event
     * @return transition
     */
    public Optional<Transition<State, Trigger>> getFilteredTransition(Trigger trigger) {
        return filterTransitions(getTransitions(trigger), false, false);
    }

    /**
     * Gets filtered transition
     *
     * @param trigger trigger event
     * @return transition
     */
    public Optional<Transition<State, Trigger>> getFilteredTransition(Trigger trigger, boolean excludeConditionals) {
        return filterTransitions(getTransitions(trigger), excludeConditionals, false);
    }

    /**
     * Gets filtered transition
     *
     * @param trigger trigger event
     * @return transition
     */
    public Optional<Transition<State, Trigger>> getFilteredTransition(Trigger trigger, boolean excludeConditionals, boolean excludeNormal) {
        return filterTransitions(getTransitions(trigger), excludeConditionals, excludeNormal);
    }

    /**
     * Runs on entry event
     *
     * @param transition trigger event
     */
    public void runOnEntry(Transition transition) {
        if (onEntryAction != null) {
            onEntryAction.accept(transition);
        }
    }

    /**
     * Runs on exit event
     *
     * @param transition trigger event
     */
    public void runOnExit(Transition transition) {
        if (onExitAction != null) {
            onExitAction.accept(transition);
        }
    }

    /**
     * Disables default on entry action
     *
     * @return configuration
     */
    public StateConfiguration<State, Trigger> disableDefualtOnEntry() {
        this.runDefaultEntryAction = false;
        return this;
    }

    /**
     * Disables default on exit action
     *
     * @return configuration
     */
    public StateConfiguration<State, Trigger> disableDefualtOnExit() {
        this.runDefaultExitAction = false;
        return this;
    }

    /**
     * Sets on entry action
     *
     * @param action Action to run onEntry
     * @return configuration
     */
    public StateConfiguration<State, Trigger> configureOnEntryAction(Consumer<Transition> action) {
        this.onEntryAction = action;
        return this;
    }

    /**
     * Sets on exit action
     *
     * @param action Action to run onExit
     * @return configuration
     */
    public StateConfiguration<State, Trigger> configureOnExitAction(Consumer<Transition> action) {
        this.onExitAction = action;
        return this;
    }

    /**
     * Returns if in this State the default onEntry Action should run.
     *
     * @return do run
     */
    public boolean doRunDefaultEntryAction() {
        return runDefaultEntryAction;
    }

    /**
     * Returns if in this State the default onExit Action should run.
     *
     * @return do run
     */
    public boolean doRunDefaultExitAction() {
        return runDefaultExitAction;
    }
}
