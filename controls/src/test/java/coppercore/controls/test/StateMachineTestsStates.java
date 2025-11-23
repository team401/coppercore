package coppercore.controls.test;

import coppercore.controls.state_machine.State;

public class StateMachineTestsStates {

    public static enum States {
        Idle,
        Intaking,
        WarmingUp,
        Shooting
    }
    public static class StateDataHolder {
        public boolean hasNote = false;
        public double motorSpeed = 0;
        public double armPos = 0;
    }

    public abstract static class StateBase extends State<States, Integer> {

        final StateDataHolder dataHolder;

        public StateBase(StateDataHolder dataHolder) {
            this.dataHolder = dataHolder;
        }

        void setSpeed(double speed) {
            dataHolder.motorSpeed = speed;
        }
    }

    public static class IdleState extends StateBase {

        public IdleState(StateDataHolder dataHolder) {
            super(dataHolder);
        }

        @Override
        protected void periodic(Integer world) {
            setSpeed(0);
        }
    }

    public static class IntakingState extends StateBase {

        public IntakingState(StateDataHolder dataHolder) {
            super(dataHolder);
        }

        @Override
        protected void periodic(Integer world) {
            setSpeed(-100);
            dataHolder.armPos = -1;
            dataHolder.hasNote = true;
            finish();
        }
    }

    public static class WarmingUpState extends StateBase {

        public WarmingUpState(StateDataHolder dataHolder) {
            super(dataHolder);
        }

        @Override
        protected void periodic(Integer world) {
            setSpeed(0);
            dataHolder.armPos = 3;
            finish();
        }
    }

    public static class ShootingState extends StateBase {

        public ShootingState(StateDataHolder dataHolder) {
            super(dataHolder);
        }

        @Override
        protected void periodic(Integer world) {
            setSpeed(100);
            dataHolder.hasNote = false;
            finish();
        }
    }
}
