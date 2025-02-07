package coppercore.controls.state_machine;

public class TransitionStructure {

    public String type;
    public String destination;

    public TransitionStructure(String type, String destination){
        this.type = type;
        this.destination = destination;
    }

    public StringBuilder getJSONString(StringBuilder builder){ 
        return builder.append("type:").append(type).append(",destination:").append(destination).append(",");
    }

    public String getJSONString(){
        return getJSONString(new StringBuilder()).toString();
    } 
}
