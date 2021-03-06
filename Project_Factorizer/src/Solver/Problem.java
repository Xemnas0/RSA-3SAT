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

import com.sun.javafx.binding.SelectBinding.AsString;

import Solver.Clause.GroupType;

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
	private int posVarId=-1;
	// a variable that has value=false
	private int negVarId=-1;
	// number to factorize
	private BigInteger n;
	// first prime factor
	private BigInteger p;
	// second prime factor
	private BigInteger q;
	// position of p and q in variables
	private int startIndexP, endIndexP, startIndexQ, endIndexQ;
	
	
	public void solve() {
		
		sortClauses();
		
		// first phase
		System.out.println("STEP 1: MONOCLAUSE");
		solveMonoClause();
		cleanEmptyClause();
		sortClauses();
		System.out.println(this.getInfo());
		
		System.out.println("STEP 2: LSB ASSIGNMENT");
		// assigning 2 values
		firstBitIsOne();		
		cleanEmptyClause();
		sortClauses();
		System.out.println(this.getInfo());
		
		System.out.println("STEP 3: LENGTH DIFFERENCE");
		supposeDiffLength(0);
		System.out.println(this.getInfo());
		// second phase
		
		int currentProgress;
		int previousProgress= numberRelations();;
		
		for(int i=0; i < 1000000; ++i) {
			
			
			
			
			//System.out.println("STEP 4: TWO BY TWO");
			solveClausesTwoByTwo();
			cleanEmptyClause();
			sortClauses();
			//System.out.println(this.getInfo());
			
			//System.out.println("STEP 5: FOUR BY THREE");
			solveClausesFourByThree();
			cleanEmptyClause();
			sortClauses();
			//System.out.println(this.getInfo());
			
			//System.out.println("STEP 6: MONOCLAUSE");
			solveMonoClause();
			cleanEmptyClause();
			sortClauses();
			//System.out.println(this.getInfo());
			
			//System.out.println("STEP 7: KNOWN VARIABLES");
			assignKnownVars();
			cleanEmptyClause();
			sortClauses();
			System.out.println("\nCYCLE #"+i+"\n"+this.getInfo());
			
			currentProgress = numberRelations();
			
			//no progress
			if(currentProgress == previousProgress) break;
			//else
			previousProgress=currentProgress;
			
			
		}

		


			/*
			 * TODO create another solve monoclause
			solveMonoClause();
			cleanEmptyClause();
			sortClauses();
			*/
			//System.out.println("SITUAZIONE CLAUSES:\n"+this.printClauses());
			//System.out.println(this.relationsInfo());
		
		//System.out.println(this.getInfo());
		//System.out.println(this.relationsInfo());


		System.out.println("posVar: "+posVarId);
		System.out.println("negVar: "+negVarId);
		
		decodeResult();
		
		
		
		if(resultIsValid())
			System.out.println("Problem Solved!!!");
		else
			System.out.println("Problem Not Solved :(");
		
		return;
	}
	
	/**
	 * Assigns the variables that are equals to a known variable.</br>
	 * Example:</br>
	 * 1 = 4 </br>
	 * 4 = unknown</br>
	 * 1 = true.</br>
	 * Then (4 = true) => assign(4, true).
	 */
	private void assignKnownVars() {
		// TODO Auto-generated method stub
		int rootTrue = qu.find(posVarId);
		int rootFalse = qu.find(negVarId);
		
		variables.values().stream().filter(s-> !s.isAssigned()).forEach(s->{
			
			int id = s.getId();
			int root = qu.find(id);
			
			if(root == rootTrue) {
				assignVariable(id, true);
				//System.out.println("Variable with ID="+id+" assigned to true.");
			}
				
			
			if(root == rootFalse) {
				assignVariable(id, false);
				//System.out.println("Variable with ID="+id+" assigned to false.");
			}
				
		});
		
	}

	private void solveClausesFourByThree() {

		clauses.stream().filter(s->s.nColumns()==3).filter(s->s.nRows()==4).filter(Clause::hasRelation).forEach(s->{
			
			//System.out.println("Prima:\n"+s.print());
			
			int whatToDo = s.solveClauseFourByThree();
			
			//System.out.println("Eseguo op "+whatToDo+" per "+s.getGroupType());
			if(s.getGroupType() == GroupType.MostFalse)
				solveMostFalseClause(whatToDo, s);
			else
				solveMostTrueClause(whatToDo, s);
			//System.out.println("Dopo:\n"+s.print());
		});
		
	}

	private void solveMostTrueClause(int whatToDo, Clause clause) {
		// TODO Auto-generated method stub
		List<Integer> vars = clause.getVars();
		
		int v1=vars.get(0);
		int v2=vars.get(1);
		int v3=vars.get(2);
		
		switch (whatToDo) {
		case 0:
			assignVariable(v3, false);
			break;
		case 1:
			assignVariable(v3, true);
			break;
		case 2:
			assignVariable(v1, false);
			break;
		case 3:
			assignVariable(v1, true);
			break;
		case 4:
			assignVariable(v2, false);
			break;
		case 5:
			assignVariable(v2, true);
			break;
		default:
			System.err.println("Error in Problem.solveMostTrueClause()");
			break;
		}
	}

	private void solveMostFalseClause(int whatToDo, Clause clause) {
		
		List<Integer> vars = clause.getVars();
		
		int v1=vars.get(0);
		int v2=vars.get(1);
		int v3=vars.get(2);
		
		switch (whatToDo) {
		case 0:
			qu.union(v2, v3);
			//TODO check if it is right to eliminate the clause
			clause.emptyVarOfClause();
			//clauses.remove(clause);
			break;
		case 1:
			assignVariable(v3, false);
			break;
		case 2:
			//nothing
			break;
		case 3:
			assignVariable(v1, false);
			assignVariable(v2, true);
			assignVariable(v3, false);
			break;
		case 4:
			//nothing
			break;
		case 5:
			assignVariable(v1, true);
			assignVariable(v2, false);
			assignVariable(v3, true);
			break;
		default:
			System.err.println("Error in Problem.solveMostFalseClause()");
			break;
		}
		
		
	}

	private void solveClausesTwoByTwo() {
		
		List<Clause> toRemove = new ArrayList<Clause>();
		clauses.stream().filter(s->s.nColumns()==2).filter(s->s.nRows()==2).forEach(s->{
			int whatToDo = s.solveClauseTwoByTwo();
			doWhatMustbeDone(whatToDo, s);
			toRemove.add(s);
		});
		
		toRemove.stream().forEach(s->{
			s.emptyVarOfClause();
			clauses.remove(s);
		});
	}

	private void doWhatMustbeDone(int whatToDo, Clause clause) {
		
		List<Integer> vars = clause.getVars();
		int v1=vars.get(0);
		int v2=vars.get(1);
		
		switch (whatToDo) {
		case 0:
			qu.union(v1, v2);
			break;
		case 1:
			qu.disj(v1, v2);
			break;
		case 2:
			assignVariable(v1, false);
			break;
		case 3:
			assignVariable(v1, true);
			break;
		case 4:
			assignVariable(v2, false);
			break;
		case 5:
			assignVariable(v2, true);
			break;
		default:
			System.err.println("Error in Problem.doWhatMustBeDone()");
			break;
		}
	}

	private void firstBitIsOne() {
		assignVariable(startIndexP, true);
		assignVariable(startIndexQ, true);
		qu.union(startIndexP, posVarId);
		qu.union(startIndexQ, posVarId);
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
						this.n = new BigInteger(fields[5]);
					}
					
					continue;
				}

				
				if (line.startsWith("p")) {
					this.nTotVars = Integer.decode(fields[2]);
					this.nTotClauses = Integer.decode(fields[3]);
					this.qu = new QUForest(nTotVars);

					for (int i = 0; i < nTotVars; ++i)
						variables.put(i, new Variable(i));

					continue;
				}

				List<Integer> vars = Arrays.asList(fields).stream().map(s -> Integer.decode(s))
						.collect(Collectors.toList());

				if (vars.get(vars.size() - 1) == 0)
					vars.remove(vars.size() - 1);

				if (clause == null) {
					clause = new Clause(vars,this);
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
					clause = new Clause(vars,this);
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
		
		//TODO can be parallelized
		clauses.stream().forEach(s->{
			s.evaluateGroupType();
		});
		
		long t2 = System.nanoTime();
		
		initializeIndexResult();
		
		System.out.println(String.format("File read in: %.3f ms.", (double) (t2 - t1) / Math.pow(10, 6)));

	}

	private void initializeIndexResult() {
		int nBitProduct = n.bitLength();
		startIndexP = 0;
		endIndexP = nBitProduct-1-1;
		startIndexQ = endIndexP+1;
		endIndexQ = (int) (startIndexQ+ Math.ceil((double)nBitProduct/2))-1;
		
	}



	public String printClauses() {
		return clauses.stream().map(s -> s.print()).collect(Collectors.joining(""));
	}

	public String printPresentsOfVariables() {
		return variables.values().stream()
				.map(s -> "Variable " + (s.getId() + 1) + " appears in " + s.presentInNClauses() + " clauses.")
				.collect(Collectors.joining("\n"));
	}

	public String result() {
		return variables.values().stream().filter(s-> s.isAssigned()).map(s-> (s.getValue() ? "":"-")+(s.getId()+1) ).collect(Collectors.joining(" "));
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
		
		if(monoClauses.isEmpty()) return;
		
		//TODO clean code
		List<Clause> positiveMonoClause = monoClauses.stream().filter(Clause::isPositiveMonoClause)
				.collect(Collectors.toList());
		List<Clause> negativeMonoClause = monoClauses.stream().filter(Clause::isNegativeMonoClause)
				.collect(Collectors.toList());
		
		//if the default negative and positive variables are not assigned then they are defined
		if(posVarId == -1) {
			posVarId = positiveMonoClause.get(0).getVarOfMonoClause();
			positiveMonoClause.remove(0);
		}
			
		if(negVarId == -1) {
			negVarId = negativeMonoClause.get(0).getVarOfMonoClause();
			negativeMonoClause.remove(0);
		}
		
		
		if(positiveMonoClause.size() > 0)
		positiveMonoClause.stream().forEach(s -> {
			qu.union(posVarId, s.getVarOfMonoClause());
		});
		
		if(negativeMonoClause.size() > 0)
		negativeMonoClause.stream().forEach(s -> {
			qu.union(negVarId, s.getVarOfMonoClause());
		});

		qu.disj(posVarId, negVarId);



	}

	private void assignVariable(int idVar, boolean value) {
		//System.out.println("Assegno a " + normalizedVar(idVar) + " il valore : " + value);
		//TODO modify, implement union with rootTrue e rootFalse
		
		int rootTrue = qu.find(posVarId);
		int rootFalse = qu.find(negVarId);
		
		variables.get(idVar).assignValue(value);
		
		if(value == true) {
			qu.union(idVar, rootTrue);
		}
		else
			qu.union(idVar, rootFalse);
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
	
	private void decodeResult() {
		

		
		System.out.println(String.format(
				"nBitProduct: %d\n"
				+ "startIndexP: %d\n"
				+ "endIndexP: %d\n"
				+ "startIndexQ: %d\n"
				+ "endIndexQ: %d\n",
				n.bitLength(),
				startIndexP,
				endIndexP,
				startIndexQ,
				endIndexQ));
		
		StringBuilder p = new StringBuilder();
		StringBuilder q = new StringBuilder();
		
		variables.subMap(startIndexP, endIndexP+1).values().stream().map(Variable::getValue).forEach(s->{
			p.append((s ? "1" : "0"));
		});
		
		variables.subMap(startIndexQ, endIndexQ+1).values().stream().map(Variable::getValue).forEach(s->{
			q.append((s ? "1" : "0"));
		});
		
		this.p = new BigInteger(p.reverse().toString(),2);
		this.q = new BigInteger(q.reverse().toString(),2);
		
		System.out.println("pStr="+p+"\nqStr="+q);
		System.out.println("p="+this.p+"\nq="+this.q);
	}
	
	private boolean resultIsValid() {
		return p.multiply(q).equals(n);
	}
	
	public SortedMap<Integer, Variable> getVarsMap() {
		return variables;
	}

	public QUForest getQu() {
		return qu;
	}
	
	private void supposeDiffLength(int diff) {
		
		int realLenP, realLenQ;
		
		int lengthN = n.bitLength();
		
		/*
	     * The following equations come from:
	     * LenP+LenQ = LenN
	     * LenP-LenQ = difference
	     */
		//TODO to check correctness
	    realLenP = (int)Math.ceil((double)(lengthN + diff) / 2);
	    realLenQ = (int)Math.ceil((double)(lengthN - diff) / 2);
		
	    /*
	     *   
	     *   int startIndexP = realLenP, startIndexQ = structLenP + realLenQ;
	     *   int endIndexP = structLenP - 1, endIndexQ = structLenP + structLenQ - 1;
	     */
	    int structLenP = endIndexP+1;
	    int structLenQ = endIndexQ-startIndexQ+1;
	    
		int startZeroP = realLenP, startZeroQ = structLenP+realLenQ;
		int endZeroP = structLenP-1, endZeroQ = structLenP+structLenQ-1;
		
		System.out.println("Setting Zero:\nP => ["+startZeroP+".."+endZeroP+"]");
		System.out.println("Q => ["+startZeroQ+".."+endZeroQ+"]");


		for(int i = startZeroP; i <= endZeroP; ++i)
			assignVariable(i, false);
		for(int i = startZeroQ; i <= endZeroQ; ++i)
			assignVariable(i, false);
		
		assignVariable(startZeroP-1, true);
		assignVariable(startZeroQ-1, true);
	}
}
