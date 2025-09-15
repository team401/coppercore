package coppercore.controls.state_machine;

import coppercore.controls.state_machine.state.StateConfiguration;
import coppercore.controls.state_machine.transition.TransitionBase;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Object to configure State Machine */
public class StateMachineConfiguration<State, Trigger> {
    private final Map<State, StateConfiguration<State, Trigger>> stateConfigurations;

    /** Creates StateMachineConfiuration Object */
    public StateMachineConfiguration() {
        // temp solution
        stateConfigurations = new HashMap<>();
    }

    /**
     * Starts configuration of a state returning a StateConfiguration and registers it for the
     * state.
     *
     * @param source Source State
     * @return configuration
     */
    public StateConfiguration<State, Trigger> configure(State source) {
        StateConfiguration<State, Trigger> configuration = stateConfigurations.get(source);
        if (configuration == null) {
            configuration = new StateConfiguration<>(source);
            stateConfigurations.put(source, configuration);
        }
        return configuration;
    }

    /**
     * Gets a StateConfiguration specified by State
     *
     * @param source Source State
     * @return Optional configuration
     */
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

    /**
     * Gets a Transition defined by State and Trigger
     *
     * @param state Start state
     * @param trigger Trigger event
     * @return Optional of Transition
     */
    public Optional<TransitionBase<State, Trigger>> getTransition(State state, Trigger trigger) {
        Optional<TransitionBase<State, Trigger>> transition = Optional.empty();
        Optional<StateConfiguration<State, Trigger>> configurationOptional =
                getStateConfiguration(state);

        if (configurationOptional.isPresent()) {
            StateConfiguration<State, Trigger> configuration = configurationOptional.get();
            Optional<TransitionBase<State, Trigger>> transitionOptional =
                    configuration.getFilteredTransition(trigger);
            transition = transition.or(() -> transitionOptional);
        }

        return transition;
    }
}
