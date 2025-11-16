package coppercore.controls.state_machine;

/* Design sketch:
 * To simplify the implementation, and to allow enums to be actual states of a state machine
 * rather than merely keys, this design prototypes the following ideas
 *
 * - State is an interface implementing period, onEntry, and onExit
 * - The state machine and a generic "world" are passed as arguments ("dependency injected")
 * - `State` instances therefore do not carry any internal info regarding the state of the robot
 * - periodic signals whether a state has finished or not by returning true/false.
 *   (alternatively or in addition, a method StateMachine.finish(State) could be added)
 */

public interface State<
        StateEnumType extends Enum<StateEnumType> & State<StateEnumType, World>, World> {
    // return true if state has finished
    public boolean periodic(StateMachine<StateEnumType, World> stateMachine, World world);

    public default void onEntry(StateMachine<StateEnumType, World> stateMachine, World world) {}

    public default void onExit(StateMachine<StateEnumType, World> stateMachine, World world) {}
}
