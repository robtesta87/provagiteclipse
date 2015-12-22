package bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import util.Pair;

/**
 * Model for a Wikipedia article. 
 * It contains all the informations about a single article.
 * @author matteo
 *
 */
public class WikiArticle{
	
	private String wikid;
	private String mid;
	private String title;
	private String text;
	
	// testo--->wikid,mid
	private TreeMap<String, Pair<String, String>> mentions;
	private Map<String, MappingObject> info_mentions;

	// lista delle frasi annotate con i mid di freebase
	private List<String> phrases;

	/**
	 * Costruttore
	 */
	public WikiArticle(String title, String wikid, String text){
		this.title = title;
		this.wikid = wikid;
		this.text = text;
		this.mentions = new TreeMap<String, Pair<String, String>>();
		this.phrases = new ArrayList<String>();
		this.info_mentions = new HashMap<String, MappingObject>();
	}
	
	/**
	 * @return the wikid
	 */
	public String getWikid() {
		return wikid;
	}

	/**
	 * @param wikid the wikid to set
	 */
	public void setWikid(String wikid) {
		this.wikid = wikid;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the mentions
	 */
	public TreeMap<String, Pair<String, String>> getMentions() {
		return mentions;
	}

	/**
	 * @return the phrases
	 */
	public List<String> getPhrases() {
		return phrases;
	}

	/**
	 * @param phrases the phrases to set
	 */
	public void setPhrases(List<String> phrases) {
		this.phrases = phrases;
	}

	
	
	/**
	 * @return the mid
	 */
	public String getMid() {
		return mid;
	}

	/**
	 * @param mid the mid to set
	 */
	public void setMid(String mid) {
		this.mid = mid;
	}

	/**
	 * @return the info_mentions
	 */
	public Map<String, MappingObject> getInfo_mentions() {
		return info_mentions;
	}

	/**
	 * @param info_mentions the info_mentions to set
	 */
	public void setInfo_mentions(Map<String, MappingObject> info_mentions) {
		this.info_mentions = info_mentions;
	}

	public void addMapping_object (String wikid, MappingObject mapping_object){
		info_mentions.put(wikid, mapping_object);
	}
	/**
	 * 
	 * @param text
	 */
	public void addMention(String text){
		String wikifiedText = text.replaceAll(" ", "_");
		Pair<String, String> pair_ids = new Pair<String,String>(wikifiedText, "null");
		this.mentions.put(text, pair_ids);
	}
	
	/**
	 * 
	 * @param text
	 * @param wikid
	 */
	public void addMention(String text, String wikid){
		Pair<String, String> pair_ids = new Pair<String,String>(wikid, "null");
		this.mentions.put(text, pair_ids);
	}
	
	

	/**
	 * 
	 * @param text
	 * @param wikid
	 * @param mid
	 */
	public void addMention(String text, String wikid, String mid){
		Pair<String, String> pair_ids = new Pair<String, String>(wikid, mid);
		this.mentions.put(text, pair_ids);
	}
	
	
	@Override
	public String toString() {
		return "WikiArticle [title=" + title + ", wikid=" + wikid + "]";
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((wikid == null) ? 0 : wikid.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WikiArticle other = (WikiArticle) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (wikid == null) {
			if (other.wikid != null)
				return false;
		} else if (!wikid.equals(other.wikid))
			return false;
		return true;
	}

	

}
