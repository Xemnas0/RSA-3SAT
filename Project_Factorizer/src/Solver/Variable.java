package Solver;

import java.util.ArrayList;
import java.util.List;

public class Variable {
	
	//indicates the ID of the Variable: the value of "id" is decreased by 1 from the real value, because arrays start from 0.
	private int id;
	private boolean assigned;
	private boolean value;
	// List of all the clauses where the Variable is contained
	private List<Clause> clauses = new ArrayList<Clause>();
	
	
	
	public void addClause(Clause clause) {
		clauses.add(clause);
	}
	
	public Variable(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	public boolean isAssigned() {
		return assigned;
	}
	public boolean getValue() {
		return value;
	}
	
	public int presentInNClauses() {
		return clauses.size();
	}

	public void assignValue(boolean value) {

		assigned=true;
		this.value=value;
		
		clauses.stream().forEach(s->{
			s.assignVariable(id, value);
		});;
		
		//the variable now does not appear in any clause
		clauses.clear();
	}
	
}
