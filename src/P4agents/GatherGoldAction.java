package P4agents;

import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;

public class GatherGoldAction implements StripsAction {


	@Override
	public GameState apply(GameState state) {
		
	}

	public boolean preconditionsMet(GameState state) {
		Position peasPos = state.peasPos;
		for(ResourceView resView: state.resNodes){
			double xd = Math.abs(peasPos.x - resView.getXPosition());
			double yd = Math.abs(peasPos.y - resView.getYPosition());
			if((xd <= 1 && yd <= 1) && resView.getAmountRemaining() >= 100){
				return true;
			}
		}
		
		return false;
	}

}
