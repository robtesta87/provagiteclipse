package multithread;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.compress.compressors.CompressorException;

import Logger.Logger;
import Printer.PrinterOutput;
import bean.WikiArticle;
import configuration.Configuration;
import loader.Loader;

public class Producer {

	private Configuration config;
	private static int cores = 2*Runtime.getRuntime().availableProcessors()-1;
//	private static int cores =1;
	private Logger logger;
	private Logger logger_quantitativeAnalysis;
	private Logger logger_countMid;
	private PrinterOutput printer_output;
	private String ip_elasticsearch;
	
	/**
	 * 
	 * @param config
	 */
	public Producer(Configuration config){
		this.config = config;
		setLogger(new Logger(config.getLog_file()));
		setQuantitativeAnalysisBase(new Logger(config.getAnalysis_folder()+"quantitativeAnalysis.csv"));
		setCountMidFile(new Logger(config.getAnalysis_folder()+"countMid.csv"));
		setPrinterOutput(new PrinterOutput(config.getOutput_file()));
		this.ip_elasticsearch = config.getIp_elasticsearch();		
		
	}

	/**
	 * @throws InterruptedException 
	 * 
	 */
	public void process() throws InterruptedException{
		// carica aricoli da sorgente esterna
		Loader loader = new Loader(config.getWikipediaDump_path());
		
		System.out.println("Classificatore: "+config.getClassificatore_path());
		System.out.println("Analysis_folder: "+config.getAnalysis_folder());
		System.out.println("WikipediaDump_path: "+config.getWikipediaDump_path());
		System.out.println("Version: "+config.getVersion());

		// input_buffer
		Queue<WikiArticle> input_buffer = null;
		try {
			input_buffer = new ConcurrentLinkedQueue<WikiArticle>(loader.getArticles());
		} catch (FileNotFoundException | CompressorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// multithread architecture
		int threads = Math.min(cores, input_buffer.size());
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		CountDownLatch latch = new CountDownLatch(threads);

		System.out.println("Inizio processo.");
		
		Date start = new Date();
		// a seconda della versione, scegli il consumer adatto
		switch(config.getVersion()){
		case Base:
			for (int i = 0; i< threads ; i++){
				executor.submit(new ConsumerBase(latch, input_buffer, config.getFreebase_searcher(),
												config.getClassifier(),config.getAnalysis_folder(),
												logger,logger_quantitativeAnalysis, logger_countMid,
												config.getRedirect_searcher(),printer_output,ip_elasticsearch));
			}
			break;
		case Intermedia:
			for (int i = 0; i< threads ; i++){
				executor.submit(new ConsumerIntermedia(latch, input_buffer, config.getFreebase_searcher(),
													config.getClassifier(),config.getAnalysis_folder(),
													logger,logger_quantitativeAnalysis, logger_countMid,
													config.getRedirect_searcher(),printer_output,ip_elasticsearch));
			}
			break;
		case IntermediaBis:
			for (int i = 0; i< threads ; i++){
				executor.submit(new ConsumerIntermediaBis(latch, input_buffer, config.getFreebase_searcher(),
													config.getClassifier(),config.getAnalysis_folder(),
													logger,logger_quantitativeAnalysis, logger_countMid,
													config.getRedirect_searcher(),printer_output,ip_elasticsearch));
			}
			break;
		case Completa:
			for (int i = 0; i< threads ; i++){
				executor.submit(new ConsumerCompleta(latch, input_buffer, config.getFreebase_searcher(),
													config.getClassifier(),config.getAnalysis_folder(),
													logger,logger_quantitativeAnalysis, logger_countMid,
													config.getRedirect_searcher(),printer_output,ip_elasticsearch));
			}
			break;
		}
		
		latch.await();
		Date end = new Date();
		System.out.println("Tempo di esecuzione in ms: "+(end.getTime()-start.getTime()));
		executor.shutdown();

		System.out.println("Processo finito.");

		
	}
	

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public Logger getQuantitativeAnalysisBase() {
		return logger_quantitativeAnalysis;
	}

	public void setQuantitativeAnalysisBase(Logger quantitativeAnalysisBase) {
		this.logger_quantitativeAnalysis = quantitativeAnalysisBase;
	}

	public Logger getCountMidFile() {
		return logger_countMid;
	}

	public void setCountMidFile(Logger countMidFile) {
		this.logger_countMid = countMidFile;
	}
	
	public void setPrinterOutput(PrinterOutput printer_output) {
		this.printer_output = printer_output;
	}
	
	/**
	 * Entry point.
	 * @param args
	 */
	public static void main(String[] args){
		
		String config_file = "/home/roberto/workspace/WikiMiningText/src/util/config.properties";
//		String config_file = "/home/ubuntu/input/util/config.properties";
//		String config_file = args[0];
		
		
		Configuration config = new Configuration(config_file);
		Producer producer = new Producer(config);
		
		
		try {
			producer.process();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
