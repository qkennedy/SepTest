package P1agents;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.LocatedProductionAction;
import edu.cwru.sepia.action.ProductionAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class MyResourceAgent extends Agent {

	public MyResourceAgent(int playernum) {
		super(playernum);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = -7481143097108592969L;

	public Map initialStep(StateView newstate, HistoryView statehistory) {
		return middleStep(newstate, statehistory);
	}

	public Map middleStep(StateView newstate, HistoryView statehistory) {
		// This stores the action that each unit will perform
		// if there are no changes to the current actions then this
		// map will be empty.
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		
		// this will return a list of all of your units
		// You will need to check each unit ID to determine the unit's type
		List<Integer> myUnitIds = newstate.getUnitIds(playernum);
		
		// These will store the Unit IDs that are peasants and townhalls respectively
		List<Integer> peasantIds = new ArrayList<Integer>();
		List<Integer> townhallIds = new ArrayList<Integer>();
		List<Integer> farmIds = new ArrayList<Integer>();
		List<Integer> barracksIds = new ArrayList<Integer>();
		List<Integer> footmanIds = new ArrayList<Integer>();
		// This loop will examine each of our unit IDs and classify them as either
		// a Townhall or a Peasant
		for(Integer unitID : myUnitIds)
		{
			// UnitViews extract information about a specified unit id
			// from the current state. Using a unit view you can determine
			// the type of the unit with the given ID as well as other information
			// such as health and resources carried.
			UnitView unit = newstate.getUnit(unitID);
			
			// To find properties that all units of a given type share
			// access the UnitTemplateView using the `getTemplateView()`
			// method of a UnitView instance. In this case we are getting
			// the type name so that we can classify our units as Peasants and Townhalls
			String unitTypeName = unit.getTemplateView().getName();
			/* Here I find all of my available units, so that I can give them commands as soon as they 
			 * spawn, and have an easy way to keep track of all unit IDs
			 */
			if(unitTypeName.equals("TownHall"))
				townhallIds.add(unitID);
			else if(unitTypeName.equals("Peasant"))
				peasantIds.add(unitID);
			else if(unitTypeName.equals("Farm"))
				farmIds.add(unitID);
			else if(unitTypeName.equals("Barracks"))
				barracksIds.add(unitID);
			else if(unitTypeName.equals("Footman"))
				footmanIds.add(unitID);
			else
				System.err.println("Unexpected Unit type: " + unitTypeName);
		}
		
		// get the amount of wood and gold you have in your Town Hall
		int currentGold = newstate.getResourceAmount(playernum, ResourceType.GOLD);
		int currentWood = newstate.getResourceAmount(playernum, ResourceType.WOOD);
		
		List<Integer> goldMines = newstate.getResourceNodeIds(Type.GOLD_MINE);
		List<Integer> trees = newstate.getResourceNodeIds(Type.TREE);
		
		// Now that we know the unit types we can assign our peasants to collect resources
		for(Integer peasantID : peasantIds)
		{
			Action action = null;
			if(newstate.getUnit(peasantID).getCargoAmount() > 0)
			{
				// If the agent is carrying cargo then command it to deposit what its carrying at the townhall.
				// Here we are constructing a new TargetedAction. The first parameter is the unit being commanded.
				// The second parameter is the action type, in this case a COMPOUNDDEPOSIT. The actions starting
				// with COMPOUND are convenience actions made up of multiple move actions and another final action
				// in this case DEPOSIT. The moves are determined using A* planning to the location of the unit
				// specified by the 3rd argument of the constructor.
				action = new TargetedAction(peasantID, ActionType.COMPOUNDDEPOSIT, townhallIds.get(0));
			}
			else
			{
				/* If the peasant is not carrying anything, they check their gold supplies, and if they are under the threshold, they mine
				 * if they have >700, they get wood, unless they already have a barracks (the last thing I build with wood 
				 * if they have enough of both, they build a farm, and a barracks if they don't have one
				 */
				if(currentGold<700){
					action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, goldMines.get(0));
				} else if(currentWood<400 && barracksIds.isEmpty()){
					action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, trees.get(0));
				} else if(farmIds.isEmpty()){
					action = new LocatedProductionAction(peasantID, ActionType.COMPOUNDBUILD, newstate.getTemplate(playernum, "Farm").getID(), 9,8);
				} else if(barracksIds.isEmpty()){
					action = new LocatedProductionAction(peasantID, ActionType.COMPOUNDBUILD, newstate.getTemplate(playernum, "Barracks").getID(), 9,7);
				}
				
			}
			
			
			// Put the actions in the action map.
			// Without this step your agent will do nothing.
			actions.put(peasantID, action);
		}
		
		/*  Here I instruct the barracks (if I have one, and the resources) to make footmen
		 * 
		 */
		if(!barracksIds.isEmpty()&& currentGold>=300 && footmanIds.size()<3){
			Action action = null;
			int bId = barracksIds.get(0);
			action = new ProductionAction(bId, ActionType.COMPOUNDPRODUCE, newstate.getTemplate(playernum, "Footman").getID());
			actions.put(bId, action);
		}
		
		return actions;
	}

	public void terminalStep(StateView newstate, HistoryView statehistory) {
		System.out.println("Finsihed the episode");
	}

	public void savePlayerData(OutputStream os) {
		// this agent doesn't learn so nothing needs to be saved
	}

	public void loadPlayerData(InputStream is) {
		// this agent doesn't learn so nothing is loaded
	}

}