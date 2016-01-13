package ee.ut.oracle.automata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import ee.ut.oracle.utils.Trace;

public class Automata {
	State root;
	TreeSet<String> dictionary;
	BiMap<String, State> stateMap; 
	BiMap<State, Integer> traceCounter; 
	
	public Automata(XLog log) {
		dictionary = new TreeSet<>();
		stateMap = HashBiMap.create();
		
		LinkedList<String> past = new LinkedList<String>();
		
		root = new State(HashMultiset.create());
		root.addTrace(new Trace(past));
		stateMap.put("[]", root);
		
		for (XTrace t : log) {
			State current = root;
			Trace trace = new Trace(past);
			Multiset<String> conf = HashMultiset.create();
			
			for (XEvent e : t) {
				if (isCompleteEvent(e) && e.getAttributes().get(XConceptExtension.KEY_NAME) != null) {
					String label = getEventName(e);
					dictionary.add(label);
					conf.add(label);
					trace.addEvent(label);
					
					State newSt = new State(conf);
					newSt.addTrace(trace.clone());
					
					if(stateMap.containsKey(conf.toString())){
						stateMap.get(conf.toString()).addTraces(newSt.getTraces().iterator().next());
						newSt = stateMap.get(conf.toString());
					}else
						stateMap.put(conf.toString(), newSt);
					
					current.addTransition(label, newSt);
					current = newSt;
				}
			}
		}
	}

	public Automata(String string) {
		dictionary = new TreeSet<>();
		stateMap = HashBiMap.create();
		
		LinkedList<String> past = new LinkedList<String>();
		
		root = new State(HashMultiset.create());
		root.addTrace(new Trace(past));
		stateMap.put("[]", root);
		
		try{
			BufferedReader buffer = new BufferedReader(new FileReader(string));
			
			String line = buffer.readLine();
	
			while (line != null) {
				State current = root;
				Trace trace = new Trace(past);
				Multiset<String> conf = HashMultiset.create();
				
				for (int i = 0; i < line.length(); i++) {
					String e = line.charAt(i) + "";
					dictionary.add(e);
					conf.add(e);
					trace.addEvent(e);
						
					State newSt = new State(conf);
					newSt.addTrace(trace.clone());
						
					if(stateMap.containsKey(conf.toString())){
						stateMap.get(conf.toString()).addTraces(newSt.getTraces().iterator().next());
						newSt = stateMap.get(conf.toString());
					}else
						stateMap.put(conf.toString(), newSt);
						
					current.addTransition(e, newSt);
					current = newSt;
				}
				
				line = buffer.readLine();
			}
			
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Automata(HashSet<XTrace> runs) {
		dictionary = new TreeSet<>();
		stateMap = HashBiMap.create();
		
		LinkedList<String> past = new LinkedList<String>();
		
		root = new State(HashMultiset.create());
		root.addTrace(new Trace(past));
		stateMap.put("[]", root);
		
		for (XTrace t : runs) {
			State current = root;
			Trace trace = new Trace(past);
			Multiset<String> conf = HashMultiset.create();
			
			for (XEvent e : t) {
				if (isCompleteEvent(e) && e.getAttributes().get(XConceptExtension.KEY_NAME) != null) {
					String label = getEventName(e);
					dictionary.add(label);
					conf.add(label);
					trace.addEvent(label);
					
					State newSt = new State(conf);
					newSt.addTrace(trace.clone());
					
					if(stateMap.containsKey(conf.toString())){
						stateMap.get(conf.toString()).addTraces(newSt.getTraces().iterator().next());
						newSt = stateMap.get(conf.toString());
					}else
						stateMap.put(conf.toString(), newSt);
					
					current.addTransition(label, newSt);
					current = newSt;
				}
			}
		}
	}

	private String getEventName(XEvent e) {
		return e.getAttributes().get(XConceptExtension.KEY_NAME).toString();
	}

	private boolean isCompleteEvent(XEvent e) {
		XAttributeMap amap = e.getAttributes();
		return (amap.get(XLifecycleExtension.KEY_TRANSITION).toString()
				.toLowerCase().equals("complete"));
	}

	public String toDot() {
		StringWriter str = new StringWriter();
		PrintWriter out = new PrintWriter(str);

		HashBiMap<State, Integer> indexes = HashBiMap.create();

		out.println("digraph G {");

		out.println("\tnode[shape=box];");
		for (Iterator<State> iterator = stateMap.values().iterator(); iterator.hasNext();) {
			State s = iterator.next();
			out.printf("\tn%d [label=\"%s(%d)\"];\n", indexes.size(),
					s.toString(), indexes.size());
			indexes.put(s, indexes.size());
		}

		for (State s : indexes.keySet())
			for (String l : s.transitions.keySet())
				out.printf("\tn%d -> n%d [label=\"%s\"];\n", indexes.get(s),
						indexes.get(s.transitions.get(l)), l);

		out.println("}");

		return str.toString();
	}

	public HashSet<State> getSinks() {
		HashSet<State> sinks = new HashSet<>();
		
		for(State s : stateMap.values())
			if(s.transitions.isEmpty())
				sinks.add(s);
		
		return sinks;
	}

	public State getRoot() {
		return root;
	}
	
	public HashSet<State> getTargets() {
		HashSet<State> targets = new HashSet<State>();
		
		for(State st : stateMap.values())
			if(st.getTansitions() == null || st.getTansitions().isEmpty())
				targets.add(st);
		
		return targets;
	}

	public State getNodeBySize(int size) {
		for(State s : stateMap.values()){
			System.out.println(s.configuration + " = " + s.configuration.size());
			if(s.configuration.size() == size && !s.configuration.contains("end"))
				return s;
		}
		
		return null;
	}

	public HashSet<Trace> getTracesFrom(Multiset<String> state) {
		State current = null;
		for(State s : this.stateMap.values())
			if(s.configuration.equals(state))
				current = s;
		
		if(current == null){
			System.out.println("State not found");
			return null;
		}
		
		return getTracesFrom(current);
	}
	
	public HashSet<Trace> getTracesFrom(State state) {
		HashSet<Trace> traces = new HashSet<>();
		LinkedList<State> toAnalize = new LinkedList<>();
		LinkedList<State> visited = new LinkedList<>();
		toAnalize.add(state);

		while (!toAnalize.isEmpty()) {
			State current = toAnalize.poll();
			
			if(!visited.contains(current)){
				visited.add(current);
				
				if(current == null)
					System.out.println("Null");
				
				if(current.transitions == null || current.transitions.isEmpty())
					for(Trace t : current.getTraces()){
						Trace tr = new Trace(new LinkedList<String>(t.getRun().subList(state.configuration.size(), t.getRun().size())));	
						traces.add(tr);
					}
				else
					for (String transition : current.getTansitions().keySet()) 
						toAnalize.add(current.getTansitions().get(transition));
			}
		}

		return traces;
	}

	public Set<State> getStates() {
		return stateMap.values();
	}
}
