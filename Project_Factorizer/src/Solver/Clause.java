package Solver;

import java.util.ArrayList;
import java.util.List;

public class Clause {

	private List<Variable> vars = new ArrayList<Variable>();
	private Type tipo;
	
	
	
	public static enum Type {
		Charlie, Jeremy
	};

}
