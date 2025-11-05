package coppercore.controls.state_machine;

import java.util.function.Supplier;

public class FunctionalState<StateKey extends Enum> extends State<StateKey> {

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
