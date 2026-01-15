package coppercore.controls.test;

import static coppercore.controls.test.StateMachineTests.Robot;
import static org.junit.jupiter.api.Assertions.assertSame;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import coppercore.controls.test.StateMachineTests.Robot;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/*
 * Version of state machine test with discrete event simulation.
 */
public class StateMachineTestsDES {

    public void runDESStateMachineTest(
            DES sim,
            Function<State, DES.Runnable> assertIn,
            Robot stateMachineWorld,
            State<Robot> idleState,
            State<Robot> intakingState,
            State<Robot> warmingUpState,
            State<Robot> shootingState) {
        // ### Ensure that when there is no input, the state remains the same
        sim.schedule(1, assertIn.apply(idleState));
        // ### Test transition to Intaking
        sim.schedule(
                5,
                (_time) -> {
                    stateMachineWorld.shouldIntake = true;
                });

        sim.schedule(25, assertIn.apply(intakingState));

        // ### Test transition back to Idle from Intaking when intaking is canceled
        sim.schedule(
                30,
                (_time) -> {
                    stateMachineWorld.shouldIntake = false;
                });

        sim.schedule(50, assertIn.apply(idleState));

        // ### Return to Intaking and simulate note acquisition to test finishing transition
        sim.schedule(
                55,
                (_time) -> {
                    stateMachineWorld.shouldIntake = true;
                });

        // Ensure we are in Intaking state
        sim.schedule(65, assertIn.apply(intakingState));

        // Ensure it does not transition back to Idle without note acquisition
        sim.schedule(85, assertIn.apply(intakingState));

        // Simulate note acquisition
        sim.schedule(
                90,
                (_time) -> {
                    stateMachineWorld.hasNote = true;
                });

        sim.schedule(110, assertIn.apply(idleState));

        // Reset shouldIntake for next test
        sim.schedule(
                115,
                (_time) -> {
                    stateMachineWorld.shouldIntake = false;
                });

        // ### Test transition to WarmingUp
        sim.schedule(
                125,
                (_time) -> {
                    stateMachineWorld.shouldShoot = true;
                });

        sim.schedule(145, assertIn.apply(warmingUpState));

        // ### Test transition to Idle from WarmingUp when shooting is canceled
        sim.schedule(
                150,
                (_time) -> {
                    stateMachineWorld.shouldShoot = false;
                });

        // NB. Setting shouldShoot to false here doesn't trigger a
        // transition to idleState here because it doesn't fire at 160.
        // Instead, the warmingUpState will finish, which triggers a
        // transition to shootingState at 160. shootingState finishes
        // at 180, transitioning to idleState.
        sim.schedule(161, assertIn.apply(shootingState));
        sim.schedule(181, assertIn.apply(idleState));

        // ### Return to WarmingUp and simulate not having a note in warmup to test requested
        // transition
        sim.schedule(
                185,
                (_time) -> {
                    stateMachineWorld.shouldShoot = true;
                });
        sim.schedule(201, assertIn.apply(warmingUpState));

        // Simulate not having a note
        sim.schedule(
                205,
                (_time) -> {
                    stateMachineWorld.hasNote = false;
                });

        sim.schedule(221, assertIn.apply(idleState));

        // Reset for next test
        sim.schedule(
                225,
                (_time) -> {
                    stateMachineWorld.shouldShoot = true;
                    stateMachineWorld.hasNote = true;
                });

        // ### Test transition to Shooting
        sim.schedule(241, assertIn.apply(warmingUpState));

        // I think there should be an external trigger, such as
        // note left the robot
        sim.schedule(261, assertIn.apply(shootingState));

        // ### Test transition back to Idle from Shooting when shooting is canceled
        sim.schedule(
                265,
                (_time) -> {
                    stateMachineWorld.shouldShoot = false;
                });

        sim.schedule(281, assertIn.apply(idleState));

        // ### Return to Shooting and simulate not having a note in shooting to test finishing
        sim.schedule(
                285,
                (_time) -> {
                    stateMachineWorld.shouldShoot = true;
                });

        // Ensure we are in WarmingUp state
        sim.schedule(301, assertIn.apply(warmingUpState));

        // Finish WarmingUp to get to Shooting
        sim.schedule(321, assertIn.apply(shootingState));

        // Simulate not having a note
        sim.schedule(
                325,
                (_time) -> {
                    stateMachineWorld.hasNote = false;
                });

        sim.schedule(341, assertIn.apply(idleState));

        // Finally, run the simulation
        sim.simulate(500);
    }

    @Test
    public void stateMachineDESTransitionsTestInlinedVersion() {
        State<Robot> idleState =
                new State<Robot>("Idle") {
                    @Override
                    protected void periodic(StateMachine<Robot> stateMachine, Robot world) {
                        world.motorSpeed = 0;
                    }
                };

        State<Robot> intakingState =
                new State<Robot>("Intaking") {
                    @Override
                    protected void periodic(StateMachine<Robot> stateMachine, Robot world) {
                        world.motorSpeed = -100;
                        world.armPos = -1;
                        if (world.hasNote) {
                            finish();
                        }
                    }
                };

        State<Robot> warmingUpState =
                new State<Robot>("WarmingUp") {
                    @Override
                    protected void periodic(StateMachine<Robot> stateMachine, Robot world) {
                        if (!world.hasNote) {
                            stateMachine.requestState(idleState);
                            return;
                        }
                        world.motorSpeed = 50;
                        world.armPos = 3;
                        finish();
                    }
                };

        State<Robot> shootingState =
                new State<Robot>("Shooting") {
                    @Override
                    protected void periodic(StateMachine<Robot> stateMachine, Robot world) {
                        world.motorSpeed = 100;
                        if (!world.hasNote) {
                            finish();
                        }
                    }
                };

        // ### Setting up the state machine
        // Creating the State Machine
        Robot stateMachineWorld = new Robot();

        StateMachine<Robot> stateMachine =
                StateMachineTests.createTestStateMachine(
                        stateMachineWorld, idleState, intakingState, warmingUpState, shootingState);

        DES sim = StateMachineTests.createRobotLoopDES(stateMachine);

        // Testing
        assertSame(idleState, stateMachine.getCurrentState());
        final Function<State, DES.Runnable> assertIn =
                (state) ->
                        _time ->
                                assertSame(
                                        state,
                                        stateMachine.getCurrentState(),
                                        "Incorrect State at time: " + _time);

        runDESStateMachineTest(
                sim,
                assertIn,
                stateMachineWorld,
                idleState,
                intakingState,
                warmingUpState,
                shootingState);
    }

    @Test
    public void stateMachineDESTransitionsTest() {
        State<Robot> idleState = StateMachineTestsStates.IDLE;
        State<Robot> intakingState = StateMachineTestsStates.INTAKING;
        State<Robot> warmingUpState = StateMachineTestsStates.WARMING_UP;
        State<Robot> shootingState = StateMachineTestsStates.SHOOTING;

        // ### Setting up the state machine
        // Creating the State Machine
        Robot stateMachineWorld = new Robot();

        StateMachine<Robot> stateMachine =
                StateMachineTests.createTestStateMachine(
                        stateMachineWorld, idleState, intakingState, warmingUpState, shootingState);

        DES sim = StateMachineTests.createRobotLoopDES(stateMachine);

        // Testing
        assertSame(idleState, stateMachine.getCurrentState());
        final Function<State, DES.Runnable> assertIn =
                (state) ->
                        _time ->
                                assertSame(
                                        state,
                                        stateMachine.getCurrentState(),
                                        "Incorrect State at time: " + _time);

        runDESStateMachineTest(
                sim,
                assertIn,
                stateMachineWorld,
                idleState,
                intakingState,
                warmingUpState,
                shootingState);
    }
}
