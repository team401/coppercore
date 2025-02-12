package coppercore.controls.state_machine;

public class StateMachineEvent {
    public String type;
    public StateMachineEventData data;
    public String id;

    public StateMachineEvent(String type, StateMachineEventData data) {
        this.type = type;
        this.data = data;
        this.id = generateUniqueID();
    }

    private final String characters =
            "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM!@#$%^&*()";

    public String generateUniqueID() {
        String id = "";
        for (int i = 0; i < 16; i++) {
            int rand = (int) (Math.random() * characters.length());
            id += characters.charAt(rand);
        }
        return id;
    }

    public String getJsonString() {
        return "{\"type\":\""
                + type
                + "\",\"id\":\""
                + id
                + "\",\"data\":"
                + data.getJsonString()
                + "}";
    }
}
