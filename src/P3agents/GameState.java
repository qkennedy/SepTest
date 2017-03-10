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
    private int xLim;
    private int yLim;
    private int playerNum;
    private int enemyNum;
    private boolean isArcherPlayer = false; //Assumes that the footmen user always has the first turn.
    private List<Integer> enemyUnitIDs;
    private List<Integer> unitIDs;
    private State.StateView state;
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
        
        if (isArcherPlayer) {
            playerNum = 1;
            enemyNum = 0;
        }
        else {
            playerNum = 0;
            enemyNum = 1;
        }
        
        enemyUnitIDs = state.getUnitIds(enemyNum);
        unitIDs = state.getUnitIds(playerNum);
        xLim = state.getXExtent();
        yLim = state.getYExtent();
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
     * One addition to our utility is the health left for each unit. If an archer has low health, then it would be rational
     * to focus on that archer. On the opposite side of the spectrum,
     * if a footman is injured (i.e. low health), it would not be wise to charge into the battle.
     *
     * Also, if an enemy archer is close, the utility for the footmen will increase (as the footmen need to get in as close as possible
     * to attack the archers).
     *
     * @return The weighted linear combination of the features
     */
    public double getUtility() {
        double archerHealthSum = 0;
        for(Integer i: enemyUnitIDs){
            archerHealthSum += state.getUnit(i).getHP();
        }
        archerHealthSum = -0.5 * archerHealthSum;
        
        double footmanHealthSum = 0;
        for(Integer i: unitIDs){
            footmanHealthSum += state.getUnit(i).getHP();
        }
        footmanHealthSum = 0.3 * footmanHealthSum;
        
        double footmenToArchers = 0.0;
        for(Integer i : unitIDs) {
            footmenToArchers += getDistToArcher(state.getUnit(i).getID());
        }
        footmenToArchers = 0.4 * footmenToArchers;
        
        double footmenWithinRange = 0.0;
        for(Integer i : unitIDs) {
            if (getDistToArcher(i) <= 2) {
                footmenWithinRange = 100.0;
            }
            else footmenWithinRange = 0.0;
        }
        
        return footmanHealthSum + archerHealthSum + footmenToArchers + footmenWithinRange;
    }
    
    //Gets the distance of the nearest archer relative to the current footman.
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
    
    //Finds the location of an archer around a footman
    public int findNearbyEnemLoc(int unid) {
        
        int enemID = -1;
        List<UnitView> enemUnits = state.getUnits(enemyNum);
        UnitView fm = state.getUnit(unid);
        int fmX = fm.getXPosition();
        int fmY = fm.getYPosition();
        int sumXCoords = fmX + fmY;
        
        for(UnitView enemies : enemUnits) {
            
            int sumCoords = enemies.getXPosition() + enemies.getYPosition();
            
            if (Math.abs(sumXCoords - sumCoords) <= 2) {
                enemID = enemies.getID();
            }
        }
        
        return enemID;
    }
    
    //Switches the player of the current state.
    public void switchPlayers() {
        isArcherPlayer = !isArcherPlayer;
        
        if (isArcherPlayer) {
            playerNum = 1;
            enemyNum = 0;
        }
        else {
            playerNum = 0;
            enemyNum = 1;
        }
        
        enemyUnitIDs.clear();
        unitIDs.clear();
        
        enemyUnitIDs = state.getUnitIds(enemyNum);
        unitIDs = state.getUnitIds(playerNum);
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
        
        List<GameStateChild> children = new ArrayList<GameStateChild>();
        
        GameStateChild child;
        
        List<UnitView> currPlayerUnitViews = state.getUnits(playerNum);
        List<UnitView> opposingPlayerUnitViews = state.getUnits(enemyNum);
        
        //Checks to see whether or not current state is terminal. Returns empty list if that is the case.
        if((currPlayerUnitViews.size() == 0) || (opposingPlayerUnitViews.size() == 0))
            return children;
        
        
        List<Unit> playerUnits = new ArrayList<Unit>();
        List<Unit> opposingPlayerUnits = new ArrayList<Unit>();
        
        for(UnitView unitView : currPlayerUnitViews) {
            Unit unit = new Unit(new UnitTemplate(unitView.getID()), unitView.getID());
            unit.setxPosition(unitView.getXPosition());
            unit.setyPosition(unitView.getYPosition());
            unit.setHP(unitView.getHP());
            unit.setDurativeStatus(unitView.getCurrentDurativeAction(), unitView.getCurrentDurativeProgress());
            unit.setCargo(unitView.getCargoType(), unitView.getCargoAmount());
            playerUnits.add(unit);
        }
        
        for(UnitView unitView : opposingPlayerUnitViews) {
            Unit unit = new Unit(new UnitTemplate(unitView.getID()), unitView.getID());
            unit.setxPosition(unitView.getXPosition());
            unit.setyPosition(unitView.getYPosition());
            unit.setHP(unitView.getHP());
            unit.setDurativeStatus(unitView.getCurrentDurativeAction(), unitView.getCurrentDurativeProgress());
            unit.setCargo(unitView.getCargoType(), unitView.getCargoAmount());
            opposingPlayerUnits.add(unit);
        }
        
        Map<Integer, Action> unitActionMap = new HashMap<Integer,Action>();
        
        if (playerNum == 0) {
            
            
            
            if(playerUnits.size() == 1) {
                Unit footman = playerUnits.get(0);
                int currX = footman.getxPosition();
                int currY = footman.getyPosition();
                
                if(getDistToArcher(footman.ID) <= 2 && findNearbyEnemLoc(footman.ID) != -1) {
                    Action footmanAction = Action.createCompoundAttack(footman.ID, findNearbyEnemLoc(footman.ID));
                    unitActionMap.put(footman.ID, footmanAction);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                }
                
                if(doesLocationExist(state, currX + 1, currY) && isLocationNotBlocked(state, currX + 1, currY)) {
                    footman.setxPosition(currX + 1);
                    Action footmanAction = Action.createCompoundMove(footman.ID, currX + 1, currY);
                    unitActionMap.put(footman.ID, footmanAction);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                }
                
                if(doesLocationExist(state, currX, currY + 1) && isLocationNotBlocked(state, currX, currY + 1)) {
                    footman.setyPosition(currY + 1);
                    Action footmanAction = Action.createCompoundMove(footman.ID, currX, currY + 1);
                    unitActionMap.put(footman.ID, footmanAction);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                }
                
                if(doesLocationExist(state, currX - 1, currY) && isLocationNotBlocked(state, currX - 1, currY)) {
                    footman.setxPosition(currX - 1);
                    Action footmanAction = Action.createCompoundMove(footman.ID, currX - 1, currY);
                    unitActionMap.put(footman.ID, footmanAction);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                }
                
                if(doesLocationExist(state, currX, currY - 1) && isLocationNotBlocked(state, currX, currY - 1)) {
                    footman.setyPosition(currY - 1);
                    Action footmanAction = Action.createCompoundMove(footman.ID, currX, currY - 1);
                    unitActionMap.put(footman.ID, footmanAction);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                }
            } else {
                Unit footman1 = playerUnits.get(0);
                Unit footman2 = playerUnits.get(1);
                int currX1 = footman1.getxPosition();
                int currY1 = footman1.getyPosition();
                int currX2 = footman2.getxPosition();
                int currY2 = footman2.getyPosition();
                
                if(getDistToArcher(footman1.ID) <= 2 && findNearbyEnemLoc(footman1.ID) != -1) {
                    Action footmanAction = Action.createCompoundAttack(footman1.ID, findNearbyEnemLoc(footman1.ID));
                    unitActionMap.put(footman1.ID, footmanAction);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    
                    if(getDistToArcher(footman2.ID) <= 2 && findNearbyEnemLoc(footman2.ID) != -1) {
                        Action footman2Action = Action.createCompoundAttack(footman2.ID, findNearbyEnemLoc(footman2.ID));
                        unitActionMap.put(footman2.ID, footman2Action);
                    }
                    
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                }
                
                if(getDistToArcher(footman2.ID) <= 2 && findNearbyEnemLoc(footman2.ID) != -1) {
                    Action footman2Action = Action.createCompoundAttack(footman2.ID, findNearbyEnemLoc(footman2.ID));
                    unitActionMap.put(footman2.ID, footman2Action);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                }
                
                if(doesLocationExist(state, currX1 + 1, currY1) && isLocationNotBlocked(state, currX1 + 1, currY1)) {
                    footman1.setxPosition(currX1 + 1);
                    Action footmanAction = Action.createCompoundMove(footman1.ID, currX1 + 1, currY1);
                    unitActionMap.put(footman1.ID, footmanAction);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2) && isLocationNotBlocked(state, currX2 + 1, currY2)) {
                        footman2.setxPosition(currX2 + 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2 + 1, currY2);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1) && isLocationNotBlocked(state, currX2, currY2 + 1)) {
                        footman2.setyPosition(currY2 + 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2, currY2 + 1);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2) && isLocationNotBlocked(state, currX2 - 1, currY2)) {
                        footman2.setxPosition(currX2 - 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2 - 1, currY2);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1) && isLocationNotBlocked(state, currX2, currY2 - 1)) {
                        footman2.setyPosition(currY2 - 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2, currY2 - 1);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                }
                
                if(doesLocationExist(state, currX1, currY1 + 1) && isLocationNotBlocked(state, currX1, currY1 + 1)) {
                    footman1.setyPosition(currY1 + 1);
                    Action footmanAction = Action.createCompoundMove(footman2.ID, currX1, currY1 + 1);
                    unitActionMap.put(footman1.ID, footmanAction);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2) && isLocationNotBlocked(state, currX2 + 1, currY2)) {
                        footman2.setxPosition(currX2 + 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2 + 1, currY2);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1) && isLocationNotBlocked(state, currX2, currY2 + 1)) {
                        footman2.setyPosition(currY2 + 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2, currY2 + 1);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2) && isLocationNotBlocked(state, currX2 - 1, currY2)) {
                        footman2.setxPosition(currX2 - 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2 - 1, currY2);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1) && isLocationNotBlocked(state, currX2, currY2 - 1)) {
                        footman2.setyPosition(currY2 - 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2, currY2 - 1);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                }
                
                if(doesLocationExist(state, currX1 - 1, currY1) && isLocationNotBlocked(state, currX1 - 1, currY1)) {
                    footman1.setxPosition(currX1 - 1);
                    Action footmanAction = Action.createCompoundMove(footman1.ID, currX1 - 1, currY1);
                    unitActionMap.put(footman1.ID, footmanAction);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2) && isLocationNotBlocked(state, currX2 + 1, currY2)) {
                        footman2.setxPosition(currX2 + 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2 + 1, currY2);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1) && isLocationNotBlocked(state, currX2, currY2 + 1)) {
                        footman2.setyPosition(currY2 + 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2, currY2 + 1);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2) && isLocationNotBlocked(state, currX2 - 1, currY2)) {
                        footman2.setxPosition(currX2 - 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2 - 1, currY2);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1) && isLocationNotBlocked(state, currX2, currY2 - 1)) {
                        footman2.setyPosition(currY2 - 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2, currY2 - 1);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                }
                
                if(doesLocationExist(state, currX1, currY1 - 1) && isLocationNotBlocked(state, currX1, currY1 - 1)) {
                    footman1.setyPosition(currY1 - 1);
                    Action footmanAction = Action.createCompoundMove(footman1.ID, currX1, currY1 - 1);
                    unitActionMap.put(footman1.ID, footmanAction);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2) && isLocationNotBlocked(state, currX2 + 1, currY2)) {
                        footman2.setxPosition(currX2 + 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2 + 1, currY2);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1) && isLocationNotBlocked(state, currX2, currY2 + 1)) {
                        footman2.setyPosition(currY2 + 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2, currY2 + 1);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                        
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2) && isLocationNotBlocked(state, currX2 - 1, currY2)) {
                        footman2.setxPosition(currX2 - 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2 - 1, currY2);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1) && isLocationNotBlocked(state, currX2, currY2 - 1)) {
                        footman2.setyPosition(currY2 - 1);
                        Action footman2Action = Action.createCompoundMove(footman2.ID, currX2, currY2 - 1);
                        unitActionMap.put(footman2.ID, footman2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 0, unitActionMap));
                    }
                }
            }
            
        }
        
        if (playerNum == 1) {
            if(playerUnits.size() == 1) {
                Unit archer = playerUnits.get(0);
                int currX = archer.getxPosition();
                int currY = archer.getyPosition();
                
                if(footmanInRange(archer.ID) != -1) {
                    Action archerAction = Action.createCompoundAttack(archer.ID, footmanInRange(archer.ID));
                    unitActionMap.put(archer.ID, archerAction);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                }
                
                if(doesLocationExist(state, currX + 1, currY) && isLocationNotBlocked(state, currX + 1, currY)) {
                    archer.setxPosition(currX + 1);
                    Action archerAction = Action.createCompoundMove(archer.ID, currX + 1, currY);
                    unitActionMap.put(archer.ID, archerAction);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                }
                
                if(doesLocationExist(state, currX, currY + 1) && isLocationNotBlocked(state, currX, currY + 1)) {
                    archer.setyPosition(currY + 1);
                    Action archerAction = Action.createCompoundMove(archer.ID, currX, currY + 1);
                    unitActionMap.put(archer.ID, archerAction);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                }
                
                if(doesLocationExist(state, currX - 1, currY) && isLocationNotBlocked(state, currX - 1, currY)) {
                    archer.setxPosition(currX - 1);
                    Action archerAction = Action.createCompoundMove(archer.ID, currX - 1, currY);
                    unitActionMap.put(archer.ID, archerAction);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                }
                
                if(doesLocationExist(state, currX, currY - 1) && isLocationNotBlocked(state, currX, currY - 1)) {
                    archer.setyPosition(currY - 1);
                    Action archerAction = Action.createCompoundMove(archer.ID, currX, currY - 1);
                    unitActionMap.put(archer.ID, archerAction);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                }
            } else {
                Unit archer1 = playerUnits.get(0);
                Unit archer2 = playerUnits.get(1);
                int currX1 = archer1.getxPosition();
                int currY1 = archer1.getyPosition();
                int currX2 = archer2.getxPosition();
                int currY2 = archer2.getyPosition();
                
                if(footmanInRange(archer1.ID) != -1) {
                    Action archerAction = Action.createCompoundAttack(archer1.ID, footmanInRange(archer1.ID));
                    unitActionMap.put(archer1.ID, archerAction);
                    
                    if(footmanInRange(archer2.ID) != -1) {
                        Action archer2Action = Action.createCompoundAttack(archer2.ID, footmanInRange(archer2.ID));
                        unitActionMap.put(archer2.ID, archer2Action);
                    }
                    
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                }
                
                if(footmanInRange(archer2.ID) != -1) {
                    Action archer2Action = Action.createCompoundAttack(archer2.ID, footmanInRange(archer2.ID));
                    unitActionMap.put(archer2.ID, archer2Action);
                    children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                }
                
                if(doesLocationExist(state, currX1 + 1, currY1) && isLocationNotBlocked(state, currX1 + 1, currY1)) {
                    archer1.setxPosition(currX1 + 1);
                    Action archerAction = Action.createCompoundMove(archer1.ID, currX1 + 1, currY1);
                    unitActionMap.put(archer1.ID, archerAction);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2) && isLocationNotBlocked(state, currX2 + 1, currY2)) {
                        archer2.setxPosition(currX2 + 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2 + 1, currY2);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1) && isLocationNotBlocked(state, currX2, currY2 + 1)) {
                        archer2.setyPosition(currY2 + 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2, currY2 + 1);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2) && isLocationNotBlocked(state, currX2 - 1, currY2)) {
                        archer2.setxPosition(currX2 - 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2 - 1, currY2);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1) && isLocationNotBlocked(state, currX2, currY2 - 1)) {
                        archer2.setyPosition(currY2 - 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2, currY2 - 1);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                }
                
                if(doesLocationExist(state, currX1, currY1 + 1) && isLocationNotBlocked(state, currX1, currY1 + 1)) {
                    archer1.setyPosition(currY1 + 1);
                    Action archerAction = Action.createCompoundMove(archer1.ID, currX1, currY1 + 1);
                    unitActionMap.put(archer1.ID, archerAction);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2) && isLocationNotBlocked(state, currX2 + 1, currY2)) {
                        archer2.setxPosition(currX2 + 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2 + 1, currY2);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1) && isLocationNotBlocked(state, currX2, currY2 + 1)) {
                        archer2.setyPosition(currY2 + 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2, currY2 + 1);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2) && isLocationNotBlocked(state, currX2 - 1, currY2)) {
                        archer2.setxPosition(currX2 - 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2 - 1, currY2);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1) && isLocationNotBlocked(state, currX2, currY2 - 1)) {
                        archer2.setyPosition(currY2 - 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2, currY2 - 1);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                }
                
                if(doesLocationExist(state, currX1 - 1, currY1) && isLocationNotBlocked(state, currX1 - 1, currY1)) {
                    archer1.setxPosition(currX1 - 1);
                    Action archerAction = Action.createCompoundMove(archer1.ID, currX1 - 1, currY1);
                    unitActionMap.put(archer1.ID, archerAction);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2) && isLocationNotBlocked(state, currX2 + 1, currY2)) {
                        archer2.setxPosition(currX2 + 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2 + 1, currY2);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1) && isLocationNotBlocked(state, currX2, currY2 + 1)) {
                        archer2.setyPosition(currY2 + 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2, currY2 + 1);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2) && isLocationNotBlocked(state, currX2 - 1, currY2)) {
                        archer2.setxPosition(currX2 - 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2 - 1, currY2);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1) && isLocationNotBlocked(state, currX2, currY2 - 1)) {
                        archer2.setyPosition(currY2 - 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2, currY2 - 1);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                }
                
                if(doesLocationExist(state, currX1, currY1 - 1) && isLocationNotBlocked(state, currX1, currY1 - 1)) {
                    archer1.setyPosition(currY1 - 1);
                    Action archerAction = Action.createCompoundMove(archer1.ID, currX1, currY1 - 1);
                    unitActionMap.put(archer1.ID, archerAction);
                    
                    if(doesLocationExist(state, currX2 + 1, currY2) && isLocationNotBlocked(state, currX2 + 1, currY2)) {
                        archer2.setxPosition(currX2 + 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2 + 1, currY2);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 + 1) && isLocationNotBlocked(state, currX2, currY2 + 1)) {
                        archer2.setyPosition(currY2 + 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2, currY2 + 1);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2 - 1, currY2) && isLocationNotBlocked(state, currX2 - 1, currY2)) {
                        archer2.setxPosition(currX2 - 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2 - 1, currY2);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                    if(doesLocationExist(state, currX2, currY2 - 1) && isLocationNotBlocked(state, currX2, currY2 - 1)) {
                        archer2.setyPosition(currY2 - 1);
                        Action archer2Action = Action.createCompoundMove(archer2.ID, currX2, currY2 - 1);
                        unitActionMap.put(archer2.ID, archer2Action);
                        children.add(generateNewState(playerUnits, opposingPlayerUnits, 1, unitActionMap));
                    }
                }
                
                //As of right now, this does not account for when footmen / achers attack their opponents as a move. This will
                //be accounted for in the future.
            }
            
            
        }
        
        return children;
    }
    
    public int footmanInRange(int uid) {
        
        UnitView archerView = state.getUnit(uid);
        int footmanID = -1;
        int aX = archerView.getXPosition();
        int aY = archerView.getYPosition();
        int midpointA = ((aX + aY) / 2);
        
        List<UnitView> footmen = state.getUnits(enemyNum);
        for(UnitView footman : footmen) {
            int fX = footman.getXPosition();
            int fY = footman.getYPosition();
            int midpointF = ((fX + fY) / 2);
            
            if (Math.abs(aX - fX) <= 7 || Math.abs(aY - fX) <= 7 || Math.abs(midpointA - midpointF) <= 7) {
                footmanID = footman.getID();
                break;
            }
            
        }
        
        return footmanID;
    }
    
    //Generates a new child state depending on the position of units as well as the current player.
    public GameStateChild generateNewState(List<Unit> playerUnits, List<Unit> opposingUnits, int playerNum, Map<Integer, Action> actionsMap) {
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
        
        GameState newGameState = new GameState(newState.getView(playerNum));
        GameStateChild child = new GameStateChild(actionsMap, newGameState);
        
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
    public boolean isLocationNotBlocked(State.StateView state, int x, int y) {
        List<ResourceView> resourceLocations = state.getAllResourceNodes();
        List<UnitView> unitLocations = state.getAllUnits();
        
        for(ResourceView resource : resourceLocations) {
            if(resource.getXPosition() == x && resource.getYPosition() == y) {
                return false;
            }
        }
        
        for(UnitView unit : unitLocations) {
            if(unit.getXPosition() == x && unit.getYPosition() == y) {
                return false;
            }
        }
        
        return true;
    }
}
