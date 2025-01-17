package coppercore.controls.state_machine;

import coppercore.controls.state_machine.state.StateConfiguration;
import coppercore.controls.state_machine.transition.Transition;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class StateMachineConfiguration<State, Trigger> {
    private final Map<State, StateConfiguration<State, Trigger>> stateConfigurations;
    private Consumer<Transition<State, Trigger>> onEntryAction;
    private Consumer<Transition<State, Trigger>> onExitAction;

    public StateMachineConfiguration() {
        // temp solution
        stateConfigurations = new HashMap<>();
    }

    /**
     * Starts configuration of a state returning a StateConfiguration and registers it for the
     * state.
     *
     * @param source
     * @return
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
     * @param source
     * @return
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
     * @param state
     * @param trigger
     * @return
     */
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

    /**
     * Set the default onEntry function.
     *
     * @param action
     */
    public StateMachineConfiguration<State, Trigger> configureDefaultOnEntryAction(
            Consumer<Transition<State, Trigger>> action) {
        this.onEntryAction = action;
        return this;
    }

    /** Set the default onExit function. */
    public StateMachineConfiguration<State, Trigger> configureDefaultOnExitAction(
            Consumer<Transition<State, Trigger>> action) {
        this.onExitAction = action;
        return this;
    }

    /**
     * Method used by statemachine to handle processing on entry of a state.
     *
     * @param transition
     */
    public void runOnEntry(Transition<State, Trigger> transition) {
        if (onEntryAction != null) {
            onEntryAction.accept(transition);
        }
    }

    /**
     * Method used by statemachine to handle processing on exiting of a state.
     *
     * @param transition
     */
    public void runOnExit(Transition<State, Trigger> transition) {
        if (onExitAction != null) {
            onExitAction.accept(transition);
        }
    }
}
