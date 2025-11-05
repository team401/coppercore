package coppercore.controls.state_machine;

import java.util.function.Function;

public class FunctionalState<StateKey extends Enum> extends State<StateKey> {

    private final Function<Void, Void> periodicFunction;

    public FunctionalState(Function<Void, Void> periodicFunction) {
        super();
        this.periodicFunction = periodicFunction;
    }

    @Override
    protected void periodic() {
        periodicFunction.apply(null);
    }
    
}
