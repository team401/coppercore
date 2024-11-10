package coppercore.controls;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class StateConfiguration<State, Trigger> {

    private List<Transition<State, Trigger>> transitions;
    private State source;
    private Consumer<Transition> onEntry;
    private Consumer<Transition> onExit;

    public StateConfiguration(State source) {
        this.source = source;
    }

    public StateConfiguration permit(Trigger trigger, State destination){
        if (getTransition(trigger).isEmpty()){
            transitions.add(new Transition<State, Trigger>(source, destination, trigger));
        }
        return this;
    }

    public Optional<Transition<State, Trigger>> getTransition(Trigger trigger){
        Optional<Transition<State, Trigger>> transitionOptional = Optional.empty();
        if (trigger == null) return transitionOptional;
        for (Transition<State, Trigger> transition : transitions) {
            if (trigger.equals(transition.getDestination())){
                transitionOptional = transitionOptional.or(() -> Optional.of(transition));
            }
        }
        return transitionOptional;
    }

    public void runOnEntry(Transition transition){
        if (onEntry != null){
            onEntry.accept(transition);
        }
    }

    public void runOnExit(Transition transition){
        if (onExit != null){
            onExit.accept(transition);
        }
    }
}
