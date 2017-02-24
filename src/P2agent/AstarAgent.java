package P2agent;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class AstarAgent extends Agent {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	class MapLocation implements Comparable
    {
        public int x, y;
        public MapLocation parent;
        public float cost;
        public MapLocation(int x, int y, MapLocation cameFrom, float cost)
        {
            this.x = x;
            this.y = y;
            this.parent = cameFrom;
            this.cost = cost;
        }
		@Override
		public int compareTo(Object arg0) {
			MapLocation map = (MapLocation)arg0;
				if(this.cost>map.cost){
					return 1;
				} else if(this.cost<map.cost) {
					return -1;
				}
				return 0;
		}
    }

    Stack<MapLocation> path;
    int footmanID, townhallID, enemyFootmanID;
    MapLocation nextLoc;

    private long totalPlanTime = 0; // nsecs
    private long totalExecutionTime = 0; //nsecs

    public AstarAgent(int playernum)
    {
        super(playernum);

        System.out.println("Constructed AstarAgent");
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        // get the footman location
        List<Integer> unitIDs = newstate.getUnitIds(playernum);

        if(unitIDs.size() == 0)
        {
            System.err.println("No units found!");
            return null;
        }

        footmanID = unitIDs.get(0);

        // double check that this is a footman
        if(!newstate.getUnit(footmanID).getTemplateView().getName().equals("Footman"))
        {
            System.err.println("Footman unit not found");
            return null;
        }

        // find the enemy playernum
        Integer[] playerNums = newstate.getPlayerNumbers();
        int enemyPlayerNum = -1;
        for(Integer playerNum : playerNums)
        {
            if(playerNum != playernum) {
                enemyPlayerNum = playerNum;
                break;
            }
        }

        if(enemyPlayerNum == -1)
        {
            System.err.println("Failed to get enemy playernumber");
            return null;
        }

        // find the townhall ID
        List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);

        if(enemyUnitIDs.size() == 0)
        {
            System.err.println("Failed to find enemy units");
            return null;
        }

        townhallID = -1;
        enemyFootmanID = -1;
        for(Integer unitID : enemyUnitIDs)
        {
            Unit.UnitView tempUnit = newstate.getUnit(unitID);
            String unitType = tempUnit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall"))
            {
                townhallID = unitID;
            }
            else if(unitType.equals("footman"))
            {
                enemyFootmanID = unitID;
            }
            else
            {
                System.err.println("Unknown unit type");
            }
        }

        if(townhallID == -1) {
            System.err.println("Error: Couldn't find townhall");
            return null;
        }

        long startTime = System.nanoTime();
        path = findPath(newstate);
        totalPlanTime += System.nanoTime() - startTime;

        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        long startTime = System.nanoTime();
        long planTime = 0;

        Map<Integer, Action> actions = new HashMap<Integer, Action>();

        if(shouldReplanPath(newstate, statehistory, path)) {
            long planStartTime = System.nanoTime();
            path = findPath(newstate);
            planTime = System.nanoTime() - planStartTime;
            totalPlanTime += planTime;
        }

        Unit.UnitView footmanUnit = newstate.getUnit(footmanID);

        int footmanX = footmanUnit.getXPosition();
        int footmanY = footmanUnit.getYPosition();

        if(!path.empty() && (nextLoc == null || (footmanX == nextLoc.x && footmanY == nextLoc.y))) {

            // start moving to the next step in the path
            nextLoc = path.pop();

            System.out.println("Moving to (" + nextLoc.x + ", " + nextLoc.y + ")");
        }

        if(nextLoc != null && (footmanX != nextLoc.x || footmanY != nextLoc.y))
        {
            int xDiff = nextLoc.x - footmanX;
            int yDiff = nextLoc.y - footmanY;

            // figure out the direction the footman needs to move in
            Direction nextDirection = getNextDirection(xDiff, yDiff);

            actions.put(footmanID, Action.createPrimitiveMove(footmanID, nextDirection));
        } else {
            Unit.UnitView townhallUnit = newstate.getUnit(townhallID);

            // if townhall was destroyed on the last turn
            if(townhallUnit == null) {
                terminalStep(newstate, statehistory);
                System.exit(0);
                return actions;
            }

            if(Math.abs(footmanX - townhallUnit.getXPosition()) > 1 ||
                    Math.abs(footmanY - townhallUnit.getYPosition()) > 1)
            {
                System.err.println("Invalid plan. Cannot attack townhall");
                totalExecutionTime += System.nanoTime() - startTime - planTime;
                return actions;
            }
            else {
                System.out.println("Attacking TownHall");
                // if no more movements in the planned path then attack
                actions.put(footmanID, Action.createPrimitiveAttack(footmanID, townhallID));
            }
        }

        totalExecutionTime += System.nanoTime() - startTime - planTime;
        return actions;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {
        System.out.println("Total turns: " + newstate.getTurnNumber());
        System.out.println("Total planning time: " + totalPlanTime/1e9);
        System.out.println("Total execution time: " + totalExecutionTime/1e9);
        System.out.println("Total time: " + (totalExecutionTime + totalPlanTime)/1e9);
    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this method.
     *
     * This method should return true when the path needs to be replanned
     * and false otherwise. This will be necessary on the dynamic map where the
     * footman will move to block your unit.
     *
     * @param state
     * @param history
     * @param currentPath
     * @return
     */
    private boolean shouldReplanPath(State.StateView state, History.HistoryView history, Stack<MapLocation> currentPath)
    {    	
    	
    	//If an enemy footman exists on the map, and we have a current path
    	if(enemyFootmanID != -1 && !currentPath.empty()) {
 
    		boolean enemCloseBy = findEnemyOnPath(state, currentPath);
       
    		//If the enemy is on the path and has a distance of 3 or less from the player, then return true
    		if(enemCloseBy) {
    			return true;
    		}
    		else {
    			return false;
    		}
    		
    	} else {
    		return false;
    	}
    	
    }
    
    //Checks to see if the enemy footman is on the current path that our footman is on. 
    //Returns null if there is no enemy on our path.
    public boolean findEnemyOnPath(State.StateView state, Stack<MapLocation> currentPath) {
    	
    	if(enemyFootmanID == -1) {
    		System.out.println("There is no enemy footman on this map!");
    		return false;
    	} 
    	
    	else {
    		
        	Unit.UnitView enemyFootmanUnit = state.getUnit(enemyFootmanID);
        	ArrayList<MapLocation> pathBlocks = new ArrayList<MapLocation>();
        	boolean enemyOnPath = false;
        	int enemyPosX = enemyFootmanUnit.getXPosition();
        	int enemyPosY = enemyFootmanUnit.getYPosition();
        	int pathDepth = 0;
        	
        	//Searches through the path for an enemy footman. Only searches three blocks deep before giving up. 
        	while(pathDepth <= 2 && !currentPath.isEmpty()) {
        		
        		MapLocation currPathPoint = currentPath.pop();
        		pathBlocks.add(currPathPoint);
        		
        		//The enemy is on this particular point of the path.
        		if((enemyPosX == currPathPoint.x) && (enemyPosY == currPathPoint.y)) {
        			enemyOnPath = true;
        		}	
        		
        		pathDepth++;
        	}
        	
        	//Rebuilds the path stack.
        	for(int i = pathBlocks.size() - 1; i >= 0; i--) {
        		currentPath.push(pathBlocks.get(i));
        	}
        	
        	pathBlocks.clear();
        	
        	return enemyOnPath;
    	}	
    }

    /**
     * This method is implemented for you. You should look at it to see examples of
     * how to find units and resources in Sepia.
     *
     * @param state
     * @return
     */
    private Stack<MapLocation> findPath(State.StateView state)
    {
        Unit.UnitView townhallUnit = state.getUnit(townhallID);
        Unit.UnitView footmanUnit = state.getUnit(footmanID);

        MapLocation startLoc = new MapLocation(footmanUnit.getXPosition(), footmanUnit.getYPosition(), null, 0);

        MapLocation goalLoc = new MapLocation(townhallUnit.getXPosition(), townhallUnit.getYPosition(), null, 0);

        MapLocation footmanLoc = null;
        if(enemyFootmanID != -1) {
            Unit.UnitView enemyFootmanUnit = state.getUnit(enemyFootmanID);
            footmanLoc = new MapLocation(enemyFootmanUnit.getXPosition(), enemyFootmanUnit.getYPosition(), null, 0);
        }

        // get resource locations
        List<Integer> resourceIDs = state.getAllResourceIds();
        Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
        for(Integer resourceID : resourceIDs)
        {
            ResourceNode.ResourceView resource = state.getResourceNode(resourceID);

            resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null, 0));
        }

        
        return AstarSearch(startLoc, goalLoc, state.getXExtent(), state.getYExtent(), footmanLoc, resourceLocations);
    }
    /**
     * This is the method you will implement for the assignment. Your implementation
     * will use the A* algorithm to compute the optimum path from the start position to
     * a position adjacent to the goal position.
     *
     * You will return a Stack of positions with the top of the stack being the first space to move to
     * and the bottom of the stack being the last space to move to. If there is no path to the townhall
     * then return null from the method and the agent will print a message and do nothing.
     * The code to execute the plan is provided for you in the middleStep method.
     *
     * As an example consider the following simple map
     *
     * F - - - -
     * x x x - x
     * H - - - -
     *
     * F is the footman
     * H is the townhall
     * x's are occupied spaces
     *
     * xExtent would be 5 for this map with valid X coordinates in the range of [0, 4]
     * x=0 is the left most column and x=4 is the right most column
     *
     * yExtent would be 3 for this map with valid Y coordinates in the range of [0, 2]
     * y=0 is the top most row and y=2 is the bottom most row
     *
     * resourceLocations would be {(0,1), (1,1), (2,1), (4,1)}
     *
     * The path would be
     *
     * (1,0)
     * (2,0)
     * (3,1)
     * (2,2)
     * (1,2)
     *
     * Notice how the initial footman position and the townhall position are not included in the path stack
     *
     * @param start Starting position of the footman
     * @param goal MapLocation of the townhall
     * @param xExtent Width of the map
     * @param yExtent Height of the map
     * @param resourceLocations Set of positions occupied by resources
     * @return Stack of positions with top of stack being first move in plan
     */
    private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent, MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations)
    {
    	/*First, initialize the queue, a 2D bool array, and
    	 * Iterate through the resourceLocations, making them closed
    	 */
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
    	
        return daddyPathStacks;
    }

    private int hEst(int x, int y, MapLocation goal){
    	int tmp = Math.abs(goal.x-x);
    	if(Math.abs(goal.y-y)>tmp){
    		return Math.abs(goal.y-y);
    	}
    	else
    	{
    		return tmp;
    	}
    }

    /**
     * Primitive actions take a direction (e.g. NORTH, NORTHEAST, etc)
     * This converts the difference between the current position and the
     * desired position to a direction.
     *
     * @param xDiff Integer equal to 1, 0 or -1
     * @param yDiff Integer equal to 1, 0 or -1
     * @return A Direction instance (e.g. SOUTHWEST) or null in the case of error
     */
    private Direction getNextDirection(int xDiff, int yDiff) {

        // figure out the direction the footman needs to move in
        if(xDiff == 1 && yDiff == 1)
        {
            return Direction.SOUTHEAST;
        }
        else if(xDiff == 1 && yDiff == 0)
        {
            return Direction.EAST;
        }
        else if(xDiff == 1 && yDiff == -1)
        {
            return Direction.NORTHEAST;
        }
        else if(xDiff == 0 && yDiff == 1)
        {
            return Direction.SOUTH;
        }
        else if(xDiff == 0 && yDiff == -1)
        {
            return Direction.NORTH;
        }
        else if(xDiff == -1 && yDiff == 1)
        {
            return Direction.SOUTHWEST;
        }
        else if(xDiff == -1 && yDiff == 0)
        {
            return Direction.WEST;
        }
        else if(xDiff == -1 && yDiff == -1)
        {
            return Direction.NORTHWEST;
        }

        System.err.println("Invalid path. Could not determine direction");
        return null;
    }
}
