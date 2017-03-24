package P4agents;

import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class DepositAction implements StripsAction {

	/**
	 * This class takes a unit that is affected, and a unit doing the action
	 * This sets the framework for how the STRIPS Actions should be implemented
	 * @param uID
	 * @param tcID
	 */
	@Override
	public boolean preconditionsMet(GameState state) {
		UnitView peas = state.units.get(state.peasID);
		Position peasPos = state.peasPos;
		Position thPos = state.thPos;
		double xd = Math.abs(peasPos.x - thPos.x);
		double yd = Math.abs(peasPos.y - thPos.y);
		return((xd <= 1 && yd <= 1) && peas.getCargoAmount()>0);
	}

	@Override
	public GameState apply(GameState state) {
		// TODO Auto-generated method stub
		return null;
	}
}
