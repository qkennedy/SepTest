package P5agents;

import java.util.ArrayList;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class GatherWoodAction implements StripsAction {
    
    public ArrayList<myPeasant> gathPeas;
    public ArrayList<Integer> resIDs;
    
    public GatherWoodAction(ArrayList<myPeasant> gathPeas, ArrayList<Integer> resIDs){
        this.gathPeas = gathPeas;
        this.resIDs = resIDs;
    }
    
    @Override
    public GameState apply(GameState state) {
        GameState copy = state;
        
        for(myPeasant peas : gathPeas) {
            int index = gathPeas.indexOf(peas);
            int resID = resIDs.get(index);
            
            copy.gatherFromNode(peas.getID(), resID, ResourceType.WOOD);
        }
        
        copy.actions.push(this);
        return copy;
    }
    
    public boolean preconditionsMet(GameState state) {
        
        if(!sameSize()) {
            return false;
        }
        
        for(myPeasant peas : gathPeas) {
            int index = gathPeas.indexOf(peas);
            Position peasPos = peas.getPos();
            ResourceView view = state.getResource(resIDs.get(index));
            double xd = Math.abs(peasPos.x - view.getXPosition());
            double yd = Math.abs(peasPos.y - view.getYPosition());
            boolean emptyHold = peas.getCAmt() == 0;
            boolean precondition = ((xd <= 1 && yd <= 1) && view.getAmountRemaining() >= 100) && emptyHold;
            
            if(!precondition) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean sameSize() {
        return (gathPeas.size() == resIDs.size());
    }
    
    @Override
    public ArrayList<Action> ResultantAction() {
        
        ArrayList<Action> actionList = new ArrayList<Action>();
        
        for(myPeasant peas : gathPeas) {
            int index = gathPeas.indexOf(peas);
            int resID = resIDs.get(index);
            actionList.add(Action.createCompoundGather(peas.getID(), resID));
        }
        
        return actionList;        
    }
    
}
