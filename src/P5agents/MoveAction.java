package P5agents;

import java.util.ArrayList;
import java.util.List;

import edu.cwru.sepia.action.Action;

public class MoveAction implements StripsAction {

	//takes pid and direction
	//have a switch statement for preconditionsMet
	private List<myPeasant> peasants;
	private List<PositionType> targets;
	private GameState state;
	public MoveAction(List<myPeasant> pl, List<PositionType> pt, GameState state) {
		this.peasants = pl;
		this.targets = pt;
		this.state = state;
	}
	
	@Override
	public GameState apply(GameState state) {
        GameState copy = new GameState(state);
        for(int i = 0; i<peasants.size(); i++){
        	myPeasant p = copy.peasants.get(peasants.get(i).getID());
        	p.setPosT(targets.get(i));
        	p.setPos(getPosByType(p.getID(), targets.get(i)));
        	i++;
        }
        copy.actions.push(this);
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
		} else if(type.equals(PositionType.G)){
			return state.getClosestGM(pid);
		} else {
			return state.getClosestWood(pid);
		}
	}
}