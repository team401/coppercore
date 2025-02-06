package coppercore.controls.state_machine;

import coppercore.controls.state_machine.state.PeriodicStateInterface;
import coppercore.controls.state_machine.state.StateConfiguration;
import coppercore.controls.state_machine.state.StateContainer;
import coppercore.controls.state_machine.state.StateInterface;
import coppercore.controls.state_machine.transition.Transition;
import coppercore.controls.state_machine.transition.TransitionInfo;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringPublisher;
import java.util.Optional;

/** Generic State Machine */
public class StateMachine<State extends Enum, Trigger extends Enum> {
    private final StateMachineConfiguration<State, Trigger> configuration;
    private TransitionInfo<State, Trigger> transitionInfo;
    private State currentState;

    private boolean debugging;

    private StringPublisher statePublisher;
    private StringPublisher eventPublisher;

    public StateMachine(StateMachineConfiguration<State, Trigger> config, State initialState) {
        this(config, initialState, false);
    }

    /**
     * Creates a StateMachine in the given state with the given configuration
     *
     * @param config The state machine configuration
     * @param initialState default state
     */
    public StateMachine(
            StateMachineConfiguration<State, Trigger> config,
            State initialState,
            boolean debugging) {
        configuration = config;
        currentState = initialState;
        this.debugging = debugging;
        if (debugging) {
            NetworkTableInstance inst = NetworkTableInstance.getDefault();
            NetworkTable table = inst.getTable("StateMachine");
            statePublisher = table.getStringTopic("State").publish();
            eventPublisher = table.getStringTopic("Event").publish();
            statePublisher.set(initialState.name());
        }
    }

    /**
     * Method to transition States based on given trigger
     *
     * @param trigger Trigger event to run
     */
    public void fire(Trigger trigger) {
        transitionInfo = new TransitionInfo<>(currentState, trigger);
        Optional<Transition<State, Trigger>> transitionOptional =
                configuration.getTransition(currentState, trigger);
        if (transitionOptional.isEmpty()) {
            transitionInfo.fail();
            return;
        }
        Transition<State, Trigger> transition = transitionOptional.get();
        if (!transition.canTransition()) {
            transitionInfo.fail();
            return;
        }
        transitionInfo.setTransition(transition);
        if (!transition.isInternal()) {
            Optional<StateConfiguration<State, Trigger>> currentStateConfigurationOptional =
                    configuration.getStateConfiguration(currentState);
            Optional<StateConfiguration<State, Trigger>> nextStateConfigurationOptional =
                    configuration.getStateConfiguration(transition.getDestination());
            if (currentStateConfigurationOptional.isPresent()) {
                StateConfiguration<State, Trigger> config = currentStateConfigurationOptional.get();
                if (config.doRunDefaultExitAction()) {
                    configuration.runOnExit(transition);
                } else {
                    config.runOnExit(transition);
                }
            } else {
                configuration.runOnExit(transition);
            }
            transition.runAction();
            if (nextStateConfigurationOptional.isPresent()) {
                StateConfiguration<State, Trigger> config = nextStateConfigurationOptional.get();
                if (config.doRunDefaultExitAction()) {
                    configuration.runOnEntry(transition);
                } else {
                    config.runOnEntry(transition);
                }
            } else {
                configuration.runOnEntry(transition);
            }
        }
        currentState = transition.getDestination();
        if (debugging) {
            statePublisher.set(currentState.name());
            eventPublisher.set(trigger.name());
        }
    }

    /**
     * Returns current state
     *
     * @return current state
     */
    public State getCurrentState() {
        return currentState;
    }

    /** Runs states Period if is periodic */
    public void periodic() {
        if (currentState instanceof PeriodicStateInterface) {
            ((PeriodicStateInterface) currentState).periodic();
        } else {
            periodicContainer();
        }
    }

    /** Runs states Period if is periodic (This method is for if state is in Container) */
    public void periodicContainer() {
        if (currentState instanceof StateContainer) {
            StateInterface state = ((StateContainer) currentState).getState();
            if (state instanceof PeriodicStateInterface) {
                ((PeriodicStateInterface) state).periodic();
            }
        }
    }

    /**
     * Returns if last transition was successful
     *
     * @return success
     */
    public boolean successfulTransition() {
        return !transitionInfo.wasFail();
    }

    /**
     * Returns infomation about last transtion
     *
     * @return information of last transiton
     */
    public TransitionInfo<State, Trigger> getTransitionInfo() {
        return transitionInfo;
    }

    /**
     * Tests if in state
     *
     * @param state target state
     * @return if in state
     */
    public boolean inState(State state) {
        return currentState.equals(state);
    }
}
