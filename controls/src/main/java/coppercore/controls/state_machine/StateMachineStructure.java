package coppercore.controls.state_machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateMachineStructure {
    public Map<String, StateStructure> states;
    public List<String> triggers;

    public StateMachineStructure(){
        states = new HashMap<>();
        triggers = new ArrayList<>();
    }

    public void addState(String name, StateStructure state){
        states.put(name, state);
    }

    public void addTrigger(String trigger){
        triggers.add(trigger);
    }

    public StringBuilder getJSONString(StringBuilder builder){
        builder.append("{'States':{");
        states.keySet().iterator().forEachRemaining((String key) -> {
            builder.append("'").append(key).append("':");
            states.get(key).getJSONString(builder);
            builder.append(",");
        });
        builder.append("},'Triggers':[");
        triggers.iterator().forEachRemaining((String trigger) -> {
            builder.append(trigger).append(",");
        });
        builder.append("],}");
        return builder;
    }

    public String getJSONString(){
        return getJSONString(new StringBuilder()).toString();
    }
}
