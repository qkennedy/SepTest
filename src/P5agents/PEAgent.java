package P5agents; //modified this package

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.agent.Agent;
import P5agents.StripsAction; //Modified code. Original: edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Template;
import edu.cwru.sepia.environment.model.state.Unit;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * This is an outline of the PEAgent. Implement the provided methods. You may add your own methods and members.
 */
public class PEAgent extends Agent {

    // The plan being executed
    private Stack<StripsAction> plan = null;

    // maps the real unit Ids to the plan's unit ids
    // when you're planning you won't know the true unit IDs that sepia assigns. So you'll use placeholders (1, 2, 3).
    // this maps those placeholders to the actual unit IDs.
    private Map<Integer, Integer> peasantIdMap;
    private int townhallId;
    private int peasantTemplateId;
    private List<Action> cAct;
    private List<Action> pAct;
    public PEAgent(int playernum, Stack<StripsAction> plan) {
        super(playernum);
        peasantIdMap = new HashMap<Integer, Integer>();
        this.plan = plan;

    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
        // gets the townhall ID and the peasant ID
        for(int unitId : stateView.getUnitIds(playernum)) {
            Unit.UnitView unit = stateView.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall")) {
                townhallId = unitId;
            } else if(unitType.equals("peasant")) {
                peasantIdMap.put(unitId, unitId);
            }
        }

        // Gets the peasant template ID. This is used when building a new peasant with the townhall
        for(Template.TemplateView templateView : stateView.getTemplates(playernum)) {
            if(templateView.getName().toLowerCase().equals("peasant")) {
                peasantTemplateId = templateView.getID();
                break;
            }
        }

        return middleStep(stateView, historyView);
    }

    /**
     * This is where you will read the provided plan and execute it. If your plan is correct then when the plan is empty
     * the scenario should end with a victory. If the scenario keeps running after you run out of actions to execute
     * then either your plan is incorrect or your execution of the plan has a bug.
     *
     * You can create a SEPIA deposit action with the following method
     * Action.createPrimitiveDeposit(int peasantId, Direction townhallDirection)
     *
     * You can create a SEPIA harvest action with the following method
     * Action.createPrimitiveGather(int peasantId, Direction resourceDirection)
     *
     * You can create a SEPIA build action with the following method
     * Action.createPrimitiveProduction(int townhallId, int peasantTemplateId)
     *
     * You can create a SEPIA move action with the following method
     * Action.createCompoundMove(int peasantId, int x, int y)
     *
     * these actions are stored in a mapping between the peasant unit ID executing the action and the action you created.
     *
     * For the compound actions you will need to check their progress and wait until they are complete before issuing
     * another action for that unit. If you issue an action before the compound action is complete then the peasant
     * will stop what it was doing and begin executing the new action.
     *
     * To check an action's progress you can use the historyview object. Here is a short example.
     * if (stateView.getTurnNumber() != 0) {
     *   Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
     *   for (ActionResult result : actionResults.values()) {
     *     <stuff>
     *   }
     * }
     * Also remember to check your plan's preconditions before executing!
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
    	List<Integer> idleList = new ArrayList<Integer>();
    	idleList.addAll(peasantIdMap.keySet());
    	if(plan == null) {
    		System.out.println("Error: Plan Null");
    		return null;
    	}
    	
    	if(plan.isEmpty() && cAct.isEmpty()) {
    		System.out.println("Error: Plan Empty");
    		return null;
    	}
    	
    	Map<Integer, Action> out = new HashMap<Integer, Action>();
    	
    	if(stateView.getTurnNumber() != 0) {
    		Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
        	idleList.clear();
    	    for (ActionResult res : actionResults.values()) {
    	    	if (res.getFeedback() !=  ActionFeedback.INCOMPLETE) {
    	    		idleList.add(res.getAction().getUnitId());
    	    	}
    	    }
    	    if(idleList.isEmpty()){
    	    	return out;
    	    }
    	}
    	List<Action> rma = new ArrayList<Action>();
    	List<Integer> rmi = new ArrayList<Integer>();
    	for(int i: idleList){
    		if(cAct != null){
    			if(!cAct.isEmpty()){
    				for(Action a: cAct){
    					if(a.getUnitId()==i && !rmi.contains(i) && idleList.contains(i)){
    						out.put(i, a);
    						rma.add(a);
    						rmi.add(i);
    					}
    				}
    			}
    		}
    	}
    	for(Action a: rma){
    		cAct.remove(cAct.indexOf(a));
    	}
    	
    	StripsAction next = plan.pop();
    	cAct.addAll(createSepiaAction(next));
    	
        return out;
    }

    /**
     * Returns a SEPIA version of the specified Strips Action.
     * @param action StripsAction
     * @return SEPIA representation of same action
     */
    private List<Action> createSepiaAction(StripsAction action) {
    	List<Action> list = new ArrayList<Action>();
    	
        return action.ResultantAction();
    }

    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {
       //Need to include how many steps were executed in this section.
    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }
}
