Jack:
	To start, I did some very minor bug fixes for P4. For a majority of the time, Quinn worked on that so I could have a solid outline for P5. Implemented the BuildPeasant, Depositk and MoveActionk methods of P5. Harvestk should be very similar in terms of implementation so, due to lack of time, I only left auto-generated methods. It seems as though we are going to need to modify our interface to exclude a couple of methods, as they no longer work in situations where there are multiple actions to test. Additionally, I am going to be modifying PEAgent for next week to test for parallelism, as per the project prompt.

Quinn:
	Okay, so, I got the code related to the last portion of the project working (mostly).  I think this was an important step in P5 considering it relies on P4 working.  Still having some quirky issues that I think could be resolved by implementing hash to quickly check for repeated states, and improving my heuristics to properly take into account distance traveled in a move.  Overall, I think this went decent.  Kind of confused documentation wise, were we supposed to keep track of everything past initial state on our own? that was eventually how I got it to work but I feel like there has to be a better way to actively change the state.  

==========
Week 2:

Jack:
	For this week, I implemented myPeasant and replaced all instances of peasPos / peasAmt, etc. in GameState with said class. I also reimplemented DepositAction, GatherGoldAction and GatherWoodAction to accommodate multiple peasants. Additionally, I created a BuildPeasant action operator. Finally, I created additional helper methods in GameState and implemented the code that generates all possible combinations of DepositActions in the generateChildren method of GameState.