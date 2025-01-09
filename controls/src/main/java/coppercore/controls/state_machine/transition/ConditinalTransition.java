package coppercore.controls.state_machine.transition;

import java.util.function.BooleanSupplier;

public class ConditinalTransition<State, Trigger> extends Transition<State, Trigger> {

    private final BooleanSupplier check;

    public ConditinalTransition(
            State source, State destination, Trigger trigger, BooleanSupplier check) {
        this(source, destination, trigger, check, false);
    }

    public ConditinalTransition(
            State source,
            State destination,
            Trigger trigger,
            BooleanSupplier check,
            boolean internalTransition) {
        super(source, destination, trigger, internalTransition);
        this.check = check;
    }

    public boolean canTransition() {
        return isCheckTrue();
    }

    public boolean isCheckTrue() {
        return check != null && check.getAsBoolean();
    }
}
