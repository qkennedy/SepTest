package P5agents;

import java.util.ArrayList;

import edu.cwru.sepia.action.Action;


public class DepositAction implements StripsAction {
    
    public int thID;
    private ArrayList<myPeasant> depPeas;
    
    /**
     * This class takes a unit that is affected, and a unit doing the action
     * This sets the framework for how the STRIPS Actions should be implemented
     * @param depPeas
     * @param thID
     */
    public DepositAction(ArrayList<myPeasant> depPeas, int thID){
        this.depPeas = depPeas;
        this.thID = thID;
    }
    
    @Override
    public boolean preconditionsMet(GameState state) {
        
        for(myPeasant peas : depPeas) {
            Position peasPos = peas.getPos();
            Position thPos = state.thPos;
            int amt = peas.getCAmt();
            double xd = Math.abs(peasPos.x - thPos.x);
            double yd = Math.abs(peasPos.y - thPos.y);
            boolean precondition = (xd <= 1 && yd <= 1) && amt > 0;
            
            if(!precondition) {
                return false;
            }
        }
        
        return true;
        
    }
    
    @Override
    public GameState apply(GameState state) {
        GameState copy = state;
        
        for(myPeasant peas : depPeas) {
            copy.deposit(peas.getID(), thID);
        }
        
        copy.actions.push(this);
        
        return copy;
    }
    
    @Override
    public ArrayList<Action> ResultantAction() {
        
        ArrayList<Action> actionList = new ArrayList<Action>();
        
        for(myPeasant peas : depPeas) {
            actionList.add(Action.createCompoundDeposit(peas.getID(), thID));
        }
        
        return actionList;
    }

}
