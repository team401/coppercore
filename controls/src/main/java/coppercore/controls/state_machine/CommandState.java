package coppercore.controls.state_machine;

import edu.wpi.first.wpilibj2.command.Command;

public class CommandState<StateKey extends Enum<StateKey>> extends State<StateKey> {

    private final Command command;

    /**
     * Constructs a CommandState with the given command.
     * @param command Command to run
     */
    public CommandState(Command command) {
        this.command = command;
    }

    /**
     * Initializes command to get it ready to execute in the periodic
     */
    @Override
    protected void onEntry() {
        command.initialize();
    }

    /**
     * Calls command exit and passes if transition was forced.
     */
    @Override
    protected void onExit() {
        // Command wants to know if it ended normally or was interrupted
        command.end(isFinished());
    }

    /** 
     * Executes the Command and checks if it is finished.
     */
    @Override
    protected void periodic() {
        command.execute();
        if (command.isFinished()) {
            finish();
        }
    }
}
