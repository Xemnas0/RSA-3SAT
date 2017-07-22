package Solver;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The {@code WeightedQuickUnionPathCompressionUF} class represents a union–find
 * data structure. It supports the <em>union</em> and <em>find</em> operations,
 * along with methods for determining whether two sites are in the same
 * component and the total number of components.
 * <p>
 * This implementation uses weighted quick union (by size) with full path
 * compression. Initializing a data structure with <em>n</em> sites takes linear
 * time. Afterwards, <em>union</em>, <em>find</em>, and <em>connected</em> take
 * logarithmic time (in the worst case) and <em>count</em> takes constant time.
 * Moreover, the amortized time per <em>union</em>, <em>find</em>, and
 * <em>connected</em> operation has inverse Ackermann complexity.
 * <p>
 * For additional documentation, see
 * <a href="http://algs4.cs.princeton.edu/15uf">Section 1.5</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 * 
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public class QUForest {
	private int[] parent; // parent[i] = parent of i
	private int[] enemy; // enemy[i] = enemy of i
	private int[] size; // size[i] = number of sites in tree rooted at i
						// Note: not necessarily correct if i is not a root node
	private int count; // number of components
	private int n; // total number of elements

	/**
	 * Initializes an empty union–find data structure with {@code n} sites
	 * {@code 0} through {@code n-1}. Each site is initially in its own
	 * component.
	 *
	 * @param n
	 *            the number of sites
	 * @throws IllegalArgumentException
	 *             if {@code n < 0}
	 */
	public QUForest(int n) {
		count = n;
		this.n = n;
		parent = new int[n];
		size = new int[n];
		enemy = new int[n];
		for (int i = 0; i < n; i++) {
			parent[i] = i;
			size[i] = 1;
			enemy[i] = -1;
		}
	}

	/**
	 * Returns the number of components.
	 *
	 * @return the number of components (between {@code 1} and {@code n})
	 */
	public int count() {
		return count;
	}

	/**
	 * Returns the component identifier for the component containing site
	 * {@code p}.
	 *
	 * @param p
	 *            the integer representing one site
	 * @return the component identifier for the component containing site
	 *         {@code p}
	 * @throws IllegalArgumentException
	 *             unless {@code 0 <= p < n}
	 */
	public int find(int p) {
		validate(p);
		int root = p;
		while (root != parent[root])
			root = parent[root];
		while (p != root) {
			int newp = parent[p];
			parent[p] = root;
			p = newp;
		}
		return root;
	}

	/**
	 * Returns true if the the two sites are in the same component.
	 *
	 * @param p
	 *            the integer representing one site
	 * @param q
	 *            the integer representing the other site
	 * @return {@code true} if the two sites {@code p} and {@code q} are in the
	 *         same component; {@code false} otherwise
	 * @throws IllegalArgumentException
	 *             unless both {@code 0 <= p < n} and {@code 0 <= q < n}
	 */
	public boolean connected(int p, int q) {
		return find(p) == find(q);
	}

	// validate that p is a valid index
	private void validate(int p) {
		int n = parent.length;
		if (p < 0 || p >= n) {
			throw new IllegalArgumentException("index " + p + " is not between 0 and " + (n - 1));
		}
	}

	/**
	 * Merges the component containing site {@code p} with the the component
	 * containing site {@code q}.
	 *
	 * @param p
	 *            the integer representing one site
	 * @param q
	 *            the integer representing the other site
	 * @throws IllegalArgumentException
	 *             unless both {@code 0 <= p < n} and {@code 0 <= q < n}
	 */
	/*
	 * ------------------------------------- OLD VERSION
	 * -------------------------------------
	 */
	/*
	 * public void union(int p, int q) { int rootP = find(p); int rootQ =
	 * find(q); if (rootP == rootQ) return;
	 * 
	 * // make smaller root point to larger one if (size[rootP] < size[rootQ]) {
	 * parent[rootP] = rootQ; size[rootQ] += size[rootP]; } else { parent[rootQ]
	 * = rootP; size[rootP] += size[rootQ]; } count--; }
	 */

	public void union(int p, int q) {

		// find father in union forest
		int rootP = find(p);
		int rootQ = find(q);

		// find the enemy in disjunction forest
		int enemyP = enemy[rootP];
		int enemyQ = enemy[rootQ];

		if (rootP == rootQ)
			return; // already in same union tree
		if (enemyP == rootQ || enemyQ == rootP)
			return; // ERROR: union not possible between enemies.
		--count;
		/*
		 * case 1: none of them has enemy
		 */
		if (enemyP == -1 && enemyQ == -1) {
			unionStandard(rootP, rootQ);
			return;
		}

		/*
		 * case 2: one has an enemy
		 */
		// P has enemy
		if (enemyP != -1 && enemyQ == -1) {
			unionOneEnemy(rootP, rootQ);
			return;
		}
		// Q has enemy
		if (enemyQ != -1 && enemyP == -1) {
			unionOneEnemy(rootQ, rootP);
			return;
		}
		/*
		 * case 3: both have enemies
		 */
		// case 3.a) different enemies
		if (enemyQ != -1 && enemyP != -1 && enemyP != enemyQ) {
			unionTwoEnemies(rootP, rootQ);
			return;
		}
		if (enemyQ != -1 && enemyP != -1 && enemyP == enemyQ) { // should never
																// happen if
																// disjunction
																// is well
																// implemented
			unionTwoEnemiesEquals(rootP, rootQ);
		}
	}

	private void unionStandard(int rootP, int rootQ) {
		// make smaller root point to larger one
		if (size[rootP] < size[rootQ]) {
			parent[rootP] = rootQ;
			size[rootQ] += size[rootP];
		} else {
			parent[rootQ] = rootP;
			size[rootP] += size[rootQ];
		}
	}

	// rootP must be the one with the enemy
	private void unionOneEnemy(int rootP, int rootQ) {

		if (size[rootP] < size[rootQ]) {
			parent[rootP] = rootQ;
			size[rootQ] += size[rootP];

			enemy[enemy[rootP]] = rootQ; // the new enemy of the (old) enemy of
											// P in now Q, the biggest
			enemy[rootQ] = enemy[rootP]; // Q inherits the enemy of P
			enemy[rootP] = -1; // enemy of P is resetted to -1
		} else {
			parent[rootQ] = rootP;
			size[rootP] += size[rootQ];
			if (enemy[rootQ] != -1)
				enemy[enemy[rootQ]] = rootP;
			enemy[rootQ] = -1;
		}

	}

	private void unionTwoEnemies(int rootP, int rootQ) {

		int x = enemy[rootP];
		int y = enemy[rootQ];
		// reset enemy for secondary members, MAYBE UNNECESSARY: will be
		// overwritted during unionOneEnemy
		enemy[y] = enemy[rootQ] = -1;
		// assign enemy for union, MAYBE UNNECESSARY: is already assigned
		enemy[x] = rootP;
		enemy[rootP] = x;

		unionOneEnemy(x, y);
		unionOneEnemy(rootP, rootQ);

	}

	private void unionTwoEnemiesEquals(int rootP, int rootQ) {

		unionOneEnemy(rootP, rootQ);
		if (enemy[rootP] != -1) {
			enemy[enemy[rootP]] = rootP;
		} else
			enemy[enemy[rootQ]] = rootQ;
	}

	public void disj(int p, int q) {

		int rootP = find(p);
		int rootQ = find(q);

		int enemyP = enemy[rootP];
		int enemyQ = enemy[rootQ];

		if (enemyP == rootQ && enemyQ == rootP)
			return; // already enemy
		if (rootP == rootQ)
			return; // ERROR: 2 equals cannot be different
		--count;
		/*
		 * case 1: none of them has enemy
		 */

		if (enemyP == -1 && enemyQ == -1) {
			enemy[rootP] = rootQ;
			enemy[rootQ] = rootP;
			return;
		}

		/*
		 * case 2: one has enemy
		 */
		// P has enemy
		if (enemyP != -1 && enemyQ == -1) {
			unionOneEnemy(enemyP, rootQ);
			return;
		}
		// Q has enemy
		if (enemyQ != -1 && enemyP == -1) {
			unionOneEnemy(enemyQ, rootP);
			return;
		}

		/*
		 * case 3: both have enemies
		 */
		if (enemyP != -1 && enemyQ != -1) {
			unionTwoEnemies(rootP, enemyQ);
		}
	}

	public boolean areEnemy(int p, int q) {
		int rootP = find(p);
		int rootQ = find(q);
		return (enemy[rootP] == rootQ && rootP == enemy[rootQ]);
	}

	/**
	 * Reads in a sequence of pairs of integers (between 0 and n-1) from
	 * standard input, where each integer represents some site; if the sites are
	 * in different components, merge the two components and print the pair to
	 * standard output.
	 *
	 * @param args
	 *            the command-line arguments
	 */
	/*
	 * public static void main(String[] args) { int n = StdIn.readInt();
	 * QUForest uf = new QUForest(n); while (!StdIn.isEmpty()) { int p =
	 * StdIn.readInt(); int q = StdIn.readInt(); if (uf.connected(p, q))
	 * continue; uf.union(p, q); StdOut.println(p + " " + q); }
	 * StdOut.println(uf.count() + " components"); }
	 */
	
	
	/**
	 * Gives information about the current relations in the system.
	 * @return A string containing all the relations. Equalities have =, inequalities have !=. Everything is grouped together.
	 */
	public String currentState() {
		StringBuilder[] v = new StringBuilder[n];
		
		
		// management of equalities
		for (int i = 0; i < n; ++i) {
			int rootI = find(i);
			if (i == rootI) { // he is parent of himself
				if (v[i] == null) { // alone at the moment
					v[i] = new StringBuilder(i);
				} else {// already found a son
						// do nothing
				}
			} else { // he is son of someone else
				
				if (v[rootI] == null) { // the father has not been
											// initialized
					v[rootI] = new StringBuilder(rootI + " = " + i);
				} else {
					if (v[rootI].toString().isEmpty())
						v[rootI].append(rootI);
					v[rootI].append(" = " + i);
				}
			}
		}
		
		//System.out.println("prints before:\n" + Arrays.asList(v));
		
		for(int i=0; i < n; ++i) {
			if(v[i] != null) {
				if(!v[i].toString().isEmpty()) {
					v[i] = new StringBuilder("("+v[i].toString()+")");
				}
			}
		}
		//System.out.println("prints after:\n" + Arrays.asList(v));
		// management of inequalities
		for (int i = 0; i < n; ++i) {

			if (enemy[i] == -1)
				continue;
			if (v[i] == null)
				continue; // should never happen

			if (v[enemy[i]].toString().isEmpty()) { // if the parent enemy has
													// not sons
				if (v[i].toString().isEmpty())
					v[i].append(i + " != " + enemy[i]);
				else
					v[i].append(" != " + v[enemy[i]]);
			} else {
				if (v[i].toString().isEmpty())
					v[i].append(i + " != " + v[enemy[i]].toString());
				else
					v[i].append(" != " + v[enemy[i]].toString());
			}
			v[enemy[i]] = null;
		}
		
		/*
		 * DEBUG
		 */
		
		System.out.println("parent:\n"+Arrays.toString(parent));
		System.out.println("enemy:\n" + Arrays.toString(enemy));
		System.out.println("prints:\n" + Arrays.asList(v));
		System.out.println("#nonNull: " + Arrays.asList(v).stream().filter(s -> s != null).count());
		
		
		String ret = Arrays.asList(v).stream().filter(s -> s != null).filter(s -> !s.toString().isEmpty()).collect(Collectors.joining("\n"));
		return ret;
	}
	
	public String currentStateNormalized() {
		StringBuilder[] v = new StringBuilder[n];
		
		
		// management of equalities
		for (int i = 0; i < n; ++i) {
			if (i == parent[i]) { // he is parent of himself
				if (v[i] == null) { // alone at the moment
					v[i] = new StringBuilder((i+1));
				} else {// already found a son
						// do nothing
				}
			} else { // he is son of someone else
				if (v[parent[i]] == null) { // the father has not been
											// initialized
					v[parent[i]] = new StringBuilder((parent[i]+1) + " = " + (i+1));
				} else {
					if (v[parent[i]].toString().isEmpty())
						v[parent[i]].append((parent[i]+1));
					v[parent[i]].append(" = " + (i+1));
				}
			}
		}
		
		//System.out.println("prints before:\n" + Arrays.asList(v));
		
		for(int i=0; i < n; ++i) {
			if(v[i] != null) {
				if(!v[i].toString().isEmpty()) {
					v[i] = new StringBuilder("("+v[i].toString()+")");
				}
			}
		}
		//System.out.println("prints after:\n" + Arrays.asList(v));
		// management of inequalities
		for (int i = 0; i < n; ++i) {

			if (enemy[i] == -1)
				continue;
			if (v[i] == null)
				continue; // should never happen

			if (v[enemy[i]].toString().isEmpty()) { // if the parent enemy has
													// not sons
				if (v[i].toString().isEmpty())
					v[i].append((i+1) + " != " + (enemy[i]+1));
				else
					v[i].append(" != " + v[enemy[i]]);
			} else {
				if (v[i].toString().isEmpty())
					v[i].append((i+1) + " != " + v[enemy[i]].toString());
				else
					v[i].append(" != " + v[enemy[i]].toString());
			}
			v[enemy[i]] = null;
		}
		
		/*
		 * DEBUG
		 */
		/*
		System.out.println("parent:\n"+Arrays.toString(parent));
		System.out.println("enemy:\n" + Arrays.toString(enemy));
		System.out.println("prints:\n" + Arrays.asList(v));
		System.out.println("#nonNull: " + Arrays.asList(v).stream().filter(s -> s != null).count());
		*/
		
		String ret = Arrays.asList(v).stream().filter(s -> s != null).filter(s -> !s.toString().isEmpty()).collect(Collectors.joining("\n"));
		return ret;
	}
	
	/**
	 * Gives information about the total number of current existing relations.
	 * @return A string containing {@code"xx,xx%"} that indicates </br>{@code (# of relations)/(# of variables)}
	 */
	public String progress() {
		
		//for n = 10 are needed n-1=9 relations
		double done = (double) n-count();
		return String.format("%.2f", (done /(n-1)) * 100) + "%";
	}

}