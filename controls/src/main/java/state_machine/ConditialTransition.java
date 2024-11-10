package coppercore.controls;

import java.util.function.BooleanSupplier;

public class ConditialTransition<State, Trigger> extends Transition<State, Trigger> {

    private final BooleanSupplier check;

    public ConditialTransition(
            State source, State destination, Trigger trigger, BooleanSupplier check) {
        super(source, destination, trigger);
        this.check = check;
    }

    public boolean canTransition() {
        return isCheckTrue();
    }

    public boolean isCheckTrue() {
        return check != null && check.getAsBoolean();
    }
}
