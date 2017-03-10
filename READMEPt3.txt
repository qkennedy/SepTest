Jack:
	For our first commit, we implemented a rudimentary version of the alphabeta search algorithm. For my task, I created an algorithm that sorts the children nodes based on their utility values. I did this using a modified version of quicksort, which I may change in the future for optimization reasons. However, its outputted result is a list of children game states that is ordered from lowest utility to highest utility. Since we do not have our code fully implemented, I have yet to fully test the sorting algorithm out. However, it should be alright for the time being. In terms of the documentation, I had no issues with this assignment.

I also added some skeleton (commented-out) code for the getChildren() method of GameState. We have yet to implement any methods in this class, but our goal is to get these methods implemented for the next commit. Details on the getChildren method (i.e. how we are going to approach the method) are shown in comments under the method.

Quinn:  
	I basically started implementing the alphabeta method, it doesn’t totally work but I got a good start on it.  Came up with some possible heuristics, kinda outlined the next steps going forward.  Also I fixed/broke/fixed/broke our .gitignore, and the repository in general
COMMIT 2: The ReCommitening
	So after reading more about how alphabeta pruning works, I made some changes to my implementation, still not 100% that it will work but we shall see.  Additionally, I started the process of adding the methods we will use to get the utility values for each of the states.  Nothing Really else to say.

Jack: After looking at my original quick sort equation, I modified it slightly as it had an out of bounds condition error. I fixed that error. Besides that, I began implementing the getChildren method of GameState. I encountered some issues, specifically in terms of transferring data from the original state into the new state. In its current form, the method creates a state for every combination in which a two footmen / two archers can move. I have yet to implement the combinations where either the footmen / archers are attacking. That will come in a later date. The current form of the method is also incredibly repetitive and cumbersome, and I hope to fix it within the next week. This implementation is simply a means of showing my thought process as to how to create the new states.
Commit 3:
Quinn Kennedy
	On this commit, I worked on getting the configuration functional, I changed around the implementation of minimax to allow it to function on 0 plies.  I also changed a couple lines of it hopefully to avoid bugs.  I contributed to restructuring how we generate the children states, and I continued working on making the getutility to work.


Commit 3:
Jack La Rue
	With this commit, I attempted to add additional features to getChildren, including a means of passing primitive actions to the new state. Besides that, I modified the constructor, the getUtility, and the generateNewState methods. 
So far, I’m not sure if this is going to work. However, I’m confident that, to an extent, our ideas are down on paper.

***Edit: I only realize now that I didn’t implement a full means of mapping archers attacking others in range. Didn’t get to it on time.