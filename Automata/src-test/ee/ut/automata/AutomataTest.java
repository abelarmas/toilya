package ee.ut.automata;

import static org.junit.Assert.*;

import org.deckfour.xes.model.XLog;
import org.jbpt.utils.IOUtils;
import org.junit.Test;

import ee.ut.mining.log.XLogReader;
import ee.ut.oracle.automata.Automata;

public class AutomataTest {
	private String model = "cycle10.bpmn";
	private String fileNameTemplate = "logs/%s.mxml.gz";
	
	@Test
	public void test() throws Exception {
			XLog log = XLogReader.openLog(String.format(fileNameTemplate, model));
			Automata at = new Automata(log);
			IOUtils.toFile(model + "automata.dot", at.toDot());
		
	}

}
