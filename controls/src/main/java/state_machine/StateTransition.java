package coppercore.controls.state_machine;

public class StateTransition<T extends AbstractState> {

    public final T from;
    public final T to;

    public StateTransition(T from, T to) {
        this.from = from;
        this.to = to;
    }

    public boolean valid(T from, T to) {
        return (this.from == from && this.to == to);
    }
}
