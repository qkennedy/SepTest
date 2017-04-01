package P4agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class GatherWoodAction implements StripsAction {
    
    public int pID;
    public int resID;
    public GatherWoodAction(int pID, int resID){
    	this.pID = pID; 
    	this.resID = resID;
    }
    @Override
    public GameState apply(GameState state) {
        GameState copy = state;
        copy.gatherFromNode(pID, resID, ResourceType.WOOD);
        copy.actions.push(this);
        return copy;
    }
    
    public boolean preconditionsMet(GameState state) {
        Position peasPos = state.peasPos;
        ResourceView view = state.getResource(resID);
            double xd = Math.abs(peasPos.x - view.getXPosition());
            double yd = Math.abs(peasPos.y - view.getYPosition());
            boolean emptyHold = state.pAmt == 0;
            return ((xd <= 1 && yd <= 1) && view.getAmountRemaining() >= 100) && emptyHold;
    }
    
    @Override
    public Action ResultantAction() {
        return Action.createCompoundGather(pID, resID);
    }
	@Override
	public int getPID() {
		return pID;
	}
    
}
