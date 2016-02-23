package posTagger;

import FSM.RelationalFilter;
import bean.Text_pos;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagger {

	private Text_pos text_pos;

	public PosTagger(){
		this.text_pos = new Text_pos();
	}

	public Text_pos getPos(String text,MaxentTagger tagger){

		String tagged = tagger.tagString(text);
		//System.out.println(tagged);
		String[] splitted_taggedText = tagged.split(" ");
		String pos ="";
		String tokens = "";
		String single_pos ="";
		String single_token = "";
		String[] token_pos=null;
		for (int i = 0; i < splitted_taggedText.length; i++) {
			token_pos = splitted_taggedText[i].split("_");
			if (token_pos.length>1){
				single_pos = token_pos[1];
				if ((!(single_pos.equals("``")))&&(!(single_pos.equals("''")))){
					pos= pos+single_pos+"#";
					single_token = token_pos[0];
					tokens = tokens+single_token+"#";
					splitted_taggedText[i]= splitted_taggedText[i].split("_")[0];
				}
			}
		}
		text_pos.setWords(tokens.split("#"));
		text_pos.setPos(pos.split("#"));
		return text_pos;
	}


	public String cleanText(String text){
		//text = text.replaceAll("(\\s-LRB-).+?(\\s-RRB-)|(\\s-LSB-).+?(\\s-RSB-)|(\\s-LCB-).+?(\\s-RCB-)", " ");
		text = text.replaceAll("\\s(\\s)+", " ");
		//text = text.replaceAll(",", "");
		text = text.trim();
		return text;
	}

	public String getTokenText(String text,MaxentTagger tagger){
		String tagged = tagger.tagString(text);
		String[] splitted_taggedText = tagged.split(" ");
		String pos ="";
		String tokens = "";
		String single_pos ="";
		String single_token = "";
		for (int i = 0; i < splitted_taggedText.length; i++) {
			if (splitted_taggedText[i].split("_").length>1){
				single_pos = splitted_taggedText[i].split("_")[1];
				if ((!(single_pos.equals("``")))&&(!(single_pos.equals("''")))){
					pos= pos+single_pos+"#";
					single_token = splitted_taggedText[i].split("_")[0];
					tokens = tokens+single_token+"#";
					splitted_taggedText[i]= splitted_taggedText[i].split("_")[0];
				}
			}
		}
		text ="";
		for (String token : tokens.split("#")) {
			text = text + token + " ";
		}
		return text;
	}

	public static void main(String[] args) {
		RelationalFilter filter =new RelationalFilter();
		//String text = "won a best director award for american movies such as";
		String text = "album"; 
		//String text =".SIS";
		PosTagger pos_tagger = new PosTagger();
		text = pos_tagger.cleanText(text);
		MaxentTagger tagger = tagger = new MaxentTagger("/home/roberto/Scrivania/TesiMagistrale/Seconda_fase/english-left3words-distsim.tagger");
		System.out.println(pos_tagger.getPos(text, tagger));
		System.out.println(pos_tagger.getTokenText(text, tagger));
		if (filter.isRelational(pos_tagger.getPos(text, tagger).getPos())){
			System.out.println("il frammento di testo è relazionale");
		}
		else
			System.out.println("il frammento di testo NON è relazionale");
	}
}
