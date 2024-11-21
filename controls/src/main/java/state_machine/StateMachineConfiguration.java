package coppercore.controls;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class StateMachineConfiguration<State, Trigger> {
    private Map<State, StateConfiguration<State, Trigger>> stateConfigurations;
    private BiConsumer<State, Transition> onEntryAction;
    private BiConsumer<State, Transition> onExitAction;
    private BiConsumer<State, Transition> transitionAction;

    public StateMachineConfiguration() {
        // temp solution
        stateConfigurations = new HashMap<>();
    }

    public StateConfiguration<State, Trigger> configure(State source) {
        StateConfiguration<State, Trigger> configuration = stateConfigurations.get(source);
        if (configuration == null) {
            configuration = new StateConfiguration<>(source);
            stateConfigurations.put(source, configuration);
        }

        return configuration;
    }

    public Optional<StateConfiguration<State, Trigger>> getStateConfiguration(State source) {
        Optional<StateConfiguration<State, Trigger>> configurationOptional = Optional.empty();

        if (stateConfigurations.containsKey(source)) {
            StateConfiguration<State, Trigger> configuration = stateConfigurations.get(source);
            if (configuration != null) {
                configurationOptional = Optional.of(configuration);
            }
        }

        return configurationOptional;
    }

    public Optional<Transition<State, Trigger>> getTransition(State state, Trigger trigger) {
        Optional<Transition<State, Trigger>> transition = Optional.empty();
        Optional<StateConfiguration<State, Trigger>> configurationOptional =
                getStateConfiguration(state);

        if (configurationOptional.isPresent()) {
            StateConfiguration<State, Trigger> configuration = configurationOptional.get();
            Optional<Transition<State, Trigger>> transitionOptional =
                    configuration.getFilteredTransition(trigger);
            transition = transition.or(() -> transitionOptional);
        }

        return transition;
    }

    public StateMachineConfiguration<State, Trigger> configureDefaultOnEntryAction(BiConsumer<State, Transition> action){
        this.onEntryAction = action;
        return this;
    }

    public StateMachineConfiguration<State, Trigger> configureDefaultOnExitAction(BiConsumer<State, Transition> action){
        this.onExitAction = action;
        return this;
    }

    public StateMachineConfiguration<State, Trigger> configureDefaultTransitionAction(BiConsumer<State, Transition> action){
        this.transitionAction = action;
        return this;
    }
}
