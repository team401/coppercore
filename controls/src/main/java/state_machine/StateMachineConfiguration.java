package coppercore.controls;

import java.util.Map;
import java.util.Optional;

public class StateMachineConfiguration<State, Trigger> {
    private Map<State, StateConfiguration<State, Trigger>> stateConfigurations;

    public StateConfiguration configure(State source) {
        StateConfiguration<State, Trigger> configuration = stateConfigurations.get(source);
        if (configuration == null) {
            configuration = new StateConfiguration<>(source);
            stateConfigurations.put(source, configuration);
        }

        return configuration;
    }

    public Optional<StateConfiguration<State, Trigger>> getStateConfiguration(State source){
        Optional<StateConfiguration<State, Trigger>> configurationOptional = Optional.empty();

        if (stateConfigurations.containsKey(source)){
            StateConfiguration<State, Trigger> configuration = stateConfigurations.get(source);
            if (configuration != null){
                configurationOptional = Optional.of(configuration);
            }
        }

        return configurationOptional;
    }

    public Optional<Transition<State, Trigger>> getTransition(State state, Trigger trigger){
        Optional<Transition<State, Trigger>> transition = Optional.empty();
        Optional<StateConfiguration<State, Trigger>> configurationOptional = getStateConfiguration(state);

        if (configurationOptional.isPresent()){
            StateConfiguration<State, Trigger> configuration = configurationOptional.get();
            Optional<Transition<State, Trigger>> transitionOptional = configuration.getTransition(trigger);
            transition = transition.or(() -> transitionOptional);
        }

        return transition;
    }

    
}
