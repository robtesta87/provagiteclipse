package multithread;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.compress.compressors.CompressorException;

import configuration.Configuration;
import Logger.Logger;
import Printer.PrinterOutput;
import bean.WikiArticle;
import loader.Loader;

public class Producer_lucene {

	private Configuration config;
	private static int cores = 2*Runtime.getRuntime().availableProcessors()-1;
	private Logger logger;
	private Logger logger_quantitativeAnalysis;
	private Logger logger_countMid;
	private PrinterOutput printer_output;
	private Logger logger_cont_person;
	private Logger logger_cont_keyword_person;
	private PrinterOutput printer_output_such_as;
	
	
	/**
	 * 
	 * @param config
	 */
	public Producer_lucene(Configuration config){
		this.config = config;
		setLogger(new Logger(config.getLog_file()));
		setQuantitativeAnalysisBase(new Logger(config.getAnalysis_folder()+"quantitativeAnalysis.csv"));
		setCountMidFile(new Logger(config.getAnalysis_folder()+"countMid.csv"));
		setPrinterOutput(new PrinterOutput(config.getOutput_file()));
		setLogger_count_person(new Logger(config.getAnalysis_folder()+"count_person.txt"));
		setLogger_cont_keyword_person(new Logger(config.getAnalysis_folder()+"count_keyword_person.txt"));
		setPrinter_output_such_as(new PrinterOutput(config.getOutput_such_as_file()));
	}

	/**
	 * @throws InterruptedException 
	 * 
	 */
	public void process(File segmentDir) throws InterruptedException{
		// carica aricoli da sorgente esterna
		Loader loader = new Loader(segmentDir.getPath());
		System.out.println(segmentDir.getPath());
		//setPrinterOutput(new PrinterOutput("/data/rtesta/output/output_"+segmentDir.getName().substring(0, segmentDir.getName().length()-4)+".tsv"));
		//setPrinter_output_such_as(new PrinterOutput("/data/rtesta/output/output_such_as_"+segmentDir.getName().substring(0, segmentDir.getName().length()-4)+".tsv"));

		System.out.println("PRIMA FASE");
		System.out.println("Classificatore: "+config.getClassificatore_path());
		System.out.println("Analysis_folder: "+config.getAnalysis_folder());
		System.out.println("WikipediaDump_path: "+config.getWikipediaDump_path());
		System.out.println("Version: "+config.getVersion());

		// input_buffer
		Queue<WikiArticle> input_buffer = null;
		try {
			input_buffer = new ConcurrentLinkedQueue<WikiArticle>(loader.getArticles());
		} catch (FileNotFoundException | CompressorException e) {
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
												config.getRedirect_searcher(),printer_output,config.getTagger(),logger_cont_person,logger_cont_keyword_person,printer_output_such_as));
			}
			break;
		case Intermedia:
			for (int i = 0; i< threads ; i++){
				executor.submit(new ConsumerIntermedia(latch, input_buffer, config.getFreebase_searcher(),
													config.getClassifier(),config.getAnalysis_folder(),
													logger,logger_quantitativeAnalysis, logger_countMid,
													config.getRedirect_searcher(),printer_output,config.getTagger(),logger_cont_person,logger_cont_keyword_person,printer_output_such_as));
			}
			break;
		case IntermediaBis:
			for (int i = 0; i< threads ; i++){
				executor.submit(new ConsumerIntermediaBis(latch, input_buffer, config.getFreebase_searcher(),
													config.getClassifier(),config.getAnalysis_folder(),
													logger,logger_quantitativeAnalysis, logger_countMid,
													config.getRedirect_searcher(),printer_output,config.getTagger(),logger_cont_person,logger_cont_keyword_person,printer_output_such_as));
			}
			break;
		case Completa:
			for (int i = 0; i< threads ; i++){
				executor.submit(new ConsumerCompleta(latch, input_buffer, config.getFreebase_searcher(),
													config.getClassifier(),config.getAnalysis_folder(),
													logger,logger_quantitativeAnalysis, logger_countMid,
													config.getRedirect_searcher(),printer_output,config.getTagger(),logger_cont_person,logger_cont_keyword_person,printer_output_such_as));
			}
			break;
		}
		
		latch.await();
		
		Date end = new Date();
		System.out.println("Tempo di esecuzione in ms: "+(end.getTime()-start.getTime()));
		executor.shutdown();

		System.out.println("Processo finito.");
		Queue<String> logQueue = new ConcurrentLinkedQueue<String>();
		logQueue.add("segmento terminato con successo: \t"+segmentDir+"Tempo di esecuzione in ms: "+(end.getTime()-start.getTime()));
		logger.addResult(logQueue);
		
	}
	public void process() throws InterruptedException{
		// carica aricoli da sorgente esterna
		Loader loader = new Loader(config.getWikipediaDump_path());
		System.out.println("PRIMA FASE");
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
												config.getRedirect_searcher(),printer_output,config.getTagger(),logger_cont_person,logger_cont_keyword_person,printer_output_such_as));
			}
			break;
		case Intermedia:
			for (int i = 0; i< threads ; i++){
				executor.submit(new ConsumerIntermedia(latch, input_buffer, config.getFreebase_searcher(),
													config.getClassifier(),config.getAnalysis_folder(),
													logger,logger_quantitativeAnalysis, logger_countMid,
													config.getRedirect_searcher(),printer_output,config.getTagger(),logger_cont_person,logger_cont_keyword_person,printer_output_such_as));
			}
			break;
		case IntermediaBis:
			for (int i = 0; i< threads ; i++){
				executor.submit(new ConsumerIntermediaBis(latch, input_buffer, config.getFreebase_searcher(),
													config.getClassifier(),config.getAnalysis_folder(),
													logger,logger_quantitativeAnalysis, logger_countMid,
													config.getRedirect_searcher(),printer_output,config.getTagger(),logger_cont_person,logger_cont_keyword_person,printer_output_such_as));
			}
			break;
		case Completa:
			for (int i = 0; i< threads ; i++){
				executor.submit(new ConsumerCompleta(latch, input_buffer, config.getFreebase_searcher(),
													config.getClassifier(),config.getAnalysis_folder(),
													logger,logger_quantitativeAnalysis, logger_countMid,
													config.getRedirect_searcher(),printer_output,config.getTagger(),logger_cont_person,logger_cont_keyword_person,printer_output_such_as));
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
		Date start = new Date();
		String config_file = args[0];
		Configuration config = new Configuration(config_file);
		Producer_lucene producer = new Producer_lucene(config);
		/*try {
			producer.process();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		
		
		File dir = new File("/data/rtesta/parsing");
		File[] directoryListing = dir.listFiles();
		for (File segmentDir : directoryListing) {
			try {
				producer.process(segmentDir);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Date end = new Date();
		System.out.println("Tempo di esecuzione in ms: "+(end.getTime()-start.getTime()));
		
	}

	public Logger getLogger_cont_person() {
		return logger_cont_person;
	}

	public void setLogger_count_person(Logger logger_cont_person) {
		this.logger_cont_person = logger_cont_person;
	}

	public Logger getLogger_cont_keyword_person() {
		return logger_cont_keyword_person;
	}

	public void setLogger_cont_keyword_person(Logger logger_cont_keyword_person) {
		this.logger_cont_keyword_person = logger_cont_keyword_person;
	}

	public PrinterOutput getPrinter_output_such_as() {
		return printer_output_such_as;
	}

	public void setPrinter_output_such_as(PrinterOutput printer_output_such_as) {
		this.printer_output_such_as = printer_output_such_as;
	}

}
