package P4agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class DepositAction implements StripsAction {
    
	public int pID, thID;
	public ResourceType type;
    /**
     * This class takes a unit that is affected, and a unit doing the action
     * This sets the framework for how the STRIPS Actions should be implemented
     * @param uID
     * @param thID
     */
	public DepositAction(int pID, int thID){
		this.pID = pID;
		this.thID = thID;
	}
    @Override
    public boolean preconditionsMet(GameState state) {

        Position peasPos = state.peasPos;
        Position thPos = state.thPos;
        double xd = Math.abs(peasPos.x - thPos.x);
        double yd = Math.abs(peasPos.y - thPos.y);
        return((xd <= 1 && yd <= 1) && state.pAmt>0);
    }
    
    @Override
    public GameState apply(GameState state) {
    	GameState copy = state;
        copy.deposit(pID, thID);
        copy.actions.push(this);
        //TODO Add Cost Changes
        return copy;
    }
    
    @Override
    public Action ResultantAction() {
        return Action.createCompoundDeposit(pID, thID);
    }
	@Override
	public int getPID() {
		// TODO Auto-generated method stub
		return pID;
	}
}
