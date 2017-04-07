package P5agents;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
    public GameState parent;
    private double cost;
    private double TCOST;
    public double gold;
    public double wood;
    public List<Unit.UnitView> units;
    public boolean buildPeasants;
    public Position peasPos;
    public ResourceType pCType;
    public int pAmt;
    public int peasID;
    public int townhallID;
    public Position thPos;
    public List<ResourceView> goldNodes;
    public List<ResourceView> woodNodes;
    public List<ResourceView> Nodes;
    public Stack<StripsAction> actions;
    public List<Direction> directionList;
    public HashMap<Integer, myPeasant> peasants = new HashMap<Integer, myPeasant>();
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
        
        
        Direction[] directions = {Direction.EAST, Direction.NORTH, Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.WEST};
        List<Direction> directionList = new ArrayList<Direction>();
        for(int i = 0; i <= directions.length - 1; i++) {
            directionList.add(directions[i]);
        }
        
        this.directionList = directionList;
        
        for(int unitId : state.getUnitIds(playerNum)) {
            Unit.UnitView unit = state.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if (unitType.equals("peasant")) {
                myPeasant peas = new myPeasant(unitId);
                peasants.put(unitId, peas);
            }
            if (unitType.equals("townhall")) {
                this.townhallID = unitId;
                this.thPos = new Position(unit.getXPosition(), unit.getYPosition());
            }
        }
        this.Nodes = state.getAllResourceNodes();
        this.goldNodes = state.getResourceNodes(Type.GOLD_MINE);
        this.woodNodes = state.getResourceNodes(Type.TREE);
        this.cost = 0;
        actions = new Stack();
    }
    public GameState(GameState parent){
        this.state = parent.state;
        this.playerNum = parent.playerNum;
        this.requiredGold = parent.requiredGold;
        this.requiredWood = parent.requiredWood;
        this.buildPeasants = parent.buildPeasants; //For this project, buildPeasants will always be false. It will be true in Project 5.
        this.xExt = parent.state.getXExtent();
        this.yExt = parent.state.getYExtent();
        this.units = parent.state.getUnits(playerNum);
        this.gold = parent.gold;
        this.wood = parent.wood;
        List<Direction> directionList = parent.directionList;
        
        this.directionList = directionList;
        
        for(int unitId : state.getUnitIds(playerNum)) {
            Unit.UnitView unit = state.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if (unitType.equals("peasant")) {
                this.peasID = unitId;
            }
            if (unitType.equals("townhall")) {
                this.townhallID = unitId;
                this.thPos = new Position(unit.getXPosition(), unit.getYPosition());
            }
        }
        Nodes = new ArrayList<ResourceView>();
        woodNodes = new ArrayList<ResourceView>();
        goldNodes = new ArrayList<ResourceView>();
        for(ResourceView res: parent.Nodes){
            if(res.getAmountRemaining()>0){
                Nodes.add(res);
            }
        }
        for(ResourceView res: parent.Nodes){
            if(res.getType() == Type.TREE){
                woodNodes.add(res);
            }
        }
        for(ResourceView res: parent.Nodes){
            if(res.getType() == Type.GOLD_MINE){
                goldNodes.add(res);
            }
        }
        this.peasPos = parent.peasPos;
        this.pCType = parent.pCType;
        this.pAmt = parent.pAmt;
        cost = parent.cost;
        actions = new Stack();
        actions.addAll(parent.actions);
        
    }
    
    
    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        return (gold >= requiredGold && wood >= requiredWood );
    }
    
    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        
        List<GameState> children = new ArrayList<GameState>();
        
        for (Map.Entry<Integer, myPeasant> entry : peasants.entrySet()) {
            
            myPeasant peas = entry.getValue();
            int ID = peas.getID();
            
            
            DepositAction depot = new DepositAction(ID, townhallID);
            
            if(depot.preconditionsMet(this)) {
                GameState child = new GameState(this);
                child = depot.apply(child);
                child.setParent(this);
                children.add(child);
                child.cost = cost + 1;
            }
            for(ResourceView woodSource : woodNodes) {
                GatherWoodAction woodAction = new GatherWoodAction(ID, woodSource.getID());
                
                if(woodAction.preconditionsMet(this)) {
                    GameState child = new GameState(this);
                    child = woodAction.apply(child);
                    children.add(child);
                    child.cost = cost + 1;
                }
            }
            for(ResourceView goldSource : goldNodes) {
                GatherGoldAction goldAction = new GatherGoldAction(ID, goldSource.getID());
                
                if(goldAction.preconditionsMet(this)) {
                    GameState child = new GameState(this);
                    child = goldAction.apply(child);
                    children.add(child);
                    child.cost = cost + 1;
                }
            }
            List<MoveAction> moves = getPossibleMoves(ID);
            for(MoveAction move : moves) {
                if(move.preconditionsMet(this)) {
                    GameState child = new GameState(this);
                    child = move.apply(child);
                    child.setParent(this);
                    children.add(child);
                    child.cost = cost + 1;
                }
                
            }
        }
        
        return children;
    }
    
    //Helper method: Determines if a position on the map exists.
    public boolean positionExists(int x, int y) {
        return (x >= 0 && y >= 0 && x <= xExt && y <= yExt);
    }
    public boolean positionFree(int x, int y) {
        boolean filled = false;
        Collection<myPeasant> ps = peasants.values();
        for(myPeasant p: ps){
        	if(p.getPos().x == x && p.getPos().y == y){
        		filled = true;
        	}
        }
        for(ResourceView r: Nodes){
        	if(r.getXPosition() == x && r.getYPosition() == y){
        		filled = true;
        	}
        }
        return !filled;
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
        //First, have a subtracted amount from the amount of each resource we have
        double gdiff = gold;
        double wdiff = wood;
        double tdiff = 0;
        if(gold>requiredGold){
            gdiff = requiredGold;
            tdiff = 1;
        }
        if(wood>requiredWood){
            wdiff = requiredWood;
            tdiff = 1;
        }
        
        
        double woodDist;
        double carryCoeff;
        if(pAmt!=0){
            carryCoeff = 1;
        } else {
            carryCoeff = 0;
        }
        return (gdiff/20  + wdiff/20) - tdiff;
    }
    public Position getClosestWood(int pID){
        double closest = Double.MAX_VALUE;
        
        myPeasant peasant = peasants.get(pID);
        Position peasantPos = peasant.getPos();
        Position pos = null;
        for(ResourceView view: Nodes){
            
            double xd = Math.abs(peasantPos.x - view.getXPosition());
            double yd = Math.abs(peasantPos.y - view.getYPosition());
            double dist = xd + yd / 2;
            
            if((xd + yd / 2) < closest && view.getType().equals(Type.TREE) && view.getAmountRemaining() > 0){
                closest = dist;
                pos = new Position(view.getXPosition(),view.getYPosition());
            }
        }
        return pos;
    }
    
    public Position getClosestGM(int pID){
        double closest = Double.MAX_VALUE;
        myPeasant peasant = peasants.get(pID);
        Position peasantPos = peasant.getPos();
        
        Position pos = null;
        for(ResourceView view: Nodes){
            double xd = Math.abs(peasantPos.x - view.getXPosition());
            double yd = Math.abs(peasantPos.y - view.getYPosition());
            double dist = xd + yd / 2;
            if((xd + yd / 2) < closest && view.getType().equals(Type.GOLD_MINE) && view.getAmountRemaining() > 0){
                closest = dist;
                pos = new Position(view.getXPosition(),view.getYPosition());
            }
        }
        return pos;
    }
    public double distFromTH(int pID){
        double closest = 9999;
        myPeasant peasant = peasants.get(pID);
        Position peasantPos = peasant.getPos();
        UnitView view = units.get(townhallID);
        double xd = Math.abs(peasantPos.x - view.getXPosition());
        double yd = Math.abs(peasantPos.y - view.getYPosition());
        Double dist = xd + yd / 2;
        return dist;
    }
    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        TCOST = cost - heuristic();
        return cost - heuristic();
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
        GameState other = (GameState) o;
        boolean res = this.gold == other.gold && this.wood == other.wood;
        
        boolean pos = true;
        boolean carrying = true;
        
        if(peasants.size() == other.peasants.size()) {
            
            for(Map.Entry<Integer, myPeasant> entry : peasants.entrySet()) {
                for(Map.Entry<Integer, myPeasant> otherEntry : other.peasants.entrySet()) {
                    myPeasant peas = entry.getValue();
                    myPeasant otherPeas = otherEntry.getValue();
                    
                    if(peas.getID() == otherPeas.getID()) {
                        
                        if(peas.getCAmt() != otherPeas.getCAmt() || peas.getRType() != otherPeas.getRType()) {
                            carrying = false;
                            break;
                        }
                        
                        if(peas.getPos().x != otherPeas.getPos().x || peas.getPos().y != otherPeas.getPos().y) {
                            pos = false;
                            break;
                        }
                    }
                }
            }
            
        }
        
        return res && pos && carrying;
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
    
    public void setParent(GameState parent) {
        this.parent = parent;
    }
    
    public GameState getParent() {
        return parent;
    }
    
    public int getXExt() {
        return xExt;
    }
    
    public int getYExt() {
        return yExt;
    }
    
    public void gatherFromNode(int pID, int resID, ResourceType type){
        gatherToUnit(units.get(pID), type, 100);
        if(type.equals(ResourceType.GOLD)){
            ResourceView rView = gatherGoldNode(resID);
            for(ResourceView view: Nodes){
                if(view.getID() == rView.getID()){
                    Nodes.remove(view);
                    break;
                }
            }
            Nodes.add(rView);
        } else {
            ResourceView rView = gatherWoodNode(resID);
            for(ResourceView view: Nodes){
                if(view.getID() == rView.getID()){
                    Nodes.remove(view);
                    break;
                }
            }
            Nodes.add(rView);
        }
        
        
    }
    
    //Helper method that generates a Unit from a UnitView.
    public void moveUnit(myPeasant p, int x, int y) {
        p.setPos(new Position(x,y));
    }
    public void gatherToUnit(Unit.UnitView unitView, ResourceType type, int amt ) {
        myPeasant peasant = peasants.get(unitView.getID());
        peasant.setRType(type);
        peasant.setCAmt(amt);
        
    }
    public ResourceView gatherWoodNode(int resID) {
        ResourceView prev = state.getResourceNode((resID));
        ResourceNode node = new ResourceNode(prev.getType(), prev.getXPosition(), prev.getXPosition(), prev.getAmountRemaining()-100, resID);
        return node.getView();
    }
    public ResourceView gatherGoldNode(int resID) {
        ResourceView prev = state.getResourceNode(resID);
        ResourceNode node = new ResourceNode(prev.getType(), prev.getXPosition(), prev.getXPosition(), prev.getAmountRemaining()-100, resID);
        return node.getView();
    }
    
    public void deposit(int uID, int thID){
        System.out.println("DEPOSITED SUCCESSFULLY");
        myPeasant depPeas = peasants.get(uID);
        
        if(depPeas.getRType() == ResourceType.GOLD) {
            gold += depPeas.getCAmt();
        } else {
            wood += depPeas.getCAmt();
        }
        
        depPeas.setRType(null);
        depPeas.setCAmt(0);
    }
    
    public ResourceView getResource(int ID){
        return state.getResourceNode(ID);
    }
    public List<MoveAction> getPossibleMoves(ArrayList<myPeasant> peas){
        List<MoveAction> list =  new ArrayList<MoveAction>();
        //First, add the positions surrounding townhall
        UnitView th = units.get(townhallID);
        Position thPos = new Position(th.getXPosition(), th.getYPosition());
        list.add(getBestSurr(thPos));
        //Next, add the positions next to all resource nodes which still contain resources
        list.add(getBestSurr(getClosestGM(p.getID())));
        list.add(getBestSurr(getClosestWood(p.getID())));
        return list;
    }
    public Position getBestSurr(Position pos){
        Position best = null;
        double shortest = Double.MAX_VALUE;
        double dist = 0;
        for(Direction d:  directionList){
            Position newPos = newPos(d, pos);
            dist =  ChebDist(peasPos, pos);
            if(positionFree(pos.x, pos.y)&& shortest > dist) {
                shortest = dist;
                best = newPos;
            }
        }
        return best;
    }
    public double ChebDist(Position start, Position end){
        double dx = Math.abs(start.x - end.x);
        double dy = Math.abs(start.y - end.y);
        return Math.max(dx, dy);
    }
    public Position newPos(Direction direction, Position orig){
        switch(direction){
            case NORTH:
                return new Position(orig.x, orig.y - 1);
            case NORTHEAST:
                return new Position(orig.x +1, orig.y - 1);
            case EAST:
                return new Position(orig.x +1, orig.y);
            case SOUTHEAST:
                return new Position(orig.x + 1, orig.y + 1);
            case SOUTH:
                return new Position(orig.x, orig.y + 1);
            case SOUTHWEST:
                return new Position(orig.x -1, orig.y - 1);
            case WEST:
                return new Position(orig.x - 1, orig.y);
            case NORTHWEST:
                return new Position(orig.x - 1, orig.y - 1);
        }
        return null;
    }
    public UnitView getUnit(int unitID){
        for(UnitView v: units){
            if(v.getID() == unitID){
                return v;
            }
        }
        return null;
    }
}
