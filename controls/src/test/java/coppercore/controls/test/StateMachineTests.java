package coppercore.controls.test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import coppercore.controls.test.StateMachineTestsStates.StateMachineWorld;
import java.io.File;
import java.io.PrintWriter;
import org.junit.jupiter.api.Test;

public class StateMachineTests {

    // This method exist to keep the Transition Test State Machine and the State Machine Graph State
    // Machine the same
    StateMachine<StateMachineWorld> createTestStateMachine(StateMachineWorld stateMachineWorld) {
        StateMachine<StateMachineWorld> stateMachine = new StateMachine<>(stateMachineWorld);

        // Registering States
        State<StateMachineWorld> idleState =
                stateMachine.registerState(StateMachineTestsStates.IDLE);
        State<StateMachineWorld> intakingState =
                stateMachine.registerState(StateMachineTestsStates.INTAKING);
        State<StateMachineWorld> warmingUpState =
                stateMachine.registerState(StateMachineTestsStates.WARMINGUP);
        State<StateMachineWorld> shootingState =
                stateMachine.registerState(StateMachineTestsStates.SHOOTING);

        // Defining Transitions

        // Transitions that can happen from Idle state
        idleState
                .when(
                        (StateMachineWorld world) -> world.shouldIntake && !world.hasNote,
                        "should intake")
                .transitionTo(intakingState);
        idleState
                .when(
                        (StateMachineWorld world) -> world.shouldShoot && world.hasNote,
                        "should shoot")
                .transitionTo(warmingUpState);

        // Transitions that can happen from Intaking state
        intakingState.whenFinished().transitionTo(idleState);
        intakingState
                .when((StateMachineWorld world) -> !world.shouldIntake, "should not intake")
                .transitionTo(idleState);

        // Transitions that can happen from WarmingUp state
        warmingUpState.whenFinished().transitionTo(shootingState);
        warmingUpState
                .when((StateMachineWorld world) -> !world.shouldShoot, "should not shoot")
                .transitionTo(idleState);
        warmingUpState.whenRequested(idleState).transitionTo(idleState);

        // Transitions that can happen from Shooting state
        shootingState.whenFinished().transitionTo(idleState);
        shootingState
                .when((StateMachineWorld world) -> !world.shouldShoot, "should not shoot")
                .transitionTo(idleState);
        shootingState.whenRequested(idleState).transitionTo(idleState);

        // Setting initial state
        stateMachine.setState(idleState);

        return stateMachine;
    }

    @Test
    public void StateMachineTransitionsTest() {

        // ### Setting up the state machine
        // Creating the State Machine
        StateMachineWorld stateMachineWorld = new StateMachineWorld();

        StateMachine<StateMachineWorld> stateMachine = createTestStateMachine(stateMachineWorld);

        // Testing
        assertSame(StateMachineTestsStates.IDLE, stateMachine.getCurrentState());

        // ### Ensure that when there is no input, the state remains the same
        stateMachine.periodic();
        assertSame(StateMachineTestsStates.IDLE, stateMachine.getCurrentState());

        // ### Test transition to Intaking
        stateMachineWorld.shouldIntake = true;
        stateMachine.updateStates();
        assertSame(StateMachineTestsStates.INTAKING, stateMachine.getCurrentState());

        // ### Test transition back to Idle from Intaking when intaking is canceled
        stateMachineWorld.shouldIntake = false;
        stateMachine.updateStates();
        assertSame(StateMachineTestsStates.IDLE, stateMachine.getCurrentState());

        // ### Return to Intaking and simulate note acquisition to test finishing transition
        stateMachineWorld.shouldIntake = true;
        stateMachine.updateStates();

        // Ensure we are in Intaking state
        assertSame(StateMachineTestsStates.INTAKING, stateMachine.getCurrentState());

        // Ensure it does not transition back to Idle without note acquisition
        stateMachine.periodic();
        stateMachine.updateStates();
        assertSame(StateMachineTestsStates.INTAKING, stateMachine.getCurrentState());

        // Simulate note acquisition
        stateMachineWorld.hasNote = true;
        stateMachine.periodic();
        stateMachine.updateStates();
        assertSame(StateMachineTestsStates.IDLE, stateMachine.getCurrentState());

        // Reset shouldIntake for next test
        stateMachineWorld.shouldIntake = false;

        // ### Test transition to WarmingUp
        stateMachineWorld.shouldShoot = true;
        stateMachine.updateStates();
        assertSame(StateMachineTestsStates.WARMINGUP, stateMachine.getCurrentState());

        // ### Test transition to Idle from WarmingUp when shooting is canceled
        stateMachineWorld.shouldShoot = false;
        stateMachine.updateStates();
        assertSame(StateMachineTestsStates.IDLE, stateMachine.getCurrentState());

        // ### Return to WarmingUp and simulate not having a note in warmup to test requested
        // transition
        stateMachineWorld.shouldShoot = true;
        stateMachine.updateStates();
        assertSame(StateMachineTestsStates.WARMINGUP, stateMachine.getCurrentState());

        // Simulate not having a note
        stateMachineWorld.hasNote = false;
        stateMachine.periodic();
        stateMachine.updateStates();
        assertSame(StateMachineTestsStates.IDLE, stateMachine.getCurrentState());

        // Reset for next test
        stateMachineWorld.shouldShoot = true;
        stateMachineWorld.hasNote = true;

        // ### Test transition to Shooting
        stateMachine.updateStates();
        assertSame(StateMachineTestsStates.WARMINGUP, stateMachine.getCurrentState());
        stateMachine.periodic();

        stateMachine.updateStates();
        assertSame(StateMachineTestsStates.SHOOTING, stateMachine.getCurrentState());

        // ### Test transition back to Idle from Shooting when shooting is canceled
        stateMachineWorld.shouldShoot = false;
        stateMachine.updateStates();
        assertSame(StateMachineTestsStates.IDLE, stateMachine.getCurrentState());

        // ### Return to Shooting and simulate not having a note in shooting to test finishing
        // transition
        stateMachineWorld.shouldShoot = true;
        stateMachine.updateStates();

        // Ensure we are in WarmingUp state
        assertSame(StateMachineTestsStates.WARMINGUP, stateMachine.getCurrentState());

        // Finish WarmingUp to get to Shooting
        stateMachine.periodic();

        // Move to Shooting state
        stateMachine.updateStates();
        assertSame(StateMachineTestsStates.SHOOTING, stateMachine.getCurrentState());

        // Simulate not having a note
        stateMachineWorld.hasNote = false;
        stateMachine.periodic();
        stateMachine.updateStates();
        assertSame(StateMachineTestsStates.IDLE, stateMachine.getCurrentState());
    }

    @Test
    public void StateMachineGraphTest() {
        StateMachineWorld stateMachineWorld = new StateMachineWorld();
        StateMachine<StateMachineWorld> stateMachine = createTestStateMachine(stateMachineWorld);

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
}
