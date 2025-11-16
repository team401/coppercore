package coppercore.controls.test;

import static coppercore.controls.test.StateMachineTests2.States.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

public class StateMachineTests2 {

    public enum States implements State<States, Robot> {
        Idle {
            @Override
            public boolean periodic(StateMachine<States, Robot> stateMachine, Robot robot) {
                robot.motorSpeed = 0;
                robot.armPos = 0;
                robot.isWarmedUp = false;
                return false;
            }
        },
        Intaking {
            @Override
            public boolean periodic(StateMachine<States, Robot> stateMachine, Robot robot) {
                robot.motorSpeed = -100;
                robot.armPos = -1;
                robot.hasNote = true;
                return true;
            }
        },
        WarmingUp {
            @Override
            public boolean periodic(StateMachine<States, Robot> stateMachine, Robot robot) {
                robot.motorSpeed = 0;
                robot.armPos = 3;
                robot.isWarmedUp = true;
                stateMachine.finish(this); // example use of .finish
                return false; // ignored because finish was called
            }
        },
        Shooting {
            @Override
            public boolean periodic(StateMachine<States, Robot> stateMachine, Robot robot) {
                robot.motorSpeed = 100;
                robot.hasNote = false;
                stateMachine.requestTransitionTo(Idle); // example use of requestTransitionTo
                return false;
            }
        };
    }

    static class Robot {
        private boolean hasNote = false;
        private double motorSpeed = 0;
        private double armPos = 0;
        private boolean isWarmedUp = false;
        private boolean shouldShoot = false;
        private boolean shouldIntake = false;
    }

    @Test
    public void StateMachineTestNoteScenario() {
        Robot theRobot = new Robot();
        StateMachine<States, Robot> stateMachine = new StateMachine<>(States.class, theRobot);

        var fromIdle = stateMachine.transitionFrom(Idle);
        fromIdle.to(Intaking).when((robot) -> robot.shouldIntake && !robot.hasNote);
        fromIdle.to(WarmingUp).when((robot) -> robot.shouldShoot && robot.hasNote);

        var fromIntaking = stateMachine.transitionFrom(Intaking);
        fromIntaking.to(Idle).when((robot) -> !robot.shouldIntake);
        fromIntaking.to(Idle).whenFinished();

        var fromWarmingUp = stateMachine.transitionFrom(WarmingUp);
        fromWarmingUp.to(Idle).when((robot) -> !robot.shouldShoot);
        fromWarmingUp.to(Shooting).whenFinished();

        var fromShooting = stateMachine.transitionFrom(Shooting);
        fromShooting.to(Idle).when((robot) -> !robot.shouldShoot);
        fromShooting.to(Idle).whenRequested();
        stateMachine.setState(Idle);

        // Testing
        testStateMachine(stateMachine, theRobot);
    }

    public void testStateMachine(StateMachine<States, Robot> stateMachine, Robot world) {
        Consumer<States> assertState =
                (state) -> {
                    assertEquals(state, stateMachine.getCurrentState());
                };

        // Tests
        assertState.accept(Idle);

        stateMachine.periodic();
        assertState.accept(Idle);

        world.shouldShoot = true;
        stateMachine.updateStates();
        assertState.accept(Idle);

        world.shouldShoot = false;
        stateMachine.periodic();
        assertState.accept(Idle);

        stateMachine.updateStates();
        stateMachine.periodic();
        assertState.accept(Idle);

        world.shouldIntake = true;
        stateMachine.periodic();
        assertState.accept(Idle);

        stateMachine.updateStates();
        stateMachine.periodic();
        assertState.accept(Intaking);

        stateMachine.updateStates();
        assertState.accept(Idle);

        world.shouldShoot = true;
        stateMachine.updateStates();
        assertState.accept(WarmingUp);

        world.shouldShoot = false;
        stateMachine.periodic();
        assertState.accept(WarmingUp);

        world.shouldShoot = true;
        stateMachine.updateStates();
        assertState.accept(Shooting);

        stateMachine.periodic();
        assertState.accept(Shooting);

        stateMachine.updateStates();
        assertState.accept(Idle);
        world.shouldShoot = false;
    }
}
