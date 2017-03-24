package P4agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.util.Direction;

public class MoveAction implements StripsAction {

	//takes pid and direction
	//have a switch statement for preconditionsMet
	private int uid;
	private Direction direction;
	
	public MoveAction(int uid, Direction direction) {
		this.uid = uid;
		this.direction = direction;
	}
	
	@Override
	public GameState apply(GameState state) {
		return null;
	}

	public boolean preconditionsMet(GameState state) {

		return true;
	}

	@Override
	public Action ResultantAction(GameState state) {
		return Action.createCompoundMove(unitid, x, y)
	}

}