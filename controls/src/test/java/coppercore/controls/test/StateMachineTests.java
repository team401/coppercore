package coppercore.controls.test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import coppercore.controls.test.StateMachineTestsStates.States;

public class StateMachineTests {

    public boolean shouldShoot = false;
    public boolean shouldIntake = false;

    @BeforeEach
    public void reset() {
        shouldShoot = false;
        shouldIntake = false;
    }

    @Test
    public void StateClassesTest() {
        StateMachineTestsStates.StateDataHolder dataHolder =
                new StateMachineTestsStates.StateDataHolder();

        // TODO: Rewrite tests to use new StateMachine implementation

        StateMachine<States, Integer> stateMachine = new StateMachine<>(0);

        State<States, Integer> idleState =
                stateMachine.registerState(
                        States.Idle, new StateMachineTestsStates.IdleState(dataHolder));
        State<States, Integer> intakingState =
                stateMachine.registerState(
                        States.Intaking, new StateMachineTestsStates.IntakingState(dataHolder));
        State<States, Integer> warmingUpState =
                stateMachine.registerState(
                        States.WarmingUp, new StateMachineTestsStates.WarmingUpState(dataHolder));
        State<States, Integer> shootingState =
                stateMachine.registerState(
                        States.Shooting, new StateMachineTestsStates.ShootingState(dataHolder));

        idleState.transitionWhen(() -> shouldIntake && !dataHolder.hasNote, States.Intaking);
        idleState.transitionWhen(() -> shouldShoot && dataHolder.hasNote, States.WarmingUp);

        intakingState.transitionWhen(() -> !shouldIntake, States.Idle);
        intakingState.transitionWhenFinished(States.Idle);

        warmingUpState.transitionWhenRequested(States.Idle);
        warmingUpState.transitionWhen(() -> !shouldShoot, States.Idle);
        warmingUpState.transitionWhenFinished(States.Shooting);
        

        shootingState.transitionWhen(() -> !shouldShoot, States.Idle);
        shootingState.transitionWhenFinished(States.Idle);

        stateMachine.setState(States.Idle);

        // Testing
        testStateMachine(stateMachine);
    }

    public void testStateMachine(StateMachine<States, Integer> stateMachine) {
        Consumer<States> assertState =
                (state) -> {
                    assertEquals(state, stateMachine.getCurrentStateKey());
                };

        // Tests
        assertState.accept(States.Idle);

        stateMachine.periodic();
        assertState.accept(States.Idle);

        shouldShoot = true;
        stateMachine.updateStates();
        assertState.accept(States.Idle);

        shouldShoot = false;
        stateMachine.periodic();
        assertState.accept(States.Idle);

        stateMachine.updateStates();
        stateMachine.periodic();
        assertState.accept(States.Idle);

        shouldIntake = true;
        stateMachine.periodic();
        assertState.accept(States.Idle);

        stateMachine.updateStates();
        stateMachine.periodic();
        assertState.accept(States.Intaking);

        stateMachine.updateStates();
        assertState.accept(States.Idle);

        shouldShoot = true;
        stateMachine.updateStates();
        assertState.accept(States.WarmingUp);

        shouldShoot = false;
        stateMachine.periodic();
        assertState.accept(States.WarmingUp);

        shouldShoot = true;
        stateMachine.updateStates();
        assertState.accept(States.Shooting);

        stateMachine.periodic();
        assertState.accept(States.Shooting);

        stateMachine.updateStates();
        assertState.accept(States.Idle);
        shouldShoot = false;
    }
}
