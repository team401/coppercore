package coppercore.controls.state_machine;

/** A state that executes a provided periodic function. */
public class FunctionalState<StateKey extends Enum<StateKey>, World> extends State<StateKey, World> {

    private final Runnable periodicFunction;

    /**
     * Constructs a FunctionalState with the given periodic function.
     *
     * @param periodicFunction The function to be executed periodically while in this state
     */
    private FunctionalState(Runnable periodicFunction) {
        this.periodicFunction = periodicFunction;
    }

    public static FunctionalState of(Runnable periodicFunction) {
        return new FunctionalState<>(periodicFunction);
    }

    /** Executes the periodic function. */
    @Override
    protected void periodic() {
        periodicFunction.run();
    }
}
