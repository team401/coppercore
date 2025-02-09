package coppercore.controls.state_machine;

import coppercore.controls.state_machine.state.PeriodicStateInterface;
import coppercore.controls.state_machine.state.StateConfiguration;
import coppercore.controls.state_machine.state.StateContainer;
import coppercore.controls.state_machine.state.StateInterface;
import coppercore.controls.state_machine.transition.Transition;
import coppercore.controls.state_machine.transition.TransitionInfo;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringArrayPublisher;
import edu.wpi.first.networktables.StringPublisher;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Generic State Machine */
public class StateMachine<State extends Enum, Trigger extends Enum> {
    private final StateMachineConfiguration<State, Trigger> configuration;
    private TransitionInfo<State, Trigger> transitionInfo;
    private State currentState;

    private boolean debugging;

    private String[] lastEvents = new String[0];
    private final List<String> eventList = new ArrayList<>();
    private StringPublisher statePublisher;
    private StringPublisher triggerPublisher;
    private StringPublisher structurePublisher;
    private StringArrayPublisher eventPublisher;
    private StringArrayPublisher secondaryEventPublisher;

    public void addEvent(StateMachineEvent event) {
        eventList.add(event.getJsonString());
    }

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
        config.getStateConfiguration(currentState)
                .ifPresent(
                        (StateConfiguration<State, Trigger> stateConfiguration) -> {
                            List<Transition<State, Trigger>> transitions =
                                    stateConfiguration.getAllTransitions();
                            if (!transitions.isEmpty()) {
                                Trigger transitionTrigger = transitions.get(0).getTrigger();
                                @SuppressWarnings("unchecked")
                                Trigger[] possibleTriggers =
                                        (Trigger[])
                                                transitionTrigger
                                                        .getDeclaringClass()
                                                        .getEnumConstants();
                                StateMachineStructure structure = config.getStructure();
                                for (Trigger trigger : possibleTriggers) {
                                    structure.addTrigger(trigger.name());
                                }
                            }
                        });
        this.debugging = debugging;
        if (debugging) {
            NetworkTableInstance inst = NetworkTableInstance.getDefault();
            NetworkTable table = inst.getTable("StateMachine");
            statePublisher = table.getStringTopic("State").publish();
            triggerPublisher = table.getStringTopic("Trigger").publish();
            structurePublisher = table.getStringTopic("Structure").publish();
            eventPublisher = table.getStringArrayTopic("Events").publish();
            secondaryEventPublisher = table.getStringArrayTopic("SecondaryEvents").publish();
            statePublisher.set(initialState.name());
            structurePublisher.set(config.getStructure().getJSONString());
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
            triggerPublisher.set(trigger.name());
            addEvent(
                    new StateMachineEvent(
                            "fire",
                            new FireEventData(
                                    transitionInfo.getCurrentState().name(),
                                    transitionInfo.getTargetState().name(),
                                    transitionInfo.getTrigger().name())));
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
        if (debugging) {
            debugPeriodic();
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

    public void debugPeriodic() {
        secondaryEventPublisher.set(lastEvents);
        lastEvents = new String[eventList.size()];
        int index = 0;
        for (String event : eventList) {
            lastEvents[index] = event;
            index++;
        }
        eventPublisher.set(lastEvents);
        eventList.clear();
    }
}
