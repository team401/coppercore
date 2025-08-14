package coppercore.controls.state_machine.transition.transitions;

import java.util.function.BooleanSupplier;

import coppercore.controls.state_machine.transition.TransitionBase;

/** Transition with condition */
public class ConditionalTransition<State, Trigger> extends TransitionBase<State, Trigger> {

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
        this(source, destination, trigger, check, false);
    }

    /**
     * Creates condition with condition
     *
     * @param source Transition Source
     * @param destination Transition Destination
     * @param trigger Transition Trigger
     * @param check Condition Supplier
     * @param internalTransition is Interal Transition
     */
    public ConditionalTransition(
            State source,
            State destination,
            Trigger trigger,
            BooleanSupplier check,
            boolean internalTransition) {
        super(source, destination, trigger, internalTransition);
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
