package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Extract_such_as {
	public static void main(String[] args) throws IOException {
		FileReader f = new FileReader("/media/roberto/ROB/Università(Roma Tre)/TesiMagistrale/output_prima_fase/output_prima_fase(04-02-16)KeywordAnalyzer/output_such_as_file.tsv");
		BufferedReader br = new BufferedReader(f);
		PrintWriter pw = new PrintWriter("/media/roberto/ROB/Università(Roma Tre)/TesiMagistrale/output_prima_fase/output_prima_fase(04-02-16)KeywordAnalyzer/phrases_such_as.tsv");
		String line = "";
		while((((line = br.readLine())!=null))){
			String[] splitted_line = line.split("\t");
			String text = line.split("\t")[3];
			System.out.println(text);
			Pattern pattern = Pattern.compile("\\[\\[.+?\\]\\](.+?)such as\\s+(\\[\\[.+?\\]\\])");
			Matcher matcher = pattern.matcher(text);

			if(matcher.find()){
				pw.println(line);
			}
		}
		pw.close();
	}
}
