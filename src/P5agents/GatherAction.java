package P5agents;

import java.util.ArrayList;
import java.util.List;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class GatherAction implements StripsAction {
    
    public List<myPeasant> gathPeas;
    public List<Integer> resIDs;
    
    public GatherAction(List<myPeasant> pList, List<Integer> rList){
        this.gathPeas = pList;
        this.resIDs = rList;
    }
    
    @Override
    public GameState apply(GameState state) {
        GameState copy = state;
        
        for(myPeasant peas : gathPeas) {
            int index = gathPeas.indexOf(peas);
            int resID = resIDs.get(index);
            
            copy.gatherFromNode(peas.getID(), resID);
        }
        
        copy.actions.push(this);
        return copy;
    }
    
    public boolean preconditionsMet(GameState state) {
        
        if(!sameSize() || gathPeas.isEmpty()) {
            return false;
        }
   
        
        for(myPeasant p : gathPeas) {
            int index = gathPeas.indexOf(p);
            ResourceView view = state.getResource(resIDs.get(index));
            boolean emptyHold = p.getCAmt() == 0;
            boolean precondition = ((p.getPosT().equals(PositionType.G) || p.getPosT().equals(PositionType.W)) && view.getAmountRemaining() >= 100) && emptyHold;
            
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
