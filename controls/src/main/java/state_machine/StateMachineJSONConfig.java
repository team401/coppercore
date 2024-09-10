package coppercore.controls.state_machine;

import coppercore.parameter_tools.JSONSync;
import java.util.ArrayList;
import java.util.HashMap;

public class StateMachineJSONConfig {
    private static transient JSONSync<StateMachineJSONConfig> sync =
            new JSONSync<StateMachineJSONConfig>(
                    new StateMachineJSONConfig(),
                    "filePath",
                    new JSONSync.JSONSyncConfigBuilder().build());

    protected ArrayList<StateTransitionStringPair> transitionPairs;
    protected transient AbstractState defaultState;
    protected transient HashMap<String, AbstractState> states;

    private StateMachineJSONConfig() {}

    public StateMachineJSONConfig getConfig(String file, HashMap<String, AbstractState> states) {
        return getConfig(file, states, states.values().iterator().next());
    }

    public StateMachineJSONConfig getConfig(
            String file, HashMap<String, AbstractState> states, AbstractState defaultState) {
        sync.setFile(file);
        sync.loadData();
        StateMachineJSONConfig instance = sync.getObject();
        instance.defaultState = defaultState;
        return instance;
    }

    protected static class StateTransitionStringPair {
        public String state1;
        public String state2;
        public boolean bothWays;
    }
}
