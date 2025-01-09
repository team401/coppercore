package coppercore.controls.state_machine.state;

import coppercore.controls.state_machine.transition.ConditinalTransition;
import coppercore.controls.state_machine.transition.Transition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class StateConfiguration<State, Trigger> {

    private List<Transition<State, Trigger>> transitions;
    private State source;
    private Consumer<Transition> onEntryAction;
    private Consumer<Transition> onExitAction;
    private Consumer<Transition> transitionAction;
    private boolean runDefaultTransitionAction = true;
    private boolean runDefaultEntryAction = true;
    private boolean runDefaultExitAction = true;

    public StateConfiguration(State source) {
        this.source = source;
        // Temp solution
        transitions = new ArrayList<>();
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
        if (onEntryAction != null) {
            onEntryAction.accept(transition);
        }
    }

    public void runOnExit(Transition transition) {
        if (onExitAction != null) {
            onExitAction.accept(transition);
        }
    }

    public void runTransition(Transition transition) {
        if (transitionAction != null) {
            transitionAction.accept(transition);
        }
    }

    public StateConfiguration<State, Trigger> disableDefaultTransitionAction() {
        this.runDefaultTransitionAction = false;
        return this;
    }

    public StateConfiguration<State, Trigger> disableDefualtOnEntry() {
        this.runDefaultEntryAction = false;
        return this;
    }

    public StateConfiguration<State, Trigger> disableDefualtOnExit() {
        this.runDefaultExitAction = false;
        return this;
    }

    public StateConfiguration<State, Trigger> configureOnEntryAction(Consumer<Transition> action) {
        this.onEntryAction = action;
        return this;
    }

    public StateConfiguration<State, Trigger> configureOnExitAction(Consumer<Transition> action) {
        this.onExitAction = action;
        return this;
    }

    public StateConfiguration<State, Trigger> configureTransitionAction(
            Consumer<Transition> action) {
        this.transitionAction = action;
        return this;
    }

    public boolean doRunDefaultEntryAction() {
        return runDefaultEntryAction;
    }

    public boolean doRunDefaultExitAction() {
        return runDefaultExitAction;
    }

    public boolean doRunDefaultTransitionAction() {
        return runDefaultTransitionAction;
    }
}
