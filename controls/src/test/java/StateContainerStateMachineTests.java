package coppercore.controls.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import coppercore.controls.PeriodicStateInterface;
import coppercore.controls.StateContainer;
import coppercore.controls.StateMachine;
import coppercore.controls.StateMachineConfiguration;

public class StateContainerStateMachineTests {

    static class IdleState implements PeriodicStateInterface {};
    static class ReadyState implements PeriodicStateInterface {};
    static class WaitingState implements PeriodicStateInterface {};
    static class DoneState implements PeriodicStateInterface {};
    static class ShutdownState implements PeriodicStateInterface {};

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
        
        public PeriodicStateInterface getState(){
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

    public static StateMachineConfiguration<testStateContainer, testEnumTriggers> stateContainerTestMachineConfig;

    @BeforeAll
    public static void setup() {
        stateContainerTestMachineConfig = new StateMachineConfiguration<>();

        stateContainerTestMachineConfig
                .configure(testStateContainer.IDLE)
                .permit(testEnumTriggers.PREPARE, testStateContainer.READY);

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
    }

    @Test
    void stateMachineTransitionErrorTest() {
        StateMachine<testStateContainer, testEnumTriggers> stateMachine =
                new StateMachine<>(stateContainerTestMachineConfig, testStateContainer.IDLE);
    }
}
