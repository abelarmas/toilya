package ee.ut.oracle;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.deckfour.xes.model.XLog;
import org.jbpt.utils.IOUtils;

import com.google.common.collect.Multiset;

import ee.ut.oracle.automata.Automata;
import ee.ut.oracle.automata.State;
import ee.ut.oracle.utils.Trace;
import ee.ut.mining.log.ConcurrencyRelations;

public class AutomataBasedOracle implements ConcurrencyRelations {
	Automata automaton;
	ConcurrencyOracle oracle;
	
	public AutomataBasedOracle(XLog log, ConcurrencyOracle oracle){
		this.automaton = new Automata(log);
		this.oracle = oracle;
	}
	
	public AutomataBasedOracle(Automata automaton, ConcurrencyOracle oracle){
		this.automaton = automaton;
		this.oracle = oracle;
	}
	
	public void computeRelationsFromRoot(){
		HashSet<Trace> traces = automaton.getTracesFrom(automaton.getRoot());
		oracle.computeConcurrency(traces);
	}
	
	public void computeRelationsFrom(State state) {
		System.out.println("Starting from state: " + state);
		HashSet<Trace> traces = automaton.getTracesFrom(state);
		oracle.computeConcurrency(traces);
	}
	
	public void computeRelationsRegressive(String folder, String prefix){
		Queue<State> queue = new LinkedList<>();
		LinkedList<State> visited = new LinkedList<>();
		queue.addAll(automaton.getTargets());

		while(!queue.isEmpty()){
			State current = queue.poll();
			
			if(!visited.contains(current)){
				oracle.computeConcurrency(automaton.getTracesFrom(current));
				
				if(!oracle.isEmpty()){
					String newFolder = "target/"+folder+"/"+current.getConfiguration();
					checkFolderExist(newFolder);
					IOUtils.toFile(folder + "/"+ current.getConfiguration() + "/" + prefix + current.getConfiguration()+".dot", oracle.toDot());
				}
				
				if(current.getPreTansitions()!=null)
					queue.addAll(current.getPreTansitions().values());
				
				visited.add(current);
			}
		}
	}

	public void computeRelationsProgressive(String folder, String prefix){
		Queue<State> queue = new LinkedList<>();
		LinkedList<State> visited = new LinkedList<>();
		queue.add(automaton.getRoot());

		while(!queue.isEmpty()){
			State current = queue.poll();
			
			if(!visited.contains(current)){
				oracle.computeConcurrency(automaton.getTracesFrom(current));
				
				if(!oracle.isEmpty()){
					String newFolder = "target/"+folder+"/"+current.getConfiguration();
					checkFolderExist(newFolder);
					IOUtils.toFile(folder + "/"+ current.getConfiguration() + "/" + prefix +current.getConfiguration()+".dot", oracle.toDot());
				}
				
				if(current.getTansitions()!=null)
					queue.addAll(current.getTansitions().values());
				
				visited.add(current);
			}
		}
	}
	
	private void checkFolderExist(String string) {
		File dir = new File(string);
		
		if(!dir.exists())
			dir.mkdirs();
	}

	@Override
	public boolean areConcurrent(String label1, String label2) {
		HashSet<Trace> traces = automaton.getTracesFrom(automaton.getRoot());
		oracle.computeConcurrency(traces);
		
		return oracle.areConcurrent(label1, label2);
	}

	@Override
	public boolean areConcurrent(String label1, String label2, Multiset<String> configuration) {
		System.out.println(configuration);
		HashSet<Trace> traces = automaton.getTracesFrom(configuration);
		
		oracle.computeConcurrency(traces);
		
		return oracle.areConcurrent(label1, label2);
	}
}
