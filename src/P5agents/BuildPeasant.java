package P5agents;

import P4agents.GameState;
import P4agents.StripsAction;
import edu.cwru.sepia.action.Action;

public class BuildPeasant implements StripsAction {

	private int pID;
	private int thID;
	private GameState state;
	
	public BuildPeasant(int pID, int thID, GameState state) {
		this.pID = pID;
		this.thID = thID;
		this.state = state;
	}
	
	@Override
	public int getPID() {
		return pID;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		int numUnits = state.getUnitSize(); //substituting for food for now, as I am not sure how to get the amount of food that a townhall has.
		int maxUnits = 4;
		double peasBuyout = 400.0;
		
		return (state.gold >= peasBuyout && numUnits < maxUnits && state.buildPeasants);
	}

	@Override
	public GameState apply(GameState state) {
		GameState copy = state;
		//include method to add new peasant to the state.
		copy.actions.push(this);
		return copy;
	}

	@Override
	public Action ResultantAction() {
		int tempID = state.getPeasantTemplateID();
		return Action.createCompoundProduction(thID, tempID);
	}

}
