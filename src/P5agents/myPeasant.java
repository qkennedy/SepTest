package P5agents;

import edu.cwru.sepia.environment.model.state.ResourceType;

public class myPeasant {
    private int id;
    private int cAmt;
    private ResourceType rType;
    private PositionType posType;
    private Position pos;
    public myPeasant(int id){
        this.id = id;
        posType = PositionType.TH;
        rType = null;
        cAmt = 0;
    }
    public myPeasant(myPeasant c){
    	this.id = c.id;
    	this.cAmt = c.cAmt;
    	this.rType = c.rType;
    	this.posType = c.posType;
    	this.pos = c.pos;
    }
    
    public void setRType(ResourceType rType) {
        this.rType = rType;
    }
    
    public void setCAmt(int cAmt) {
        this.cAmt = cAmt;
    }
    
    public void setPosT(PositionType posType) {
        this.posType = posType;
    }
    
    public int getCAmt() {
        return cAmt;
    }
    
    public ResourceType getRType() {
        return rType;
    }
    
    public int getID() {
        return id;
    }
    
    public PositionType getPosT() {
        return posType;
    }
    public Position getPos(){
    	return pos;
    }
    public void setPos(Position pos){
    	this.pos = pos;
    }
}
