Quinn- For my section I focused on implementing A* search.  Towards that goal, I altered MapLocation to actually take and keep cost and a parent Maplocation.  I also implemented Comparable in Maplocation to allow for the use of a priority Queue.  Then I began the A*search method.  I used a Priority Queue to store the open list, and a 2D boolean array “closed” to keep track of what tiles were closed.  First, I change all the resource locations to be closed, along with the enemy agent location.  Then, I begin exploring the graph, by adding all nonclosed, within-bounds nodes, to the openlist, simultaneously calculating the heuristic estimation, giving the cost, and marking the node as closed.  I do the closing to avoid nodes that have already been discovered being added more than once to the open list.  I then add 1 to the cost variable, close out the node I’m on, and restart the loop  I continue iterating like this until I find a node that is one away from the goal.  Then I follow the parents until I get back to the start.  And return the path.


No problem with documentation, it was A Okay.


Jack- This week, I implemented the shouldReplanPath() method. If there is an enemy footman in the map, then this method begins to take effect. Essentially, this method checks for two possible positions of said footman. If the enemy footman is blocking the next location of the path (i.e. the footman is right next to our footman unit), then the method returns true. Additionally, the method also returns true if the enemy footman is on the path and is less than or equal to 3 blocks away (i.e. he is three blocks or less down the path). The method accomplishes this through the help of the findEnemyOnPath() method, which searches through the stack for a position that the enemy may be inhabiting. Hopefully, this will optimize the method by making our unit change paths early. 


Beyond that, I also helped squash a bug where our unit did not recognize the enemy footman as a blocking object. Additionally, I did some code cleaning / commenting of my sections. For the written assignments, I did problems 11 - 13.


I had no issues with the documentations this week. All was well!



README FOR COMMIT 2:

Jack- Optimized the shouldReplanPath() method by remove unnecessary conditions. I figured that, since findEnemyOnPath() already searches for an enemy in the path, there is no need for a condition that checks for an enemy foot soldier at the next block.

Additionally, I spent extra time optimizing my findEnemyOnPath() method. Originally, the method would look through the entire path for an enemy. If there was one on the path, a condition in shouldReplanPath() would then determine whether or not it was close enough to warrant replacing the path. I changed the while loop in the findEnemyOnPath() method to have a search depth of 3. Therefore, the friendly footman’s “vision” has a depth of three down the path. If there is an enemy footman within 3 steps down the path, then the method calls for a replacing of the path. I also changed this method to a boolean as the MapLocation type was no longer needed.

Beyond that, I cleaned up the code by removing unnecessary System.out logs.

Quinn- I did some code cleaning in terms of System.out logs and comments. Beyond that, I modified the condition statement in the while loop of the A* method so that our footman could reach the goal location using different coordinates (originally, we just had him tuck up into the top corner next to the goal. Although he still does this, it is solely because of how A* is implemented in this format).


