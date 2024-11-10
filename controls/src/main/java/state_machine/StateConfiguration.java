package coppercore.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class StateConfiguration<State, Trigger> {

    private List<Transition<State, Trigger>> transitions;
    private State source;
    private Consumer<Transition> onEntry;
    private Consumer<Transition> onExit;

    public StateConfiguration(State source) {
        this.source = source;
        // Temp solution
        transitions = new ArrayList<Transition<State, Trigger>>();
    }

    public StateConfiguration<State, Trigger> permit(Trigger trigger, State destination) {
        if (getFilteredTransition(trigger).isEmpty()) {
            transitions.add(new Transition<>(source, destination, trigger, false));
        }
        return this;
    }

    public StateConfiguration<State, Trigger> permitInternal(Trigger trigger, State destination) {
        if (getFilteredTransition(trigger).isEmpty()) {
            transitions.add(new Transition<>(source, destination, trigger, true));
        }
        return this;
    }

    public StateConfiguration<State, Trigger> permitIf(
            Trigger trigger, State destination, BooleanSupplier check) {
        if (getFilteredTransition(trigger).isEmpty()) {
            transitions.add(new ConditinalTransition<>(source, destination, trigger, check, false));
        }
        return this;
    }

    public StateConfiguration<State, Trigger> permitInternalIf(
            Trigger trigger, State destination, BooleanSupplier check) {
        if (getFilteredTransition(trigger).isEmpty()) {
            transitions.add(new ConditinalTransition<>(source, destination, trigger, check, true));
        }
        return this;
    }

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

    private Optional<Transition<State, Trigger>> filterTransitions(
            List<Transition<State, Trigger>> transitions) {
        Optional<Transition<State, Trigger>> returnOptional = Optional.empty();
        boolean conditinal = false;
        for (Transition<State, Trigger> transition : transitions) {
            if (transition instanceof ConditinalTransition) {
                if (conditinal
                        && ((ConditinalTransition<State, Trigger>) transition).isCheckTrue()) {
                    return Optional.empty();
                } else {
                    returnOptional = Optional.of(transition);
                    conditinal = true;
                }
            } else if (!conditinal) {
                returnOptional = Optional.of(transition);
            }
        }
        return returnOptional;
    }

    public Optional<Transition<State, Trigger>> getFilteredTransition(Trigger trigger) {
        return filterTransitions(getTransitions(trigger));
    }

    public void runOnEntry(Transition transition) {
        if (onEntry != null) {
            onEntry.accept(transition);
        }
    }

    public void runOnExit(Transition transition) {
        if (onExit != null) {
            onExit.accept(transition);
        }
    }
}
