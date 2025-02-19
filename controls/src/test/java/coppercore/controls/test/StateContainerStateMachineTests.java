package coppercore.controls.test;

import coppercore.controls.state_machine.StateMachine;
import coppercore.controls.state_machine.StateMachineConfiguration;
import coppercore.controls.state_machine.state.PeriodicStateInterface;
import coppercore.controls.state_machine.state.StateContainer;
import coppercore.controls.state_machine.transition.Transition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StateContainerStateMachineTests {

    static class IdleState implements PeriodicStateInterface {
        public static void customOnEntry(
                Transition<StateContainerStateMachineTests, testEnumTriggers> transition) {}

        public static void customOnExit(
                Transition<StateContainerStateMachineTests, testEnumTriggers> transition) {}

        public static void customTransitionAction(
                Transition<StateContainerStateMachineTests, testEnumTriggers> transition) {}
    }
    ;

    static class ReadyState implements PeriodicStateInterface {}
    ;

    static class WaitingState implements PeriodicStateInterface {}
    ;

    static class DoneState implements PeriodicStateInterface {}
    ;

    static class ShutdownState implements PeriodicStateInterface {}
    ;

    static enum testStateContainer implements StateContainer {
        IDLE(new IdleState()),
        READY(new ReadyState()),
        WAITING(new WaitingState()),
        DONE(new DoneState()),
        SHUTDOWN(new ShutdownState());

        private final PeriodicStateInterface state;

        testStateContainer(PeriodicStateInterface state) {
            this.state = state;
        }

        @Override
        public PeriodicStateInterface getState() {
            return state;
        }
    }

    static enum testEnumTriggers {
        PREPARE,
        POLL,
        WAITING,
        FINISH,
        SHUTDOWN,
        ERROR
    }

    protected static StateMachineConfiguration<testStateContainer, testEnumTriggers>
            stateContainerTestMachineConfig;

    @BeforeAll
    public static void setup() {
        stateContainerTestMachineConfig = new StateMachineConfiguration<>();

        stateContainerTestMachineConfig
                .configureDefaultOnExitAction(
                        (Transition<testStateContainer, testEnumTriggers> transition) ->
                                transition.getSource().getState().onEntry(transition))
                .configureDefaultOnExitAction(
                        (Transition<testStateContainer, testEnumTriggers> transition) ->
                                transition.getSource().getState().onExit(transition));

        // stateContainerTestMachineConfig
        //    .registerBlankParent(testStateContainer.SOME_PARENT_STATE);  Not in first
        // Implemenation

        // stateContainerTestMachineConfig
        //    .configure(testStateContainer.SOME_STATE);  Not in first Implemenation

        stateContainerTestMachineConfig
                .configure(testStateContainer.IDLE)
                // .parentState(testStateContainer.SOME_STATE) Not in first Implemenation
                .permit(testEnumTriggers.PREPARE, testStateContainer.READY)
                .permitInternal(testEnumTriggers.PREPARE, testStateContainer.READY)
                .configureOnEntryAction(IdleState::customOnEntry)
                .configureOnExitAction(IdleState::customOnExit)
                .disableDefualtOnEntry()
                .disableDefualtOnExit();

        stateContainerTestMachineConfig
                .configure(testStateContainer.READY)
                .permit(testEnumTriggers.POLL, testStateContainer.WAITING);

        stateContainerTestMachineConfig
                .configure(testStateContainer.WAITING)
                .permit(testEnumTriggers.FINISH, testStateContainer.DONE)
                .permit(testEnumTriggers.ERROR, testStateContainer.SHUTDOWN);

        stateContainerTestMachineConfig
                .configure(testStateContainer.DONE)
                .permit(testEnumTriggers.PREPARE, testStateContainer.READY)
                .permit(testEnumTriggers.SHUTDOWN, testStateContainer.SHUTDOWN);
    }

    @Test
    void stateMachineTransitionNoErrorTest() {
        StateMachine<testStateContainer, testEnumTriggers> stateMachine =
                new StateMachine<>(stateContainerTestMachineConfig, testStateContainer.IDLE);
        testStateContainer stateContainer = stateMachine.getCurrentState();
        // stateMachine.inState(testStateContainer.SOME_STATE);  Not in first Implemenation //True
        stateMachine.inState(testStateContainer.IDLE); // True
        // stateMachine.inStateExactly(testStateContainer.SOME_STATE);  Not in first Implemenation
        // //False
        // stateMachine.inState(testStateContainer.SOME_PARENT_STATE);  Not in first Implemenation
        // //False
        stateContainer.getState().periodic();
    }

    @Test
    void stateMachineTransitionErrorTest() {
        StateMachine<testStateContainer, testEnumTriggers> stateMachine =
                new StateMachine<>(stateContainerTestMachineConfig, testStateContainer.IDLE);
    }
}
