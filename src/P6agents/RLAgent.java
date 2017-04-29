
package P6agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.DamageLog;
import edu.cwru.sepia.environment.model.history.DeathLog;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.io.*;
import java.util.*;

public class RLAgent extends Agent {

    /**
     * Set in the constructor. Defines how many learning episodes your agent should run for.
     * When starting an episode. If the count is greater than this value print a message
     * and call sys.exit(0)
     */
    public final int numEpisodes;
    public final boolean shouldPrint = false;
    /**
     * List of your footmen and your enemies footmen
     */
    private List<Integer> myFootmen;
    private List<Integer> enemyFootmen;

    /**
     * Convenience variable specifying enemy agent number. Use this whenever referring
     * to the enemy agent. We will make sure it is set to the proper number when testing your code.
     */
    public static final int ENEMY_PLAYERNUM = 1;

    /**
     * Set this to whatever size your feature vector is.
     */
    public static final int NUM_FEATURES = 4;

    /** Use this random number generator for your epsilon exploration. When you submit we will
     * change this seed so make sure that your agent works for more than the default seed.
     */
    public final Random random = new Random(54321);
    /**
     * Your Q-function weights.
     */
    public Double[] weights;

    /**
     * These variables are set for you according to the assignment definition. You can change them,
     * but it is not recommended. If you do change them please let us know and explain your reasoning for
     * changing them.
     */
    public final Double gamma = 0.9;
    public final Double learningRate = .0001;
    public final Double epsilon = .02;
    public int damagePerSwing;
    public int baseFMHealth;
    public int triggerUnitId;
    public Order[] orders = new Order[30];
    public boolean isEvalEp;
    public int epLeft = 0;
    public int totalEp = 0;
    public List<Double> testData = new ArrayList<Double>(); 
    public Double[] cumulativeReward = new Double[5];
    public Double[] rewards = new Double[30];
    public Double epReward;
    public RLAgent(int playernum, String[] args) {
        super(playernum);

        if (args.length >= 1) {
            numEpisodes = Integer.parseInt(args[0]);
            System.out.println("Running " + numEpisodes + " episodes.");
        } else {
            numEpisodes = 10;
            System.out.println("Warning! Number of episodes not specified. Defaulting to 10 episodes.");
        }

        boolean loadWeights = false;
        if (args.length >= 2) {
            loadWeights = Boolean.parseBoolean(args[1]);
        } else {
            System.out.println("Warning! Load weights argument not specified. Defaulting to not loading.");
        }

        if (loadWeights) {
            weights = loadWeights();
        } else {
            // initialize weights to random values between -1 and 1
            weights = new Double[NUM_FEATURES];
            for (int i = 0; i < weights.length; i++) {
                weights[i] = random.nextDouble() * 2 - 1;
            }
        }
    }

    /**
     * We've implemented some setup code for your convenience. Change what you need to.
     */
    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {

        // You will need to add code to check if you are in a testing or learning episode
    	if(epLeft == 0){
    		if(totalEp == 0 || isEvalEp){
    			epLeft = 10;
    			isEvalEp = false;
    		} else {
    			epLeft = 5;
    			isEvalEp = true;
    		}
    	}
        // Find all of your units
        myFootmen = new LinkedList<Integer>();
        int numFootmen = 0;
        for (Integer unitId : stateView.getUnitIds(playernum)) {
            Unit.UnitView unit = stateView.getUnit(unitId);

            String unitName = unit.getTemplateView().getName().toLowerCase();
            if (unitName.equals("footman")) {
                myFootmen.add(unitId);
                numFootmen++;
            } else {
                System.err.println("Unknown unit type: " + unitName);
            }
        }

        // Find all of the enemy units
        enemyFootmen = new LinkedList<Integer>();
        for (Integer unitId : stateView.getUnitIds(ENEMY_PLAYERNUM)) {
            Unit.UnitView unit = stateView.getUnit(unitId);

            String unitName = unit.getTemplateView().getName().toLowerCase();
            if (unitName.equals("footman")) {
                enemyFootmen.add(unitId);
            } else {
                System.err.println("Unknown unit type: " + unitName);
            }
        }

        return middleStep(stateView, historyView);
    }

    /**
     * You will need to calculate the reward at each step and update your totals. You will also need to
     * check if an event has occurred. If it has then you will need to update your weights and select a new action.
     *
     * If you are using the footmen vectors you will also need to remove killed units. To do so use the historyView
     * to get a DeathLog. Each DeathLog tells you which player's unit died and the unit ID of the dead unit. To get
     * the deaths from the last turn do something similar to the following snippet. Please be aware that on the first
     * turn you should not call this as you will get nothing back.
     *
     * for(DeathLog deathLog : historyView.getDeathLogs(stateView.getTurnNumber() -1)) {
     *     System.out.println("Player: " + deathLog.getController() + " unit: " + deathLog.getDeadUnitID());
     * }
     *
     * You should also check for completed actions using the history view. Obviously you never want a footman just
     * sitting around doing nothing (the enemy certainly isn't going to stop attacking). So at the minimum you will
     * have an even whenever one your footmen's targets is killed or an action fails. Actions may fail if the target
     * is surrounded or the unit cannot find a path to the unit. To get the action results from the previous turn
     * you can do something similar to the following. Please be aware that on the first turn you should not call this
     *
     * Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
     * for(ActionResult result : actionResults.values()) {
     *     System.out.println(result.toString());
     * }
     *
     * @return New actions to execute or nothing if an event has not occurred.
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        //Check if an event has occurred, if it hasn't, just return same , we can catch that in 
    	if(stateView.getTurnNumber() != 0){
    		double pTurnReward = 0;
    		for(Integer id: myFootmen){
    			double r = calculateReward(stateView, historyView, id);
    			rewards[id] = r;
    			pTurnReward += r;
    		}
    		epReward = pTurnReward;
    	}
    	if(!hasEventOccurred(stateView, historyView) && stateView.getTurnNumber() != 0){
    		//State.StateView stateView, History.HistoryView historyView, int footmanId
    		Map<Integer, Action> actions = new HashMap<Integer, Action>();
    		for(Order o: orders){
    			if(o != null){
    				actions.put(o.attackerId, o.getSepAction());
    			}
    		}
    		return actions;
    	} else {
    		//First, updateUnitLists to figure out who we have left
    		updateUnitLists(stateView);
    		//Next, determine what unit triggered the event should be triggerUnitId
    		//Right now this is always an enemy, check our current attack actions, see which of our units should be affected
    		//if current attack order is attacking tUID, add them to the list
    		//Did that, should pull their orders off of the orders list here, so that # attacking computes correctly
    		
    		//Begin process of updating orders for each footman
    		if(!isEvalEp){
    			for(int id: myFootmen){
    				Order tmp = orders[id];
    				if (tmp != null){
    					//Double[] oldWeights, Double[] oldFeatures, Double totalReward, State.StateView stateView, History.HistoryView historyView, int footmanId
                        weights = updateWeights(weights, tmp.oldFeatures, rewards[id], stateView, historyView, id);
                        
    				}
    			}
    			if(shouldPrint){
    				System.out.println(Arrays.toString(weights));
    			}
    		}
    		for(int id: myFootmen){
                int target = selectAction(stateView, historyView, id);
                orders[id] = new Order(id, target);
                
                Order order = orders[id];
                
                order.QVal = calcQValue(stateView, historyView, id, target);
                order.oldFeatures = calculateFeatureVector(stateView, historyView, id, target);
    			
    		}
    		//Create the actions list
    		Map<Integer, Action> actions = new HashMap<Integer, Action>();
    		for(Order o: orders){
    			if(o != null){
    				actions.put(o.attackerId, o.getSepAction());
    			}
    		}
    		
    		return actions;
    	}
    }

    /**
     * Here you will calculate the cumulative average rewards for your testing episodes. If you have just
     * finished a set of test episodes you will call out testEpisode.
     *
     * It is also a good idea to save your weights with the saveWeights function.
     */
    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {
    	
        // MAKE SURE YOU CALL printTestData after you finish a test episode.
    	if(!isEvalEp){
    		totalEp++;
    	} else {
    		cumulativeReward[5 - epLeft] = epReward;
    		if(epLeft == 1){
    			double average = 0;
    			for(Double d: cumulativeReward){
    				average += d;
    			}
    			average = average / 5;
    			testData.add(average);
    			if(shouldPrint){
    				System.out.println("Done with eval Period");
    				System.out.println(Arrays.toString(cumulativeReward));
    			}
    			cumulativeReward = new Double[5];
    		}
    	}
    	epLeft--;
    	if(totalEp/10 >= numEpisodes){
    		printTestData(testData);
    		System.exit(0);
    	}
        // Save your weights
        saveWeights(weights);

    }
    
    //Checks to see if an event has occurred. Returns true if so.
    public boolean hasEventOccurred(State.StateView stateView, History.HistoryView historyView){
        
        int prevTurn = stateView.getTurnNumber() - 1;
        boolean event = false;
        
        if(prevTurn < 0) {
            return false;
        }
        
        for(DeathLog deathLogs : historyView.getDeathLogs(prevTurn)) {
                event = true;
        }
        
        return event;
    }
    
    /**
     * 
     * @param stateView
     */
    public void updateUnitLists(State.StateView stateView){
    	List<Integer> myUpdFootmen = new ArrayList<Integer>();
    	List<Integer> eUpdFootmen = new ArrayList<Integer>();
    	 for (Integer unitId : stateView.getUnitIds(playernum)) {
             Unit.UnitView unit = stateView.getUnit(unitId);

             String unitName = unit.getTemplateView().getName().toLowerCase();
             if (unitName.equals("footman")) {
                myUpdFootmen.add(unitId);
             } else {
                 System.err.println("Unknown unit type: " + unitName);
             }
         }
    	 for (Integer unitId : stateView.getUnitIds(ENEMY_PLAYERNUM)) {
             Unit.UnitView unit = stateView.getUnit(unitId);

             String unitName = unit.getTemplateView().getName().toLowerCase();
             if (unitName.equals("footman")) {
                 eUpdFootmen.add(unitId);
             } else {
                 System.err.println("Unknown unit type: " + unitName);
             }
         }
    	 myFootmen = myUpdFootmen;
    	 enemyFootmen = eUpdFootmen;
    	 
    }
    /**
     * This method takes the state, and an enemyId, usually the enemy who triggered the Event, 
     * and finds which units need to recalculate their orders, based on this.
     * @param stateView
     * @param eId
     * @return
     */
    public List<Order> getAffectedUnits(State.StateView stateView, int eId){
    	List<Order> affectedUnits  = new ArrayList<Order>();
    	for(Order o: orders){
    		if(o.defenderId == eId){
    			affectedUnits.add(o);
    		}
    	}
    	return affectedUnits;
    }
    /**
     * Calculate the updated weights for this agent. 
     * @param oldWeights Weights prior to update
     * @param oldFeatures Features from (s,a)
     * @param totalReward Cumulative discounted reward for this footman.
     * @param stateView Current state of the game.
     * @param historyView History of the game up until this point
     * @param footmanId The footman we are updating the weights for
     * @return The updated weight vector.
     */
    public Double[] updateWeights(Double[] oldWeights, Double[] oldFeatures, Double totalReward, State.StateView stateView, History.HistoryView historyView, int footmanId) {
        Double[] newWeights = new Double[oldWeights.length];
    	for(int i = 0; i < newWeights.length; i++){
    		newWeights[i] = oldWeights[i] + learningRate*(totalReward + gamma*(getMaxQ(stateView, historyView, footmanId)-orders[footmanId].QVal))*oldFeatures[i];
    	}
    	return newWeights;
    	
    }

    /**
     * Given a footman and the current state and history of the game select the enemy that this unit should
     * attack. This is where you would do the epsilon-greedy action selection.
     *
     * @param stateView Current state of the game
     * @param historyView The entire history of this episode
     * @param attackerId The footman that will be attacking
     * @return The enemy footman ID this unit should attack
     */
    public int selectAction(State.StateView stateView, History.HistoryView historyView, int attackerId) {

    	Double max = Double.NEGATIVE_INFINITY;
        int maxId = -1;
        for (Integer enemy : enemyFootmen) {
        	Double tmp = calcQValue(stateView, historyView, attackerId, enemy);
        	if(shouldPrint){
        		System.out.println(tmp);
        	}
        	if(tmp > max){
        		maxId = enemy;
        		max = tmp;
        	}
        }

        //This block of code determines whether or not the agent should take a random action instead of the best action.
        int randVal = generateRandomVal(0, 100);
        Double randFactor = (double)(randVal/100);

        if(randFactor <= epsilon) {

        	int randomEnem = generateRandomVal(0, enemyFootmen.size() - 1);
        	return enemyFootmen.get(randomEnem);

        } else {

        	return maxId;

        }
    }
    
    //Generates a random value within a range.
    public int generateRandomVal(int min, int max) {
        
        int rangeVal = (max - min) + 1;
        return (int)(Math.random() * rangeVal) + min;
        
    }
    
    public Double getMaxQ(State.StateView stateView, History.HistoryView historyView, int attackerId){
        
        Double max = Double.NEGATIVE_INFINITY;
        for (Integer enemy : enemyFootmen) {
        	Double tmp = calcQValue(stateView, historyView, attackerId, enemy);
            if(tmp > max){
            	max = tmp;
            }
            
        }
        return max;
    }

    /**
     * Given the current state and the footman in question calculate the reward received on the last turn.
     * This is where you will check for things like Did this footman take or give damage? Did this footman die
     * or kill its enemy. Did this footman start an action on the last turn? See the assignment description
     * for the full list of rewards.
     *
     * Remember that you will need to discount this reward based on the timestep it is received on. See
     * the assignment description for more details.
     *
     * As part of the reward you will need to calculate if any of the units have taken damage. You can use
     * the history view to get a list of damages dealt in the previous turn. Use something like the following.
     *
     * for(DamageLog damageLogs : historyView.getDamageLogs(lastTurnNumber)) {
     *     System.out.println("Defending player: " + damageLog.getDefenderController() + " defending unit: " + \
     *     damageLog.getDefenderID() + " attacking player: " + damageLog.getAttackerController() + \
     *     "attacking unit: " + damageLog.getAttackerID());
     * }
     *
     * You will do something similar for the deaths. See the middle step documentation for a snippet
     * showing how to use the deathLogs.
     *
     * To see if a command was issued you can check the commands issued log.
     *
     * Map<Integer, Action> commandsIssued = historyView.getCommandsIssued(playernum, lastTurnNumber);
     * for (Map.Entry<Integer, Action> commandEntry : commandsIssued.entrySet()) {
     *     System.out.println("Unit " + commandEntry.getKey() + " was command to " + commandEntry.getValue().toString);
     * }
     *
     * @param stateView The current state of the game.
     * @param historyView History of the episode up until this turn.
     * @param footmanId The footman ID you are looking for the reward from.
     * @return The current reward
     */
    public Double calculateReward(State.StateView stateView, History.HistoryView historyView, int footmanId) {
        
    	Double damageReward = 0.0;
    	Double actionReward = 0.0;
    	Double deathReward = 0.0;
    	Double totalReward = 0.0;
    	int id = footmanId;
    	
    	int prevTurn = stateView.getTurnNumber() - 1;
    	
    	if (prevTurn < 0) {
    		return 0.0;
    	}
    	
    		
    	//This for block determines the amount of damage given / taken within the last round.
    	for(DamageLog damageLogs : historyView.getDamageLogs(prevTurn)) {
    			
    		int attackerId = damageLogs.getAttackerID();
    		int defenderId = damageLogs.getDefenderID();
    		int damageAmt = damageLogs.getDamage();
    		
    			
    		if (attackerId == id) {
    			damageReward = damageReward + damageAmt;
    	
    			//Checks to see if the unit the friendly footman attacked was killed. If so, then a reward is given.
    		    
    		} else if (defenderId == id) {
    			damageReward = damageReward - damageAmt;
    		}
    			
   		}
    		
    	//This for block determines the death reward amount within the last round.
    	for(DeathLog deathLogs : historyView.getDeathLogs(prevTurn)) {
    			
    		int deadUnit = deathLogs.getDeadUnitID();
    			
    		if (id == deadUnit) {
    			deathReward = deathReward - 100.0;
    		}
    			
   		}
    
    		//This block of code determines how many actions were performed by friendly footmen.
    	Map<Integer, Action> commandsIssued = historyView.getCommandsIssued(0, prevTurn);
    		
    	for (Map.Entry<Integer, Action> commandEntry : commandsIssued.entrySet()) {
    			
    		if (commandEntry.getKey() == id) {
    			actionReward = actionReward - 0.1;
    		}
    			
   		}
    	
    	
    	totalReward = damageReward + actionReward + deathReward;
    	
    	return totalReward;
    }

    /**
     * Calculate the Q-Value for a given state action pair. The state in this scenario is the current
     * state view and the history of this episode. The action is the attacker and the enemy pair for the
     * SEPIA attack action.
     *
     * This returns the Q-value according to your feature approximation. This is where you will calculate
     * your features and multiply them by your current weights to get the approximate Q-value.
     *
     * @param stateView Current SEPIA state
     * @param historyView Episode history up to this point in the game
     * @param attackerId Your footman. The one doing the attacking.
     * @param defenderId An enemy footman that your footman would be attacking
     * @return The approximate Q-value
     */
    public Double calcQValue(State.StateView stateView,
		History.HistoryView historyView,
		int attackerId,
		int defenderId) {
	Double Qval = 0.0;
	Double[] featureVector = calculateFeatureVector(stateView, historyView, attackerId, defenderId);

	for(int i = 0; i <= featureVector.length - 1; i++) {
		Qval = Qval + (featureVector[i] * weights[i]);
	}
	

	return Qval;
}
    /**
     * Given a state and action calculate your features here. Please include a comment explaining what features
     * you chose and why you chose them.
     *
     * All of your feature functions should evaluate to a Double. Collect all of these into an array. You will
     * take a dot product of this array with the weights array to get a Q-value for a given state action.
     *
     * It is a good idea to make the first value in your array a constant. This just helps remove any offset
     * from 0 in the Q-function. The other features are up to you. Many are suggested in the assignment
     * description.
     *
     * @param stateView Current state of the SEPIA game
     * @param historyView History of the game up until this turn
     * @param attackerId Your footman. The one doing the attacking.
     * @param defenderId An enemy footman. The one you are considering attacking.
     * @return The array of feature function outputs.
     */
    public Double[] calculateFeatureVector(State.StateView stateView, History.HistoryView historyView,int attackerId,int defenderId) {
        
        Double constant = 3.0;
        Double chebDist = chebDist(stateView, attackerId, defenderId);
        Double numFriendsAttackingE = numFAttackingE(stateView, historyView, defenderId);
        Double enemRemainingHealth = enemRemainingHealth(stateView, defenderId);
        
        Double[] featureVector = {constant, numFriendsAttackingE, chebDist, enemRemainingHealth};
        
        return featureVector;
    }
    
    public Double enemRemainingHealth(State.StateView stateView, int enemId) {
        
        UnitView enemUnit = stateView.getUnit(enemId);
        int health = enemUnit.getHP();
        
        return (double)health;
        
    }
    
    public Double numFAttackingE(State.StateView stateView, History.HistoryView historyView, int enemId) {
        
        Double numAttackingE = 0.0;

        for(Order order : orders) {
        	if(order != null){
        		if (enemId == order.defenderId) {
        			numAttackingE = numAttackingE + 1.0;
        		}
        	}            
        }

        return numAttackingE;
    }
    
    public double chebDist(State.StateView stateView, int friend, int enem) {
        
        UnitView friendUnit = stateView.getUnit(friend);
        UnitView enemUnit = stateView.getUnit(enem);
        
        double dx = Math.abs(friendUnit.getXPosition() - enemUnit.getXPosition());
        double dy = Math.abs(friendUnit.getYPosition() - enemUnit.getYPosition());
        
        return Math.max(dx, dy);
    }

    /**
     * DO NOT CHANGE THIS!
     *
     * Prints the learning rate data described in the assignment. Do not modify this method.
     *
     * @param averageRewards List of cumulative average rewards from test episodes.
     */
    public void printTestData (List<Double> averageRewards) {
        System.out.println("");
        System.out.println("Games Played      Average Cumulative Reward");
        System.out.println("-------------     -------------------------");
        for (int i = 0; i < averageRewards.size(); i++) {
            String gamesPlayed = Integer.toString(10*i);
            String averageReward = String.format("%.2f", averageRewards.get(i));

            int numSpaces = "-------------     ".length() - gamesPlayed.length();
            StringBuffer spaceBuffer = new StringBuffer(numSpaces);
            for (int j = 0; j < numSpaces; j++) {
                spaceBuffer.append(" ");
            }
            System.out.println(gamesPlayed + spaceBuffer.toString() + averageReward);
        }
        System.out.println("");
    }

    /**
     * DO NOT CHANGE THIS!
     *
     * This function will take your set of weights and save them to a file. Overwriting whatever file is
     * currently there. You will use this when training your agents. You will include th output of this function
     * from your trained agent with your submission.
     *
     * Look in the agent_weights folder for the output.
     *
     * @param weights Array of weights
     */
    public void saveWeights(Double[] weights) {
        File path = new File("agent_weights/weights.txt");
        // create the directories if they do not already exist
        path.getAbsoluteFile().getParentFile().mkdirs();

        try {
            // open a new file writer. Set append to false
            BufferedWriter writer = new BufferedWriter(new FileWriter(path, false));

            for (Double weight : weights) {
                writer.write(String.format("%f\n", weight));
            }
            writer.flush();
            writer.close();
        } catch(IOException ex) {
            System.err.println("Failed to write weights to file. Reason: " + ex.getMessage());
        }
    }

    /**
     * DO NOT CHANGE THIS!
     *
     * This function will load the weights stored at agent_weights/weights.txt. The contents of this file
     * can be created using the saveWeights function. You will use this function if the load weights argument
     * of the agent is set to 1.
     *
     * @return The array of weights
     */
    public Double[] loadWeights() {
        File path = new File("agent_weights/weights.txt");
        if (!path.exists()) {
            System.err.println("Failed to load weights. File does not exist");
            return null;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            List<Double> weights = new LinkedList<Double>();
            while((line = reader.readLine()) != null) {
                weights.add(Double.parseDouble(line));
            }
            reader.close();

            return weights.toArray(new Double[weights.size()]);
        } catch(IOException ex) {
            System.err.println("Failed to load weights from file. Reason: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }
}
class Order {
	public int attackerId;
	public int defenderId;
	public Double[] oldFeatures;
	public Double QVal;
	public Order(int aId, int dId){
		this.attackerId = aId;
		this.defenderId = dId;
	}
	public Action getSepAction(){
		return Action.createCompoundAttack(attackerId, defenderId);
	}
}
