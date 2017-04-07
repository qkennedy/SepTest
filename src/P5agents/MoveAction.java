package P5agents;

import java.util.ArrayList;
import java.util.List;

import edu.cwru.sepia.action.Action;

public class MoveAction implements StripsAction {

	//takes pid and direction
	//have a switch statement for preconditionsMet
	private ArrayList<myPeasant> peasants;
	private ArrayList<PositionType> targets;
	private GameState state;
	public MoveAction(ArrayList<myPeasant> pList, ArrayList<PositionType> targets, GameState state) {
		this.peasants = pList;
		this.targets = targets;
	}
	
	@Override
	public GameState apply(GameState state) {
        GameState copy = new GameState(state);
        int i = 0;
        for(myPeasant p: peasants){
        	p.setPosT(targets.get(i));
        	i++;
        }
        return copy;
	}
//Maybe add A* here to see if the location is actually reachable
	public boolean preconditionsMet(GameState state) {
		
		for(PositionType pos: targets){
			if(pos.equals(PositionType.G)){
				if(state.goldNodes.isEmpty()){
					return false;
				}
			}
			if(pos.equals(PositionType.W)){
				if(state.woodNodes.isEmpty()){
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public List<Action> ResultantAction() {
		List<Action> actions = new ArrayList<Action>();
		for(int i = 0; i<peasants.size();i++){
			myPeasant p = peasants.get(i);
			Position pos = getPosByType(p.getID(), targets.get(i));
			actions.add(Action.createCompoundMove(p.getID(), pos.x, pos.y));
		}
		return actions;
	}
	public Position getPosByType(int pid, PositionType type){
		if(type.equals(PositionType.TH)){
			return state.thPos;
		} else if(type.equals(PositionType.TH)){
			return state.getClosestGM(pid);
		} else {
			return state.getClosestWood(pid);
		}
	}
}