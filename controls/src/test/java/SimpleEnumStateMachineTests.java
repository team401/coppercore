package coppercore.controls.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import coppercore.controls.StateMachine;
import coppercore.controls.StateMachineConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SimpleEnumStateMachineTests {

    static enum simpleEnumStates {
        IDLE,
        READY,
        WAITING,
        DONE,
        SHUTDOWN,
    }

    static enum simpleEnumTriggers {
        PREPARE,
        POLL,
        WAITING,
        FINISH,
        SHUTDOWN,
        ERROR
    }

    public static StateMachineConfiguration<simpleEnumStates, simpleEnumTriggers>
            simpleEnumMachineConfig;

    @BeforeAll
    public static void setup() {
        simpleEnumMachineConfig = new StateMachineConfiguration<>();

        simpleEnumMachineConfig
                .configure(simpleEnumStates.IDLE)
                .permit(simpleEnumTriggers.PREPARE, simpleEnumStates.READY);

        simpleEnumMachineConfig
                .configure(simpleEnumStates.READY)
                .permit(simpleEnumTriggers.POLL, simpleEnumStates.WAITING);

        simpleEnumMachineConfig
                .configure(simpleEnumStates.WAITING)
                .permit(simpleEnumTriggers.FINISH, simpleEnumStates.DONE)
                .permit(simpleEnumTriggers.ERROR, simpleEnumStates.SHUTDOWN);

        simpleEnumMachineConfig
                .configure(simpleEnumStates.DONE)
                .permit(simpleEnumTriggers.PREPARE, simpleEnumStates.READY)
                .permit(simpleEnumTriggers.SHUTDOWN, simpleEnumStates.SHUTDOWN);
    }

    @Test
    void stateMachineTransitionNoErrorTest() {
        StateMachine<simpleEnumStates, simpleEnumTriggers> stateMachine =
                new StateMachine<>(simpleEnumMachineConfig, simpleEnumStates.IDLE);
        assertEquals(simpleEnumStates.IDLE, stateMachine.getCurrentState());
        stateMachine.fire(simpleEnumTriggers.PREPARE);
        assertEquals(simpleEnumStates.READY, stateMachine.getCurrentState());
        stateMachine.fire(simpleEnumTriggers.POLL);
        assertEquals(simpleEnumStates.WAITING, stateMachine.getCurrentState());
        stateMachine.fire(simpleEnumTriggers.FINISH);
        assertEquals(simpleEnumStates.DONE, stateMachine.getCurrentState());
        stateMachine.fire(simpleEnumTriggers.PREPARE);
        assertEquals(simpleEnumStates.READY, stateMachine.getCurrentState());
        stateMachine.fire(simpleEnumTriggers.POLL);
        assertEquals(simpleEnumStates.WAITING, stateMachine.getCurrentState());
        stateMachine.fire(simpleEnumTriggers.FINISH);
        assertEquals(simpleEnumStates.DONE, stateMachine.getCurrentState());
        stateMachine.fire(simpleEnumTriggers.SHUTDOWN);
        assertEquals(simpleEnumStates.SHUTDOWN, stateMachine.getCurrentState());
    }

    @Test
    void stateMachineTransitionErrorTest() {
        StateMachine<simpleEnumStates, simpleEnumTriggers> stateMachine =
                new StateMachine<>(simpleEnumMachineConfig, simpleEnumStates.IDLE);
        assertEquals(simpleEnumStates.IDLE, stateMachine.getCurrentState());
        stateMachine.fire(simpleEnumTriggers.PREPARE);
        assertEquals(simpleEnumStates.READY, stateMachine.getCurrentState());
        stateMachine.fire(simpleEnumTriggers.POLL);
        assertEquals(simpleEnumStates.WAITING, stateMachine.getCurrentState());
        stateMachine.fire(simpleEnumTriggers.FINISH);
        assertEquals(simpleEnumStates.DONE, stateMachine.getCurrentState());
        stateMachine.fire(simpleEnumTriggers.PREPARE);
        assertEquals(simpleEnumStates.READY, stateMachine.getCurrentState());
        stateMachine.fire(simpleEnumTriggers.POLL);
        assertEquals(simpleEnumStates.WAITING, stateMachine.getCurrentState());
        stateMachine.fire(simpleEnumTriggers.ERROR);
        assertEquals(simpleEnumStates.SHUTDOWN, stateMachine.getCurrentState());
    }
}
