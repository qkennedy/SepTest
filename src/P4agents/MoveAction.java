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
	private Direction direction;
	private Position p;
	public MoveAction(int uID, Direction direction) {
		this.uID = uID;
		this.direction = direction;
	}
	
	@Override
	public GameState apply(GameState state) {
		if(p == null){
			p = newPos(state);
		}
        GameState copy = state;
        copy.moveUnit(state.units.get(uID),p.x, p.y);
        copy.actions.add(this);
        //TODO
        return copy;
	}

	public boolean preconditionsMet(GameState state) {
		Position pos = newPos(state);
		if(!state.positionExists(pos.x, pos.y)){
			return false;
		}
		for(ResourceView view: state.goldNodes){
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
	public Action ResultantAction(GameState state) {
		if(p == null){
			p = newPos(state);
		}
		return Action.createCompoundMove(uID, p.x, p.y);
	}
	public Position newPos(GameState state){
		UnitView view = state.units.get(uID);
		Position orig = new Position(view.getXPosition(), view.getYPosition());
		switch(direction){
		case NORTH:
			return new Position(orig.x, orig.y - 1);
		case NORTHEAST:
			return new Position(orig.x +1, orig.y - 1);
		case EAST:
			return new Position(orig.x +1, orig.y);
		case SOUTHEAST:
			return new Position(orig.x + 1, orig.y + 1);
		case SOUTH:
			return new Position(orig.x, orig.y + 1);
		case SOUTHWEST:
			return new Position(orig.x -1, orig.y - 1);
		case WEST:
			return new Position(orig.x - 1, orig.y);
		case NORTHWEST:
			return new Position(orig.x - 1, orig.y - 1);
		}
		return null;
	}

}