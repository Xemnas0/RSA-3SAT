package Solver;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.istack.internal.logging.Logger;

public class QUForestTest {

	private static final Logger log = Logger.getLogger(QUForestTest.class);

	private QUForest tm;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//log.info("@BeforeClass: runs one time, before any tests in this class.");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//log.info("@AfterClass: runs one time, after all tests in this class.");
	}

	@Before
	public void setUp() throws Exception {
		//log.info("@Before: runs before each test in this class.");
		int n = 10;
		tm = new QUForest(n);
	}

	@After
	public void tearDown() throws Exception {
		//log.info("@After: runs after each test in this class.");
	}

	@Test
	public void testConnected() {
		log.info("@Test: started testConnected().");
		/*tm.union(1, 2);
		assertFalse("2 e 3 sono connessi", tm.connected(2, 3));
		assertTrue("1 e 2 sono sconnessi", tm.connected(1, 2));
		assertEquals("Ci sono 9 componenti", 9, tm.count());
		*/
		tm.disj(1, 0);
		System.out.println("StateInTest:\n"+tm.currentState());
		tm.disj(2, 4);
		System.out.println("StateInTest:\n"+tm.currentState());
		tm.disj(0, 2);
		System.out.println("StateInTest:\n"+tm.currentState());
		tm.union(0, 7);
		System.out.println("StateInTest:\n"+tm.currentState());
		tm.disj(5, 8);
		System.out.println("StateInTest:\n"+tm.currentState());
		tm.union(5, 2);
		System.out.println("StateInTest:\n"+tm.currentState());
		
		
		//tm.union(0, 1);
		
		//System.out.println(tm.count());
		System.out.println("Final progress: "+tm.progress());
		log.info("@Test: ended testConnected().");
	}

}
