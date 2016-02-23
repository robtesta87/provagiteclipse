package multithread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.queryparser.classic.ParseException;

import posTagger.PosTagger;
import redirect.RedirectSearcher;
import util.Pair;
import Logger.Logger;
import Printer.Printer;
import Printer.PrinterOutput;
import SortMap.SortMap;
import bean.WikiArticle;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Triple;
import freebase.FreebaseSearcher;

abstract class Consumer implements Runnable {
	protected CountDownLatch latch;
	protected Queue<WikiArticle> input_buffer;
	protected FreebaseSearcher searcher;
	protected AbstractSequenceClassifier<CoreLabel> classifier;
	protected String analysis_folder;
	protected Printer printer;
	protected static SortMap sortMap;
	protected Logger logger;
	protected Logger logger_quantitativeAnalysis;
	protected Logger logger_countMid;
	protected int cont_mention;
	protected int cont_redirect;
	protected RedirectSearcher redirect_searcher;
	protected PrinterOutput printer_output;
	protected PrinterOutput printer_output_such_as;
	protected Queue<String> logQueue = new ConcurrentLinkedQueue<String>();
	//oggetti stanford nlp
	private Properties props;
	private StanfordCoreNLP pipeline;
	protected MaxentTagger tagger;
	private PosTagger posTagger;
	final static String special_char = "Éé?!#,\"'.îóçë&–üáà:°í#ἀνãİï/āèñöÖÆçæäüğş"
			+ "ãÎøÁúšúćčžŠßıüÇò";
	final static String special_char_for_bold = "\"É’é?!#,.îóçë&–üáà:°í#ἀνãİï/āèñöÖÆçæäüğş"
			+ "ãÎøÁúšúćčžŠßıüÇò";
	final static String mentionRegex = "\\[\\[.+?\\]\\]";
	final static String boldRegex = "\"'[\\w+\\s"+special_char_for_bold+"\\(\\)_-]*\"'";
	final static String file_sentence_model = "/home/roberto/Scrivania/en-sent.bin";
	private Set<String> black_list_mentions = new TreeSet<String>(Arrays.asList("the","The", "a", "an", "another", "no", "’s", "some", "any", "my", "our", "their", "her", "his", "its", "each", "every", "certain", "its", "this", "that","1", "2", "3", "4", "5", "6", "7", "8", "9", "0"));
	protected Logger logger_cont_person;
	protected Logger logger_cont_keyword_person;
	/**
	 * 
	 * @param latch
	 * @param input_buffer
	 * @param output_buffer
	 * @param searcher
	 * @param quantitativeAnalysisBase 
	 */
	public Consumer(CountDownLatch latch, Queue<WikiArticle> input_buffer, 
			FreebaseSearcher searcher, AbstractSequenceClassifier<CoreLabel> classifier, 
			String analysis_folder, Logger logger, Logger quantitativeAnalysis,
			Logger logger_countMid, RedirectSearcher redirect_searcher,PrinterOutput printer_output, MaxentTagger tagger,
			Logger logger_count_person,Logger logger_cont_keyword_person,PrinterOutput printer_output_such_as){
		this.latch = latch;
		this.input_buffer = input_buffer;
		this.searcher = searcher;
		this.analysis_folder = analysis_folder;
		this.classifier = classifier;
		this.printer = new Printer();
		this.sortMap = new SortMap();
		this.logger = logger;
		this.logger_quantitativeAnalysis = quantitativeAnalysis;
		this.logger_countMid = logger_countMid;
		this.cont_mention = 0;
		this.cont_redirect = 0;
		this.redirect_searcher = redirect_searcher;
		this.printer_output = printer_output;
		this.printer_output_such_as = printer_output_such_as;
		//creazione oggetti stanford nlp
		this.props = new Properties();
		this.props.setProperty("annotators", "tokenize, ssplit");
		this.pipeline = new StanfordCoreNLP(props);
		this.tagger = tagger;
		posTagger = new PosTagger();
		this.logger_cont_person = logger_count_person;
		this.logger_cont_keyword_person = logger_cont_keyword_person;

	}

	/**
	 * @return the latch
	 */
	public CountDownLatch getLatch() {
		return latch;
	}

	/**
	 * @param latch the latch to set
	 */
	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}

	/**
	 * @return the input_buffer
	 */
	public Queue<WikiArticle> getInput_buffer() {
		return input_buffer;
	}

	/**
	 * @param input_buffer the input_buffer to set
	 */
	public void setInput_buffer(Queue<WikiArticle> input_buffer) {
		this.input_buffer = input_buffer;
	}


	/**
	 * @return the searcher
	 */
	public FreebaseSearcher getSearcher() {
		return searcher;
	}

	/**
	 * @param searcher the searcher to set
	 */
	public void setSearcher(FreebaseSearcher searcher) {
		this.searcher = searcher;
	}

	/**
	 * estrazione delle mention originali attraverso il testo dell'oggeto wikiArticle
	 * @param wikiArticle
	 */
	public  void extractMentions (WikiArticle wikiArticle){
		String text = wikiArticle.getText();
		//aggiungo il titolo come mention
		wikiArticle.addMention(wikiArticle.getTitle());

		//estrazione mention attraverso l'espressione regolare mentionRegex
		//e sostituisco le mention trovate con i wikid corrispondenti
		Pattern pattern = Pattern.compile(mentionRegex);
		Matcher matcher = pattern.matcher(text);

		while(matcher.find()){
			String mentionString = matcher.group();
			String stringCleaned ="";
			if (mentionString.contains("[[["))
				stringCleaned = mentionString.substring(3, mentionString.length()-3);
			else{
				if (mentionString.startsWith("[["))
					stringCleaned = mentionString.substring(2, mentionString.length()-2);
			}
			if(stringCleaned.contains("|")){
				String[] splitted = stringCleaned.split("\\|");
				//primo campo: text secondo campo: wikiid

				if (!(splitted[0].contains("#"))){
					if (!splitted[0].equals("")){
						if (splitted.length>1){
							if ((!(splitted[0].equals(splitted[1])))&&(!(splitted[1].equals(wikiArticle.getTitle())))){
								//aggiungo il text come mention
								wikiArticle.addMention(splitted[1], splitted[0].replaceAll(" ", "_"));
							}
						}
						wikiArticle.addMention(splitted[0]);

						text = text.replace(mentionString, " "+splitted[0]);
					}
					else{
						text = text.replace(mentionString, " "+splitted[1]);
					}
				}
				else{
					//provo a prendere l'articolo riguardante la sotto-mention anzichè scartarla
					wikiArticle.addMention(splitted[0].split("#")[0]);
					text = text.replace(mentionString, " "+splitted[0].split("#")[0]);
				}
			}
			else{
				if (!(stringCleaned.contains("#"))){
					wikiArticle.addMention(stringCleaned);
					text = text.replace(mentionString, " "+stringCleaned);
				}
				else{
					//provo a prendere l'articolo riguardante la sotto-mention anzichè scartarla
					wikiArticle.addMention(stringCleaned.split("#")[0]);
					text = text.replace(mentionString, " "+stringCleaned.split("#")[0]);
				}

			}
		}	

		//rilevamento mention dalle parole in grassetto con l'espressione regolare bolRegex
		//pattern = Pattern.compile(boldRegex);
		pattern = Pattern.compile("\"'.+?\"'");
		if (text.length()>300){
			matcher = pattern.matcher(text.substring(0, 300));
		}
		else{
			matcher = pattern.matcher(text.substring(0, text.length()-1));
		}
		while(matcher.find()){
			String mentionString = matcher.group();
			String stringCleaned = mentionString.substring(2, mentionString.length()-2);
			wikiArticle.addMention(stringCleaned, wikiArticle.getWikid());
		}	

		wikiArticle.setText(text);

		wikiArticle.getMentions().remove("");
		wikiArticle.getMentions().remove(" ");
		wikiArticle.getMentions().remove("*");
	}


	/**
	 * aggiornamento, nella treeMap Mention dell'oggetto WikiAricle, di tutte le mention con i rispettivi MID
	 * @param freebaseSearcher
	 * @param wikiArticle
	 */
	public void updateMid (WikiArticle wikiArticle){
		//cerco il mid del wikid dell'articolo
		TreeMap<String, Pair<String, String>> mentions = wikiArticle.getMentions();
		Pair<String, String> mappingBean = null;
		Pair<String, String> pair = null;
		String wikid_article = wikiArticle.getWikid();
		try {
			mappingBean = searcher.getMid(wikid_article);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		String mid = "";
		if (mappingBean!=null){
			mid = mappingBean.getValue();
			wikiArticle.setMid(mid);
		}

		Iterator<String> keyIterator = mentions.keySet().iterator();
		String currentEntity = null;
		String wikid = null; 
		while(keyIterator.hasNext()){
			currentEntity = keyIterator.next();
			pair = mentions.get(currentEntity);
			wikid = pair.getKey();
			try {
				mappingBean = searcher.getMid(wikid);
			} catch (ParseException | IOException e) {
				e.printStackTrace();
			}
			mid="";
			if (mappingBean!=null){
				mid= mappingBean.getValue();
				pair.setValue(mid);
			}
		}
	}

	public List<String> getSentences(String text){
		List<String> phrases = new ArrayList<String>();
		String phrase = "";
		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for(CoreMap sentence: sentences) {
			//phrases.add(" "+sentence.toString().replaceAll("\\n", " ")+" ");
			phrase = " "+posTagger.getTokenText(sentence.toString().replaceAll("\\n", " "), tagger)+" ";
			phrases.add(phrase);
		}

		return phrases;
	}

	/**
	 * restituisce tutte le entità riconosciute dal NER ricevendo in input le frasi dell'articolo
	 * @param phrases
	 * @return
	 */
	public  Map<String,List<String>> getEntities(List<String> phrases){

		List<String> person = new ArrayList<String>();
		List<String> misc = new ArrayList<String>();
		List<String> location = new ArrayList<String>();
		List<String> organization = new ArrayList<String>();
		Map<String,List<String>> entityMap = new HashMap<String, List<String>>();
		Queue<String> logQueue = new ConcurrentLinkedQueue<String>();
		try {
			for(String currentPhrase : phrases){
				List<Triple<String,Integer,Integer>> triples = null;
				triples = classifier.classifyToCharacterOffsets(currentPhrase);
				for (Triple<String,Integer,Integer> trip : triples) {
					String text = currentPhrase.substring(trip.second(), trip.third());
					if (text.contains("|")){
						//logQueue.add("entità non riconosciuta: "+text);
					}
					switch (trip.first) {
					case "PERSON":
						person.add(text);
						break;
					case "ORGANIZATION":
						organization.add(text);
						break;
					case "LOCATION":
						location.add(text);
						break;
					case "MISC":
						misc.add(text);
						break;
					default:
						break;
					}
				}
			}

		} catch (ClassCastException e) {
			e.printStackTrace();
		} 
		entityMap.put("PERSON", person);
		entityMap.put("LOCATION",location);
		entityMap.put("ORGANIZATION", organization);
		entityMap.put("MISC", misc);

		logger.addResult(logQueue);
		return entityMap;
	}

	/**
	 * per ogni frase dell'articolo sostituisce le mention individuate con: [[wikid|mid]]
	 * @param phrases
	 * @param treemap
	 * @param out
	 * @return
	 */
	public List<String> replaceMid (List<String> phrases,TreeMap<String, Pair<String,String>> treemap, WikiArticle wikiArticle){
		Map<String, Pair<String, String>> sortedMap = null;

		sortedMap = sortMap.sortByValues(treemap);

		Set<Entry<String, Pair<String, String>>> setMap = sortedMap.entrySet();
		List<String> phrasesMid = new ArrayList<String>();
		int cont_mid;
		List<String> bracket_phrases = new ArrayList<String>();
		for (String phrase:phrases){
			Iterator<Entry<String, Pair<String, String>>> i = setMap.iterator();
			cont_mid = 0;
			while((i.hasNext())) {
				Entry<String, Pair<String, String>> me = i.next();
				String key = me.getKey().toString();
				if ((!key.equals(""))){
					String wikid = me.getValue().getKey();
					String mid =  me.getValue().getValue();

					try {
						phrase = phrase.replace(" "+key, " [["+wikid+"|"+mid+"]] ");
					} catch (Exception e) {
						logQueue.add("errore nella replaceAll\twikidTitle:"+wikiArticle.getWikid()+"\t"+key+"\t"+wikid+"\t"+mid);
					}
				}
			}

			//questa parte di codice commentatta estrae le frasi all'interno delle parentesi tonde
			/*
			Pattern pattern = Pattern.compile("(\\s-LRB-).+?(\\s-RRB-)");
			Matcher matcher = pattern.matcher(phrase);

			while(matcher.find()){
				String bracket_phrase = matcher.group().replaceAll("(\\s-LRB-)|(\\s-RRB-)", " ");
				bracket_phrases.add(bracket_phrase);
			}
			 */
			try{
				String[] splitted_phrase = phrase.split("\\[\\[");
				cont_mid = splitted_phrase.length-1;
				if (cont_mid>0)
					phrasesMid.add(cont_mid+"\t"+phrase.replaceAll("(\\s-LRB-).+?(-RRB-)|(\\s-LSB-).+?(-RSB-)|(\\s-LCB-).+?(-RCB-)", " ")
							.replaceAll(".+?(-RRB-)|.+?(-RSB-)|.+?(-RCB-)", " ")
							.replaceAll("(\\s-LRB-).+|(\\s-LSB-).+|(\\s-LCB-).+", " "));
			} catch (Exception e) {
				logQueue.add("errore nella replaceAll\twikidTitle:"+wikiArticle.getWikid());
			}

		}
		/*
		for (String bracket_phrase : bracket_phrases) {
			String[] splitted_phrase = bracket_phrase.split("\\[\\[");
			cont_mid = splitted_phrase.length-1;
			if (cont_mid>0)
				phrasesMid.add(cont_mid+"\t"+bracket_phrase.replaceAll("(\\s-LRB-).+?(-RRB-)|(\\s-LSB-).+?(-RSB-)|(\\s-LCB-).+?(-RCB-)", " ")
						.replaceAll(".+?(-RRB-)|.+?(-RSB-)|.+?(-RCB-)", " ")
						.replaceAll("(\\s-LRB-).+|(\\s-LSB-).+|(\\s-LCB-).+", " "));
		}*/
		return phrasesMid;
	}

	public String replace(String phrase,String key,String wikid,String mid){

		//phrase = phrase.replace(" "+key+" s", " [["+wikid+"|"+mid+"]]");
		//phrase = phrase.replace(" "+key+"s", " [["+wikid+"|"+mid+"]]");
		//phrase = phrase.replace(" ("+key+" s", " ([["+wikid+"|"+mid+"]]");
		//phrase = phrase.replace(" \""+key+" s", " ([["+wikid+"|"+mid+"]]");


		phrase = phrase.replace(" "+key+" ", " [["+wikid+"|"+mid+"]] ");
		phrase = phrase.replace(" "+key+"’", " [["+wikid+"|"+mid+"]]'");
		phrase = phrase.replace(" "+key+"'", " [["+wikid+"|"+mid+"]]'");
		phrase = phrase.replace(" "+key+"\"", " [["+wikid+"|"+mid+"]]\"");
		phrase = phrase.replace(" "+key+".", " [["+wikid+"|"+mid+"]].");
		phrase = phrase.replace(" "+key+",", " [["+wikid+"|"+mid+"]],");
		phrase = phrase.replace(" "+key+":", " [["+wikid+"|"+mid+"]]:");
		phrase = phrase.replace(" "+key+";", " [["+wikid+"|"+mid+"]];");
		phrase = phrase.replace(" "+key+"!", " [["+wikid+"|"+mid+"]]!");
		phrase = phrase.replace(" "+key+"?", " [["+wikid+"|"+mid+"]]?");
		phrase = phrase.replace(" ("+key+")", " ([["+wikid+"|"+mid+"]])");
		phrase = phrase.replace(" ("+key+" ", " ([["+wikid+"|"+mid+"]] ");
		phrase = phrase.replace(" \""+key+"\"", " \"[["+wikid+"|"+mid+"]]\"");
		phrase = phrase.replace(" \""+key+" ", " \"[["+wikid+"|"+mid+"]] ");
		phrase = phrase.replace(" \"'"+key+"\"'", " \"[["+wikid+"|"+mid+"]]\"");
		//da provare
		//phrase = phrase.replace(" '"+key+" ", " '[["+wikid+"|"+mid+"]] ");
		//phrase = phrase.replace(" \""+key+"'", " \"[["+wikid+"|"+mid+"]]'");
		//phrase = phrase.replace(" ("+key+"'", " ([["+wikid+"|"+mid+"]]'");
		return phrase;
	}

	public String replaceAll(String phrase,String key,String wikid,String mid){
		//phrase = phrase.replaceAll("(\\s)"+key+"[\\s\\?’':;!\\.\")]"," [["+wikid.toString()+"|"+mid.toString()+"]] ");
		/*phrase = phrase.replaceAll("(\\s)"+key+"[\\s\\?:;!\\.\")]|(\\s\\()"+key+"[\\)\\s]"," [["+wikid.toString()+"|"+mid.toString()+"]] ");
		phrase = phrase.replaceAll("(\\s)"+key+"[’']|(\\s\\()"+key+"(')"," [["+wikid.toString()+"|"+mid.toString()+"]]'");
		phrase = phrase.replaceAll("(\\s)"+key+"[,]"," [["+wikid.toString()+"|"+mid.toString()+"]], ");
		phrase = phrase.replaceAll("(\\s\")"+key+"[\"\\s]|(\\s\"')"+key+"(\")"," \"[["+wikid.toString()+"|"+mid.toString()+"]]\" ");
		//phrase = phrase.replaceAll("(\\s')"+key+"(\\s)"," '[["+wikid.toString()+"|"+mid.toString()+"]] ");*/

		phrase = phrase.replaceAll("(\\s)"+key+"[\\s\\?:;!]"," [["+wikid.toString()+"|"+mid.toString()+"]] ");
		phrase = phrase.replaceAll("(\\s)"+key+"(\\.)"," [["+wikid.toString()+"|"+mid.toString()+"]].");
		phrase = phrase.replaceAll("(\\s)"+key+"(\")"," [["+wikid.toString()+"|"+mid.toString()+"]]\"");
		phrase = phrase.replaceAll("(\\s\\()"+key+"(\\))"," ([["+wikid.toString()+"|"+mid.toString()+"]])");
		phrase = phrase.replaceAll("(\\s\\()"+key+"(\\s)"," ([["+wikid.toString()+"|"+mid.toString()+"]] ");
		phrase = phrase.replaceAll("(\\s)"+key+"[’']"," [["+wikid.toString()+"|"+mid.toString()+"]]'");
		phrase = phrase.replaceAll("(\\s\\()"+key+"(')"," ([["+wikid.toString()+"|"+mid.toString()+"]]'");
		phrase = phrase.replaceAll("(\\s)"+key+"(,)"," [["+wikid.toString()+"|"+mid.toString()+"]], ");
		phrase = phrase.replaceAll("(\\s\")"+key+"[\"\\s]|(\\s\"')"+key+"(\")"," \"[["+wikid.toString()+"|"+mid.toString()+"]]\" ");
		return phrase;
	}

	public List<String> replaceMidGIW (List<String> phrases,TreeMap<String, Pair<String,String>> treemap,Map<String,List<String>> entitiesMap, WikiArticle wikiArticle){
		Map<String, Pair<String, String>> sortedMap = null;

		sortedMap = sortMap.sortByValues(treemap);

		Set<Entry<String, Pair<String, String>>> setMap = sortedMap.entrySet();

		List<String> phrasesMid = new ArrayList<String>();

		for (String phrase:phrases){

			Iterator<Entry<String, Pair<String, String>>> i = setMap.iterator();
			while((i.hasNext())) {
				Entry<String, Pair<String, String>> me = i.next();
				String key = me.getKey().toString();
				if (!key.equals("")){
					String wikid = me.getValue().getKey();

					String mid =  me.getValue().getValue();
					String type ="";
					if (wikid.equals(wikiArticle.getWikid())){
						type = "PERSON";
					}
					else{
						if (entitiesMap.get("MISC").contains(wikid.replaceAll("_", " ")))
							type = "MISC";
						if (entitiesMap.get("ORGANIZATION").contains(wikid.replaceAll("_", " ")))
							type = "ORGANIZATION";
						if (entitiesMap.get("PERSON").contains(wikid.replaceAll("_", " ")))
							type = "PERSON";
						if (entitiesMap.get("LOCATION").contains(wikid.replaceAll("_", " ")))
							type = "LOCATION";
					}
					if (!(phrase.equals("")))
						phrase = phrase.replaceAll("^"+key+"[\\s]|([\\s\']"+key+"[\\s\'.,:;!?])|([\\s]"+key+")$", " [["+wikid+"|"+mid+"|"+type+"]] ").trim();
				}
			}

			phrasesMid.add(phrase);
		}
		return phrasesMid;
	}
	/**
	 * aggiunge per ogni persona individuata dal NER il cognome (ultima parola)
	 * @param entities
	 * @param wikiArticle
	 */
	public void addPerson(List<String> entities, WikiArticle wikiArticle){
		String currentEntity= null;
		TreeMap<String, Pair<String,String>> treemap = null;

		for (int i = 0; i < entities.size(); i++) {
			currentEntity = entities.get(i);
			treemap = wikiArticle.getMentions();
			String[] personSpitted = currentEntity.split(" ");
			Pair<String,String> pair = treemap.get(currentEntity);
			if (currentEntity.equals(wikiArticle.getTitle())){
				wikiArticle.addMention(currentEntity,pair.getKey(),pair.getValue());
				//aggiungo il cognome (ultima parola dell'entità) associandogli lo stesso wikid
				wikiArticle.addMention(personSpitted[personSpitted.length-1],wikiArticle.getWikid(),pair.getValue());
				cont_mention++;
			}
			else{

				String[] titleSplitted = wikiArticle.getTitle().split(" ");

				if ((wikiArticle.getTitle().contains(currentEntity))&&(titleSplitted[titleSplitted.length-1].equals(personSpitted[personSpitted.length-1]))){
					pair =treemap.get(wikiArticle.getTitle());
					wikiArticle.addMention(currentEntity,pair.getKey(),pair.getValue());
					//aggiungo il cognome (ultima parola dell'entità) associandogli lo stesso wikid
					wikiArticle.addMention(personSpitted[personSpitted.length-1],wikiArticle.getWikid(),pair.getValue());
					cont_mention++;

				}
				else{
					if (pair!=null){
						wikiArticle.addMention(currentEntity,pair.getKey(),pair.getValue());
						//aggiungo il cognome (ultima parola dell'entità) associandogli lo stesso wikid
						wikiArticle.addMention(personSpitted[personSpitted.length-1],pair.getKey(),pair.getValue());
						cont_mention++;
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param entities
	 * @param wikiArticle
	 */
	public void countMentions(List<String> entities, WikiArticle wikiArticle){

		String currentEntity= null;
		TreeMap<String, Pair<String,String>> treemap = wikiArticle.getMentions();

		for (int i = 0; i < entities.size(); i++) {
			currentEntity = entities.get(i);
			if (treemap.containsKey(currentEntity)){
				cont_mention++;
			}
		}

	}

	public String countMid(WikiArticle wikiArticle){
		List<String> phrases2mid = new ArrayList<String>();
		int countMid=0;
		Pattern pattern=null;
		Matcher matcher=null;
		int duemid = 0;
		int tremid = 0;
		int quattromid = 0;
		int cinquemid = 0;
		int altrimid = 0;
		List<String> phrases = wikiArticle.getPhrases();
		for (String phrase : phrases) {
			List<Integer> indexMention = new ArrayList<Integer>();
			countMid=0;
			pattern = Pattern.compile(mentionRegex);
			matcher = pattern.matcher(phrase);
			while(matcher.find()){
				indexMention.add(matcher.start());
				indexMention.add(matcher.end());
				countMid++;
			}
			if (countMid>1)
				phrases2mid.add(phrase);
			if (countMid==2)
				duemid++;
			if (countMid==3)
				tremid++;
			if (countMid==4)
				quattromid++;
			if (countMid==5)
				cinquemid++;
			if (countMid>5)
				altrimid++;

		}
		float percentage = (phrases2mid.size() * 100)/phrases.size();

		return (wikiArticle.getTitle()+"\t"+phrases2mid.size()+"\t"+phrases.size()+"\t"+percentage+"\t"+duemid+"\t"+tremid+"\t"+quattromid+"\t"+cinquemid+"\t"+altrimid);
	}

	public String getOutput(WikiArticle wikiArticle){
		String output = null;
		StringBuilder builder = new StringBuilder();

		//inserire la lista di frasi(finire anche la versione base e intermedia)
		List<String> phrases = wikiArticle.getPhrases();
		for (String phrase : phrases) {
			if (!(phrase.contains("such as")))
				builder.append(wikiArticle.getWikid()+"\t"+wikiArticle.getMid()+"\t"+phrase+"\n");
		}

		output= builder.toString();
		return output;
	}
	public String getOutput_such_as(WikiArticle wikiArticle){
		String output = null;
		StringBuilder builder = new StringBuilder();

		//inserire la lista di frasi(finire anche la versione base e intermedia)
		List<String> phrases = wikiArticle.getPhrases();
		for (String phrase : phrases) {
			if (phrase.contains("such as"))
				builder.append(wikiArticle.getWikid()+"\t"+wikiArticle.getMid()+"\t"+phrase+"\n");
		}

		output= builder.toString();
		return output;
	}

	public void changeMap(WikiArticle wikiArticle){
		TreeMap<String, Pair<String, String>> treemap =wikiArticle.getMentions();

		Map<String, Pair<String, String>> sortedMap = null;

		sortedMap = sortMap.sortByValues(treemap);

		Set<Entry<String, Pair<String, String>>> setMap = sortedMap.entrySet();
		Iterator<Entry<String, Pair<String, String>>> i = setMap.iterator();
		String new_key="";
		while((i.hasNext())) {
			Entry<String, Pair<String, String>> me = i.next();
			String key = me.getKey().toString();
			String mid = me.getValue().getValue();
			if (!(mid.equals("null"))){
				if ((!key.equals(""))){
					Pair<String, String> value = treemap.remove(key);
					new_key = posTagger.getTokenText(key, tagger);
					if ((new_key.length()>=key.length())&&(!black_list_mentions.contains(new_key)))
						treemap.put(new_key,value);
					else
						treemap.put(key,value);
				}
			}
			else{
				treemap.remove(key);
			}
		}
	}
}
