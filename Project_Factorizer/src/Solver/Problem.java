package Solver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
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
	// a variable that has value=true
	private int posVarId;
	// a variable that has value=false
	private int negVarId;
	// number to factorize
	private BigInteger product;
	
	
	public void solve() {

		solveMonoClause();
		cleanEmptyClause();
		
		//System.out.println(this.printClauses());
		
		sortClauses();
		
		//System.out.println(this.printClauses());
		// TODO to complete
		return;
	}

	private void sortClauses() {
		Collections.sort(clauses, Comparator.comparing(Clause::totSize).thenComparing(Clause::getVarOfMonoClause));		
	}

	private void cleanEmptyClause() {
		
		List<Clause> toRemove = new LinkedList<Clause>();
		clauses.stream().filter(Clause::isSolved).forEach(s->{
			toRemove.add(s);
		});
		
		toRemove.stream().forEach(s->{
			clauses.remove(s);
		});
		
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
			int numberLine=0;

			while ((line = reader.readLine()) != null) {
				
				++numberLine;
				
				String[] fields = line.split(" ");
				
				if (line.startsWith("c")) {
					// is a comment
					System.out.println(line);
					
					if(numberLine == 4) {
						
					}
					
					continue;
				}

				
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

					clause.getVars().stream().forEach(s -> {
						// System.out.println("Incremento:\n" + (s + 1));
						variables.get(s).addClause(clauses.get(clauses.size() - 1));
					});
					clause = new Clause(vars);
					clause.insertRow(vars);
				}

			}

			// this is for the last row
			clauses.add(clause);
			clause.getVars().stream().forEach(s -> {
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
	 * @return The current number of (unsolved) clauses.</br>
	 *         Group of clauses with same variables but different sign are
	 *         considered together.
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
		return String.format(
				"==========START INFO==========\n"
				+ "Total Clauses: %d\n"
				+ "Remaining Clauses: %d (%.2f%%)\n"
				+ "Eliminated Clauses: %d (%.2f%%)\n"
				+ "Total Compressed Clauses: %d\n"
				+ "Remaining Compressed Clauses: %d (%.2f%%)\n"
				+ "Eliminated Compressed Clauses: %d (%.2f%%)\n"
				+ "Total Variables: %d\n"
				+ "Remaining Variables: %d (%.2f%%)\n"
				+ "Assigned Variables: %d (%.2f%%)\n"
				+ "Number of relations: %d (%.2f%%)\n"
				+ "===========END INFO===========",
				getnTotClauses(), 
				getCurrentClause(), (double) getCurrentClause()*100/getnTotClauses(),
				getnTotClauses()-getCurrentClause(), (double) (getnTotClauses()-getCurrentClause())*100/getnTotClauses(),
				getnTotVars(),
				getCurrentCompressedClause(), (double) getCurrentCompressedClause()*100/getnTotVars(),
				getnTotVars()-getCurrentCompressedClause(), (double) (getnTotVars()-getCurrentCompressedClause())*100/getnTotVars(),
				getnTotVars(),
				getnTotVars()-getNAssignedVariables(), (double) (getnTotVars()-getNAssignedVariables())*100/getnTotVars(),
				getNAssignedVariables(), (double) getNAssignedVariables()*100/getnTotVars(),
				numberRelations(), (double) numberRelations()*100/(getnTotVars()-1)
				);
	}
	
	public String relationsInfo() {
		return qu.currentState();
	}
	
	private int numberRelations() {
		return getnTotVars()-qu.count();
	}
	
	public String relationsInfoNormalized() {
		return qu.currentStateNormalized();
	}
	
	/**
	 * Solve all the clauses that contains only one variable. Also performs the
	 * operations regarding relations (quickunion and disjunction)
	 */
	private void solveMonoClause() {
		List<Clause> monoClauses = clauses.stream().filter(Clause::isMonoClause).collect(Collectors.toList());

		List<Clause> positiveMonoClause = monoClauses.stream().filter(Clause::isPositiveMonoClause)
				.collect(Collectors.toList());
		List<Clause> negativeMonoClause = monoClauses.stream().filter(Clause::isNegativeMonoClause)
				.collect(Collectors.toList());

		posVarId = positiveMonoClause.get(0).getVarOfMonoClause();
		negVarId = negativeMonoClause.get(0).getVarOfMonoClause();

		positiveMonoClause.remove(0);
		negativeMonoClause.remove(0);

		positiveMonoClause.stream().forEach(s -> {
			qu.union(posVarId, s.getVarOfMonoClause());
		});

		negativeMonoClause.stream().forEach(s -> {
			qu.union(negVarId, s.getVarOfMonoClause());
		});

		qu.disj(posVarId, negVarId);

		monoClauses.stream().forEach(s -> {
			assignVariable(s.getVarOfMonoClause(), s.isPositiveMonoClause());
		});

	}

	private void assignVariable(int idVar, boolean value) {
		//System.out.println("Assegno a " + normalizedVar(idVar) + " il valore : " + value);
		variables.get(idVar).assignValue(value);
		return;
	}
	
	private int normalizedVar(int idVar) {
		return (idVar+1);
	}
	
	/**
	 * 
	 * @return Number of variables for which the final value is known.
	 */
	private long getNAssignedVariables() {
		return variables.values().stream().filter(Variable::isAssigned).count();
	}
}
