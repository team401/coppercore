package coppercore.controls.state_machine.transition.transitions;

import java.util.function.BooleanSupplier;

/** Transition with condition */
public class ConditionalTransition<State, Trigger> extends Transition<State, Trigger> {

    private final BooleanSupplier check;

    /**
     * Creates condition with condition
     *
     * @param source Transition Source
     * @param destination Transition Destination
     * @param trigger Transition Trigger
     * @param check Condition Supplier
     */
    public ConditionalTransition(
            State source, State destination, Trigger trigger, BooleanSupplier check) {
        super(source, destination, trigger);
        this.check = check;
    }

    @Override
    public boolean canTransition() {
        return isCheckTrue();
    }

    /**
     * Returns if the check is true
     *
     * @return check value
     */
    public boolean isCheckTrue() {
        return check != null && check.getAsBoolean();
    }
}
