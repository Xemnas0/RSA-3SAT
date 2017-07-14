package Solver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Problem {

	// private List<Variable> variables = new ArrayList<Variable>();
	
	
	
	
	
	
	public void solve(){
		//TODO to complete
		return;
	}
	
	
	/**
	 * Initialize the problem with a file in {@code DIMACS} format.
	 * @param path The location of the file.
	 */
	public void readFile(String path) {

		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = reader.readLine()) != null) {
				//TODO riempire variabili
			}
		} catch (IOException e) {
			System.err.println("Error during file reading: " + path);
		}
	}
	
	public String result(){
		//TODO to complete
		return null;
	}

}
