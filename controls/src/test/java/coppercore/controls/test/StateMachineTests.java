package coppercore.controls.test;

import static coppercore.controls.test.StateMachineTestsStates.States;
import static org.junit.jupiter.api.Assertions.assertEquals;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StateMachineTests {

    public boolean shouldShoot = false;
    public boolean shouldIntake = false;

    @BeforeEach
    public void reset() {
        shouldShoot = false;
        shouldIntake = false;
    }

    @Test
    public void FunctionalStatesTest() {
        StateMachineTestsStates.FunctionalStateHolder stateHolder =
                new StateMachineTestsStates.FunctionalStateHolder();

        StateMachine<States> stateMachine = new StateMachine<>();

        State<States> idleState =
                stateMachine.addState(States.Idle, stateHolder::functionalIdlePeriodic);
        State<States> intakingState =
                stateMachine.addState(States.Intaking, stateHolder::functionalIntakingPeriodic);
        State<States> warmingUpState =
                stateMachine.addState(States.WarmingUp, stateHolder::functionalWarmingUpPeriodic);
        State<States> shootingState =
                stateMachine.addState(States.Shooting, stateHolder::functionalShootingPeriodic);

        idleState.transitionWhen(() -> shouldIntake && !stateHolder.hasNote, States.Intaking);
        idleState.transitionWhen(() -> shouldShoot && stateHolder.hasNote, States.WarmingUp);

        intakingState.transitionWhen(() -> !shouldIntake, States.Idle);
        intakingState.transitionWhen(() -> stateHolder.hasNote, States.Idle);

        warmingUpState.transitionWhen(() -> !shouldShoot, States.Idle);
        warmingUpState.transitionWhen(() -> stateHolder.isWarmedUp, States.Shooting);

        shootingState.transitionWhen(() -> !shouldShoot, States.Idle);
        shootingState.transitionWhen(() -> !stateHolder.hasNote, States.Idle);

        stateMachine.setState(States.Idle);

        // Testing
        testStateMachine(stateMachine);
    }

    @Test
    public void StateClassesTest() {
        StateMachineTestsStates.StateDataHolder dataHolder =
                new StateMachineTestsStates.StateDataHolder();

        StateMachine<States> stateMachine = new StateMachine<>();

        State<States> idleState =
                stateMachine.registerState(
                        States.Idle, new StateMachineTestsStates.IdleState(dataHolder));
        State<States> intakingState =
                stateMachine.registerState(
                        States.Intaking, new StateMachineTestsStates.IntakingState(dataHolder));
        State<States> warmingUpState =
                stateMachine.registerState(
                        States.WarmingUp, new StateMachineTestsStates.WarmingUpState(dataHolder));
        State<States> shootingState =
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

    public void testStateMachine(StateMachine<States> stateMachine) {
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
