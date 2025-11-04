package coppercore.controls.state_machine.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import coppercore.controls.state_machine.transition.TransitionBase;
import coppercore.controls.state_machine.transition.transitions.ConditionalTransition;
import coppercore.controls.state_machine.transition.transitions.Transition;

/** Configures State Machine State behavior */
public class StateConfiguration<State, Trigger> {

    private final List<TransitionBase<State, Trigger>> transitions;
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

    public StateConfiguration<State, Trigger> permit(Transition<State, Trigger> transition) {
        if (!source.equals(transition.getSource())) {
            throw new RuntimeException(
                    "Transition Source must Be same as transition is permitted from");
        }
        if (Objects.isNull(transition.getDestination())) {
            throw new RuntimeException("Transition Destination must not be null");
        }
        if (Objects.isNull(transition.getTrigger())) {
            throw new RuntimeException("Transition Trigger must not be null");
        }
        transitions.add(transition);
        return this;
    }

    /**
     * Create Transition between States
     *
     * @param trigger trigger event
     * @param destination end state
     * @return configuration
     */
    public StateConfiguration<State, Trigger> permit(Trigger trigger, State destination) {
        TransitionBase<State, Trigger> transition = new Transition<>(source, destination, trigger);
        transitions.add(transition);
        return this;
    }

    /**
     * Create Transition between states without trigger the enter or exit functions.
     *
     * @param trigger trigger event
     * @param destination end state
     * @return configuration
     */
    public StateConfiguration<State, Trigger> permitInternal(Trigger trigger, State destination) {
        TransitionBase<State, Trigger> transition =
                new Transition<>(source, destination, trigger).disableOnEntry().disableOnExit();
        transitions.add(transition);
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
        TransitionBase<State, Trigger> transition =
                new ConditionalTransition<>(source, destination, trigger, check);
        transitions.add(transition);
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
        TransitionBase<State, Trigger> transition =
                new ConditionalTransition<>(source, destination, trigger, check)
                        .disableOnEntry()
                        .disableOnExit();
        transitions.add(transition);
        return this;
    }

    /**
     * Returns Transitions that use the trigger event
     *
     * @param trigger trigger event
     * @return list of transitions
     */
    public List<TransitionBase<State, Trigger>> getTransitions(Trigger trigger) {
        if (Objects.isNull(trigger)) return new ArrayList<>();
        return transitions.stream()
                .filter(
                        (TransitionBase<State, Trigger> transition) ->
                                trigger.equals(transition.getTrigger()))
                .toList();
    }

    /**
     * Filters Transitions
     *
     * @param transitions list of transitions
     * @return filtered transition
     */
    private Optional<TransitionBase<State, Trigger>> filterTransitions(
            List<TransitionBase<State, Trigger>> transitions) {
        Optional<TransitionBase<State, Trigger>> returnOptional = Optional.empty();
        int highest_priority = Integer.MIN_VALUE;
        for (TransitionBase<State, Trigger> transition : transitions) {
            int priority = transition.getPriority();
            if (priority > highest_priority && transition.canTransition()) {
                highest_priority = priority;
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
        return filterTransitions(getTransitions(trigger));
    }
}
