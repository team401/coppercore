package coppercore.controls.test;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;

public class StateMachineTestsStates {

    // This is not a recommended practice on how to store states, but it makes test code cleaner
    public static final IdleState IDLE = new IdleState();
    public static final IntakingState INTAKING = new IntakingState();
    public static final WarmingUpState WARMINGUP = new WarmingUpState();
    public static final ShootingState SHOOTING = new ShootingState();

    public static class StateMachineWorld {
        public boolean shouldShoot = false;
        public boolean shouldIntake = false;
        public boolean hasNote = false;
        public double motorSpeed = 0;
        public double armPos = 0;
    }

    public static class IdleState extends State<StateMachineWorld> {

        @Override
        protected void periodic(
                StateMachine<StateMachineWorld> stateMachine, StateMachineWorld world) {
            world.motorSpeed = 0;
        }
    }

    public static class IntakingState extends State<StateMachineWorld> {

        @Override
        protected void periodic(
                StateMachine<StateMachineWorld> stateMachine, StateMachineWorld world) {
            world.motorSpeed = -100;
            world.armPos = -1;
            if (world.hasNote) {
                finish();
            }
        }
    }

    public static class WarmingUpState extends State<StateMachineWorld> {

        @Override
        protected void periodic(
                StateMachine<StateMachineWorld> stateMachine, StateMachineWorld world) {
            if (!world.hasNote) {
                stateMachine.requestState(IDLE);
                return;
            }
            world.motorSpeed = 50;
            world.armPos = 3;
            finish();
        }
    }

    public static class ShootingState extends State<StateMachineWorld> {

        @Override
        protected void periodic(
                StateMachine<StateMachineWorld> stateMachine, StateMachineWorld world) {
            world.motorSpeed = 100;
            if (!world.hasNote) {
                finish();
            }
        }
    }
}
