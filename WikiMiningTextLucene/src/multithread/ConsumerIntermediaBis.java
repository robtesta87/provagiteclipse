package multithread;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.queryparser.classic.ParseException;
import org.atteo.evo.inflector.English;

import posTagger.PosTagger;
import redirect.RedirectSearcher;
import util.Pair;
import Logger.Logger;
import Printer.PrinterOutput;
import bean.MappingObject;
import bean.WikiArticle;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import freebase.FreebaseSearcher;

public class ConsumerIntermediaBis extends Consumer{
	private static String articleIntermedia_folder = "raccoltaDati/ArticleIntermedia/";
	private static String mentionIntermedia_folder = "raccoltaDati/mentionIntermedia/";
	//private Queue<String> logQueue = new ConcurrentLinkedQueue<String>();
	private Map<String, MappingObject> info_mentions;
	private Set<String> black_list_name = new TreeSet<String>(Arrays.asList("the", "a", "an", "another", "no", "’s", "some", "any", "my", "our", "their", "her", "his", "its", "each", "every", "certain", "its", "this", "that","1", "2", "3", "4", "5", "6", "7", "8", "9", "0"));
	private Queue<String> logQueuePerson = new ConcurrentLinkedQueue<String>();
	private Queue<String> logQueueKeywordPerson = new ConcurrentLinkedQueue<String>();

	public ConsumerIntermediaBis(CountDownLatch latch,
			Queue<WikiArticle> input_buffer,
			FreebaseSearcher searcher,AbstractSequenceClassifier<CoreLabel> classifier, String analysis_folder, Logger logger, Logger logger_quantitativeAnalysis, Logger logger_countMid, RedirectSearcher redirect_searcher, PrinterOutput printer_output, MaxentTagger tagger,
			Logger logger_cont_person, Logger logger_cont_keyword_person, PrinterOutput printer_output_such_as) {
		super(latch, input_buffer, searcher, classifier, analysis_folder, logger, logger_quantitativeAnalysis, logger_countMid,redirect_searcher, printer_output, tagger, logger_cont_person,logger_cont_keyword_person,printer_output_such_as);
		
	}

	@Override
	public void run() {
		WikiArticle current_article = null;
		List<String> phrases = null;
		List<String> phrases_mid = null;
		
		
		Queue<String> logQueueMid = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueueOutput = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueueOutput_such_as = new ConcurrentLinkedQueue<String>();
		

		int size_queue = 0;

		while ((current_article = input_buffer.poll()) != null){
			try {
				
			
			/*if ((current_article.getWikid().equals("Dino_Zoff"))||
					(current_article.getWikid().equals("Dave_Brubeck"))||
					(current_article.getWikid().equals("Douglas_Hofstadter"))||
					(current_article.getWikid().equals("Alessandro_Allori"))||
					(current_article.getWikid().equals("Ancus_Marcius"))||
					(current_article.getWikid().equals("André-Marie_Ampère"))||
					(current_article.getWikid().equals("Ahmed_II"))||
					(current_article.getWikid().equals("Alcaeus_of_Mytilene"))||
					(current_article.getWikid().equals("Alexander_Aetolus"))||
					(current_article.getWikid().equals("Andocides"))||
					(current_article.getWikid().equals("Atle_Selberg"))||
					(current_article.getWikid().equals("Abdul_Rashid_Dostum"))||
					(current_article.getWikid().equals("Amalric,_Prince_of_Tyre"))||
					(current_article.getWikid().equals("Albert_Spalding"))||
					(current_article.getWikid().equals("Chris_Cunningham"))||
					(current_article.getWikid().equals("Carl_Rogers"))||
					(current_article.getWikid().equals("Bill_Haley"))||
					(current_article.getWikid().equals("André_Weil"))||
					(current_article.getWikid().equals("Bill_Holbrook"))||
					(current_article.getWikid().equals("Blind_Willie_McTell"))||
					(current_article.getWikid().equals("David_Thompson_(explorer)"))||
					(current_article.getWikid().equals("David_Hume"))
					){*/
			//if (current_article.getWikid().equals("Tito_Rodríguez")){
				cont_mention=0;
				System.out.println(current_article.getTitle());

				String dirty_text = current_article.getText();

				extractMentions(current_article);

				String cleaned_text = current_article.getText();
				//questa parte di codice commentatta estrae le frasi all'interno delle parentesi tonde
				
				Pattern pattern = Pattern.compile("(\\s\\().+?(\\))");
				Matcher matcher = pattern.matcher(cleaned_text);
				
				while(matcher.find()){
					String bracket_phrase = matcher.group().replaceAll("\\(|\\)", "");
					cleaned_text=cleaned_text+ bracket_phrase+". \n";
				}
				current_article.setText(cleaned_text);
				
				try {
					updateMidBis(current_article);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				phrases = getSentences(current_article.getText());
				if (!(phrases.isEmpty())){
					//entitiesMap = getEntities(phrases);
					//list_persons = getListPersons(current_article.getInfo_mentions(), current_article);
					if (!(current_article.getMid().equals("null"))){
						addPronoun(current_article);
						//questo metodo aggiunge nomi,cognomi e plurali
						addMentions(current_article);
					}
					//addPerson(list_persons, current_article);
					
					changeMap(current_article);
					phrases_mid = replaceMid(phrases, current_article.getMentions(),current_article);
					current_article.setPhrases(phrases_mid);
					logQueueOutput.add(getOutput(current_article));
					logQueueOutput_such_as.add(getOutput_such_as(current_article));
					size_queue++;

					//conto quanti mid ci sono per frase e salvo i risultati in un log
					//logQueueMid.add(countMid(current_article));


					//logQueue.add(current_article.getTitle()+"\t"+current_article.getMentions().size());
					//printArticles(current_article, dirty_text, cleaned_text,phrases);

					logger.addResult(logQueue);
					//scrivo i risultati delle analisi nei file di log
					if (size_queue>=10){
						//logger_countMid.addResult(logQueueMid);
						logger_cont_person.addResult(logQueuePerson);
						logger_cont_keyword_person.addResult(logQueueKeywordPerson);
						printer_output.addResult(logQueueOutput);
						printer_output_such_as.addResult(logQueueOutput_such_as);
						size_queue = 0;
					}
				}
			//}
			} catch (Exception e) {
				// TODO: handle exception
				logQueue.add("eccezione nell'articolo: "+current_article.getWikid());
				logger.addResult(logQueue);
			}
		}

		//logger_countMid.addResult(logQueueMid);
		printer_output.addResult(logQueueOutput);
		printer_output_such_as.addResult(logQueueOutput_such_as);
		logger_cont_person.addResult(logQueuePerson);
		logger_cont_keyword_person.addResult(logQueueKeywordPerson);
		latch.countDown();
	}
	public void printArticles(WikiArticle current_article,String dirty_text,String cleaned_text, List<String> phrases){
		PrintWriter outArticle = null;
		PrintWriter outMentions = null;
		try {
			outArticle = new PrintWriter(new BufferedWriter(new FileWriter(analysis_folder+articleIntermedia_folder+current_article.getTitle()+".txt", true)));
			outMentions = new PrintWriter(new BufferedWriter(new FileWriter(analysis_folder+mentionIntermedia_folder+current_article.getTitle()+".csv", true)));
		} catch (IOException e) {
			System.out.println("errore nella creazione file di testo di analisi!");
			e.printStackTrace();
		}	

		printer.PrintDirtyText(outArticle, dirty_text);
		printer.PrintCleanedText(outArticle, cleaned_text);
		printer.PrintMention(outArticle, current_article);
		printer.PrintMention(outMentions, current_article);

		for (int i=0; i<phrases.size();i++) {
			outArticle.println(phrases.get(i));
			outArticle.println(current_article.getPhrases().get(i));

		}

		//printer.PrintMentionMapping(outMentions, current_article);
		outArticle.close();
		outMentions.close();
	}
	
	/**
	 * dato un oggetto wikiarticle aggiungo i nomi, cognomi e plurali
	 * @param wikiArticle
	 */
	public void addMentions(WikiArticle wikiArticle){
		info_mentions = wikiArticle.getInfo_mentions();
		Iterator<String> keyIterator = info_mentions.keySet().iterator();
		String currentEntity = null;
		MappingObject mapping = null;
		String keywords="";
		String wikid = "";
		String mid = "";
		String[] personSpitted = null;
		while(keyIterator.hasNext()){
			currentEntity = keyIterator.next();
			mapping = info_mentions.get(currentEntity);
			wikid = mapping.getWikid();
			mid = mapping.getMid();
			keywords = mapping.getKeywords();
			if ((keywords.equals("He"))||(keywords.equals("She"))){
				if (!(currentEntity.contains(" of "))){
					if (currentEntity.contains("(")){
						currentEntity = currentEntity.split(" \\(")[0];
						wikiArticle.addMention(currentEntity, wikid, mid);
						//logQueue.add("person inserita: "+mapping.getWikid()+"\t"+currentEntity);
					}
					personSpitted = currentEntity.split(" ");
					//logQueue.add("person inserita: "+mapping.getWikid()+"\t"+mapping.getTypes()+"\t"+mapping.getKeywords()+"\t"+personSpitted[personSpitted.length-1]);
					if (wikiArticle.getMentions().get(personSpitted[personSpitted.length-1])==null){
						//aggiungo l'ultima parola della persona assunta come cognome
						wikiArticle.addMention(personSpitted[personSpitted.length-1], wikid, mid);
					}
				}
				personSpitted = currentEntity.split(" ");
				if (wikiArticle.getMentions().get(personSpitted[0])==null){
					//aggiungo la prima parola della persona assunta come nome
					
					if((wikid.split("_")[0].equals(personSpitted[0]))&&(!black_list_name.contains(personSpitted[0])))
						wikiArticle.addMention(personSpitted[0], wikid, mid);
					
				}
				//else logQueue.add("person NON inserita: "+mapping.getWikid()+"\t"+currentEntity);
			}
			else{
				//aggiungo i plurali
				if (!(currentEntity.contains("("))){
					//wikiArticle.addMention(currentEntity+"s", wikid, mid);
					wikiArticle.addMention(English.plural(currentEntity), wikid, mid);
					
				}
			}


		}
	}
	
	/**
	 * aggiungo i pronomi alla lista mention se l'articolo in questione è una persona
	 * @param wikiArticle
	 */
	public void addPronoun(WikiArticle wikiArticle){
		Map<String, MappingObject> info_mentions = wikiArticle.getInfo_mentions();
		//logQueue.add("articolo:"+wikiArticle.getTitle());
		MappingObject mapping = info_mentions.get(wikiArticle.getTitle().replaceAll("_", " "));
		if (mapping!=null){
			if (mapping.getTypes().contains("people.person"))
				logQueuePerson.add(wikiArticle.getWikid());
			if (mapping.getKeywords().equals("He")){
				logQueueKeywordPerson.add(wikiArticle.getWikid());
				wikiArticle.addMention("he", mapping.getWikid(), mapping.getMid());
				wikiArticle.addMention("He", mapping.getWikid(), mapping.getMid());
				//aggiungo il nome
				wikiArticle.addMention(mapping.getWikid().split("_")[0], mapping.getWikid(), mapping.getMid());
				//aggiungo il cognome
				//wikiArticle.addMention(mapping.getWikid().split("_")[mapping.getWikid().split("_").length-1], mapping.getWikid(), mapping.getMid());

				//logQueue.add("articolo riconosciuta persona:"+mapping.getWikid());

			}
			if(mapping.getKeywords().equals("She")){
				logQueueKeywordPerson.add(wikiArticle.getWikid());
				wikiArticle.addMention("she", mapping.getWikid(), mapping.getMid());
				wikiArticle.addMention("She", mapping.getWikid(), mapping.getMid());
				//aggiungo il nome
				wikiArticle.addMention(mapping.getWikid().split("_")[0], mapping.getWikid(), mapping.getMid());
				//aggiungo il cognome
				//wikiArticle.addMention(mapping.getWikid().split("_")[mapping.getWikid().split("_").length-1], mapping.getWikid(), mapping.getMid());

				//logQueue.add("articolo riconosciuta persona:"+mapping.getWikid());

			}
			if (mapping.getKeywords().equals("Other"))
				logQueueKeywordPerson.add(wikiArticle.getWikid());
		}
	}
	public void updateMidBis (WikiArticle wikiArticle) throws ParseException, IOException{
		//cerco il mid del wikid dell'articolo
		String wikid_article = wikiArticle.getWikid();
		wikiArticle.setMid(searcher.getMapping(wikid_article).getMid());
		MappingObject mapping_object=null;
		TreeMap<String, Pair<String, String>> mentions = wikiArticle.getMentions();
		Pair<String, String> mappingBean = null;
		Pair<String, String> pair = null;
		Iterator<String> keyIterator = mentions.keySet().iterator();
		String currentEntity = null;
		String wikid = null; 
		String mid ="";
		while(keyIterator.hasNext()){
			currentEntity = keyIterator.next();
			pair = mentions.get(currentEntity);
			wikid = pair.getKey();
			//mid = searcherES.getMid(wikid, client);
			mapping_object = searcher.getMapping(wikid);
			mid = mapping_object.getMid();
			if (!(mid.equals("null"))){
				wikiArticle.addMapping_object(currentEntity, mapping_object);
				pair.setValue(mid);
			}

		}
	}
}
