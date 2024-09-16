public class StateMachineStateData<S extends AbstractState> {

    private final S state;
    private final ArrayList<StateTransition<T>> transitions;

    public S getState(){
        return state;
    }

    public ArrayList<StateTransition<T>> getTransitions(){
        return transitions;
    }

    public StateTransition<T> getTransition(){
        
    }
    
}
