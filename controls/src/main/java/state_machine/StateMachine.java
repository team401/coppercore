package coppercore.controls.state_machine;

import java.util.ArrayList;
import java.util.HashMap;

public class StateMachine<T extends AbstractState> {

    protected HashMap<String, T> states = new HashMap<String, T>();
    protected ArrayList<StateTransition<T>> transitions = new ArrayList<StateTransition<T>>();
    protected T currentState;
    protected T targetState;

    public StateMachine() {}

    public StateMachine(HashMap<String, T> states, ArrayList<StateTransition<T>> transitions){
        this.states = states;
        this.transitions = transitions;
        this.currentState = states.keySet.getIterator().getNext();
        this.targetState = this.currentState;
    }

    public StateMachine(HashMap<String, T> states, ArrayList<StateTransition<T>> transitions, T defaultState){
        this.states = states;
        this.transitions = transitions;
        this.currentState = defaultState;
        this.targetState = defaultState;
    }

    public void periodic() {
        this.currentState.periodic();
        T nextState = (T) this.currentState.getNextState(this.targetState);
        this.transitionToState(nextState);
    }

    public void registerState(String name, T state) {
        states.put(name, state);
        if (states.size() == 0){
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

    protected boolean canTransition(T from, T to) {
        for (StateTransition transition : transitions) {
            if (transition.valid(from, to)) return true;
        }
        return false;
    }

    public boolean transitionToState(T to) {
        if (this.currentState == to) return false;
        if (!canTransition(this.currentState, to)) return false;
        this.currentState.onExit();
        this.currentState = to;
        return true;
    }
}
