package P4agents; //Modified this package.

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import P4agents.StripsAction; //Modified this code! Was originally edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.*;
import java.util.*;

/**
 * Created by Devin on 3/15/15.
 */
public class PlannerAgent extends Agent {

    final int requiredWood;
    final int requiredGold;
    final boolean buildPeasants;

    // Your PEAgent implementation. This prevents you from having to parse the text file representation of your plan.
    PEAgent peAgent;

    public PlannerAgent(int playernum, String[] params) {
        super(playernum);

        if(params.length < 3) {
            System.err.println("You must specify the required wood and gold amounts and whether peasants should be built");
        }

        requiredWood = Integer.parseInt(params[0]);
        requiredGold = Integer.parseInt(params[1]);
        buildPeasants = Boolean.parseBoolean(params[2]);


        System.out.println("required wood: " + requiredWood + " required gold: " + requiredGold + " build Peasants: " + buildPeasants);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {

        Stack<StripsAction> plan = AstarSearch(new GameState(stateView, playernum, requiredGold, requiredWood, buildPeasants));

        if(plan == null) {
            System.err.println("No plan was found");
            System.exit(1);
            return null;
        }

        // write the plan to a text file
        savePlan(plan);


        // Instantiates the PEAgent with the specified plan.
        peAgent = new PEAgent(playernum, plan);

        return peAgent.initialStep(stateView, historyView);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        if(peAgent == null) {
            System.err.println("Planning failed. No PEAgent initialized.");
            return null;
        }

        return peAgent.middleStep(stateView, historyView);
    }

    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }

    /**
     * Perform an A* search of the game graph. This should return your plan as a stack of actions. This is essentially
     * the same as your first assignment. The implementations should be very similar. The difference being that your
     * nodes are now GameState objects not MapLocation objects.
     *
     * @param startState The state which is being planned from
     * @return The plan or null if no plan is found.
     */
    private Stack<StripsAction> AstarSearch(GameState startState) {
    	/**
    	 * Below is our A* implementation from programming assignment 2.
    	 * We are going to change the following on it:
    	 * 
    	 * 1) MapLocations will be replaced by GameStates
    	 * 2) The 2d bool array and hEst will be modified to accommodate for 
    	 * resource collection, rather than reaching a destination (as per the goal of Programming Assign. 2)
    	 */
    	
    	
    	/*First, initialize the queue, a 2D bool array, and
    	 * Iterate through the resourceLocations, making them closed
    	 */
    	
    	/**
    	PriorityQueue<MapLocation> openList = new PriorityQueue<MapLocation>();
    	//does this default to false, true, null?
    	boolean[][] closed = new boolean[xExtent][yExtent];
    	MapLocation map;
    	for(Iterator<MapLocation> i = resourceLocations.iterator(); i.hasNext();){
    		map = i.next();
    		closed[map.x][map.y] = true;
    	}
    	if(enemyFootmanLoc != null) {
    		closed[enemyFootmanLoc.x][enemyFootmanLoc.y] = true;
    	}
    	int cost = 1;
    	MapLocation curr = start;
    	openList.add(start);

    	while(hEst(curr.x,curr.y,goal) != 1){
    		//If we find that the queue is empty, break, print PNF
    		if(openList.isEmpty()){
    			System.out.println("No Available Path");
    			System.exit(0);
    		}
    		//Not sure that priorityqueue will actually prioritize by lowest cost.
    		//Either way, should work like this, just won't be efficient, basically ignores the heuristic
    		curr = openList.poll();
    		if(curr.x-1>=0 && !closed[curr.x-1][curr.y]){
    			//if its valid, add it to the queue
    			openList.add(new MapLocation(curr.x-1, curr.y, curr, cost + hEst(curr.x-1,curr.y, goal)));
        		closed[curr.x-1][curr.y] = true;
    			//check for left up/left down
    			//Hey, how high do you think the cost is per operation to calc curr.y-1>0? 
    			//if I have two calls to that (per iteration), does it make sense to make a var to hold the value, and just reference it?
    			if(curr.y-1>=0 && !closed[curr.x-1][curr.y-1]){
    				openList.add(new MapLocation(curr.x-1, curr.y-1, curr, cost + hEst(curr.x-1,curr.y-1, goal)));
            		closed[curr.x-1][curr.y-1] = true;

    			}
    			if(curr.y+1<yExtent && !closed[curr.x-1][curr.y+1]){
    				openList.add(new MapLocation(curr.x-1, curr.y+1, curr, cost + hEst(curr.x-1,curr.y+1, goal)));
            		closed[curr.x-1][curr.y+1] = true;
    			}
    		}
    		if(curr.x+1>=0 && !closed[curr.x+1][curr.y]){
    			//if its valid, add it to the queue
    			openList.add(new MapLocation(curr.x+1, curr.y, curr, cost + hEst(curr.x+1,curr.y, goal)));
        		closed[curr.x+1][curr.y] = true;

    			//check for right up/right down
    			if(curr.y-1>=0 && !closed[curr.x+1][curr.y-1]){
    				openList.add(new MapLocation(curr.x+1, curr.y-1, curr, cost + hEst(curr.x+1,curr.y-1, goal)));
            		closed[curr.x+1][curr.y-1] = true;
    			}
    			if(curr.y+1<yExtent && !closed[curr.x+1][curr.y+1]){
    				openList.add(new MapLocation(curr.x+1, curr.y+1, curr, cost + hEst(curr.x+1,curr.y+1, goal)));
            		closed[curr.x+1][curr.y+1] = true;
    			}
    		} 
    		//Might be able to eek out some performance by changing the ordering of the if statements to cause a 
    		//failure of specific ones to cause it to skip others.
    		//I do this in the earlier ones, feel like this could get nested somewhere
    		if(curr.y-1>=0 && !closed[curr.x][curr.y-1]){
				openList.add(new MapLocation(curr.x, curr.y-1, curr, cost + hEst(curr.x,curr.y-1, goal)));
        		closed[curr.x][curr.y-1] = true;
    		}
    		if(curr.y+1<yExtent && !closed[curr.x][curr.y+1]){
				openList.add(new MapLocation(curr.x, curr.y+1, curr, cost + hEst(curr.x,curr.y+1, goal)));
        		closed[curr.x][curr.y+1] = true;
    		}
    		closed[curr.x][curr.y]=true;
        	cost++;
        }
    	Stack<MapLocation> daddyPathStacks = new Stack<MapLocation>();
    	while(curr != start){
    		daddyPathStacks.add(curr);
    		curr = curr.parent;
    	}
    	
    	**/
        return null;
    }

    /**
     * This has been provided for you. Each strips action is converted to a string with the toString method. This means
     * each class implementing the StripsAction interface should override toString. Your strips actions should have a
     * form matching your included Strips definition writeup. That is <action name>(<param1>, ...). So for instance the
     * move action might have the form of Move(peasantID, X, Y) and when grounded and written to the file
     * Move(1, 10, 15).
     *
     * @param plan Stack of Strips Actions that are written to the text file.
     */
    private void savePlan(Stack<StripsAction> plan) {
        if (plan == null) {
            System.err.println("Cannot save null plan");
            return;
        }

        File outputDir = new File("saves");
        outputDir.mkdirs();

        File outputFile = new File(outputDir, "plan.txt");

        PrintWriter outputWriter = null;
        try {
            outputFile.createNewFile();

            outputWriter = new PrintWriter(outputFile.getAbsolutePath());

            Stack<StripsAction> tempPlan = (Stack<StripsAction>) plan.clone();
            while(!tempPlan.isEmpty()) {
                outputWriter.println(tempPlan.pop().toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputWriter != null)
                outputWriter.close();
        }
    }
}
