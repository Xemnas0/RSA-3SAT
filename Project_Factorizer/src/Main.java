import java.util.Arrays;

import Solver.Problem;

public class Main {

	public static void main(String[] args) {
		/*
		
		Problem instance = new Problem();
		String filePath = "C:\\Users\\franu_000\\git\\Project_Factorizer\\5b_2d.txt";
		
		instance.readFile(filePath);
		
		instance.solve();

		System.out.println(instance.result());
		
		*/
		int[] vett = new int[10];
		
		for(int i=0; i < 10; ++i) {
			vett[i]= i*i;
		}
		System.out.println(Arrays.stream(vett).filter(s -> s != 0).count());
		
	}

}
