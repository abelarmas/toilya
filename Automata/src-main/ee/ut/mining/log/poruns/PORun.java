package ee.ut.mining.log.poruns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import ee.ut.graph.transitivity.MatrixBasedTransitivity;
import ee.ut.mining.log.ConcurrencyRelations;
import ee.ut.oracle.AutomataBasedOracle;

public class PORun implements PORunConst {
	private static AtomicInteger nextId = new AtomicInteger();

	protected boolean[][] adjmatrix;
	protected Map<Integer, String> labels;
	private Multimap<Integer, Integer> concurrency;
	protected List<Integer> vertices;
	protected Map<Integer, Integer> vertexIndexMap;
	private Multimap<Integer, Integer> predList = null;
	private HashMap<Integer, Multiset<String>> confMap;
	
	public PORun(ConcurrencyRelations alphaRelations, XTrace trace) {
		this(alphaRelations, trace, null);
	}
	
	public PORun(ConcurrencyRelations oracle, XTrace trace, Map<Integer, Pair<XEvent,String>> xeventMap, boolean old) {
		this.labels = new HashMap<>();	
		this.vertices = new ArrayList<>();
		this.vertexIndexMap = new HashMap<>();
		this.concurrency = HashMultimap.create();
		
		String traceId = trace.getAttributes().get("concept:name").toString();
		// === Map events to local identifiers
		Integer id = nextId.getAndIncrement();
		vertices.add(id);
		vertexIndexMap.put(id, 0);
		labels.put(id, GLOBAL_SOURCE_LABEL);
		for (XEvent e: trace)
			if (isCompleteEvent(e) && e.getAttributes().get(XConceptExtension.KEY_NAME) != null) {
				id = nextId.getAndIncrement();
				vertices.add(id);
				vertexIndexMap.put(id, vertexIndexMap.size());
				labels.put(id, getEventName(e));

				
				if (xeventMap != null) xeventMap.put(id, new Pair<>(e, traceId));
			}
		id = nextId.getAndIncrement();
		vertices.add(id);
		vertexIndexMap.put(id, vertexIndexMap.size());
		labels.put(id, GLOBAL_SINK_LABEL);
//		id = nextId.getAndIncrement();
//		vertices.add(id);
//		vertexIndexMap.put(id, vertexIndexMap.size());
//		labels.put(id, GLOBAL_SINK_PRIME_LABEL);
		
		// === We compute the transitive closure of causality
		// === Initially we have a trivial sequence. The order on events reflects
		// === the causal relation. Therefore, the transitive closure of causality
		// === corresponds with an upper triangle matrix.
		int size = labels.size();
		this.adjmatrix = new boolean[size][size];
		for (int i = 0; i < size-1; i++)
			Arrays.fill(adjmatrix[i], i+1, size, true);
//		System.out.println("=============================");
//		GraphUtils.print(adjmatrix);
		
		// ======================================================
		// Introduce the concurrency relation
		// ======================================================
		for (int i = 1; i < size - 1; i++) {
			Integer vertex1 = vertices.get(i);
			String label1 = labels.get(vertex1);
			for (int j = i + 1; j < size - 1; j++) {
				Integer vertex2 = vertices.get(j);
				String label2 = labels.get(vertex2);
				if (oracle.areConcurrent(label1, label2)) {
					adjmatrix[i][j] = false;
					concurrency.put(vertex1, vertex2);
					concurrency.put(vertex2, vertex1);
				}
			}
		}
//		System.out.println("=============================");
//		GraphUtils.print(adjmatrix);

		MatrixBasedTransitivity.transitiveReduction(adjmatrix);
//		System.out.println("=============================");
//		GraphUtils.print(adjmatrix);
//		System.out.println("=============================");
	}
	
	public PORun(ConcurrencyRelations oracle, XTrace trace, Map<Integer, Pair<XEvent,String>> xeventMap) {
		this.labels = new HashMap<>();	
		this.vertices = new ArrayList<>();
		this.vertexIndexMap = new HashMap<>();
		this.concurrency = HashMultimap.create();
		this.confMap = new HashMap<Integer, Multiset<String>>();
		
		String traceId = trace.getAttributes().get("concept:name").toString();
		// === Map events to local identifiers
		Integer id = nextId.getAndIncrement();
		vertices.add(id);
		vertexIndexMap.put(id, 0);
		labels.put(id, GLOBAL_SOURCE_LABEL);
		Multiset<String> globalConf = HashMultiset.create();
		
		for (XEvent e: trace)
			if (isCompleteEvent(e) && e.getAttributes().get(XConceptExtension.KEY_NAME) != null) {
				id = nextId.getAndIncrement();
				vertices.add(id);
				vertexIndexMap.put(id, vertexIndexMap.size());
				labels.put(id, getEventName(e));
				globalConf.add(labels.get(id));
				confMap.put(id, HashMultiset.create(globalConf));
				
				if (xeventMap != null) xeventMap.put(id, new Pair<>(e, traceId));
			}
		
		id = nextId.getAndIncrement();
		vertices.add(id);
		vertexIndexMap.put(id, vertexIndexMap.size());
		labels.put(id, GLOBAL_SINK_LABEL);
//		id = nextId.getAndIncrement();
//		vertices.add(id);
//		vertexIndexMap.put(id, vertexIndexMap.size());
//		labels.put(id, GLOBAL_SINK_PRIME_LABEL);
		
		// === We compute the transitive closure of causality
		// === Initially we have a trivial sequence. The order on events reflects
		// === the causal relation. Therefore, the transitive closure of causality
		// === corresponds with an upper triangle matrix.
		int size = labels.size();
		this.adjmatrix = new boolean[size][size];
		for (int i = 0; i < size-1; i++)
			Arrays.fill(adjmatrix[i], i+1, size, true);
//		System.out.println("=============================");
//		GraphUtils.print(adjmatrix);
		
		// ======================================================
		// Introduce the concurrency relation
		// ======================================================
		for (int i = 1; i < size - 1; i++) {
			Integer vertex = vertices.get(i);
			Multiset<String> conf = confMap.get(vertex);
			
			for (int j = 1; j < size - 1; j++) 
				if(adjmatrix[i][j]){
					Integer vertex2 = vertices.get(j);
					String label2 = labels.get(vertex2);
					
					for (int k = 1; k < size - 1; k++) 
						if(adjmatrix[i][k]){
							Integer vertex3 = vertices.get(k);
							String label3 = labels.get(vertex3);
							
							if (!label3.equals(label2) && oracle.areConcurrent(label2, label3, conf)) {
								System.out.println("-----label2 = " + label2);
								System.out.println("label3 = " + label3 +"-----");
								adjmatrix[j][k] = false;
								adjmatrix[k][j] = false;
								concurrency.put(vertex2, vertex3);
								concurrency.put(vertex3, vertex2);
							}
						}
				}
		}
		
//		System.out.println("=============================");
//		GraphUtils.print(adjmatrix);

		MatrixBasedTransitivity.transitiveReduction(adjmatrix);
//		System.out.println("=============================");
//		GraphUtils.print(adjmatrix);
//		System.out.println("=============================");
	}
	
	public PORun(AutomataBasedOracle oracle, XTrace trace) {
		this(oracle, trace, null);
	}

	private String getEventName(XEvent e) {
		return e.getAttributes().get(XConceptExtension.KEY_NAME).toString();
	}

	private boolean isCompleteEvent(XEvent e) {
		XAttributeMap amap = e.getAttributes();
		return (amap.get(XLifecycleExtension.KEY_TRANSITION).toString().toLowerCase().equals("complete"));
	}
	
	public Map<Integer, String> getLabelMap() {
		return labels;
	}
	
	public Multimap<Integer, Integer> asSuccessorsList() {
		Multimap<Integer, Integer> succList = HashMultimap.create();
		this.predList = HashMultimap.create();
		int size = adjmatrix.length;
		for (int i = 0; i < size - 1; i++) {
			for (int j = i + 1; j < size; j++)
				if (adjmatrix[i][j]) {
					succList.put(vertices.get(i), vertices.get(j));
					predList.put(vertices.get(j), vertices.get(i));
				}
		}
		return succList;
	}
	
	public Multimap<Integer, Integer> asPredecessorsList() {
		if (predList == null) {
			predList = HashMultimap.create();
			int size = adjmatrix.length;
			for (int i = 0; i < size - 1; i++) {
				for (int j = i + 1; j < size; j++)
					if (adjmatrix[i][j])
						predList.put(vertices.get(j), vertices.get(i));
			}
		}
		return predList;
	}
	
	public Multimap<Integer, Integer> getConcurrencyRelation() {
		return concurrency;
	}
	
	public Integer getSource() {
		return vertices.get(0);
	}
	
	public Integer getSink() {
		return vertices.get(vertices.size() - 1);
	}
}
