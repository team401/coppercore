package coppercore.controls.test;

import static coppercore.controls.test.StateMachineTests.Robot;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;

public class StateMachineTestsStates {

    // This is not a recommended practice on how to store states, but it makes test code cleaner
    public static final IdleState IDLE = new IdleState("Idle");
    public static final IntakingState INTAKING = new IntakingState();
    public static final WarmingUpState WARMING_UP = new WarmingUpState();
    public static final ShootingState SHOOTING = new ShootingState();

    public static class IdleState extends State<Robot> {

        public IdleState(String name) {
            super(name);
        }

        @Override
        protected void periodic(StateMachine<Robot> stateMachine, Robot world) {
            world.motorSpeed = 0;
        }
    }

    public static class IntakingState extends State<Robot> {

        @Override
        protected void periodic(StateMachine<Robot> stateMachine, Robot world) {
            world.motorSpeed = -100;
            world.armPos = -1;
            if (world.hasNote) {
                finish();
            }
        }
    }

    public static class WarmingUpState extends State<Robot> {

        @Override
        protected void periodic(StateMachine<Robot> stateMachine, Robot world) {
            if (!world.hasNote) {
                // Only getting the state by the name to test if getStateByName works correctly
                stateMachine.requestState(stateMachine.getStateByName("Idle"));
                return;
            }
            world.motorSpeed = 50;
            world.armPos = 3;
            finish();
        }
    }

    public static class ShootingState extends State<Robot> {

        @Override
        protected void periodic(StateMachine<Robot> stateMachine, Robot world) {
            world.motorSpeed = 100;
            if (!world.hasNote) {
                finish();
            }
        }
    }
}
