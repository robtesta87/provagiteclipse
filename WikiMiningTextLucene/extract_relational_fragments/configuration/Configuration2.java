package configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Configuration2 {
	String path_tagger = null;
	String path_input = null;
	String log_in_file = null;
	String log_out_file = null;
	String log_file = null;
	
	public Configuration2(String configFilePath){
		Properties props = null;
		
		try {
			InputStream file = new FileInputStream(new File(configFilePath)) ;
			props = new Properties();
			props.load(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		path_tagger = props.getProperty("path_tagger").toString();
		path_input = props.getProperty("path_input").toString();
		log_in_file = props.getProperty("log_in_file").toString();
		log_out_file = props.getProperty("log_out_file").toString();
		log_file = props.getProperty("log_file").toString();
	}

	/**
	 * @return the path_tagger
	 */
	public String getPath_tagger() {
		return path_tagger;
	}

	/**
	 * @param path_tagger the path_tagger to set
	 */
	public void setPath_tagger(String path_tagger) {
		this.path_tagger = path_tagger;
	}

	/**
	 * @return the path_input
	 */
	public String getPath_input() {
		return path_input;
	}

	/**
	 * @param path_input the path_input to set
	 */
	public void setPath_input(String path_input) {
		this.path_input = path_input;
	}

	/**
	 * @return the log_in_file
	 */
	public String getLog_in_file() {
		return log_in_file;
	}

	/**
	 * @param log_in_file the log_in_file to set
	 */
	public void setLog_in_file(String log_in_file) {
		this.log_in_file = log_in_file;
	}

	/**
	 * @return the log_out_file
	 */
	public String getLog_out_file() {
		return log_out_file;
	}

	/**
	 * @param log_out_file the log_out_file to set
	 */
	public void setLog_out_file(String log_out_file) {
		this.log_out_file = log_out_file;
	}

	/**
	 * @return the log_file
	 */
	public String getLog_file() {
		return log_file;
	}

	/**
	 * @param log_file the log_file to set
	 */
	public void setLog_file(String log_file) {
		this.log_file = log_file;
	}
	
	
}
