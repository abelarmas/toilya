package ee.ut.oracle.utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Trace implements Comparable<Trace>{
	LinkedList<String> run = new LinkedList<>();

	public Trace(LinkedList<String> run) {
		this.run.addAll(run);
	}

	public void addEvent(String event) {
		this.run.add(event);
	}

	public LinkedList<String> getRun() {
		return run;
	}

	public Trace getReverse() {
		LinkedList<String> reverse = new LinkedList<>();

		for (int i = 0; i < run.size(); i++)
			reverse.add(0, run.get(i));

		return new Trace(reverse);
	}

	public Trace clone() {
		Trace newR = new Trace(new LinkedList<String>(run));

		return newR;
	}

	public String toString() {
		return run.toString();
	}

	public int length() {
		return run.size();
	}

	public LinkedList<String> substring(int i, int j) {
		LinkedList<String> sString = new LinkedList<>();

		for (int k = i; k < j; k++)
			sString.add(run.get(k));

		return sString;
	}

	public boolean contains(LinkedList<String> query) {
		for (int i = 0; i < run.size(); i++) 
			if (run.get(i).equals(query.get(0))) {
				if (i + query.size() > run.size())
					return false;

				boolean found = true;
				for (int j = 0; j < query.size(); j++) 
					if (!run.get(i + j).equals(query.get(j))) {
						found = false;
						break;
					}

				if (found)
					return true;
			}

		return false;
	}

	public int countMatches(List<String> query) {
		int i = 0;
		List<String> sub = new LinkedList<String>(run);
		int index = Collections.indexOfSubList(sub, query);

		while (index != -1) {
			i++;

			if ((index + 1) < sub.size()
					&& (sub.size() - index) >= query.size()) {
				sub = sub.subList(index + 1, sub.size());
				index = Collections.indexOfSubList(sub, query);
			} else
				index = -1;
		}

		return i;
	}

	public void addTrace(Trace t) {
		if (run.isEmpty())
			run.addAll(t.getRun());
		else {
			run.add("$");
			run.addAll(t.getRun());
		}
	}

	@Override
	public int compareTo(Trace o) {
		if(this.run.equals(o.run))
			return 0;
		
		return 1;
	}
	
	@Override
	public boolean equals(Object trace){
		return run.equals(((Trace) trace).getRun());
	}
}
