package P1agents;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class MyCombatAgent extends Agent {
	
	private int enemyPlayerNum = 1;

	public MyCombatAgent(int playernum, String[] otherargs) {
		super(playernum);
		
		if(otherargs.length > 0)
		{
			enemyPlayerNum = new Integer(otherargs[0]);
		}
		
		System.out.println("Constructed MyCombatAgent");
	}

	@Override
	public Map<Integer, Action> initialStep(StateView newstate,
			HistoryView statehistory) {
		// This stores the action that each unit will perform
		// if there are no changes to the current actions then this
		// map will be empty
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		
		// This is a list of all of your units
		// Refer to the resource agent example for ways of
		// differentiating between different unit types based on
		// the list of IDs
		List<Integer> myUnitIDs = newstate.getUnitIds(playernum);
		
		// This is a list of enemy units
		List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);
		
		if(enemyUnitIDs.size() == 0)
		{
			// Nothing to do because there is no one left to attack
			return actions;
		}

		//This block of text sends all units to the corner, with the ballistas being in the back row.
		actions.put(myUnitIDs.get(0), Action.createCompoundMove(myUnitIDs.get(0), 1, 16));
		actions.put(myUnitIDs.get(1), Action.createCompoundMove(myUnitIDs.get(1), 2, 17));
		actions.put(myUnitIDs.get(2), Action.createCompoundMove(myUnitIDs.get(2), 17, 8));
		actions.put(myUnitIDs.get(3), Action.createCompoundMove(myUnitIDs.get(3), 0, 16));
		actions.put(myUnitIDs.get(4), Action.createCompoundMove(myUnitIDs.get(4), 2, 18));
		actions.put(myUnitIDs.get(5), Action.createCompoundMove(myUnitIDs.get(5), 0, 17));
		actions.put(myUnitIDs.get(6), Action.createCompoundMove(myUnitIDs.get(6), 1, 18));

		return actions;
	}

	@Override
	public Map<Integer, Action> middleStep(StateView newstate,
			HistoryView statehistory) {
		
		// This stores the action that each unit will perform
		// if there are no changes to the current actions then this
		// map will be empty
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		
		// This is a list of enemy units
		List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);
		
		List<Integer> myUnitIDs = newstate.getUnitIds(1);
		
		if(enemyUnitIDs.size() == 0) //Changed to accommodate unitIDs
		{
			// Nothing to do because there is no one left to attack
			return actions;
		}
		
		int currentStep = newstate.getTurnNumber();

		// go through the action history
		for(ActionResult feedback : statehistory.getCommandFeedback(playernum, currentStep-1).values())
		{

			int unitID = feedback.getAction().getUnitId();
			UnitView unitView = newstate.getUnit(unitID);
			
			// if the previous action is no longer in progress (either due to failure or completion)
			if(feedback.getFeedback() != ActionFeedback.INCOMPLETE)
			{
				//This moves the singular runner footman into the proper defense position.
				if (unitID == 8 && enemyUnitIDs.size() == 6) {
					actions.put(unitID, Action.createCompoundMove(unitID, 1, 17));
				}
				
				//Once the runner is in the correct defense position, then the ballistas attack all enemy footmen.
				if (unitID == 8 && (unitView.getXPosition() == 1 && unitView.getYPosition() == 17)) {
					
					for(int i = 0; i < enemyUnitIDs.size() - 1; i++) {
						actions.put(11, Action.createCompoundAttack(11, enemyUnitIDs.get(i)));
						actions.put(12, Action.createCompoundAttack(12, enemyUnitIDs.get(i)));
					}
				}
				
				if (feedback.getAction().getType() == ActionType.COMPOUNDATTACK) {
					
					//If the last move was an attack and there are still enemy footmen on the board, attack the enemy footmen.
					if (enemyUnitIDs.size() != 1) {
						for(int i = 0; i < enemyUnitIDs.size() - 1; i++) {
							actions.put(11, Action.createCompoundAttack(11, enemyUnitIDs.get(i)));
							actions.put(12, Action.createCompoundAttack(12, enemyUnitIDs.get(i)));
						}	
					}
					else {   //Otherwise, attack the enemy tower.
						actions.put(myUnitIDs.get(5), Action.createCompoundAttack(myUnitIDs.get(5), enemyUnitIDs.get(0)));
						actions.put(myUnitIDs.get(6), Action.createCompoundAttack(myUnitIDs.get(6), enemyUnitIDs.get(0)));
					}
					
				}
				
				//If only the tower is left, move one of the archers out of the way, and let the ballistas reposition to attack said tower.
				if (enemyUnitIDs.size() == 1) {
					
					actions.put(myUnitIDs.get(4), Action.createCompoundMove(myUnitIDs.get(4), 24, 18));
					actions.put(myUnitIDs.get(5), Action.createCompoundAttack(myUnitIDs.get(5), enemyUnitIDs.get(0)));
					actions.put(myUnitIDs.get(6), Action.createCompoundAttack(myUnitIDs.get(6), enemyUnitIDs.get(0)));
				}

			} 
			
			//If, by any chance, a unit is unable to reach his destination by the end of the turn, then tell him to move to that same position again.
			if(feedback.getFeedback() == ActionFeedback.FAILED && feedback.getAction().getType() == ActionType.COMPOUNDMOVE && enemyUnitIDs.size() != 1) {
				
				int[] unitXPosition = {1, 2, 2, 0, 2, 0, 1};
				int[] unitYPosition = {16, 17, 16, 16, 18, 17, 18};
				
				int unitPositionInList = myUnitIDs.indexOf(unitID);
				actions.put(unitID, Action.createCompoundMove(unitID, unitXPosition[unitPositionInList], unitYPosition[unitPositionInList]));	
					
			}
			
		}

		return actions;
	}
	
	@Override
	public void terminalStep(StateView newstate, HistoryView statehistory) {
		System.out.println("Finished the episode");
	}

	@Override
	public void savePlayerData(OutputStream os) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadPlayerData(InputStream is) {
		// TODO Auto-generated method stub

	}
}