package P3agents;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MinimaxAlphaBeta extends Agent {

    private final int numPlys;

    public MinimaxAlphaBeta(int playernum, String[] args)
    {
        super(playernum);

        if(args.length < 1)
        {
            System.err.println("You must specify the number of plys");
            System.exit(1);
        }

        numPlys = Integer.parseInt(args[0]);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate),
                numPlys,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);

        return bestChild.action;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    public GameStateChild winnerKid = null;
    public double winAlpha = Double.MIN_VALUE;
    public int startDepth;
    /**
     * You will implement this.
     *
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     *
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     */
    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta)
    {
    	startDepth = depth;
    	alphaBeta(node, depth, alpha, beta, true);
    	return winnerKid;
    	
    }
    

    public double alphaBeta(GameStateChild node, int depth, double alpha, double beta, boolean isA){
    	List<GameStateChild> chillens = new ArrayList<GameStateChild>();
    	if(depth==0 || isTerminal(node)){
    		if(isA){
    			return beta;
    		} else {
    			return alpha;
    		}
    	}
    	if(isA){
    		chillens = orderChildrenWithHeuristics(node.state.getChildren());
    		for(GameStateChild kid: chillens){

    			alpha = Math.max(alpha, alphaBeta(kid, depth-1, alpha, beta, !isA));
    			if(depth - 1 == startDepth){
    				if(alpha>winAlpha){
    					winnerKid = kid;
    					winAlpha=alpha;
    				}
    			}
    			if(beta<=alpha){
    				break;
    			}
    		}
    	} else {
    		chillens = orderChildrenWithHeuristics(node.state.getChildren());
    		for(GameStateChild kid: chillens){
    			beta = Math.min(beta, alphaBeta(kid, depth-1, alpha, beta, !isA));
    			if(beta<=alpha){
    				break;
    			}
    		}
    	}
    	return 0;
    }
    
    public static boolean isTerminal(GameStateChild kid){
    	return kid.state.getChildren().size() == 0;
    }
    /**
     * You will implement this.
     *
     * Given a list of children you will order them according to heuristics you make up.
     * See the assignment description for suggestions on heuristics to use when sorting.
     *
     * Use this function inside of your alphaBetaSearch method.
     *
     * Include a good comment about what your heuristics are and why you chose them.
     *
     * @param children
     * @return The list of children sorted by your heuristic.
     */
    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children)
    {
    	
    	GameStateChild[] unsortedChildren = new GameStateChild[children.size() - 1];
    	
    	for(int i = 0; i < children.size() - 1; i++) {
    		unsortedChildren[i] = children.get(i);
    	}
    	
    	quickSort(unsortedChildren);
    	
    	List<GameStateChild> sortedChildren = new ArrayList<GameStateChild>();
    	
    	for(int i = 0; i < unsortedChildren.length - 1; i++) {
    		sortedChildren.add(unsortedChildren[i]);
    	}
    	
        return sortedChildren;
    }
    
    public void quickSort(GameStateChild[] childList) {
    	childNodeQuickSort(childList, 0, childList.length - 1);
    }
    
    public static void childNodeQuickSort(GameStateChild[] childList, int first, int last) {	
    	if(first >= last)
    		return;
    	int split = partition(childList, first, last);
    	childNodeQuickSort(childList, first, split);
    	childNodeQuickSort(childList, split+1, last);
    }
    
    public static int partition(GameStateChild[] childList, int first, int last) {
    	double pivot = childList[(first + last)/2].state.getUtility();
    	int left = first - 1;
    	int right = last + 1;
    	while (true) {
            do {
                left++;
            } while(childList[left].state.getUtility() < pivot);
            do {
                right--;
            } while(childList[right].state.getUtility() > pivot);
    	
    		if(left < right) {
    			GameStateChild child = childList[left];
    			childList[left] = childList[right];
    			childList[right] = child;
    		}
    	
    		else {
    			return right;
    		}
    	}
    }
    
}
