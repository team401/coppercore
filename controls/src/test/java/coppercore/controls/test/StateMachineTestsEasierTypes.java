package coppercore.controls.test;

import static org.junit.jupiter.api.Assertions.assertSame;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;

public class StateMachineTestsEasierTypes {

    public static class Robot {}

    public static class EasierStateTypingClass extends State<Robot> {

        @Override
        public void periodic(StateMachine<Robot> stateMachine, Robot world) {
            // No implementation needed for this test
        }
    }

    public static class SecondState extends State<Robot> {

        @Override
        public void periodic(StateMachine<Robot> stateMachine, Robot world) {
            // No implementation needed for this test
        }
    }

    EasierStateTypingClass easierState;
    SecondState secondState;

    StateMachine<Robot> createTestStateMachine(Robot stateMachineWorld) {
        StateMachine<Robot> stateMachine = new StateMachine<>(stateMachineWorld);

        // Registering States
        easierState = stateMachine.registerState(new EasierStateTypingClass());
        secondState = stateMachine.registerState(new SecondState());

        // Defining Transitions
        easierState
                .when(this::shouldTransitionToSecondState, "external condition met")
                .transitionTo(secondState);

        stateMachine.setState(easierState);
        return stateMachine;
    }

    // This is just an easy way to simulate an external condition that is not in world
    public boolean externalCondition = false;

    public boolean shouldTransitionToSecondState() {
        return externalCondition;
    }

    @Test
    void StateMachineEasierTypesTest() {
        Robot stateMachineWorld = new Robot();
        StateMachine<Robot> stateMachine = createTestStateMachine(stateMachineWorld);

        DES sim = new DES();

        // Run robot loop every 20ms, that is, at 0, 20, 40, 60 and so on.
        DES.Runnable robotLoop =
                new DES.Runnable() {
                    @Override
                    public void run(int simulationTime) {
                        stateMachine.periodic();
                        sim.schedule(simulationTime + 20, this);
                    }
                };
        sim.schedule(0, robotLoop);
        assertSame(easierState, stateMachine.getCurrentState());

        final BiFunction<State, String, DES.Runnable> assertIn =
                (state, msg) -> _time -> assertSame(state, stateMachine.getCurrentState(), msg);

        // Schedule checks
        sim.schedule(100, assertIn.apply(easierState, "Should still be in easierState at 100ms"));

        // Change external condition to true to trigger transition
        sim.schedule(185, _time -> externalCondition = true);

        // Check that we are still in easierState just before transition should occur
        sim.schedule(190, assertIn.apply(easierState, "Should still be in easierState at 190ms"));

        // Give one cycle for the state machine to process the transition
        sim.schedule(
                230,
                assertIn.apply(secondState, "Should have transitioned to secondState at 220ms"));

        sim.simulate(300);
    }
}
