package Solver;

public class Variable {
	
	int id;
	private boolean assigned;
	private boolean value;
	
	public Variable(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	public boolean isAssigned() {
		return assigned;
	}
	public boolean isValue() {
		return value;
	}
	
}
