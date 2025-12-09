package coppercore.controls.test;

import static org.junit.jupiter.api.Assertions.assertSame;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import coppercore.controls.test.StateMachineTestsStates.StateMachineWorld;
import org.junit.jupiter.api.Test;

public class StateMachineTests {

	// This method exist to keep the Transition Test State Machine and the State Machine Graph State Machine the same
	StateMachine<StateMachineWorld> createTestStateMachine(StateMachineWorld stateMachineWorld) {
		StateMachine<StateMachineWorld> stateMachine = new StateMachine<>(stateMachineWorld);

		// Registering States
        State<StateMachineWorld> idleState =
                stateMachine.registerState("Idle", StateMachineTestsStates.IDLE);
        State<StateMachineWorld> intakingState =
                stateMachine.registerState("Intaking", StateMachineTestsStates.INTAKING);
        State<StateMachineWorld> warmingUpState =
                stateMachine.registerState("WarmingUp", StateMachineTestsStates.WARMINGUP);
        State<StateMachineWorld> shootingState =
                stateMachine.registerState("Shooting", StateMachineTestsStates.SHOOTING);

		// Defining Transitions

        // Transitions that can happen from Idle state
        idleState
                .when((StateMachineWorld world) -> world.shouldIntake && !world.hasNote)
                .transitionTo(intakingState);
        idleState
                .when((StateMachineWorld world) -> world.shouldShoot && world.hasNote)
                .transitionTo(warmingUpState);

        // Transitions that can happen from Intaking state
        intakingState.whenFinished().transitionTo(idleState);
        intakingState
                .when((StateMachineWorld world) -> !world.shouldIntake)
                .transitionTo(idleState);

        // Transitions that can happen from WarmingUp state
        warmingUpState.whenFinished().transitionTo(shootingState);
        warmingUpState
                .when((StateMachineWorld world) -> !world.shouldShoot)
                .transitionTo(idleState);
        warmingUpState.whenRequested(idleState).transitionTo(idleState);

        // Transitions that can happen from Shooting state
        shootingState.whenFinished().transitionTo(idleState);
        shootingState.when((StateMachineWorld world) -> !world.shouldShoot).transitionTo(idleState);
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
}
