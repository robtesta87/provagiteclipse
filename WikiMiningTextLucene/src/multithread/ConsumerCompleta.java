package multithread;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.xml.serialize.Printer;

import redirect.RedirectSearcher;
import util.Pair;
import Logger.Logger;
import Printer.PrinterOutput;
import bean.WikiArticle;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import freebase.FreebaseSearcher;

public class ConsumerCompleta extends Consumer{

	private static String articleCompleta_folder = "raccoltaDati/ArticleCompleta/";
	private static String mentionCompleta_folder = "raccoltaDati/mentionCompleta/";

	public ConsumerCompleta(CountDownLatch latch,
			Queue<WikiArticle> input_buffer,FreebaseSearcher searcher,
			AbstractSequenceClassifier<CoreLabel> classifier,
			String analysis_folder, Logger logger, Logger quantitativeAnalysis,
			Logger logger_countMid, RedirectSearcher redirect_searcher, PrinterOutput printer_output,MaxentTagger tagger, Logger logger_cont_person,Logger logger_cont_keyword_person,PrinterOutput printer_output_such_as) {
		super(latch, input_buffer, searcher, classifier,
				analysis_folder, logger, quantitativeAnalysis, logger_countMid, redirect_searcher,printer_output,tagger,logger_cont_person,logger_cont_keyword_person,printer_output_such_as);
		// TODO Auto-generated constructor stub
	}


	/**
	 * 
	 * @param entities
	 * @param wikiArticle
	 */
	public void addRedirect (List<String> entities, WikiArticle wikiArticle){
		TreeMap<String, Pair<String, String>> treemap = wikiArticle.getMentions();
		Pair<String,String> redirect_pair =  null;
		for (int i = 0; i < entities.size(); i++) {
			String currentEntity = entities.get(i);
			if (!treemap.containsKey(currentEntity)){
				try {
					redirect_pair = redirect_searcher.getRedirect(currentEntity);
				} catch (ParseException | IOException e) {
					e.printStackTrace();
				}
				if (redirect_pair!=null){
					wikiArticle.addMention(currentEntity, redirect_pair.getValue());
					cont_redirect++;
				}
			}
		}
	}

	public void addPersonRedirect(List<String> entities, WikiArticle wikiArticle){
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
					else{
						//se non si trova un match allora interrogo l'indice redirect
						Pair<String,String> redirect_pair =  null;
						try {
							redirect_pair = redirect_searcher.getRedirect(currentEntity);
						} catch (ParseException | IOException e) {
							e.printStackTrace();
						}
						if (redirect_pair!=null){
							wikiArticle.addMention(currentEntity, redirect_pair.getValue());
							cont_redirect++;
						}
					}
				}
			}
		}
	}


	@Override
	public void run() {
		WikiArticle current_article = null;
		List<String> phrases = null;
		List<String> phrases_mid = null;
		Map<String,List<String>> entitiesMap = null;
		Queue<String> logQueue = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueueMid = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueueOutput = new ConcurrentLinkedQueue<String>();
		int size_queue = 0;

		while ((current_article = input_buffer.poll()) != null){
			cont_mention = 0;
			cont_redirect = 0;
			System.out.println(current_article.getTitle());
			
			String dirty_text = current_article.getText();
			
			extractMentions(current_article);

			int cont_original_mention =current_article.getMentions().size();	//contatore delle mention originali

			String cleaned_text = current_article.getText();

			phrases = getSentences(current_article.getText());
			entitiesMap = getEntities(phrases);

			addPersonRedirect(entitiesMap.get("PERSON"), current_article);

			//controllo se nelle entità ci sono dei match esatti nelle mention originali per il controllo quantitativo
			countMentions(entitiesMap.get("ORGANIZATION"), current_article);
			countMentions(entitiesMap.get("MISC"), current_article);
			countMentions(entitiesMap.get("LOCATION"), current_article);

			//aggiungo la quantità delle mention trovate in un log
			cont_mention = cont_mention + cont_original_mention;

			addRedirect(entitiesMap.get("ORGANIZATION"), current_article);
			addRedirect(entitiesMap.get("MISC"), current_article);
			addRedirect(entitiesMap.get("LOCATION"), current_article);
			updateMid(current_article);
			System.out.println();
			phrases_mid = replaceMid(phrases, current_article.getMentions(),current_article);
			current_article.setPhrases(phrases_mid);

			cont_redirect = cont_redirect+cont_mention;
			logQueue.add(current_article.getTitle()+"\t"+cont_original_mention+"\t"+cont_mention+" \t"+ cont_redirect);
			size_queue++;
			
			//conto quanti mid ci sono per frase e salvo i risultati in un log
			//logQueueMid.add(countMid(current_article));
			
			//printArticles(current_article, dirty_text, cleaned_text, phrases);
			
			//scrivo i risultati delle analisi nei file di log
			if (size_queue>=10){
				logger_quantitativeAnalysis.addResult(logQueue);
				logger_countMid.addResult(logQueueMid);
				printer_output.addResult(logQueueOutput);
				size_queue = 0;
			}
			logQueueOutput.add(getOutput(current_article));
		}

		//scrivo le informazioni degli articoli rimanenti nelle rispettive code
		logger_quantitativeAnalysis.addResult(logQueue);
		logger_countMid.addResult(logQueueMid);
		printer_output.addResult(logQueueOutput);

		latch.countDown();
	}
	
	public void printArticles(WikiArticle current_article,String dirty_text,String cleaned_text, List<String> phrases){
		PrintWriter outArticle = null;
		PrintWriter outMentions = null;
		try {
			outArticle = new PrintWriter(new BufferedWriter(new FileWriter(analysis_folder+articleCompleta_folder+current_article.getTitle()+".txt", true)));
			outMentions = new PrintWriter(new BufferedWriter(new FileWriter(analysis_folder+mentionCompleta_folder+current_article.getTitle()+".csv", true)));
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
		outArticle.close();
		outMentions.close();
	}

}
