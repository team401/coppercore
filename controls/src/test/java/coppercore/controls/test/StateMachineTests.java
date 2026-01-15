package coppercore.controls.test;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import java.util.List;

public class StateMachineTests {

    public static class Robot {
        public boolean shouldShoot = false;
        public boolean shouldIntake = false;
        public boolean hasNote = false;
        public double motorSpeed = 0;
        public double armPos = 0;
    }

    // This method exist try and keep some of the state machine tests consistent
    public static StateMachine<Robot> createTestStateMachine(
            Robot stateMachineWorld,
            State<Robot> idleState,
            State<Robot> intakingState,
            State<Robot> warmingUpState,
            State<Robot> shootingState) {

        StateMachine<Robot> stateMachine = new StateMachine<>(stateMachineWorld);

        // Registering States
        List.of(idleState, intakingState, warmingUpState, shootingState)
                .forEach(stateMachine::registerState);

        // Defining Transitions

        // Transitions that can happen from Idle state
        idleState
                .when((Robot world) -> world.shouldIntake && !world.hasNote, "should intake")
                .transitionTo(intakingState);
        idleState
                .when((Robot world) -> world.shouldShoot && world.hasNote, "should shoot")
                .transitionTo(warmingUpState);

        // Transitions that can happen from Intaking state
        intakingState.whenFinished().transitionTo(idleState);
        intakingState
                .when((Robot world) -> !world.shouldIntake, "should not intake")
                .transitionTo(idleState);

        // Transitions that can happen from WarmingUp state
        warmingUpState.whenFinished().transitionTo(shootingState);
        warmingUpState
                .when((Robot world) -> !world.shouldShoot, "should not shoot")
                .transitionTo(idleState);
        warmingUpState.whenRequestedTransitionTo(idleState);

        // Transitions that can happen from Shooting state
        shootingState.whenFinished().transitionTo(idleState);
        shootingState
                .when((Robot world) -> !world.shouldShoot, "should not shoot")
                .transitionTo(idleState);
        shootingState.whenRequestedTransitionTo(idleState);

        // Setting initial state
        stateMachine.setState(idleState);

        return stateMachine;
    }

    public static DES createRobotLoopDES(StateMachine<Robot> stateMachine) {
        DES sim = new DES();

        // Run robot loop every 20ms, that is, at 0, 20, 40, 60 and so on.
        DES.Runnable robotLoop =
                new DES.Runnable() {
                    @Override
                    public void run(int simulationTime) {
                        stateMachine.periodic();
                        sim.schedule(simulationTime + 20, this);
                    }
                };
        sim.schedule(0, robotLoop);

        return sim;
    }
}
