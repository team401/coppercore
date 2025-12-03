package coppercore.controls.test;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;

public class StateMachineTestsStates {

    public static class StateMachineWorld {
        public boolean shouldShoot = false;
        public boolean shouldIntake = false;
        public boolean hasNote = false;
        public double motorSpeed = 0;
        public double armPos = 0;
    }

    public static class IdleState extends State<StateMachineWorld> {

        @Override
        protected void periodic(StateMachine<StateMachineWorld> stateMachine, StateMachineWorld world) {
            world.motorSpeed = 0;
        }
    }

    public static class IntakingState extends State<StateMachineWorld> {

        @Override
        protected void periodic(StateMachine<StateMachineWorld> stateMachine, StateMachineWorld world) {
            world.motorSpeed = -100;
            world.armPos = -1;
            world.hasNote = true;
            finish();
        }
    }

    public static class WarmingUpState extends State<StateMachineWorld> {

        @Override
        protected void periodic(StateMachine<StateMachineWorld> stateMachine, StateMachineWorld world) {
            world.motorSpeed = 0;
            world.armPos = 3;
            finish();
        }
    }

    public static class ShootingState extends State<StateMachineWorld> {

        @Override
        protected void periodic(StateMachine<StateMachineWorld> stateMachine, StateMachineWorld world) {
            world.motorSpeed = 100;
            world.hasNote = false;
            finish();
        }
    }
}
