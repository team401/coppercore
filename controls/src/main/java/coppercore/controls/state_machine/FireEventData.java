package coppercore.controls.state_machine;

public class FireEventData implements StateMachineEventData {
    String startState;
    String endState;
    String trigger;

    public FireEventData(String startState, String endState, String trigger) {
        this.endState = endState;
        this.startState = startState;
        this.trigger = trigger;
    }

    @Override
    public String getJsonString() {
        return "{\"startState\":\""
                + startState
                + "\",\"endState\":\""
                + endState
                + "\",\"trigger\":\""
                + trigger
                + "\"}";
    }
}
