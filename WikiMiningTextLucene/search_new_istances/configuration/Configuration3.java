package configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Configuration3 {
	String wikipedia_unrelated_fragments = null;
	String language_model = null;
	String new_instances = null;
		
	public Configuration3(String configFilePath){
		Properties props = null;
		
		try {
			InputStream file = new FileInputStream(new File(configFilePath)) ;
			props = new Properties();
			props.load(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wikipedia_unrelated_fragments = props.getProperty("wikipedia_unrelated_fragments").toString();
		language_model = props.getProperty("language_model").toString();
		new_instances = props.getProperty("new_instances").toString();
	}

	/**
	 * @return the wikipedia_unrelated_fragments
	 */
	public String getWikipedia_unrelated_fragments() {
		return wikipedia_unrelated_fragments;
	}

	/**
	 * @param wikipedia_unrelated_fragments the wikipedia_unrelated_fragments to set
	 */
	public void setWikipedia_unrelated_fragments(
			String wikipedia_unrelated_fragments) {
		this.wikipedia_unrelated_fragments = wikipedia_unrelated_fragments;
	}

	/**
	 * @return the language_model
	 */
	public String getLanguage_model() {
		return language_model;
	}

	/**
	 * @param language_model the language_model to set
	 */
	public void setLanguage_model(String language_model) {
		this.language_model = language_model;
	}

	/**
	 * @return the new_instances
	 */
	public String getNew_instances() {
		return new_instances;
	}

	/**
	 * @param new_instances the new_instances to set
	 */
	public void setNew_instances(String new_instances) {
		this.new_instances = new_instances;
	}

	
	
	
}
