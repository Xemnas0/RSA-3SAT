package Solver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Problem {

	// map <IdVar, Var>
	private SortedMap<Integer, Variable> variables = new TreeMap<Integer, Variable>();
	// list of clauses (becomes smaller as the problem is being solved)
	private List<Clause> clauses = new ArrayList<Clause>();
	// structure for quickUnion and Disjunction management
	private QUForest qu;
	// initial number of variables
	private int nTotVars;
	// initial number of clauses
	private int nTotClauses;

	public void solve() {
		// TODO to complete
		return;
	}

	/**
	 * Initialize the problem with a file in {@code DIMACS} format.
	 * 
	 * @param path
	 *            The location of the file.
	 */
	public void readFile(String path) {

		long t1 = System.nanoTime();

		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {

			String line;
			Clause clause = null;

			while ((line = reader.readLine()) != null) {

				if (line.startsWith("c")) {
					// is a comment
					// System.out.println(line);
					continue;
				}

				String[] fields = line.split(" ");
				if (line.startsWith("p")) {
					nTotVars = Integer.decode(fields[2]);
					nTotClauses = Integer.decode(fields[3]);
					qu = new QUForest(nTotVars);

					for (int i = 0; i < nTotVars; ++i)
						variables.put(i, new Variable(i));

					continue;
				}

				List<Integer> vars = Arrays.asList(fields).stream().map(s -> Integer.decode(s))
						.collect(Collectors.toList());

				if (vars.get(vars.size() - 1) == 0)
					vars.remove(vars.size() - 1);

				if (clause == null) {
					clause = new Clause(vars);
					clause.insertRow(vars);
					continue;
				}

				if (clause.hasTheseVars(vars)) {
					clause.insertRow(vars);
				} else {
					clauses.add(clause);
					// System.out.println("adding:\n" + clause.print());
					// System.out.println("compare:\n"+clauses.get(clauses.size()-1).print());

					clause.getVars().stream().map(s -> s - 1).forEach(s -> {
						// System.out.println("Incremento:\n" + (s + 1));
						variables.get(s).addClause(clauses.get(clauses.size() - 1));
					});
					clause = new Clause(vars);
					clause.insertRow(vars);
				}

			}

			// this is for the last row
			clauses.add(clause);
			clause.getVars().stream().map(s -> s - 1).forEach(s -> {
				// System.out.println("Incremento:\n"+(s+1));
				variables.get(s).addClause(clauses.get(clauses.size() - 1));
			});

		} catch (IOException e) {
			System.err.println("Error during file reading: " + path);
		}

		long t2 = System.nanoTime();

		System.out.println(String.format("File read in: %.3f ms.", (double) (t2 - t1) / Math.pow(10, 6)));

	}

	public String printClauses() {
		return clauses.stream().map(s -> s.print()).collect(Collectors.joining("\n"));
	}

	public String printPresentsOfVariables() {
		return variables.values().stream()
				.map(s -> "Variable " + (s.getId() + 1) + " appears in " + s.presentInNClauses() + " clauses.")
				.collect(Collectors.joining("\n"));
	}

	public String result() {
		return null;
	}
	
	
	/**
	 * 
	 * @return The initial number of variables in the problem.
	 */
	private int getnTotVars() {
		return nTotVars;
	}
	
	/**
	 * 
	 * @return The initial number of clauses in the problem.
	 */
	private int getnTotClauses() {
		return nTotClauses;
	}
	
	/**
	 * 
	 * @return The current number of (unsolved) clauses.</br>Group of clauses
	 * with same variables but different sign are considered together.
	 */
	private int getCurrentCompressedClause() {
		return clauses.size();
	}
	
	/**
	 * 
	 * @return The current number of (unsolved) clauses.
	 */
	private int getCurrentClause() {
		return clauses.stream().collect(Collectors.summingInt(Clause::getNumInternalClauses));
	}
	
	public String getInfo() {
		return String.format("getCurrentClause(): %d\ngetCurrentCompressedClause(): %d\ngetnTotClauses(): %d\ngetnToVars(): %d\n",
				getCurrentClause(),
				getCurrentCompressedClause(),
				getnTotClauses(),
				getnTotVars());
	}
}
