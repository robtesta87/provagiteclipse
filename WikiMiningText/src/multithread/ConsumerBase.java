package multithread;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redirect.RedirectSearcher;
import util.Pair;
import Logger.Logger;
import Printer.PrinterOutput;
import bean.WikiArticle;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import freebase.FreebaseSearcher;

class ConsumerBase extends Consumer {
	private static String articleBase_folder = "raccoltaDati/ArticleBase/";
	private static String mentionBase_folder = "raccoltaDati/mentionBase/";

	/**
	 * 
	 * @param latch
	 * @param input_buffer
	 * @param output_buffer
	 * @param searcher
	 */
	public ConsumerBase(CountDownLatch latch, Queue<WikiArticle> input_buffer, 
			FreebaseSearcher searcher,AbstractSequenceClassifier<CoreLabel> classifier, String analysis_folder, Logger logger, Logger logger_quantitativeAnalysis, Logger logger_countMid, RedirectSearcher redirect_searcher,PrinterOutput printer_output,String ip_elasticsearch) {
		super(latch, input_buffer, searcher, classifier, analysis_folder, logger, logger_quantitativeAnalysis, logger_countMid, redirect_searcher, printer_output,ip_elasticsearch);
	}

	public int getNumberMention(WikiArticle wikiArticle){
		List<String> phrases = wikiArticle.getPhrases();
		int cont = 0;

		for (String phrase : phrases) {
			Pattern pattern = Pattern.compile(mentionRegex);
			Matcher matcher = pattern.matcher(phrase);
			while(matcher.find()){
				cont++;
			}
		}

		return cont;
	}
	@Override
	public void run() {


		WikiArticle current_article = null;
		List<String> phrases = null;
		Queue<String> logQueue = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueueOutput = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueueMid = new ConcurrentLinkedQueue<String>();

		int size_queue = 0;
		while ((current_article = input_buffer.poll()) != null){
				System.out.println(current_article.getTitle());
				String dirty_text = current_article.getText();
				extractMentions(current_article);
				
				String cleaned_text = current_article.getText();
				
				
				updateMid(current_article);
				phrases = getSentences(current_article.getText());	
				
				phrases = replaceMid(phrases, current_article.getMentions());
				current_article.setPhrases(phrases);
				
				logQueueOutput.add(getOutput(current_article));

				//aggiungo la quantità delle mention trovate in un log
				logQueue.add(current_article.getTitle()+"\t"+current_article.getMentions().size());

				logQueue.add(current_article.getTitle()+"\t"+getNumberMention(current_article));

				//conto quanti mid ci sono per frase e salvo i risultati in un log
				logQueueMid.add(countMid(current_article));
				//printArticles(current_article, dirty_text, cleaned_text);
				
				size_queue++;
				if (size_queue>=10){
					logger_quantitativeAnalysis.addResult(logQueue);
					logger_countMid.addResult(logQueueMid);
					printer_output.addResult(logQueueOutput);
					size_queue = 0;
				}

				//conto quanti mid ci sono per frase e salvo i risultati in un log
				countMid(current_article);
				
		}
		logger_quantitativeAnalysis.addResult(logQueue);
		printer_output.addResult(logQueueOutput);

		latch.countDown();
	}
	
	public void printArticles(WikiArticle current_article,String dirty_text,String cleaned_text){
		PrintWriter outArticle = null;
		PrintWriter outMentions = null;
		try {
			outArticle = new PrintWriter(new BufferedWriter(new FileWriter(analysis_folder+articleBase_folder+current_article.getTitle()+".txt", true)));
			outMentions = new PrintWriter(new BufferedWriter(new FileWriter(analysis_folder+mentionBase_folder+current_article.getTitle()+".csv", true)));
		} catch (IOException e) {
			System.out.println("errore nella creazione file di testo di analisi!");
			e.printStackTrace();
		}	
		
		printer.PrintDirtyText(outArticle, dirty_text);
		printer.PrintCleanedText(outArticle, cleaned_text);
		printer.PrintMention(outArticle, current_article);
		printer.PrintMention(outMentions, current_article);
		outArticle.close();
		outMentions.close();
	}

}
