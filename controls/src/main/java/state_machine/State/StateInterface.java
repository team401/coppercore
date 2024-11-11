package coppercore.controls;

public interface StateInterface {

    public default void onEntry(Transition transition) {}

    public default void onExit(Transition transition) {}
}
