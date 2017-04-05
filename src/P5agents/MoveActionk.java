package P5agents;

import java.util.List;

import P4agents.*;
import edu.cwru.sepia.action.Action;

public class MoveActionk implements StripsAction {

	private List<Integer> idleIDs;
	private List<Position> newPos;

	
	public MoveActionk(List<Integer> idleIDs, List<Position> newPos) {
		this.idleIDs = idleIDs;
		this.newPos = newPos;
	}
	
	@Override
	public int getPID() {
		if(idleIDs.size() == 0) 
			return -1;
		else 
			return idleIDs.get(0);
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		int index = 0;
		
		if(idleIDs.size() != newPos.size()) {
			return false;
		}
		
		while(index != idleIDs.size() - 1) {
			int unitID = idleIDs.get(index);
			Position newUnPos = newPos.get(index);
			
			MoveAction move = new MoveAction(unitID, newUnPos);
			
			if(!move.preconditionsMet(state)) {
				return false;
			}
		}
		
		return (idleIDs.size() <= 3);
	}

	@Override
	public GameState apply(GameState state) {
		int index = 0;
		GameState copy = state;
		
		while(index != idleIDs.size() - 1) {
			int unitID = idleIDs.get(index);
			Position newUnPos = newPos.get(index);
			
			copy.moveUnit(state.units.get(unitID), newUnPos.x, newUnPos.y);
		}
		
		
        copy.actions.push(this);
		
		return null;
	}

	@Override
	public Action ResultantAction() {
		// TODO Auto-generated method stub
		return null; //need to modify this code to return a list of actions (one for each move).
	}

}
