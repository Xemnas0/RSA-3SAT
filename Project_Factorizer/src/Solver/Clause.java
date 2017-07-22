package Solver;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class Clause {

	private Problem problem;
	// are the vars (column) in the clause. They start from id=0.
	private List<Integer> vars = new ArrayList<Integer>();

	private List<List<Boolean>> signs = new ArrayList<List<Boolean>>();

	private GroupType groupType;

	/**
	 * Accepts normal variables that will be decreased of 1.
	 * 
	 * @param variables
	 *            in normal form (starting from 1).
	 */
	public Clause(List<Integer> variables, Problem problem) {
		vars = variables.stream().map(s -> Math.abs(s) - 1).collect(Collectors.toList());
		this.problem = problem;
	}

	public static enum GroupType {
		MostTrue, MostFalse
	};

	public void insertRow(List<Integer> values) {

		if (values.size() > 3)
			return; // error

		List<Boolean> tmpSigns = new ArrayList<Boolean>();

		values.stream().forEach(s -> {
			tmpSigns.add(tmpSigns.size(), new Boolean((s.intValue() > 0 ? true : false)));
		});
		signs.add(signs.size(), tmpSigns);
	}

	public String print() {
		StringBuilder ret = new StringBuilder();

		for (int i = 0; i < signs.size(); ++i) {
			for (int j = 0; j < vars.size(); ++j) {

				List<Boolean> tmpBV = signs.get(i);
				ret.append(String.format("%s%d ", (tmpBV.get(j) ? "" : "-"), vars.get(j).intValue()));

			}
			ret.append("\n");
		}

		return ret.toString();
	}

	public boolean hasTheseVars(List<Integer> vars) {
		// System.out.println("Inside hasTheVars():\nvars: "+vars+"\nclauseVars:
		// "+this.vars);
		return vars.stream().filter(s -> !this.vars.contains(Math.abs(s) - 1)).count() == 0;
	}

	public int getNumInternalClauses() {
		return signs.size();
	}

	public List<Integer> getVars() {
		return vars;
	}

	/**
	 * 
	 * @return True if the clause has only one column and one row, false
	 *         otherwise.
	 */
	public boolean isMonoClause() {
		return vars.size() == 1 && signs.size() == 1;
	}

	/**
	 * 
	 * @return True if the only variable inside is positive, false otherwise.
	 */
	public boolean isPositiveMonoClause() {
		return signs.get(0).get(0);
	}

	/**
	 * 
	 * @return True if the only variable inside is negative, false otherwise.
	 */
	public boolean isNegativeMonoClause() {
		return !signs.get(0).get(0);
	}

	/**
	 * 
	 * @return The id of the only variable inside the clause.
	 */
	public int getVarOfMonoClause() {
		return vars.get(0);
	}

	public List<List<Boolean>> getSigns() {
		return signs;
	}

	public void assignVariable(int id, boolean value) {

		int column = vars.indexOf(id);

		// System.out.println("PRIMA:\n"+this.print());
		for (int i = 0; i < signs.size(); ++i) {

			if (signs.get(i).get(column) == value) {
				signs.remove(i);
				--i; // to check;
			} else {
				signs.get(i).remove(column);
			}

		}

		vars.remove(column);

		// System.out.println("DOPO:\n"+this.print());
	}

	public boolean isSolved() {
		return signs.size() == 0;
	}

	public int totSize() {
		return signs.size() * vars.size();
	}

	public int nColumn() {
		return vars.size();
	}

	/**
	 * This method is called only for grouped Clause of dimension 2x2. It says
	 * what to do!
	 * 
	 * @return 0 for union. </br>
	 * 		1 for disjunction. </br>
	 * 		2 for first variable equal to false. </br>
	 * 		3 for first variable equal to true. </br>
	 * 		4 for second variable equal to false. </br>
	 * 		5 for second variable equal to true. </br>
	 * 		-1 for... ERROR ò.ò.
	 */
	public int solveClauseTwoByTwo() {

		List<Boolean> row0 = signs.get(0);
		List<Boolean> row1 = signs.get(1);

		/*
		 * case 0: union x -y -x y or -x y x -y
		 */
		if ((row0.get(0) && !row0.get(1) && !row1.get(0) && row1.get(1))
				|| (!row0.get(0) && row0.get(1) && row1.get(0) && !row1.get(1)))
			return 0;

		/*
		 * case 1: disjunction x y -x -y or -x -y x y
		 */
		if ((row0.get(0) && row0.get(1) && !row1.get(0) && !row1.get(1))
				|| (!row0.get(0) && !row0.get(1) && row1.get(0) && row1.get(1)))
			return 1;

		/*
		 * case 2: first variable equal to false -x y -x -y
		 */
		if (!row0.get(0) && !row1.get(0))
			return 2;

		/*
		 * case 3: first variable equal to true x y x -y
		 */
		if (row0.get(0) && row1.get(0))
			return 3;

		/*
		 * case 4: second variable equal to false x -y -x -y
		 */
		if (!row0.get(1) && !row1.get(1))
			return 4;

		/*
		 * case 5: second variable equal to true x y -x y
		 */
		if (row0.get(1) && row1.get(1))
			return 5;

		return -1;
	}

	public void emptyVarOfClause() {

		SortedMap<Integer, Variable> tmpMap = problem.getVarsMap();

		vars.stream().forEach(s -> {
			tmpMap.get(s).removeClause(this);
		});
	}

	/**
	 * 
	 * @return True if two of the variables in the clause have a relation, false
	 *         otherwise.
	 */
	public boolean hasRelation() {

		QUForest quForest = problem.getQu();

		return (quForest.connected(vars.get(0), vars.get(1)) || quForest.connected(vars.get(0), vars.get(2))
				|| quForest.connected(vars.get(1), vars.get(2)) || quForest.areEnemy(vars.get(0), vars.get(1))
				|| quForest.areEnemy(vars.get(0), vars.get(2)) || quForest.areEnemy(vars.get(1), vars.get(2)));
	}

	/**
	 * Establish if the clause is of type: GroupType.MostFalse + + - + - - - + -
	 * - - + or GroupType.MostTrue + + - + - + - + + - - -
	 * 
	 * @return An enum GroupType.
	 */
	public void evaluateGroupType() {

		long nTrue = signs.stream().flatMap(s -> s.stream()).filter(s -> s.booleanValue()).count();
		System.out.println("NumTrue: " + nTrue);
		if (nTrue == 5)
			groupType = GroupType.MostFalse;
		else
			groupType = GroupType.MostTrue;
	}

	public GroupType getGroupType() {
		return groupType;
	}

	/**
	 * This method is called only for grouped Clause of dimension 4x2. It says
	 * what to do depending on the situation.</br> CASE 1) </br>
	 * ....1 2 3</br>
	 * A + + -</br>
	 * B + - -</br>
	 * C - + -</br>
	 * D - - +</br>
	 * 
	 * 1=2 -> B,C ok, 1=2=3</br>
	 * 1!=2 -> A,D ok, 3=false</br>
	 * 2=3 -> A,C,D ok, B resta</br>
	 * 2!=3 -> B ok, 1=false, 2=true, 3=false</br>
	 * 1=3 -> A,B,D ok, C resta</br>
	 * 1!=3 -> C ok, 1=true, 2=false, 3=false</br>
	 * </br>
	 * </br>
	 * CASE 2) </br>
	 * ....1 2 3</br>
	 * A + + -</br>
	 * B + - +</br>
	 * C - + +</br>
	 * D - - -</br>
	 * 
	 * 1=2 -> B,C ok, 3=false</br>
	 * 1!=2 -> A,D ok, 3=true</br>
	 * 2=3 -> A,B ok, 1=false</br>
	 * 2!=3 -> C,D ok, 1=true</br>
	 * 1=3 -> A,C ok, 2=false</br>
	 * 1!=3 -> B,D ok, 2=true</br>
	 * 
	 * @return An int from 0 to 11, 0..5 for case 1), 6..11 for case 2).
	 */
	public int solveClauseFourByTwo() {

		if (groupType == GroupType.MostFalse)
			return solveMostFalse();
		else
			return solveMostTrue();
	}

	private int solveMostTrue() {
		// TODO Auto-generated method stub
		QUForest quForest = problem.getQu();

		if (quForest.connected(vars.get(0), vars.get(1))) {
			return 0;
		}
		if (quForest.areEnemy(vars.get(0), vars.get(1))) {
			return 1;
		}
		if (quForest.connected(vars.get(1), vars.get(2))) {
			return 2;
		}
		if (quForest.areEnemy(vars.get(1), vars.get(2))) {
			return 3;
		}
		if (quForest.connected(vars.get(0), vars.get(2))) {
			return 4;
		}
		if (quForest.areEnemy(vars.get(0), vars.get(2))) {
			return 5;
		}

		return -1;
	}

	private int solveMostFalse() {
		// TODO Auto-generated method stub
		QUForest quForest = problem.getQu();

		if (quForest.connected(vars.get(0), vars.get(1))) {
			signs.remove(1);
			signs.remove(2);
			return 0;
		}
		if (quForest.areEnemy(vars.get(0), vars.get(1))) {
			return 1;
		}
		if (quForest.connected(vars.get(1), vars.get(2))) {
			signs.remove(0);
			signs.remove(2);
			signs.remove(3);
			return 2;
		}
		if (quForest.areEnemy(vars.get(1), vars.get(2))) {
			return 3;
		}
		if (quForest.connected(vars.get(0), vars.get(2))) {
			signs.remove(0);
			signs.remove(1);
			signs.remove(3);
			return 4;
		}
		if (quForest.areEnemy(vars.get(0), vars.get(2))) {
			return 5;
		}

		return -1;
	}
}
