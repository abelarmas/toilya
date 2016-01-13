package ee.ut.oracle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.LinkedList;

import ee.ut.oracle.utils.Trace;

public abstract class ConcurrencyOracle {
	protected LinkedList<String> alphabet;
	protected Relations[][] matrix;
	
	public abstract void computeConcurrency(HashSet<Trace> traces);
	
	public abstract void computeSupport();
	
	public abstract boolean areConcurrent(String label1, String label2);

	public String toDot() {
		StringWriter str = new StringWriter();
		PrintWriter out = new PrintWriter(str);
		
		out.println("digraph G {");
		
		out.println("\tnode[shape=circle];");
		for (int i = 0; i < alphabet.size(); i++)
			out.printf("\tn%d [label=\"%s(%d)\"];\n", i, alphabet.get(i), i);
		
		String s1 = "subgraph Rel1 { \n";
		String s2 = "subgraph Rel2 {\n  edge [dir=none, color=red]";
		
		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix.length; j++){
				if (matrix[i][j] != null && matrix[i][j].equals(Relations.CAUSALITY))
					s1 += String.format("\tn%d -> n%d;\n", i, j);
				else if(i>j && matrix[i][j] != null && matrix[i][j].equals(Relations.CONCURRENCY))
					s2 += String.format("\tn%d -> n%d;\n", i, j);
			}
		
		s1 += "}";
		s2 += "}";
		out.println(s1);
		out.println(s2);
		out.println("}");
		
		return str.toString();
	}
	
	public boolean isEmpty() {
		return alphabet.isEmpty();
	}
}
