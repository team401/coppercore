package coppercore.controls.state_machine.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import coppercore.controls.state_machine.transition.TransitionBase;
import coppercore.controls.state_machine.transition.transitions.ConditionalTransition;
import coppercore.controls.state_machine.transition.transitions.Transition;

/** Configures State Machine State behavior */
public class StateConfiguration<State, Trigger> {

    private List<TransitionBase<State, Trigger>> transitions;
    private final State source;

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
            Transition<State, Trigger> transition = new Transition<>(source, destination, trigger);
            transitions.add(transition);
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
            Transition<State, Trigger> transition = new Transition<>(source, destination, trigger);
            transition.disableOnEntry();
            transition.disableOnExit();
            transitions.add(transition);
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
            ConditionalTransition<State, Trigger> transition = new ConditionalTransition<>(source, destination, trigger, check);
            transitions.add(transition);
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
            ConditionalTransition<State, Trigger> transition = new ConditionalTransition<>(source, destination, trigger, check);
            transition.disableOnEntry();
            transition.disableOnExit();
            transitions.add(transition);
        }
        return this;
    }

    /**
     * Returns Transitions that use the trigger event
     *
     * @param trigger trigger event
     * @return list of transitions
     */
    public List<TransitionBase<State, Trigger>> getTransitions(Trigger trigger) {
        List<TransitionBase<State, Trigger>> matchedTransitions = new ArrayList<>();
        if (trigger == null) return matchedTransitions;
        for (TransitionBase<State, Trigger> transition : transitions) {
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
    private Optional<TransitionBase<State, Trigger>> filterTransitions(
            List<TransitionBase<State, Trigger>> transitions, boolean excludeConditionals, boolean excludeNormal) {
        Optional<TransitionBase<State, Trigger>> returnOptional = Optional.empty();
        boolean conditinal = false;
        for (TransitionBase<State, Trigger> transition : transitions) {
            if (transition instanceof ConditionalTransition) {
                if (excludeConditionals){
                    continue;
                }
                if (conditinal
                        && ((ConditionalTransition<State, Trigger>) transition).isCheckTrue()) {
                    return Optional.empty();
                } else if (((ConditionalTransition<State, Trigger>) transition).isCheckTrue()) {
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
    public Optional<TransitionBase<State, Trigger>> getFilteredTransition(Trigger trigger) {
        return filterTransitions(getTransitions(trigger), false, false);
    }

    /**
     * Gets filtered transition
     *
     * @param trigger trigger event
     * @return transition
     */
    public Optional<TransitionBase<State, Trigger>> getFilteredTransition(Trigger trigger, boolean excludeConditionals) {
        return filterTransitions(getTransitions(trigger), excludeConditionals, false);
    }

    /**
     * Gets filtered transition
     *
     * @param trigger trigger event
     * @return transition
     */
    public Optional<TransitionBase<State, Trigger>> getFilteredTransition(Trigger trigger, boolean excludeConditionals, boolean excludeNormal) {
        return filterTransitions(getTransitions(trigger), excludeConditionals, excludeNormal);
    }
    
}
