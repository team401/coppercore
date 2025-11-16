package coppercore.controls.state_machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class StateMachine<
        StateEnumType extends Enum<StateEnumType> & State<StateEnumType, World>, World> {
    private Optional<StateEnumType> currentState = Optional.<StateEnumType>empty();
    private Optional<StateEnumType> requestedState = Optional.<StateEnumType>empty();
    private Map<StateEnumType, List<Transition>> transitionsByState = new HashMap<>();
    private Map<StateEnumType, Boolean> hasFinished = new HashMap<>();
    private World world;

    public StateMachine(Class<StateEnumType> stateEnum, World world) {
        this.world = world;
        for (var state : stateEnum.getEnumConstants()) {
            transitionsByState.put(state, new ArrayList<Transition>());
        }
    }

    public State getCurrentState() {
        return currentState.orElse(null);
    }

    public void periodic() {
        currentState.ifPresent(
                (state) -> {
                    if (state.periodic(this, world)) {
                        hasFinished.put(state, true);
                    }
                });
    }

    // alternative way to finish
    public void finish(StateEnumType state) {
        hasFinished.put(state, true);
    }

    public void requestTransitionTo(StateEnumType state) {
        Objects.requireNonNull(state);
        requestedState = Optional.<StateEnumType>of(state);
    }

    public void setState(StateEnumType newState) {
        Objects.requireNonNull(newState);
        currentState.ifPresent(
                (oldState) -> {
                    oldState.onExit(this, world);
                    requestedState = Optional.<StateEnumType>empty();
                });
        currentState = Optional.<StateEnumType>of(newState);
        hasFinished.put(newState, false);
        newState.onEntry(this, world);
    }

    public void updateStates() {
        currentState.ifPresent(
                (state) -> {
                    for (var transition : transitionsByState.get(state)) {
                        if (transition.whenCondition.isFulfilledFor(world)) {
                            setState(transition.toState);
                            break;
                        }
                    }
                });
    }

    public TransitionFrom transitionFrom(StateEnumType state) {
        return new TransitionFrom(state);
    }

    public class TransitionFrom {
        StateEnumType fromState;

        TransitionFrom(StateEnumType fromState) {
            this.fromState = fromState;
        }

        public Transition to(StateEnumType toState) {
            return new Transition(fromState, toState);
        }
    }

    public class Transition {
        StateEnumType fromState, toState;
        Condition<World> whenCondition;

        Transition(StateEnumType fromState, StateEnumType toState) {
            this.fromState = fromState;
            this.toState = toState;
            transitionsByState.get(fromState).add(this);
        }

        public void when(Condition<World> whenCondition) {
            if (this.whenCondition != null) throw new Error("Cannot chain when conditions");
            this.whenCondition = whenCondition;
        }

        public void whenFinished() {
            when((world) -> hasFinished.get(fromState));
        }

        public void whenRequested() {
            when((world) -> requestedState.isPresent() && requestedState.get() == toState);
        }
    }

    @FunctionalInterface
    public interface Condition<World> {
        public boolean isFulfilledFor(World world);
    }
}
