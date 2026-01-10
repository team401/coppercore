package coppercore.controls.test;

import static coppercore.controls.test.StateMachineTests.Robot;
import static org.junit.jupiter.api.Assertions.fail;

import coppercore.controls.state_machine.StateMachine;
import java.io.File;
import java.io.PrintWriter;
import org.junit.jupiter.api.Test;

public class StateMachineTestsGraph {

    @Test
    public void StateMachineGraphTest() {
        Robot stateMachineWorld = new Robot();
        StateMachine<Robot> stateMachine =
                StateMachineTests.createTestStateMachine(
                        stateMachineWorld,
                        StateMachineTestsStates.IDLE,
                        StateMachineTestsStates.INTAKING,
                        StateMachineTestsStates.WARMING_UP,
                        StateMachineTestsStates.SHOOTING);

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
