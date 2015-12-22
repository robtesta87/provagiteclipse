package multithread;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import redirect.RedirectSearcher;
import Logger.Logger;
import Printer.PrinterOutput;
import bean.MappingObject;
import bean.WikiArticle;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import freebase.FreebaseSearcher;

public class ConsumerIntermediaBis extends Consumer{
	private static String articleIntermedia_folder = "raccoltaDati/ArticleIntermedia/";
	private static String mentionIntermedia_folder = "raccoltaDati/mentionIntermedia/";
	private Queue<String> logQueue = new ConcurrentLinkedQueue<String>();

	public ConsumerIntermediaBis(CountDownLatch latch,
			Queue<WikiArticle> input_buffer,
			FreebaseSearcher searcher,AbstractSequenceClassifier<CoreLabel> classifier, String analysis_folder, Logger logger, Logger logger_quantitativeAnalysis, Logger logger_countMid, RedirectSearcher redirect_searcher, PrinterOutput printer_output,String ip_elasticsearch) {
		super(latch, input_buffer, searcher, classifier, analysis_folder, logger, logger_quantitativeAnalysis, logger_countMid,redirect_searcher, printer_output,ip_elasticsearch);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		WikiArticle current_article = null;
		List<String> phrases = null;
		List<String> phrases_mid = null;


		Queue<String> logQueueMid = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueueOutput = new ConcurrentLinkedQueue<String>();

		int size_queue = 0;

		while ((current_article = input_buffer.poll()) != null){
			cont_mention=0;
			System.out.println(current_article.getTitle());

			String dirty_text = current_article.getText();

			extractMentions(current_article);

			String cleaned_text = current_article.getText();

			updateMid(current_article);
			phrases = getSentences(current_article.getText());
			if (!(phrases.isEmpty())){
				//entitiesMap = getEntities(phrases);
				//list_persons = getListPersons(current_article.getInfo_mentions(), current_article);
				addPronoun(current_article);
				
				addSurnames(current_article.getInfo_mentions(), current_article);

				//addPerson(list_persons, current_article);


				phrases_mid = replaceMid(phrases, current_article.getMentions());
				current_article.setPhrases(phrases_mid);
				logQueueOutput.add(getOutput(current_article));

				size_queue++;

				//conto quanti mid ci sono per frase e salvo i risultati in un log
				//logQueueMid.add(countMid(current_article));


				printArticles(current_article, dirty_text, cleaned_text,phrases);
				logger.addResult(logQueue);
				//scrivo i risultati delle analisi nei file di log
				if (size_queue>=10){
					logger_countMid.addResult(logQueueMid);
					printer_output.addResult(logQueueOutput);
					size_queue = 0;
				}
			}

		}

		logger_countMid.addResult(logQueueMid);
		printer_output.addResult(logQueueOutput);

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

		for (int i=0; i<phrases.size();i++) {
			outArticle.println(phrases.get(i));
			outArticle.println(current_article.getPhrases().get(i));

		}

		printer.PrintMention(outArticle, current_article);
		printer.PrintMention(outMentions, current_article);
		//printer.PrintMentionMapping(outMentions, current_article);
		outArticle.close();
		outMentions.close();
	}
	public void addSurnames(Map<String, MappingObject> info_mentions, WikiArticle wikiArticle){
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
					wikiArticle.addMention(personSpitted[personSpitted.length-1], wikid, mid);
				}
				//else logQueue.add("person NON inserita: "+mapping.getWikid()+"\t"+currentEntity);
			}


		}
	}
	
	public void addPronoun(WikiArticle wikiArticle){
		Map<String, MappingObject> info_mentions = wikiArticle.getInfo_mentions();
		logQueue.add("articolo:"+wikiArticle.getTitle());
		MappingObject mapping = info_mentions.get(wikiArticle.getTitle().replaceAll("_", " "));
		if (mapping!=null){
			if (mapping.getKeywords().equals("He")){
				wikiArticle.addMention("he", mapping.getWikid(), mapping.getMid());
				wikiArticle.addMention("He", mapping.getWikid(), mapping.getMid());
				logQueue.add("articolo riconosciuta persona:"+mapping.getWikid());

			}
			if(mapping.getKeywords().equals("She")){
				wikiArticle.addMention("she", mapping.getWikid(), mapping.getMid());
				wikiArticle.addMention("She", mapping.getWikid(), mapping.getMid());
				logQueue.add("articolo riconosciuta persona:"+mapping.getWikid());

			}
		}
	}


}
