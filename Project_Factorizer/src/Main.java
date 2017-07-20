import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Solver.Clause;
import Solver.Problem;

public class Main {

	public static void main(String[] args) {
		
		Problem instance = new Problem();
		String filePath = "C:\\Users\\franu_000\\git\\Project_Factorizer\\5b_2d.txt";
		
		instance.readFile(filePath);
		
		instance.solve();

		//System.out.println(instance.result());
	}

}
