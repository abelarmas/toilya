package ee.ut.mining.log;

import com.google.common.collect.Multiset;

public interface ConcurrencyRelations {
	boolean areConcurrent(String label1, String label2);
	boolean areConcurrent(String label1, String label2, Multiset<String> configuration);
}
