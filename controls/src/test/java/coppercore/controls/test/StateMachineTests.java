package coppercore.controls.test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import coppercore.controls.test.StateMachineTestsStates.StateMachineWorld;

public class StateMachineTests {


    @Test
    public void StateClassesTest() {
        StateMachineWorld stateMachineWorld = new StateMachineWorld();

        // TODO: Rewrite tests to use new StateMachine implementation

        StateMachine<StateMachineWorld> stateMachine = new StateMachine<StateMachineWorld>(stateMachineWorld);

        State<StateMachineWorld> idleState =
                stateMachine.registerState(
                        "Idle", new StateMachineTestsStates.IdleState());
        State<StateMachineWorld> intakingState =
                stateMachine.registerState(
                        "Intaking", new StateMachineTestsStates.IntakingState());
        State<StateMachineWorld> warmingUpState =
                stateMachine.registerState(
                        "WarmingUp", new StateMachineTestsStates.WarmingUpState());
        State<StateMachineWorld> shootingState =
                stateMachine.registerState(
                        "Shooting", new StateMachineTestsStates.ShootingState());

        idleState.when((StateMachineWorld world) -> world.shouldIntake && !world.hasNote)
                .transitionTo(intakingState);
        idleState.when((StateMachineWorld world) -> world.shouldShoot && world.hasNote)
                .transitionTo(warmingUpState);

        intakingState.whenFinished()
                .transitionTo(idleState);
        intakingState.when((StateMachineWorld world) -> !world.shouldIntake)
                .transitionTo(idleState);

        warmingUpState.whenFinished()
                .transitionTo(shootingState);
        warmingUpState.when((StateMachineWorld world) -> !world.shouldShoot)
                .transitionTo(idleState);
        warmingUpState.whenRequested(idleState)
                .transitionTo(idleState);

        shootingState.whenFinished()
            .transitionTo(idleState);
            
        shootingState.when((StateMachineWorld world) -> !world.shouldShoot)
            .transitionTo(idleState);
        shootingState.whenRequested(idleState)
            .transitionTo(idleState);

        stateMachine.setState(idleState);

        // Testing
        // TODO: Move tests from testStateMachine method here
    }

    // public void testStateMachine(StateMachine<StateMachineWorld> stateMachine) {

    //     // Tests
    //     assertState.accept(States.Idle);

    //     stateMachine.periodic();
    //     assertState.accept(States.Idle);

    //     shouldShoot = true;
    //     stateMachine.updateStates();
    //     assertState.accept(States.Idle);

    //     shouldShoot = false;
    //     stateMachine.periodic();
    //     assertState.accept(States.Idle);

    //     stateMachine.updateStates();
    //     stateMachine.periodic();
    //     assertState.accept(States.Idle);

    //     shouldIntake = true;
    //     stateMachine.periodic();
    //     assertState.accept(States.Idle);

    //     stateMachine.updateStates();
    //     stateMachine.periodic();
    //     assertState.accept(States.Intaking);

    //     stateMachine.updateStates();
    //     assertState.accept(States.Idle);

    //     shouldShoot = true;
    //     stateMachine.updateStates();
    //     assertState.accept(States.WarmingUp);

    //     shouldShoot = false;
    //     stateMachine.periodic();
    //     assertState.accept(States.WarmingUp);

    //     shouldShoot = true;
    //     stateMachine.updateStates();
    //     assertState.accept(States.Shooting);

    //     stateMachine.periodic();
    //     assertState.accept(States.Shooting);

    //     stateMachine.updateStates();
    //     assertState.accept(States.Idle);
    //     shouldShoot = false;
    // }
}
