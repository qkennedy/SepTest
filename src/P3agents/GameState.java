package P3agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.State.StateBuilder;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.state.UnitTemplate;
import edu.cwru.sepia.util.Direction;

import java.util.*;

/**
 * This class stores all of the information the agent
 * needs to know about the state of the game. For example this
 * might include things like footmen HP and positions.
 *
 * Add any information or methods you would like to this class,
 * but do not delete or change the signatures of the provided methods.
 */
public class GameState {
	int xLim;
	int yLim;
	int playerNum = 0;
	int enemyNum;
	List<Integer> enemyUnitIDs;
	List<Integer> unitIDs;
	State.StateView state;
    /**
     * You will implement this constructor. It will
     * extract all of the needed state information from the built in
     * SEPIA state view.
     *
     * You may find the following state methods useful:
     *
     * state.getXExtent() and state.getYExtent(): get the map dimensions
     * state.getAllResourceIDs(): returns all of the obstacles in the map
     * state.getResourceNode(Integer resourceID): Return a ResourceView for the given ID
     *
     * For a given ResourceView you can query the position using
     * resource.getXPosition() and resource.getYPosition()
     *
     * For a given unit you will need to find the attack damage, range and max HP
     * unitView.getTemplateView().getRange(): This gives you the attack range
     * unitView.getTemplateView().getBasicAttack(): The amount of damage this unit deals
     * unitView.getTemplateView().getBaseHealth(): The maximum amount of health of this unit
     *
     * @param state Current state of the episode
     */
    public GameState(State.StateView state) {
    	//Need to initialize playernums somehow
		enemyUnitIDs = state.getUnitIds(enemyNum);
		unitIDs = state.getUnitIds(playerNum);
		this.state = state;
    }

    /**
     * You will implement this function.
     *
     * You should use weighted linear combination of features.
     * The features may be primitives from the state (such as hp of a unit)
     * or they may be higher level summaries of information from the state such
     * as distance to a specific location. Come up with whatever features you think
     * are useful and weight them appropriately.
     *
     * It is recommended that you start simple until you have your algorithm working. Then watch
     * your agent play and try to add features that correct mistakes it makes. However, remember that
     * your features should be as fast as possible to compute. If the features are slow then you will be
     * able to do less plys in a turn.
     *
     * Add a good comment about what is in your utility and why you chose those features.
     *
     * @return The weighted linear combination of the features
     */
    public double getUtility() {
    	//Health Left
    	double archerHealthSum = 0;
    	for(Integer i: enemyUnitIDs){
    		archerHealthSum += state.getUnit(i).getHP();
    	}
    	archerHealthSum = -0.5 * archerHealthSum;
    	double footmanHealthSum = 0;
    	for(Integer i: enemyUnitIDs){
    		footmanHealthSum += state.getUnit(i).getHP();
    	}
    	footmanHealthSum = 0.3 * footmanHealthSum;
    	//Dist. to Archer
    	
    	//Within Range
    	
    	//Dist to walls
    	
    	//Expect the UNEXPECTED
        return 0.0;
    }
    public double getDistToArcher(int uid){
    	UnitView fm = state.getUnit(uid);
    	int x = fm.getXPosition();
    	int y = fm.getYPosition();
    	int dx = xLim;
    	int dy = yLim;
    	for(Integer i: enemyUnitIDs){
    		dx = Math.min(dx, Math.abs(state.getUnit(i).getXPosition() - x));
    		dy = Math.abs(state.getUnit(i).getYPosition() - y);
    	}
    	return (dx + dy);
    }

    /**
     * You will implement this function.
     *
     * This will return a list of GameStateChild objects. You will generate all of the possible
     * actions in a step and then determine the resulting game state from that action. These are your GameStateChildren.
     *
     * You may find it useful to iterate over all the different directions in SEPIA.
     *
     * for(Direction direction : Directions.values())
     *
     * To get the resulting position from a move in that direction you can do the following
     * x += direction.xComponent()
     * y += direction.yComponent()
     *
     * @return All possible actions and their associated resulting game state
     */
    public List<GameStateChild> getChildren() {
        //Method currently takes parameters, which is different than before. FIX FOR FINAL COMMIT.
        int opposingPlayerNum;
        
        if (playerNum == 0)
            opposingPlayerNum = 1;
        else
            opposingPlayerNum = 0;
        
        List<GameStateChild> children = new ArrayList<GameStateChild>();
        
        List<UnitView> currPlayerUnitViews = state.getUnits(playerNum);
        List<UnitView> opposingPlayerUnitViews = state.getUnits(opposingPlayerNum);
        
        //Checks to see whether or not current state is terminal. Returns empty list if that is the case.
        if((currPlayerUnitViews.size() == 0) || (opposingPlayerUnitViews.size() == 0))
            return children;
        
        
        List<Unit> playerUnits = new ArrayList<Unit>();
        List<Unit> opposingPlayerUnits = new ArrayList<Unit>();
        
        for(UnitView unitView : currPlayerUnitViews) {
            Unit unit = new Unit(new UnitTemplate(unitView.getID()), unitView.getID());
            unit.setxPosition(unitView.getXPosition());
            unit.setyPosition(unitView.getYPosition());
            unit.setDurativeStatus(unitView.getCurrentDurativeAction(), unitView.getCurrentDurativeProgress());
            unit.setCargo(unitView.getCargoType(), unitView.getCargoAmount());
            playerUnits.add(unit);
        }
        
        for(UnitView unitView : opposingPlayerUnitViews) {
            Unit unit = new Unit(new UnitTemplate(unitView.getID()), unitView.getID());
            unit.setxPosition(unitView.getXPosition());
            unit.setyPosition(unitView.getYPosition());
            unit.setDurativeStatus(unitView.getCurrentDurativeAction(), unitView.getCurrentDurativeProgress());
            unit.setCargo(unitView.getCargoType(), unitView.getCargoAmount());
            opposingPlayerUnits.add(unit);
        }
        
        if (playerNum == 0) {
            
            if(playerUnits.size() == 1) {
                Unit footman = playerUnits.get(0);
                int currX = footman.getxPosition();
                int currY = footman.getyPosition();
                
                if(doesLocationExist(state, currX + 1, currY)) {
                    footman.setxPosition(currX + 1);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                }
                
                if(doesLocationExist(state, currX, currY + 1)) {
                    footman.setyPosition(currY + 1);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                }
                
                if(doesLocationExist(state, currX - 1, currY)) {
                    footman.setxPosition(currX - 1);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                }
                
                if(doesLocationExist(state, currX, currY - 1)) {
                    footman.setyPosition(currY - 1);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                }
            } else {
                Unit footman1 = playerUnits.get(0);
                Unit footman2 = playerUnits.get(1);
                int currX1 = footman1.getxPosition();
                int currY1 = footman1.getyPosition();
                int currX2 = footman2.getxPosition();
                int currY2 = footman2.getyPosition();
                
                if(doesLocationExist(state, currX1 + 1, currY1)) {
                    footman1.setxPosition(currX1 + 1);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2)) {
                        footman2.setxPosition(currX2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1)) {
                        footman2.setyPosition(currY2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2)) {
                        footman2.setxPosition(currX2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1)) {
                        footman2.setyPosition(currY2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                }
                
                if(doesLocationExist(state, currX1, currY1 + 1)) {
                    footman1.setyPosition(currY1 + 1);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2)) {
                        footman2.setxPosition(currX2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1)) {
                        footman2.setyPosition(currY2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2)) {
                        footman2.setxPosition(currX2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1)) {
                        footman2.setyPosition(currY2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                }
                
                if(doesLocationExist(state, currX1 - 1, currY1)) {
                    footman1.setxPosition(currX1 - 1);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2)) {
                        footman2.setxPosition(currX2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1)) {
                        footman2.setyPosition(currY2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2)) {
                        footman2.setxPosition(currX2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1)) {
                        footman2.setyPosition(currY2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                }
                
                if(doesLocationExist(state, currX1, currY1 - 1)) {
                    footman1.setyPosition(currY1 - 1);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2)) {
                        footman2.setxPosition(currX2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1)) {
                        footman2.setyPosition(currY2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2)) {
                        footman2.setxPosition(currX2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1)) {
                        footman2.setyPosition(currY2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                }
            }
            
        }
        
        if (playerNum == 1) {
            if(playerUnits.size() == 1) {
                Unit archer = playerUnits.get(0);
                int currX = archer.getxPosition();
                int currY = archer.getyPosition();
                
                if(doesLocationExist(state, currX + 1, currY)) {
                    archer.setxPosition(currX + 1);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                }
                
                if(doesLocationExist(state, currX, currY + 1)) {
                    archer.setyPosition(currY + 1);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                }
                
                if(doesLocationExist(state, currX - 1, currY)) {
                    archer.setxPosition(currX - 1);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                }
                
                if(doesLocationExist(state, currX, currY - 1)) {
                    archer.setyPosition(currY - 1);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                }
            } else {
                Unit archer1 = playerUnits.get(0);
                Unit archer2 = playerUnits.get(1);
                int currX1 = archer1.getxPosition();
                int currY1 = archer1.getyPosition();
                int currX2 = archer2.getxPosition();
                int currY2 = archer2.getyPosition();
                
                if(doesLocationExist(state, currX1 + 1, currY1)) {
                    archer1.setxPosition(currX1 + 1);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2)) {
                        archer2.setxPosition(currX2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1)) {
                        archer2.setyPosition(currY2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2)) {
                        archer2.setxPosition(currX2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1)) {
                        archer2.setyPosition(currY2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                }
                
                if(doesLocationExist(state, currX1, currY1 + 1)) {
                    archer1.setyPosition(currY1 + 1);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2)) {
                        archer2.setxPosition(currX2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1)) {
                        archer2.setyPosition(currY2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2)) {
                        archer2.setxPosition(currX2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1)) {
                        archer2.setyPosition(currY2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                }
                
                if(doesLocationExist(state, currX1 - 1, currY1)) {
                    archer1.setxPosition(currX1 - 1);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2)) {
                        archer2.setxPosition(currX2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1)) {
                        archer2.setyPosition(currY2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2)) {
                        archer2.setxPosition(currX2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1)) {
                        archer2.setyPosition(currY2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                }
                
                if(doesLocationExist(state, currX1, currY1 - 1)) {
                    archer1.setyPosition(currY1 - 1);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2)) {
                        archer2.setxPosition(currX2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1)) {
                        archer2.setyPosition(currY2 + 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2)) {
                        archer2.setxPosition(currX2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1)) {
                        archer2.setyPosition(currY2 - 1);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0));
                    }
                }
                
                //As of right now, this does not account for when footmen / achers attack their opponents as a move. This will
                //be accounted for in the future.
            }
            
            
        }
        
        /**
         * SKELETON CODE:
         * We know that, there are 16 possible moves that can be done if there are 2 footmen.
         * We also know that there is either 5 or 25 moves that the enemy can make can do depending on whether or not
         * there is one or two archers.
         * To generate children, we will create unique states where each footman moves up, moves down, moves left, moves right, and attacks (if possible).
         * For the next set of children nodes (i.e. when it is the enemy's turn, we will create unique states for when the enemy can move up, left, right, down, and attack, if possible).
         * Our terminal state is when either side doesn't have any players remaining.
         */
        return children;
    }
    
    //Generates a new child state depending on the position of units as well as the current player.
    public GameStateChild generateNewState(List<Unit> playerUnits, List<Unit> opposingUnits, int playerNum) {
        StateBuilder builder = new StateBuilder();
        for(Unit opposingUnit : opposingUnits) {
            builder.addUnit(opposingUnit, opposingUnit.getxPosition(), opposingUnit.getyPosition());
        }
        for(Unit playerUnit : playerUnits) {
            builder.addUnit(playerUnit, playerUnit.getxPosition(), playerUnit.getyPosition());
        }
        
        State newState = builder.build();
        
        newState.addPlayer(0);
        newState.addPlayer(1);
        
        GameStateChild child = new GameStateChild(newState.getView(playerNum));
        
        return child;
        
    }
    
    //Determines whether or not a xy-coordinate exists within the current map.
    public boolean doesLocationExist(State.StateView state, int x, int y) {
        int xMax = state.getXExtent();
        int yMax = state.getYExtent();
        
        if(x < 0 || y < 0) {
            return false;
        }
        
        if((x > xMax) || (y > yMax)) {
            return false;
            
        } else {
            return true;
            
        }
    }
    
    //Determines whether or not a potential move for a unit is blocked by a resource.
    public boolean isLocationBlocked(State.StateView state, int x, int y) {
        List<ResourceView> resourceLocations = state.getAllResourceNodes();
        
        for(ResourceView resource : resourceLocations) {
            if(resource.getXPosition() == x && resource.getYPosition() == y) {
                return false;
            }
        }
        
        return true;
    }
}
