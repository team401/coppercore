package coppercore.controls.state_machine;

public class FunctionalState<StateKey extends Enum<StateKey>> extends State<StateKey> {

    private final Runnable periodicFunction;

    public FunctionalState(Runnable periodicFunction) {
        super();
        this.periodicFunction = periodicFunction;
    }

    @Override
    protected void periodic() {
        periodicFunction.run();
    }
}
