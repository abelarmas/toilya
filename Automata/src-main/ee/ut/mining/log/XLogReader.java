package ee.ut.mining.log;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.in.XMxmlGZIPParser;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class XLogReader {
	public static XLog openLog(String inputLogFileName) throws Exception {
		XLog log = null;

		if(inputLogFileName.toLowerCase().contains("mxml.gz")){
			XMxmlGZIPParser parser = new XMxmlGZIPParser();
			if(parser.canParse(new File(inputLogFileName))){
				try {
					log = parser.parse(new File(inputLogFileName)).get(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}else if(inputLogFileName.toLowerCase().contains("mxml") || 
				inputLogFileName.toLowerCase().contains("xml")){
			XMxmlParser parser = new XMxmlParser();
			if(parser.canParse(new File(inputLogFileName))){
				try {
					log = parser.parse(new File(inputLogFileName)).get(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if(inputLogFileName.toLowerCase().contains("xes.gz")){
			XesXmlGZIPParser parser = new XesXmlGZIPParser();
			if(parser.canParse(new File(inputLogFileName))){
				try {
					log = parser.parse(new File(inputLogFileName)).get(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}else if(inputLogFileName.toLowerCase().contains("xes")){
			XesXmlParser parser = new XesXmlParser();
			if(parser.canParse(new File(inputLogFileName))){
				try {
					log = parser.parse(new File(inputLogFileName)).get(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}


		if(log == null)
			throw new Exception("Oops ...");
		
		return log;
	}

	public static Collection<HashSet<XTrace>> getTracesByMultiset(XLog log) {
		HashMap<String, HashSet<XTrace>> map = new HashMap<String, HashSet<XTrace>>();
		
		for (XTrace t : log) {
			Multiset<String> conf = HashMultiset.create();
			
			for (XEvent e : t) 
				if (isCompleteEvent(e) && e.getAttributes().get(XConceptExtension.KEY_NAME) != null)
					conf.add(getEventName(e));
			
			if(!map.containsKey(conf.toString()))
				map.put(conf.toString(), new HashSet<XTrace>());
			
			map.get(conf.toString()).add(t);
		}
			
		return map.values();
	}
	
	private static boolean isCompleteEvent(XEvent e) {
		XAttributeMap amap = e.getAttributes();
		return (amap.get(XLifecycleExtension.KEY_TRANSITION).toString()
				.toLowerCase().equals("complete"));
	}
	
	private static String getEventName(XEvent e) {
		return e.getAttributes().get(XConceptExtension.KEY_NAME).toString();
	}
}
