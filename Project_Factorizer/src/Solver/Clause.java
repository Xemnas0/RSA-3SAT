package Solver;

import java.util.ArrayList;
import java.util.List;

public class Clause {

	private List<Integer> vars = new ArrayList<Integer>();
	private List<List<Boolean>> signs = new ArrayList<List<Boolean>>();

	public Clause(List<Integer> variables) {
		vars=variables;
	}
	
	public void insertRow(List<Integer> values) {
		
		if(values.size() > 3) return; //error
		
		List<Boolean> tmpSigns = new ArrayList<Boolean>();
		
		values.stream().forEach(s->{
			tmpSigns.add(tmpSigns.size(),new Boolean((s.intValue()>0 ? true : false)));
		});
		signs.add(signs.size(), tmpSigns);
	}
	
	public String print() {
		StringBuilder ret = new StringBuilder();
		
		for (int i = 0; i < signs.size(); ++i) {
			List<Boolean> x = signs.get(i);
			for (int j = 0; j < vars.size(); ++j) {
				ret.append(String.format("%-10d", vars.get(j).intValue()));
				//ret.append((x.get(j).booleanValue() ? "" : "-") + vars.get(j).intValue() + "\t");
			}	
		}
		return ret.toString();
	}
	
	public boolean hasTheseVars(List<Integer> vars) {
		return vars.stream().filter(s -> !this.vars.contains(Math.abs(s))).count() == 0;
	}
	
}
