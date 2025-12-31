package coppercore.controls.test;

import static edu.wpi.first.units.Units.Seconds;
import static org.junit.jupiter.api.Assertions.assertSame;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import java.util.List;
import java.util.function.BiFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * Test `whenTimeout` transition.
 */
public class StateMachineTestsTimeout {
    private final int TIMEOUT_SECONDS = 2;

    StateMachine<Robot> createTestStateMachine(Robot stateMachineWorld) {
        StateMachine<Robot> stateMachine = new StateMachine<>(stateMachineWorld);

        // Registering States
        List.of(waitingState, timedoutState).forEach(stateMachine::registerState);

        // Defining Transitions
        waitingState.whenTimeout(Seconds.of(TIMEOUT_SECONDS)).transitionTo(timedoutState);

        stateMachine.setState(waitingState);
        return stateMachine;
    }

    @BeforeEach
    void setup() {
        HAL.initialize(500, 0);
        SimHooks.pauseTiming();
    }

    @AfterEach
    void cleanup() {
        SimHooks.resumeTiming();
    }

    @Test
    public void StateMachineTransitionsTestWithTimeout() {

        Robot stateMachineWorld = new Robot();
        StateMachine<Robot> stateMachine = createTestStateMachine(stateMachineWorld);

        DES sim = new DES();

        // Run robot loop every 20ms, that is, at 0, 20, 40, 60 and so on.
        DES.Runnable robotLoop =
                new DES.Runnable() {
                    @Override
                    public void run(int simulationTime) {
                        stateMachine.periodic();
                        stateMachine.updateStates();
                        sim.schedule(simulationTime + 20, this);
                        SimHooks.stepTiming(0.02);
                    }
                };
        sim.schedule(0, robotLoop);

        assertSame(waitingState, stateMachine.getCurrentState());
        final BiFunction<State, String, DES.Runnable> assertIn =
                (state, msg) -> _time -> assertSame(state, stateMachine.getCurrentState(), msg);

        sim.schedule(5, assertIn.apply(waitingState, "in waiting 1"));
        sim.schedule(TIMEOUT_SECONDS * 1000 - 5, assertIn.apply(waitingState, "in waiting 2"));
        sim.schedule(TIMEOUT_SECONDS * 1000 + 5, assertIn.apply(timedoutState, "in timedout 3"));
        sim.schedule(
                TIMEOUT_SECONDS * 1000 + 99,
                (_time) -> {
                    stateMachine.setState(waitingState);
                });
        sim.schedule(
                2 * TIMEOUT_SECONDS * 1000 + 100 - 5, assertIn.apply(waitingState, "in waiting 4"));
        sim.schedule(
                2 * TIMEOUT_SECONDS * 1000 + 100 + 25,
                assertIn.apply(timedoutState, "in timedout 5"));

        sim.simulate(10000);
    }

    public static class Robot {}

    static final State<Robot> waitingState =
            new State<Robot>("Waiting") {
                @Override
                protected void periodic(StateMachine<Robot> stateMachine, Robot world) {}
            };

    static final State<Robot> timedoutState =
            new State<Robot>("TimedOutFromWait") {
                @Override
                protected void periodic(StateMachine<Robot> stateMachine, Robot world) {}
            };
}
