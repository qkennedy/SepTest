package P4agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

public class MoveAction implements StripsAction {

	//takes pid and direction
	//have a switch statement for preconditionsMet
	private int uID;
	private Position newPos;
	public MoveAction(int uID, Position newPos) {
		this.uID = uID;
		this.newPos = newPos;
	}
	
	@Override
	public GameState apply(GameState state) {
        GameState copy = new GameState(state);
        copy.moveUnit(state.units.get(uID),newPos.x, newPos.y);
        copy.actions.add(this);
        //TODO
        return copy;
	}
//Maybe add A* here to see if the location is actually reachable
	public boolean preconditionsMet(GameState state) {
		Position pos = newPos;
		if(!state.positionExists(pos.x, pos.y)){
			return false;
		}
		for(ResourceView view: state.Nodes){
			if(pos.x == view.getXPosition()&&pos.y == view.getYPosition()){
				return false;
			}
		}
		for(UnitView view: state.units){
			if(pos.x == view.getXPosition()&&pos.y == view.getYPosition()){
				return false;
			}
		}
		return true;
	}

	@Override
	public Action ResultantAction() {
		return Action.createCompoundMove(uID, newPos.x, newPos.y);
	}

	@Override
	public int getPID() {
		return uID;
	}
}