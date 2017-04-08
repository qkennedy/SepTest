package P5agents;

import java.util.ArrayList;

import P5agents.GameState;
import P5agents.StripsAction;
import edu.cwru.sepia.action.Action;

public class BuildPeasant implements StripsAction {
    
    private int thID;
    private int tempID;
    
    public BuildPeasant(int thID, int tempID) {
        this.thID = thID;
        this.tempID = tempID;
    }
    
    @Override
    public boolean preconditionsMet(GameState state) {
        
        double peasBuyout = 400.0;
        int foodAmt = state.foodAmt;
        
        return (state.gold >= peasBuyout && foodAmt > 0 && state.buildPeasants);
    }
    
    @Override
    public GameState apply(GameState state) {
        GameState copy = state;
        //include method to add new peasant to the state.
        //state.createUnit();
        copy.addPeasant();
        copy.actions.push(this);
        return copy;
    }
    
    @Override
    public ArrayList<Action> ResultantAction() {
        ArrayList<Action> actionList = new ArrayList<Action>();
        
        actionList.add(Action.createCompoundProduction(thID, tempID));
        
        return actionList;
    }
    
}
