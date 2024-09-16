package coppercore.controls.state_machine;

import java.util.ArrayList;
import java.util.HashMap;

public class StateMachine<T extends AbstractState, A extends ActionInterface> {

    protected HashMap<String, T> states = new HashMap<String, T>();
    protected ArrayList<StateTransition<T>> transitions = new ArrayList<StateTransition<T>>();
    protected T currentState;
    protected A currentAction;

    public StateMachine() {}

    public StateMachine(HashMap<String, T> states, ArrayList<StateTransition<T>> transitions) {
        this(states, transitions, states.values().iterator().next());
    }

    public StateMachine(
            HashMap<String, T> states, ArrayList<StateTransition<T>> transitions, T defaultState) {
        this.states = states;
        this.transitions = transitions;
        this.currentState = defaultState;
        this.targetState = defaultState;
    }

    public StateMachine(StateMachineJSONConfig config) {
        loadConfig(config);
    }

    public void loadConfig(StateMachineJSONConfig config) {

        this.states = (HashMap<String, T>) config.states;
        this.currentState = (T) config.defaultState;
        this.targetState = (T) config.defaultState;

        for (StateMachineJSONConfig.StateTransitionStringPair pair : config.transitionPairs) {
            registerStateTransition(pair.state1, pair.state2);
            if (pair.bothWays) {
                registerStateTransition(pair.state2, pair.state1);
            }
        }
    }

    public void periodic() {
        this.currentState.periodic();
        T nextState = (T) this.currentAction.getNextState(this);
        this.transitionToState(nextState);
    }

    public void registerState(String name, T state) {
        states.put(name, state);
        if (states.size() == 0) {
            currentState = state;
            targetState = state;
        }
    }

    public void registerStateTransition(String from, String to) {
        transitions.add(new StateTransition<T>(getState(from), getState(to)));
    }

    public void registerStateTransition(T from, T to) {
        transitions.add(new StateTransition<T>(from, to));
    }

    public String getStateName(T state) {
        for (String key : states.keySet()) {
            if (state == states.get(key)) return key;
        }
        return "State not found";
    }

    public T getState(String name) {
        return states.get(name);
    }

    public void setAction(A action){
        this.currentState = action;
    }

    public void forceSetState(T state){
        this.currentState = state;
    }

    protected boolean canTransition(T from, T to) {
        for (StateTransition transition : transitions) {
            if (transition.valid(from, to)) return true;
        }
        return false;
    }

    public boolean transitionToState(T to) {
        if (this.currentState == to) return true;
        if (!canTransition(this.currentState, to)) return false;
        this.currentState = to;
        return true;
    }
}
