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
        copy.actions.add(this);
        //TODO
        return copy;
    }
    
    public boolean preconditionsMet(GameState state) {
        Position peasPos = state.peasPos;
        for(ResourceView resView: state.woodNodes){
            double xd = Math.abs(peasPos.x - resView.getXPosition());
            double yd = Math.abs(peasPos.y - resView.getYPosition());
            if((xd <= 1 && yd <= 1) && resView.getAmountRemaining() >= 100){
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public Action ResultantAction(GameState state) {
        return Action.createCompoundGather(state.peasID, resID);
    }
    
}
