package coppercore.controls.test;

import coppercore.controls.state_machine.StateMachine;
import coppercore.controls.state_machine.State;
import static coppercore.controls.test.StateMachineTestsStates.States;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StateMachineTests {

    @Test
    public void FunctionalStatesTest() {
        StateMachineTestsStates.FunctionalStateHolder stateHolder = new StateMachineTestsStates.FunctionalStateHolder();

        boolean shouldShoot = false;
        boolean shouldIntake = false;

        StateMachine<States> stateMachine = new StateMachine<>();

        State<States> idleState = stateMachine.addState(States.Idle, stateHolder::functionalIdlePeriodic);
        State<States> intakingState = stateMachine.addState(States.Intaking, stateHolder::functionalIntakingPeriodic);
        State<States> warmingUpState = stateMachine.addState(States.WarmingUp, stateHolder::functionalWarmingUpPeriodic);
        State<States> shootingState = stateMachine.addState(States.Shooting, stateHolder::functionalShootingPeriodic);

        idleState.transitionWhen(() -> shouldIntake && !stateHolder.hasNote, States.Intaking);
        idleState.transitionWhen(() -> shouldShoot && stateHolder.hasNote, States.WarmingUp);

        intakingState.transitionWhen(() -> !shouldIntake, States.Idle);
        intakingState.transitionWhen(() -> stateHolder.hasNote, States.Idle);

        warmingUpState.transitionWhen(() -> !shouldShoot, States.Idle);
        warmingUpState.transitionWhen(() -> stateHolder.isWarmedUp, States.Shooting);

        shootingState.transitionWhen(() -> !shouldShoot, States.Idle);
        shootingState.transitionWhen(() -> !stateHolder.hasNote, States.Idle);


        // Tests
    }



    @Test
    public void StateClassesTest() {
        StateMachineTestsStates.StateDataHolder dataHolder = new StateMachineTestsStates.StateDataHolder();

        boolean shouldShoot = false;
        boolean shouldIntake = false;

        StateMachine<States> stateMachine = new StateMachine<>();

        State<States> idleState = stateMachine.registerState(States.Idle, new StateMachineTestsStates.IdleState(dataHolder));
        State<States> intakingState = stateMachine.registerState(States.Intaking, new StateMachineTestsStates.IntakingState(dataHolder));
        State<States> warmingUpState = stateMachine.registerState(States.WarmingUp, new StateMachineTestsStates.WarmingUpState(dataHolder));
        State<States> shootingState = stateMachine.registerState(States.Shooting, new StateMachineTestsStates.ShootingState(dataHolder));

        idleState.transitionWhen(() -> shouldIntake && !dataHolder.hasNote, States.Intaking);
        idleState.transitionWhen(() -> shouldShoot && dataHolder.hasNote, States.WarmingUp);

        intakingState.transitionWhen(() -> !shouldIntake, States.Idle);
        intakingState.transitionWhenFinished(States.Idle);

        warmingUpState.transitionWhen(() -> !shouldShoot, States.Idle);
        warmingUpState.transitionWhenFinished(States.Shooting);

        shootingState.transitionWhen(() -> !shouldShoot, States.Idle);
        shootingState.transitionWhenFinished(States.Idle);


        // Tests
    }

}
