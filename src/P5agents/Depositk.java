package P5agents;

import java.util.List;

import P4agents.*;
import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class Depositk implements StripsAction {

    
	public int thID;
	public ResourceType type;
	public List<Integer> idleIDs;
	
    /**
     * This class takes a unit that is affected, and a unit doing the action
     * This sets the framework for how the STRIPS Actions should be implemented
     * @param uID
     * @param thID
     */
	public Depositk(int thID, List<Integer> idleIDs){
		this.thID = thID;
		this.idleIDs = idleIDs;
		
	}
    @Override
    public boolean preconditionsMet(GameState state) { //May need to reimplement to give conditions for each peasant. Peasant 1 can still deposit something even though Peasant 2 cant.
        for(int idleID : idleIDs) {
        	
        	DepositAction depot = new DepositAction(idleID, thID);
        	
        	if(!depot.preconditionsMet(state)) {
        		return false;
        	}
        	
        }
        
        return (idleIDs.size() <= 3);
    }
    
    @Override
    public GameState apply(GameState state) {
    	GameState copy = state;
    	for(int id : idleIDs) {
    		copy.deposit(id, thID);
    	}
        copy.actions.push(this);
        //TODO Add Cost Changes
        return copy;
    }
    
    @Override
    public Action ResultantAction() {
    	return null; //need to reimplement to return a list of new actions.
        //return Action.createCompoundDeposit(pID, thID);
    }
	@Override
	public int getPID() {
		// TODO Auto-generated method stub
		return 0; //This method should be removed from the interface, as it deals with the unitID of only one unit (Part 5 requires multiple units).
	}
}
