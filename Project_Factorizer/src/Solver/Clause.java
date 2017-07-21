package Solver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Clause {

	// are the vars (column) in the clause. They start from id=0.
	private List<Integer> vars = new ArrayList<Integer>();

	private List<List<Boolean>> signs = new ArrayList<List<Boolean>>();

	/**
	 * Accepts normal variables that will be decreased of 1.
	 * 
	 * @param variables
	 *            in normal form (starting from 1).
	 */
	public Clause(List<Integer> variables) {
		vars = variables.stream().map(s -> Math.abs(s) - 1).collect(Collectors.toList());
	}

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
	 * @return true if the clause has only one column and one row, false
	 *         otherwise.
	 */
	public boolean isMonoClause() {
		return vars.size() == 1 && signs.size() == 1;
	}

	public boolean isPositiveMonoClause() {
		return signs.get(0).get(0);
	}

	public boolean isNegativeMonoClause() {
		return !signs.get(0).get(0);
	}

	public int getVarOfMonoClause() {
		return vars.get(0);
	}

	public List<List<Boolean>> getSigns() {
		return signs;
	}

}
