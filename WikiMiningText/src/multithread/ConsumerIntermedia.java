package multithread;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import redirect.RedirectSearcher;
import Logger.Logger;
import Printer.PrinterOutput;
import bean.WikiArticle;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import freebase.FreebaseSearcher;

class ConsumerIntermedia extends Consumer {
	private static String articleIntermedia_folder = "raccoltaDati/ArticleIntermedia/";
	private static String mentionIntermedia_folder = "raccoltaDati/mentionIntermedia/";


	public ConsumerIntermedia(CountDownLatch latch,
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
		Map<String,List<String>> entitiesMap = null;
		Queue<String> logQueue = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueueMid = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueueOutput = new ConcurrentLinkedQueue<String>();


		int size_queue = 0;

		while ((current_article = input_buffer.poll()) != null){
			cont_mention=0;
			System.out.println(current_article.getTitle());

			String dirty_text = current_article.getText();

			extractMentions(current_article);

			String cleaned_text = current_article.getText();

			int cont_original_mention =current_article.getMentions().size();	//contatore delle mention originali

			updateMid(current_article);
			phrases = getSentences(current_article.getText());
			if (!(phrases.isEmpty())){
				entitiesMap = getEntities(phrases);

				addPerson(entitiesMap.get("PERSON"), current_article);

				//controllo se nelle entità ci sono dei match esatti nelle mention originali per il controllo quantitativo
				countMentions(entitiesMap.get("ORGANIZATION"), current_article);
				countMentions(entitiesMap.get("MISC"), current_article);
				countMentions(entitiesMap.get("LOCATION"), current_article);
				phrases_mid = replaceMid(phrases, current_article.getMentions());
				current_article.setPhrases(phrases_mid);
				logQueueOutput.add(getOutput(current_article));

				//aggiungo la quantità delle mention trovate in un log
				cont_mention = cont_mention + cont_original_mention;
				logQueue.add(current_article.getTitle()+"\t"+cont_original_mention+"\t"+cont_mention);
				size_queue++;

				//conto quanti mid ci sono per frase e salvo i risultati in un log
				//logQueueMid.add(countMid(current_article));


				printArticles(current_article, dirty_text, cleaned_text,phrases);

				//scrivo i risultati delle analisi nei file di log
				if (size_queue>=10){
					logger_quantitativeAnalysis.addResult(logQueue);
					logger_countMid.addResult(logQueueMid);
					printer_output.addResult(logQueueOutput);
					size_queue = 0;
				}
			}

		}

		logger_quantitativeAnalysis.addResult(logQueue);
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
		outArticle.close();
		outMentions.close();
	}

}
