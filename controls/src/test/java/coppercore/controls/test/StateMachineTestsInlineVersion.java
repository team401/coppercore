package coppercore.controls.test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import org.junit.jupiter.api.Test;

/*
 * Version of state machine test with
 */
public class StateMachineTestsInlineVersion {

    // This method exist to keep the Transition Test State Machine and the State Machine Graph State
    // Machine the same
    StateMachine<Robot> createTestStateMachine(Robot stateMachineWorld) {
        StateMachine<Robot> stateMachine = new StateMachine<>(stateMachineWorld);

        // Registering States
        List.of(idleState, intakingState, warmingUpState, shootingState)
                .forEach(stateMachine::registerState);

        // Defining Transitions

        // Transitions that can happen from Idle state
        idleState
                .when((Robot robot) -> robot.shouldIntake && !robot.hasNote, "should intake")
                .transitionTo(intakingState);
        idleState
                .when((Robot robot) -> robot.shouldShoot && robot.hasNote, "should shoot")
                .transitionTo(warmingUpState);

        // Transitions that can happen from Intaking state
        intakingState.whenFinished().transitionTo(idleState);
        intakingState
                .when((Robot robot) -> !robot.shouldIntake, "should not intake")
                .transitionTo(idleState);

        // Transitions that can happen from WarmingUp state
        warmingUpState.whenFinished().transitionTo(shootingState);
        warmingUpState
                .when((Robot robot) -> !robot.shouldShoot, "should not shoot")
                .transitionTo(idleState);
        warmingUpState.whenRequestedTransitionTo(idleState);

        // Transitions that can happen from Shooting state
        shootingState.whenFinished().transitionTo(idleState);
        shootingState
                .when((Robot robot) -> !robot.shouldShoot, "should not shoot")
                .transitionTo(idleState);
        shootingState.whenRequestedTransitionTo(idleState);

        // Setting initial state
        stateMachine.setState(idleState);

        return stateMachine;
    }

    @Test
    public void StateMachineTransitionsTest() {

        // ### Setting up the state machine
        // Creating the State Machine
        Robot stateMachineWorld = new Robot();

        StateMachine<Robot> stateMachine = createTestStateMachine(stateMachineWorld);

        // Testing
        assertSame(idleState, stateMachine.getCurrentState());

        // ### Ensure that when there is no input, the state remains the same
        stateMachine.periodic();
        assertSame(idleState, stateMachine.getCurrentState());

        // ### Test transition to Intaking
        stateMachineWorld.shouldIntake = true;
        stateMachine.updateStates();
        assertSame(intakingState, stateMachine.getCurrentState());

        // ### Test transition back to Idle from Intaking when intaking is canceled
        stateMachineWorld.shouldIntake = false;
        stateMachine.updateStates();
        assertSame(idleState, stateMachine.getCurrentState());

        // ### Return to Intaking and simulate note acquisition to test finishing transition
        stateMachineWorld.shouldIntake = true;
        stateMachine.updateStates();

        // Ensure we are in Intaking state
        assertSame(intakingState, stateMachine.getCurrentState());

        // Ensure it does not transition back to Idle without note acquisition
        stateMachine.periodic();
        stateMachine.updateStates();
        assertSame(intakingState, stateMachine.getCurrentState());

        // Simulate note acquisition
        stateMachineWorld.hasNote = true;
        stateMachine.periodic();
        stateMachine.updateStates();
        assertSame(idleState, stateMachine.getCurrentState());

        // Reset shouldIntake for next test
        stateMachineWorld.shouldIntake = false;

        // ### Test transition to WarmingUp
        stateMachineWorld.shouldShoot = true;
        stateMachine.updateStates();
        assertSame(warmingUpState, stateMachine.getCurrentState());

        // ### Test transition to Idle from WarmingUp when shooting is canceled
        stateMachineWorld.shouldShoot = false;
        stateMachine.updateStates();
        assertSame(idleState, stateMachine.getCurrentState());

        // ### Return to WarmingUp and simulate not having a note in warmup to test requested
        // transition
        stateMachineWorld.shouldShoot = true;
        stateMachine.updateStates();
        assertSame(warmingUpState, stateMachine.getCurrentState());

        // Simulate not having a note
        stateMachineWorld.hasNote = false;
        stateMachine.periodic();
        stateMachine.updateStates();
        assertSame(idleState, stateMachine.getCurrentState());

        // Reset for next test
        stateMachineWorld.shouldShoot = true;
        stateMachineWorld.hasNote = true;

        // ### Test transition to Shooting
        stateMachine.updateStates();
        assertSame(warmingUpState, stateMachine.getCurrentState());
        stateMachine.periodic();

        stateMachine.updateStates();
        assertSame(shootingState, stateMachine.getCurrentState());

        // ### Test transition back to Idle from Shooting when shooting is canceled
        stateMachineWorld.shouldShoot = false;
        stateMachine.updateStates();
        assertSame(idleState, stateMachine.getCurrentState());

        // ### Return to Shooting and simulate not having a note in shooting to test finishing
        // transition
        stateMachineWorld.shouldShoot = true;
        stateMachine.updateStates();

        // Ensure we are in WarmingUp state
        assertSame(warmingUpState, stateMachine.getCurrentState());

        // Finish WarmingUp to get to Shooting
        stateMachine.periodic();

        // Move to Shooting state
        stateMachine.updateStates();
        assertSame(shootingState, stateMachine.getCurrentState());

        // Simulate not having a note
        stateMachineWorld.hasNote = false;
        stateMachine.periodic();
        stateMachine.updateStates();
        assertSame(idleState, stateMachine.getCurrentState());
    }

    @Test
    public void StateMachineGraphTest() {
        Robot stateMachineWorld = new Robot();
        StateMachine<Robot> stateMachine = createTestStateMachine(stateMachineWorld);

        String outputFilePath =
                new File("").getAbsolutePath()
                        + File.separator
                        + "build"
                        + File.separator
                        + "resources"
                        + File.separator
                        + "test"
                        + File.separator
                        + "StateMachineGraphTestOutput.dot";

        try {
            // Ensure the output directory exists
            File outputFile = new File(outputFilePath);
            outputFile.getParentFile().mkdirs();

            // Ensure the file is created if it does not exist
            if (!outputFile.exists()) {
                if (!outputFile.createNewFile()) {
                    fail("Failed to create the output file: " + outputFilePath);
                }
            }

            try (PrintWriter pw = new PrintWriter(outputFilePath)) {
                // Write the graphviz file
                stateMachine.writeGraphvizFile(pw);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public static class Robot {
        public boolean shouldShoot = false;
        public boolean shouldIntake = false;
        public boolean hasNote = false;
        public double motorSpeed = 0;
        public double armPos = 0;
    }

    static final State<Robot> idleState =
            new State<Robot>("Idle") {
                @Override
                protected void periodic(StateMachine<Robot> stateMachine, Robot world) {
                    world.motorSpeed = 0;
                }
            };

    static final State<Robot> intakingState =
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

    static final State<Robot> warmingUpState =
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

    static final State<Robot> shootingState =
            new State<Robot>("Shooting") {
                @Override
                protected void periodic(StateMachine<Robot> stateMachine, Robot world) {
                    world.motorSpeed = 100;
                    if (!world.hasNote) {
                        finish();
                    }
                }
            };
}
