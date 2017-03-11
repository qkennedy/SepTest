package P4agents;

public class DepositAction implements StripsAction {
	public int uID;
	/**
	 * This class takes a unit that is affected, and a unit doing the action
	 * This sets the framework for how the STRIPS Actions should be implemented
	 * @param uID
	 * @param tcID
	 */
	public DepositAction(int uID, int tcID){
		this.uID = uID;
	}
	@Override
	public int getUID() {
		// TODO Auto-generated method stub
		return uID;
	}

	@Override
	public boolean preconditionsMet(GameState state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GameState apply(GameState state) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getUnitAffected() {
		// TODO Auto-generated method stub
		return 0;
	}

}
