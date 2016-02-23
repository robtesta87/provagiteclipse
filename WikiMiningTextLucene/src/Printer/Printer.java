package Printer;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import util.Pair;
import SortMap.SortMap;
import bean.WikiArticle;

public class Printer {

	private Map<String, Pair<String, String>> sortedMap;
	private SortMap sortMap;
	public Printer() {
		sortMap  = new SortMap();
	}

	public void PrintDirtyText(PrintWriter out,String text){
		out.println("----------------------------");
		out.println("TESTO CON MENTION");
		out.println("----------------------------");
		out.println(text);
	}

	public void PrintCleanedText(PrintWriter out, String text){
		out.println("----------------------------");
		out.println("TESTO PULITO");
		out.println("----------------------------");
		out.println(text);
	}

	public void PrintMention (PrintWriter out, WikiArticle wikiArticle){

		TreeMap<String, Pair<String,String>> treemap = wikiArticle.getMentions();
		sortedMap = sortMap.sortByValues(treemap);

		// Get a set of the entries on the sorted map
		Set<Entry<String, Pair<String, String>>> set = sortedMap.entrySet();
		// Get an iterator
		Iterator<Entry<String, Pair<String, String>>> i = set.iterator();

		// Display elements
		while(i.hasNext()) {
			Entry<String, Pair<String, String>> me = i.next();
			out.println(me.getKey()+ "\t"+me.getValue().getKey()+"\t"+me.getValue().getValue());
		}
	}

	public void PrintEntities (PrintWriter out, Map<String,List<String>> entities){
		out.println("----------------------------");
		out.println("ENTITA RICONOSCIUTE DAL NER");
		out.println("----------------------------");
		List<String> persons = entities.get("PERSON");
		out.println("PERSON");
		out.println("--------------------");
		for (String person : persons) 
			out.println(person);
		List<String> organizations = entities.get("ORGANIZATION");
		out.println("--------------------");
		out.println("ORGANIZATION");
		for (String organization : organizations) 
			out.println(organization);
		List<String> locations = entities.get("LOCATION");
		out.println("--------------------");
		out.println("LOCATION");
		for (String location : locations) 
			out.println(location);
		List<String> miscs = entities.get("MISC");
		out.println("--------------------");
		out.println("MISC");
		for (String misc : miscs) 
			out.println(misc);
	}

}
