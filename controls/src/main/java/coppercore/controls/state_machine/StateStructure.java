package coppercore.controls.state_machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateStructure {
    public Map<String, List<TransitionStructure>> transitions;

    public void addTransition(String trigger, TransitionStructure structure){
        if (!transitions.containsKey(trigger)){
            transitions.put(trigger, new ArrayList<>());
        }
        transitions.get(trigger).add(structure);
    }

    public StateStructure() {
        transitions = new HashMap<>();
    }

    public StringBuilder getJSONString(StringBuilder builder){
        builder.append("{'Transitions':{");
        transitions.keySet().iterator().forEachRemaining((String key) -> {
            builder.append("'").append(key).append("':[");
            transitions.get(key).iterator().forEachRemaining((TransitionStructure structure) -> {
                structure.getJSONString(builder);
            });
            builder.append("],");
        });
        return builder;
    }

    public String getJSONString(){
        return getJSONString(new StringBuilder()).toString();
    }
}
