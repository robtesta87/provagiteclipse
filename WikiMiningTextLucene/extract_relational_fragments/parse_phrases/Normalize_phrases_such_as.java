package parse_phrases;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import posTagger.PosTagger;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import FSM.RelationalFilter;
import bean.Text_pos;

public class Normalize_phrases_such_as {

	
	public static void main(String[] args) throws IOException {
		Text_pos text_pos;
		MaxentTagger tagger = new MaxentTagger("/home/roberto/Scrivania/TesiMagistrale/Seconda_fase/english-left3words-distsim.tagger");
		PosTagger posTagger = new PosTagger();
		String fragment_text = "";
		RelationalFilter filter = new RelationalFilter();
		
		//estraggo le nazionalità e le salvo in una lista
		FileReader f_nat = new FileReader("./extract_relational_fragments/util/nationality.tsv");
		BufferedReader br_nat = new BufferedReader(f_nat);
		
		FileReader f_nat_without_dt = new FileReader("./extract_relational_fragments/util/nationality_without_dt.tsv");
		BufferedReader br_nat_without_dt = new BufferedReader(f_nat_without_dt);
		
		String line_nat = "";
		ArrayList<String> nationality_list = new ArrayList<String>(); 
		try {
			while((((line_nat = br_nat.readLine())!=null))){
				nationality_list.add(line_nat);
			}
		} catch (IOException e) {
			System.out.println("errore lettura file nationality.tsv");
		}
		
		line_nat = "";
		ArrayList<String> nationality_without_dt_list = new ArrayList<String>(); 
		try {
			while((((line_nat = br_nat_without_dt.readLine())!=null))){
				nationality_without_dt_list.add(line_nat);
			}
		} catch (IOException e) {
			System.out.println("errore lettura file nationality senza articolo.tsv");
		}
		
		FileReader f = new FileReader("/home/roberto/Scrivania/TesiMagistrale/Seconda_fase/log_in_file.tsv");
		BufferedReader br = new BufferedReader(f);
		PrintWriter pw = new PrintWriter("/home/roberto/Scrivania/TesiMagistrale/Seconda_fase/normalize_such_as.tsv");
		String line = "";
		int cont_such_as = 0;
		int cont_rel_such_as = 0;
		while((((line = br.readLine())!=null))){
			String[] splitted_line = line.split("\t");
			if (splitted_line.length>=4){
				String wikidArticle = splitted_line[0];
				String mid1 = splitted_line[1];
				String mid2 = splitted_line[2];
				String text = splitted_line[3];
				String id_phrase = splitted_line[4];
				text = posTagger.cleanText(text);
				text_pos = posTagger.getPos(text, tagger);
				fragment_text = text_pos.getText();
				fragment_text = fragment_text.trim();
				String[] pos = text_pos.getPos();
				String[] words = text_pos.getWords();
				
				if ((words.length)>2){
					if ((words[words.length-1].equals("as"))&&(words[words.length-2].equals("such"))){
						cont_such_as++;
						
						String normalized_text = normalize_text_such_as(pos, words);

						if (filter.isRelational(posTagger.getPos(normalized_text, tagger).getPos())){
							cont_rel_such_as++;
							normalized_text = normalized_text.replaceAll(",", "");
							
							//sostituzione delle nazionalità con la stringa "NAT"
							for (String nationality : nationality_list) {
								fragment_text = fragment_text.replaceAll("\\b"+nationality+"\\b", "NAT");
							}
							for (String nationality : nationality_without_dt_list) {
								fragment_text = fragment_text.replaceAll("\\b"+nationality+"\\b", "NAT");
							}
							
							System.out.println(wikidArticle+"\t"+mid1+"\t"+mid2+"\t"+fragment_text+"\t"+normalized_text.trim());
							//pw.println(wikidArticle+"\t"+mid1+"\t"+mid2+"\t"+fragment_text+"\t"+normalized_text.trim());
							for (String mid : getListMid(mid2, line, br,id_phrase)) {
								pw.println(wikidArticle+"\t"+mid1+"\t"+mid+"\t"+normalized_text.trim());
							}
						}

					}
				}
				//
				if ((words.length)>3){
					if ((words[words.length-2].equals("as"))&&(words[words.length-3].equals("such"))&&(pos[pos.length-1].equals("DT"))){
						cont_such_as++;
						
						String normalized_text = normalize_text_such_as_DT(pos, words);
						if (filter.isRelational(posTagger.getPos(normalized_text, tagger).getPos())){
							cont_rel_such_as++;
							normalized_text = normalized_text.replaceAll(",", "");
							
							//sostituzione delle nazionalità con la stringa "NAT"
							for (String nationality : nationality_list) {
								fragment_text = fragment_text.replaceAll("\\b"+nationality+"\\b", "NAT");
							}
							for (String nationality : nationality_without_dt_list) {
								fragment_text = fragment_text.replaceAll("\\b"+nationality+"\\b", "NAT");
							}
							
							System.out.println(wikidArticle+"\t"+mid1+"\t"+mid2+"\t"+fragment_text+"\t"+normalized_text.trim());
							//pw.println(wikidArticle+"\t"+mid1+"\t"+mid2+"\t"+fragment_text+"\t"+normalized_text.trim());
							for (String mid : getListMid(mid2, line, br,id_phrase)) {
								pw.println(wikidArticle+"\t"+mid1+"\t"+mid+"\t"+normalized_text.trim());
							}					
						}
					}

				}

			}

		}
		pw.close();
		System.out.println("numero pattern such as: "+cont_such_as);
		System.out.println("numero pattern relazionali such as"+cont_rel_such_as);
	}

	static String normalize_text_such_as(String[] pos,String[] words){

		StringBuilder text_builder = new StringBuilder();

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
		//inserire secondo filtro
		Set<String> black_list = new TreeSet<String>(Arrays.asList("any", "each", "few", "many", "much", "most", "several", "some",
				"the","my", "your", "his", "her", "its", "our", "their", "whose","this", "that", "these", "those","which" ));
		int cont_last_word=0;
		if (words.length-2-contNNS-contJJ-1>=0){
			String lastWord = words[words.length-2-contNNS-contJJ-1];
			if (black_list.contains(lastWord)){
				cont_last_word++;
			}
		}
		text_builder = new StringBuilder();
		for (int j = 0; j < words.length-2-contNNS-contJJ-cont_last_word; j++) {
			text_builder.append(words[j].toLowerCase()+" ");
		}

		return text_builder.toString();
	}
	static String normalize_text_such_as_DT(String[] pos,String[] words){

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
		//inserire secondo filtro
		Set<String> black_list = new TreeSet<String>(Arrays.asList("any", "each", "few", "many", "much", "most", "several", "some",
				"the","my", "your", "his", "her", "its", "our", "their", "whose","this", "that", "these", "those","which" ));
		int cont_last_word=0;
		if (words.length-2-contNNS-contJJ-1>=0){
			String lastWord = words[words.length-2-contNNS-contJJ-1];
			if (black_list.contains(lastWord)){
				cont_last_word++;
			}
		}
		StringBuilder text_builder = new StringBuilder();
		for (int j = 0; j < words.length-2-contNNS-contJJ-cont_last_word; j++) {
			text_builder.append(words[j].toLowerCase()+" ");
		}

		return text_builder.toString();
	}
	
	static List<String> getListMid(String mid2, String line,BufferedReader br, String id_phrase) throws IOException{
		Set<String> hs = new HashSet<>();

		hs.clear();
		hs.add(mid2);
		boolean isList=true;
		while (isList==true){
			line = br.readLine();
			if (line.split("\t").length>=4){
				String current_id_phrase = line.split("\t")[4];
				String pattern_list = line.split("\t")[3];
				//System.out.println(pattern_list);

				if ((pattern_list.equals(","))||(pattern_list.equals("and"))||(pattern_list.equals("or"))){
					if (id_phrase.equals(current_id_phrase))
						hs.add(line.split("\t")[2]);
				}
				else{
					isList=false;
				}

			}
		}
		List<String> list = new ArrayList<String>(hs);
		return list;
	}

}
