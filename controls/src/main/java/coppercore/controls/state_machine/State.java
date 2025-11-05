package coppercore.controls.state_machine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public abstract class State<StateKey extends Enum<StateKey>> {

    protected final record Transition<TStateKey>(TStateKey nextState, BooleanSupplier condition) {}

    protected boolean finished = false;
    protected final List<Transition<StateKey>> transitions;

    public State() {
        this.transitions = new ArrayList<>();
    }

    private boolean isFinished() {
        return finished;
    }

    public final void transitionWhen(BooleanSupplier condition, StateKey state) {
        transitions.add(new Transition<>(state, condition));
    }

    public final void transitionWhenFinished(StateKey state) {
        transitions.add(new Transition<>(state, this::isFinished));
    }

    public final void transitionWhenFinishedAnd(BooleanSupplier condition, StateKey state) {
        transitions.add(
                new Transition<>(state, () -> this.isFinished() && condition.getAsBoolean()));
    }

    StateKey checkTransitions() {
        for (Transition<StateKey> transition : transitions) {
            if (transition.condition.getAsBoolean()) {
                return transition.nextState;
            }
        }
        return null;
    }

    protected final void _onEntry() {
        finished = false;
        onEntry();
    }

    protected final void _onExit() {
        onExit();
    }

    protected final void _onFinish() {
        onFinish();
    }

    protected final void _periodic() {
        periodic();
    }

    protected final void finish() {
        finished = true;
        _onFinish();
    }

    protected void onEntry() {}

    protected void onExit() {}

    protected void onFinish() {}

    protected abstract void periodic();
}
