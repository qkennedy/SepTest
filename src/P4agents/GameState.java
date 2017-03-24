package P4agents;

import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.State.StateBuilder;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.UnitTemplate;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to represent the state of the game after applying one of the avaiable actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {

	
	private State.StateView state;
	private int playerNum;
	private int requiredGold;
	private int requiredWood;
	private int xExt;
	private int yExt;
	public List<Unit.UnitView> units;
	private boolean buildPeasants;
	public Position peasPos;
	public int peasID;
	public int townhallID;
	public Position thPos;
	public List<ResourceView> resNodes;
	public List<ResourceView> goldNodes;
	public List<ResourceView> woodNodes;
    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
    	
    	this.state = state;
    	this.playerNum = playernum;
    	this.requiredGold = requiredGold;
    	this.requiredWood = requiredWood;
    	this.buildPeasants = buildPeasants; //For this project, buildPeasants will always be false. It will be true in Project 5.   	
    	this.xExt = state.getXExtent();
    	this.yExt = state.getYExtent();
    	this.units = state.getUnits(playernum);
    	
    	for(int unitId : state.getUnitIds(playerNum)) {
            Unit.UnitView unit = state.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if (unitType.equals("peasant")) {
            	this.peasID = unitId;
            	this.peasPos = new Position(unit.getXPosition(), unit.getYPosition());
            }
            if (unitType.equals("townhall")) {
            	this.townhallID = unitId;
            	this.thPos = new Position(unit.getXPosition(), unit.getYPosition());
            }
    	}
    	this.resNodes = state.getAllResourceNodes();
    	this.goldNodes = state.getResourceNodes(Type.GOLD_MINE);
    	this.goldNodes = state.getResourceNodes(Type.TREE);
    	
    }
    

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
    	
    	int townhallId = -1;
    	
    	for(int unitId : state.getUnitIds(playerNum)) {
            Unit.UnitView unit = state.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall")) {
                townhallId = unitId;
            }
    	}
    	
    	Unit.UnitView townhall = state.getUnit(townhallId);
    	int maxCargo = requiredGold + requiredWood; //Need to consider whether to pass requiredGold/Wood to supplemental constructors. Possibly consider changing this method.
    	
    	return (townhall.getCargoAmount() == maxCargo);
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
    	
    	List<GameState> children = new ArrayList<GameState>();
    	int peasantID = -1;
    	
    	State newStateMovements = new State();
    	
    	for(ResourceNode.ResourceView resource : state.getAllResourceNodes()) {
    		ResourceNode resourceNode = new ResourceNode(resource.getType(), resource.getXPosition(), resource.getYPosition(), resource.getAmountRemaining(), resource.getID());
    		newStateMovements.addResource(resourceNode);
    	}
    	
    	for(int unitId : state.getUnitIds(playerNum)) {
            Unit.UnitView unit = state.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("peasant")) {
                peasantID = unitId;
            }
            
            if(unitType.equals("townhall")) {
            	Unit u = createUnit(unit, unit.getXPosition(), unit.getYPosition());
            	newStateMovements.addUnit(u, u.getxPosition(), u.getyPosition());
            }
    	}
    	
    	Unit.UnitView peasant = state.getUnit(peasantID);
    	int px = peasant.getXPosition();
    	int py = peasant.getYPosition();
    	
    	Position pPos = new Position(px, py);
    	List<Position> adjPos = pPos.getAdjacentPositions();
    	
    	for(Position adj : adjPos) {
    		for(ResourceNode.ResourceView resource : state.getAllResourceNodes()) {
    			if(resource.getXPosition() == adj.x && resource.getYPosition() == adj.y) {
    				//Add to the new state a mapping of the peasant mining the resource.
    				break;
    			}
    		}
    		
        	for(int unitId : state.getUnitIds(playerNum)) {
                Unit.UnitView unit = state.getUnit(unitId);
                String unitType = unit.getTemplateView().getName().toLowerCase();
                if (unitType.equals("townhall") && unit.getXPosition() == adj.x && unit.getYPosition() == adj.y) {
                	//Add to the new state a mapping of the peasant depositing resources if it has them.
                	break;
                }
        	}
    	}
    	
    	if(positionExists(px + 1, py)) {
    	    Unit u = createUnit(peasant, px + 1, py);
    		newStateMovements.addUnit(u, px + 1, py);
    		children.add(new GameState(newStateMovements.getView(playerNum), playerNum, requiredGold, requiredWood, buildPeasants));
    		newStateMovements.removeUnit(peasantID);
    	}
    	
    	if(positionExists(px - 1, py)) {
    	    Unit u = createUnit(peasant, px - 1, py);
    		newStateMovements.addUnit(u, px - 1, py);
    		children.add(new GameState(newStateMovements.getView(playerNum), playerNum, requiredGold, requiredWood, buildPeasants));
    		newStateMovements.removeUnit(peasantID);
    	}
    	
    	if(positionExists(px, py + 1)) {
    	    Unit u = createUnit(peasant, px, py + 1);
    		newStateMovements.addUnit(u, px, py + 1);
    		children.add(new GameState(newStateMovements.getView(playerNum), playerNum, requiredGold, requiredWood, buildPeasants));
    		newStateMovements.removeUnit(peasantID);
    	}
    	
    	if(positionExists(px, py - 1)) {
    	    Unit u = createUnit(peasant, px, py - 1);
    		newStateMovements.addUnit(u, px, py - 1);
    		children.add(new GameState(newStateMovements.getView(playerNum), playerNum, requiredGold, requiredWood, buildPeasants));
    		newStateMovements.removeUnit(peasantID);
    	}
    	
        return children;
    }
    
    //Helper method that generates a Unit from a UnitView.
    public UnitView MoveUnit(Unit.UnitView unitView, int x, int y) {
        Unit unit = new Unit(new UnitTemplate(unitView.getID()), unitView.getID());
        unit.setxPosition(x);
        unit.setyPosition(y);
        unit.setHP(unitView.getHP());
        unit.setDurativeStatus(unitView.getCurrentDurativeAction(), unitView.getCurrentDurativeProgress());
        unit.setCargo(unitView.getCargoType(), unitView.getCargoAmount());
    
    	return unit.getView();
    }
    public UnitView GatherToUnit(Unit.UnitView unitView, ResourceType type, int amt ) {
        Unit unit = new Unit(new UnitTemplate(unitView.getID()), unitView.getID());
        unit.setxPosition(unitView.getXPosition());
        unit.setyPosition(unitView.getYPosition());
        unit.setHP(unitView.getHP());
        unit.setDurativeStatus(unitView.getCurrentDurativeAction(), unitView.getCurrentDurativeProgress());
        unit.setCargo(type, amt);
    
    	return unit.getView();
    }
    public ResourceView GatherFromNode(int resID) {
    	ResourceView prev = resNodes.get(resID)
        ResourceNode node = new ResourceNode(prev.getType(), prev.getXPosition(), prev.getXPosition(), prev.getAmountRemaining()-100, resID);
    	
    
    	return unit.getView();
    }
    
    //Helper method: Determines if a position on the map exists.
    public boolean positionExists(int x, int y) {
    	return (x >= 0 && y >= 0 && x <= xExt && y <= yExt);
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     *
     *
     *JL: A few Heuristic considerations that I thought about adding for the first commit:
     *1) If a unit is right next to a gatherable resource, has spare room, and has not fufilled the required quota of that resource,
     *then the unit should gather that material. This should increase the utility.
     *
     *2) If a unit is next to a townhall and has resources, it should deposit those resources. This should increase utility.
     *
     *3) (Possibly - to be implemented later) resources closest to the townhall should be collected first. Closer resource = higher utility.
     *
     *More heuristic considerations will be added in either Quinn's portion of work, or the second commit.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
    	
    	double adjResourceToCollect = 0.0;
    	double adjTownhallToDeposit = 0.0;
    	
    	List<Position> adjPos = peasPos.getAdjacentPositions();
    	Unit.UnitView peasant = state.getUnit(peasID);
    	Unit.UnitView townhall = state.getUnit(townhallID);
    	int peasantCargo = peasant.getCargoAmount();
    	
    	for(Position adj : adjPos) {
    		for(ResourceNode.ResourceView resource : state.getAllResourceNodes()) {
    			if(resource.getXPosition() == adj.x && resource.getYPosition() == adj.y && peasantCargo == 0) {
    				adjResourceToCollect = 15.0;
    			}
    		}
    		
    		if(townhall.getXPosition() == adj.x && townhall.getYPosition() == adj.y && peasantCargo != 0) {
    			adjTownhallToDeposit = 20.0;
    		}
    	}
    
        return adjResourceToCollect + adjTownhallToDeposit;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        // TODO: Implement me!
        return 0.0;
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
    	
    	if(this.getCost() > o.getCost()) {
    		return 1;
    	} 
    	else if (this.getCost() < o.getCost()) {
    		return -1;
    	}
    	else {
    		return 0;
    	}
    	
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
    	return (this.hashCode() == o.hashCode());
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
        // TODO: Implement me!
        return 0; //Still needs to be implemented.
    }
    
    
//--------------------------------------------------------------------------------------------------------------\\
//Everything below this line is newly-added for the first commit. Used to modify the code for A* search. 
    
    
    public int getXExt() {
    	return xExt;
    }
    
    public int getYExt() {
    	return yExt;
    }
    //These Methods should be stuff for changing the state to handle apply
    public void deleteUnit(int uID){
    	units.remove(uID);
    }
    public void addUnit(int uID, UnitView view){
    	units.add(uID, view);
    }
    public void moveUnit(int pID, Direction dir){
    	
    	UnitView view = units.get(pID);
    	view.
    }
    public void gatherFromNode(int pID, int resID){
    	
    }
    public void deposit(int uID, int thID){
    	
    }
}
