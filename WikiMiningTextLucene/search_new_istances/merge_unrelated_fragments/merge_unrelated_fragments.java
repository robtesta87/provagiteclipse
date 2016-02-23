package merge_unrelated_fragments;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.lucene.queryparser.classic.ParseException;

import Printer.PrinterOutput;

public class merge_unrelated_fragments {
	public static void main(String[] args) throws IOException, ParseException {
		String index_path = "/media/roberto/ROB/Università(Roma Tre)/TesiMagistrale/index_lucene_mapping2";
		FileReader f = new FileReader("/media/roberto/ROB/Università(Roma Tre)/TesiMagistrale/wikipedia_unrelated_fragments.tsv");
		BufferedReader br = new BufferedReader(f);
		//PrintWriter pw = new PrintWriter("/media/roberto/ROB/Università(Roma Tre)/TesiMagistrale/wikipedia_unrelated_fragments_with_types.tsv");
		Queue<String> logQueueOutput = new ConcurrentLinkedQueue<String>();
		PrinterOutput printer_output = new PrinterOutput("/media/roberto/ROB/Università(Roma Tre)/TesiMagistrale/wikipedia_unrelated_fragments_with_types.tsv");
		String line = "";
		String[] splitted_line;
		String mid1 = "";
		String mid2 = "";
		String fragment = "";
		String types1 = "";
		String types2 = "";
		String new_line = "";
		FreebaseSearcher searcher = new FreebaseSearcher(index_path);
		while((((line = br.readLine())!=null))){
			splitted_line = line.split("\t");
			if (splitted_line.length>2){
				mid1 = splitted_line[0];
				mid2 = splitted_line[1];
				fragment = splitted_line[2];
				types1 = searcher.getMapping(mid1).getTypes();
				types2 = searcher.getMapping(mid2).getTypes();
				new_line = mid1+"\t"+types1+"\t"+mid2+"\t"+types2+"\t"+fragment+"\n";
				System.out.println(line);
				logQueueOutput.add(new_line);
				if (logQueueOutput.size()>100){
					printer_output.addResult(logQueueOutput);
				}
			}
		}
		printer_output.addResult(logQueueOutput);
		//pw.close();
	}
}
