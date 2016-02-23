package extract_new_instances;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import Printer.PrinterOutput;
import configuration.Configuration2;
import configuration.Configuration3;

public class Extract_new_instances {

	private FileReader f_fragments;
	private static BufferedReader br_fragments;
	private FileReader f_LM;
	private static BufferedReader br_LM;
	private static PrinterOutput printer_new_istances;

	public Extract_new_instances(Configuration3 config) throws FileNotFoundException{
		f_fragments = new FileReader(config.getWikipedia_unrelated_fragments());
		br_fragments = new BufferedReader(f_fragments);
		f_LM = new FileReader(config.getLanguage_model());
		br_LM = new BufferedReader(f_LM);
		printer_new_istances = new PrinterOutput(config.getNew_instances());
	}
	public static void main(String[] args) throws IOException {
		//FileReader f = new FileReader("/media/roberto/ROB/Università(Roma Tre)/TesiMagistrale/wikipedia_unrelated_fragments_with_types.tsv");
		//BufferedReader br = new BufferedReader(f);
		String config_file = "./search_new_istances/config3.properties";

		Configuration3 config = new Configuration3(config_file);
		Extract_new_instances e = new Extract_new_instances(config);
		Queue<String> logQueueOutput = new ConcurrentLinkedQueue<String>();
		ArrayList<String> LMs = new ArrayList<String>();
		String line_LM = "";
		while((((line_LM = br_LM.readLine())!=null))){
			LMs.add(line_LM);
		}

		String line_fragments = "";
		String[] splitted_line;
		String mid1 = "";
		String mid2 = "";
		String[] fragments_list;
		String types1 = "";
		String types2 = "";
		String new_line = "";

		String[] splitted_line_LM;
		while((((line_fragments = br_fragments.readLine())!=null))){

			splitted_line = line_fragments.split("\t");
			mid1 = splitted_line[0];
			types1 = splitted_line[1];
			mid2 = splitted_line[2];
			types2 = splitted_line[3];
			fragments_list = splitted_line[4].split(",");
			ArrayList<String> fragment = new ArrayList<String>(); 
			for (String f : fragments_list) {
				fragment.add(f.split(":")[1]);
			}

			for (String f : fragment) {
				for (String LM : LMs) {
					splitted_line_LM = LM.split("\t");

					if ((types1.contains(splitted_line_LM[1]))&&(types2.contains(splitted_line_LM[2]))&&(f.equals(splitted_line_LM[3]))){
					//if(f.equals(splitted_line_LM[3])){	
						logQueueOutput.add(mid1+"\t"+types1+"\t"+mid2+"\t"+types2+"\t"+splitted_line_LM[0]+"\n");
					}
				}
			}


			if (logQueueOutput.size()>100){
				printer_new_istances.addResult(logQueueOutput);
			}
		}
		printer_new_istances.addResult(logQueueOutput);


	}
}
