package coppercore.controls.test;

import org.junit.jupiter.api.Test;

import coppercore.controls.state_machine.StateMachine;
import coppercore.controls.test.StateMachineTest1.States;

public class StateMachineTest1 {
    
    static class Robot {
        private boolean hasNote = false;
        private double motorSpeed = 0;
        private double armPos = 0;
        private boolean isWarmedUp = false;
        private boolean shouldShoot = false;
        private boolean shouldIntake = false;
    }

    public enum States {
        Idle,
        Intaking,
        WarmingUp,
        Shooting
    }

    @Test
    public void StateMachineTestNoteScenario() {
        Robot theRobot = new Robot();
        StateMachine <States, Robot> stateMachine = new StateMachine<>(theRobot);
        stateMachine.addState(States.Idle, ()->{});
        stateMachine.addState(States.Intaking, ()->{});
        stateMachine.addState(States.WarmingUp, ()->{});
        stateMachine.addState(States.Shooting, ()->{});

        stateMachine.from(States.Idle)
            .when((robot) -> robot.shouldIntake && !robot.hasNote).transitionTo(States.Intaking)
            .when((robot) -> robot.shouldShoot).andWhen((robot) -> robot.hasNote).transitionTo(States.WarmingUp);
    }


}
