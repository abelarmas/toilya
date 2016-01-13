package ee.ut.oracle.automata;

import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import ee.ut.oracle.utils.Trace;

public class State implements Comparable<State>{
	HashMap<String, State> transitions;
	HashMap<String, State> preTransitions;
	Multiset<String> configuration;
	HashSet<Trace> traces;
	HashSet<Multiset<String>> tracesMset;
	
	public State(Multiset<String> configuration){
		this.configuration = Multisets.copyHighestCountFirst(configuration);
		traces = new HashSet<>();
		tracesMset = new HashSet<>();
		transitions = new HashMap<>();
		preTransitions = new HashMap<>();
	}
	
	public void addTransition(String link, State to){
		transitions.put(link, to);
		to.setPreTransitions(link, this);
	}
	
	private void setPreTransitions(String link, State from) {
		preTransitions.put(link, from);
	}

	public void addTrace(Trace trace){
		traces.add(trace);	
	}

	@Override
	public int compareTo(State o) {
		if(o.configuration.equals(this.configuration))
			return 0;
		
		return 1;
	}
	
	public String toString(){
		return configuration.toString();
	}

	public HashMap<String, State> getTansitions() {
		return transitions;
	}
	
	public HashMap<String, State> getPreTansitions() {
		return preTransitions;
	}

	public HashSet<Trace> getTraces() {
		return traces;
	}

	public boolean wasViewed(Trace trace) {
		for(Trace t : traces)
			if(t.getRun().equals(trace))
				return true;
		
		return false;
	}

	public void addTraces(Trace trace) {
		boolean f = true;
		
		for(Trace t : this.traces)
			if(t.equals(trace))
				f = false;
		
		if(f)
			this.traces.add(trace);
	}

	public String getConfiguration() {
		return configuration.toString();
	}
}
