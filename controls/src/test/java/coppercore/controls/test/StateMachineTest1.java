package coppercore.controls.test;

import org.junit.jupiter.api.Test;

import coppercore.controls.state_machine.StateMachine;
import coppercore.controls.state_machine.FunctionalState;
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

    @SuppressWarnings("unchecked")
    @Test
    public void StateMachineTestNoteScenario() {
        Robot theRobot = new Robot();
        StateMachine <States, Robot> stateMachine = new StateMachine<>(theRobot);
        stateMachine.registerState(States.Idle, FunctionalState.of(()->{}));
        stateMachine.registerState(States.Intaking, FunctionalState.of(()->{}));
        stateMachine.registerState(States.WarmingUp, FunctionalState.of(()->{}));
        stateMachine.registerState(States.Shooting, FunctionalState.of(()->{}));

        stateMachine.from(States.Idle)
            .when((robot) -> robot.shouldIntake && !robot.hasNote).transitionTo(States.Intaking)
            .when((robot) -> robot.shouldShoot).andWhen((robot) -> robot.hasNote).transitionTo(States.WarmingUp);
    }


}
