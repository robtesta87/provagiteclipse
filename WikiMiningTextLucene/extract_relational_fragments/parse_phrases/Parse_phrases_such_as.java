package parse_phrases;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import logger.Logger;
import posTagger.PosTagger;
import FSM.RelationalFilter;
import bean.Text_pos;
import configuration.Configuration2;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Parse_phrases_such_as {
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

	public Parse_phrases_such_as(Configuration2 config) throws FileNotFoundException {

		logger_in= new Logger(config.getLog_in_file());
		logger_out = new Logger(config.getLog_out_file());
		logger = new Logger(config.getLog_file());
		tagger = new MaxentTagger(config.getPath_tagger());
		posTagger = new PosTagger();
		filter = new RelationalFilter();
		//f = new FileReader(config.getPath_input());
		f = new FileReader("/media/roberto/ROB/Università(Roma Tre)/TesiMagistrale/output_prima_fase/output_prima_fase(04-02-16)KeywordAnalyzer/output_such_as_file.tsv");
		br = new BufferedReader(f);
		
	}

	public void process() throws NumberFormatException, IOException{
		Date start = new Date();
		Queue<String> logQueue_in = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueue_out = new ConcurrentLinkedQueue<String>();
		Queue<String> logQueue = new ConcurrentLinkedQueue<String>();
		String line = "";
		int cont_phrase = 0;
		while(((line = br.readLine())!=null)){
			String[] splitted_line = line.split("\t");
			String wikidArticle = splitted_line[0];
			cont_phrase++;
			try {
				if(splitted_line.length>2){
					if (Integer.parseInt(line.split("\t")[2])>1){
						String text = line.split("\t")[3];
						String[] splitted_text = text.split("\\]\\]");
						String mid1 ="";
						String mid2 ="";
						String pattern ="";
						String fragment_text = "";
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
								System.out.println(pattern);
								text_pos = posTagger.getPos(pattern, tagger);
								fragment_text = text_pos.getText();
								fragment_text = fragment_text.trim();
								String[] pos = text_pos.getPos();
								String[] words = text_pos.getWords();
								/*
								if ((words[words.length-1].equals("as"))&&(words[words.length-2].equals("such"))){
									int contNNS= 0;
									for (int j = words.length-3; j >= 0; j--) {
										if ((pos[j].equals("NNS"))||(pos[j].equals("NN")))
											contNNS++;
									}
									int contJJ = 0;
									for (int j = words.length-3-contNNS; j >= 0; j--) {
										if (pos[j].equals("JJ"))
											contJJ++;
									}
									StringBuilder text_builder = new StringBuilder();
									for (int j = 0; j < words.length-2-contNNS-contJJ; j++) {
										text_builder.append(words[j].toLowerCase()+" ");
									}
									if (filter.isRelational(posTagger.getPos(text_builder.toString(), tagger).getPos())){
										logQueue_in.add(wikidArticle+"\t"+mid1+"\t"+mid2+"\t"+fragment_text+"\t"+text_builder.toString());
									}
									
								}
								if ((words[words.length-2].equals("as"))&&(words[words.length-3].equals("such"))&&(pos[pos.length-1].equals("DT"))){
									int contNNS= 0;
									for (int j = words.length-4; j >= 0; j--) {
										if ((pos[j].equals("NNS"))||(pos[j].equals("NN")))
											contNNS++;
									}
									int contJJ = 0;
									for (int j = words.length-4-contNNS; j >= 0; j--) {
										if (pos[j].equals("JJ"))
											contJJ++;
									}
									StringBuilder text_builder = new StringBuilder();
									for (int j = 0; j < words.length-3-contNNS-contJJ; j++) {
										text_builder.append(words[j].toLowerCase()+" ");
									}
									if (filter.isRelational(posTagger.getPos(text_builder.toString(), tagger).getPos())){
										logQueue_in.add(wikidArticle+"\t"+mid1+"\t"+mid2+"\t"+fragment_text+"\t"+text_builder.toString());
									}
								}*/
								logQueue_in.add(wikidArticle+"\t"+mid1+"\t"+mid2+"\t"+fragment_text+"\t"+cont_phrase);
							}

						}
					}
				}
				if (logQueue_in.size()>100)
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
	public static void main(String[] args) throws NumberFormatException, IOException {
		Date start = new Date();
		String config_file = "/home/roberto/Scrivania/TesiMagistrale/Seconda_fase/config.properties";

		Configuration2 config = new Configuration2(config_file);
		Parse_phrases_such_as parse_phrases_such_as = new Parse_phrases_such_as(config);

		parse_phrases_such_as.process();

		Date end = new Date();
		System.out.println("Tempo di esecuzione in ms: "+(end.getTime()-start.getTime()));

	}
}
