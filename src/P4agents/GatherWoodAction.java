package P4agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;

public class GatherWoodAction implements StripsAction {
    
    public ResourceView resNode;
    @Override
    public GameState apply(GameState state) {
        GameState copy = state;
        copy.
        return null;
    }
    
    public boolean preconditionsMet(GameState state) {
        Position peasPos = state.peasPos;
        for(ResourceView resView: state.woodNodes){
            double xd = Math.abs(peasPos.x - resView.getXPosition());
            double yd = Math.abs(peasPos.y - resView.getYPosition());
            if((xd <= 1 && yd <= 1) && resView.getAmountRemaining() >= 100){
                resNode = resView;
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public Action ResultantAction(GameState state) {
        return Action.createCompoundGather(state.peasID, resNode.getID());
    }
    
}
