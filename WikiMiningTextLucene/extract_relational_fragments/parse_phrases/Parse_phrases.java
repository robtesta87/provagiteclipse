package parse_phrases;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import configuration.Configuration2;
import logger.Logger;
import bean.Text_pos;
import FSM.RelationalFilter;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import posTagger.PosTagger;

public class Parse_phrases {
	private static MaxentTagger tagger;
	static RelationalFilter filter;
	private FileReader f;
	private static BufferedReader br;
	private static Text_pos text_pos;
	private Logger logger_in;
	private Logger logger_out;
	private Logger logger;
	private PosTagger posTagger;
	private ArrayList<String> nationality_list;
	private ArrayList<String> nationality_without_dt_list;

	public Parse_phrases(Configuration2 config) throws FileNotFoundException {
		logger_in= new Logger(config.getLog_in_file());
		logger_out = new Logger(config.getLog_out_file());
		logger = new Logger(config.getLog_file());
		tagger = new MaxentTagger(config.getPath_tagger());
		posTagger = new PosTagger();
		filter = new RelationalFilter();
		f = new FileReader(config.getPath_input());
		br = new BufferedReader(f);

		FileReader f_nat = new FileReader("./extract_relational_fragments/util/nationality.tsv");
		//FileReader f_nat = new FileReader("/home/rtesta/WikiMiningText/nationality.tsv");
		BufferedReader br_nat = new BufferedReader(f_nat);

		FileReader f_nat_without_dt = new FileReader("./extract_relational_fragments/util/nationality_without_dt.tsv");
		//FileReader f_nat_without_dt = new FileReader("/home/rtesta/WikiMiningText/nationality_without_dt.tsv");
		BufferedReader br_nat_without_dt = new BufferedReader(f_nat_without_dt);

		String line = "";
		nationality_list = new ArrayList<String>(); 
		try {
			while((((line = br_nat.readLine())!=null))){
				nationality_list.add(line);
			}
		} catch (IOException e) {
			System.out.println("errore lettura file nationality.tsv");
		}

		line = "";
		nationality_without_dt_list = new ArrayList<String>(); 
		try {
			while((((line = br_nat_without_dt.readLine())!=null))){
				nationality_without_dt_list.add(line);
			}
		} catch (IOException e) {
			System.out.println("errore lettura file nationality senza articolo.tsv");
		}
	}

	public void process() throws NumberFormatException, IOException{
		Date start = new Date();
		Queue<String> logQueue_in = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueue_out = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueue = new ConcurrentLinkedQueue<String>();
		String line = "";
		while(((line = br.readLine())!=null)){
			String[] splitted_line = line.split("\t");
			String wikidArticle = splitted_line[0];
			try {
				if(splitted_line.length>2){
					if (Integer.parseInt(line.split("\t")[2])>1){
						String text = line.split("\t")[3];
						String[] splitted_text = text.split("\\]\\]");
						String mid1 ="";
						String mid2 ="";
						String pattern ="";
						String fragment_text = "";
						String last_word = "";
						for (int i = 0; i < splitted_text.length; i++) {
							mid1="";
							mid2="";
							pattern ="";
							if (splitted_text[i].contains("|")){
								mid1 = splitted_text[i].split("\\|")[1];
								if ((i+1)<splitted_text.length){
									pattern = splitted_text[i+1].split("\\[\\[")[0];
									if  (splitted_text[i+1].contains("[[")){
										mid2 = splitted_text[i+1].split("\\[\\[")[1].split("\\|")[1];
									}
								}
							}
							if ((!mid1.equals(""))&&(!mid2.equals(""))&&(!pattern.equals(" "))&&(!mid1.equals("null"))&&(!mid2.equals("null"))){
								pattern = posTagger.cleanText(pattern);
								if (!(pattern.contains("such as"))){
									text_pos = posTagger.getPos(pattern, tagger);
									fragment_text = text_pos.getText();
									fragment_text = fragment_text.trim();
									if (filter.isRelational(text_pos.getPos())){
										System.out.println(text_pos.toString());

										fragment_text = fragment_text.replaceAll(",", "");

										//sostituzione delle nazionalità con la stringa "NAT"
										for (String nationality : nationality_list) {
											fragment_text = fragment_text.replaceAll("\\b"+nationality+"\\b", "NAT");
										}
										for (String nationality : nationality_without_dt_list) {
											fragment_text = fragment_text.replaceAll("\\b"+nationality+"\\b", "NAT");
										}
										last_word = fragment_text.substring(fragment_text.lastIndexOf(" ")+1);
										if (!(last_word.equals("that")))
											logQueue_in.add(wikidArticle+"\t"+mid1+"\t"+mid2+"\t"+fragment_text);
										else
											logQueue_out.add(wikidArticle+"\t"+mid1+"\t"+mid2+"\t"+fragment_text);
									}

									else{
										logQueue_out.add(wikidArticle+"\t"+mid1+"\t"+mid2+"\t"+fragment_text);
									}
								}
							}

						}
					}
				}
				if (logQueue_out.size()>100)
					logger_in.addResult(logQueue_in);
				if (logQueue_out.size()>100)
					logger_out.addResult(logQueue_out);
			} catch (ArrayIndexOutOfBoundsException exception) {
				logQueue.add("errore ArrayIndexOutOfBoundsException\tWikidArticle: "+wikidArticle+"\t"+"text: "+splitted_line[3]);
				logger.addResult(logQueue);
			}
			logger.addResult(logQueue);
		}

		logger_in.addResult(logQueue_in);
		logger_out.addResult(logQueue_out);
		Date end = new Date();
		logQueue.add("Tempo di esecuzione in ms: "+(end.getTime()-start.getTime()));
		logger.addResult(logQueue);
		System.out.println("Tempo di esecuzione in ms: "+(end.getTime()-start.getTime()));
		System.out.println("Processo finito.");

	}


	public void process(File segment) throws NumberFormatException, IOException{
		FileReader f;
		BufferedReader br;
		System.out.println(segment.getPath());
		f = new FileReader(segment.getPath());
		br = new BufferedReader(f);
		Date start = new Date();
		Logger logger_in= new Logger("/data/rtesta/pattern_in_out/in_"+segment.getName());
		Logger logger_out = new Logger("/data/rtesta/pattern_in_out/out_"+segment.getName());
		Queue<String> logQueue_in = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueue_out = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueue = new ConcurrentLinkedQueue<String>();
		String line = "";
		while(((line = br.readLine())!=null)){
			String[] splitted_line = line.split("\t");
			String wikidArticle = splitted_line[0];
			try {
				if(splitted_line.length>2){
					if (Integer.parseInt(line.split("\t")[2])>1){
						String text = line.split("\t")[3];
						String[] splitted_text = text.split("\\]\\]");
						String mid1 ="";
						String mid2 ="";
						String pattern ="";
						String fragment_text = "";
						String last_word = "";
						for (int i = 0; i < splitted_text.length; i++) {
							mid1="";
							mid2="";
							pattern ="";
							if (splitted_text[i].contains("|")){
								mid1 = splitted_text[i].split("\\|")[1];
								if ((i+1)<splitted_text.length){
									pattern = splitted_text[i+1].split("\\[\\[")[0];
									if  (splitted_text[i+1].contains("[[")){
										mid2 = splitted_text[i+1].split("\\[\\[")[1].split("\\|")[1];
									}
								}
							}
							if ((!mid1.equals(""))&&(!mid2.equals(""))&&(!pattern.equals(" "))&&(!mid1.equals("null"))&&(!mid2.equals("null"))){
								pattern = posTagger.cleanText(pattern);

								text_pos = posTagger.getPos(pattern, tagger);
								fragment_text = text_pos.getText();
								fragment_text = fragment_text.trim();
								if (filter.isRelational(text_pos.getPos())){
									fragment_text = fragment_text.replaceAll(",", "");
									System.out.println(text_pos.toString());

									//sostituzione delle nazionalità con la stringa "NAT"
									for (String nationality : nationality_list) {
										fragment_text = fragment_text.replaceAll("\\b"+nationality+"\\b", "NAT");
									}
									for (String nationality : nationality_without_dt_list) {
										fragment_text = fragment_text.replaceAll("\\b"+nationality+"\\b", "NAT");
									}

									last_word = fragment_text.substring(fragment_text.lastIndexOf(" ")+1);
									if (!(last_word.equals("that")))
										logQueue_in.add(wikidArticle+"\t"+mid1+"\t"+mid2+"\t"+fragment_text);
								}
								else{
									logQueue_out.add(wikidArticle+"\t"+mid1+"\t"+mid2+"\t"+fragment_text);
								}

							}

						}
					}
				}
				if (logQueue_out.size()>100)
					logger_in.addResult(logQueue_in);
				if (logQueue_out.size()>100)
					logger_out.addResult(logQueue_out);
			} catch (ArrayIndexOutOfBoundsException exception) {
				logQueue.add("errore ArrayIndexOutOfBoundsException\tWikidArticle: "+wikidArticle+"\t"+"text: "+splitted_line[3]);
				logger.addResult(logQueue);
			}
			logger.addResult(logQueue);
		}

		logger_in.addResult(logQueue_in);
		logger_out.addResult(logQueue_out);
		Date end = new Date();
		logQueue.add(segment.getName()+" concluso correttamente in tempo: "+(end.getTime()-start.getTime()));
		logger.addResult(logQueue);
		System.out.println("Tempo di esecuzione in ms: "+(end.getTime()-start.getTime()));
		System.out.println("Processo finito.");


	}
	public static void main(String[] args) throws NumberFormatException, IOException {
		Date start = new Date();
		String config_file = args[0];

		Configuration2 config = new Configuration2(config_file);
		Parse_phrases parse_phrases = new Parse_phrases(config);

		parse_phrases.process();

		/*
		File dir = new File("/data/rtesta/output");
		File[] directoryListing = dir.listFiles();

		for (File segment : directoryListing) {
			parse_phrases.process(segment);
		}
		 */
		Date end = new Date();
		System.out.println("Tempo di esecuzione in ms: "+(end.getTime()-start.getTime()));
		System.out.println(config.getLog_in_file());

	}
}
