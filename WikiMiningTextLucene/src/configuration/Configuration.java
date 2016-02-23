package configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import redirect.RedirectSearcher;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import freebase.FreebaseSearcher;
import multithread.Version;

public class Configuration {

	String classificatore_path = null;
	String wikipediaDump_path = null;

	//String quantitativeanalysis_path = null;
	String analysis_folder = null;

	Version version = null;
	FreebaseSearcher freebase_searcher = null;
	AbstractSequenceClassifier<CoreLabel> classifier = null;
	RedirectSearcher redirect_searcher = null;
	
	String log_file = null;
	String output_file = null;
	String output_such_as_file = null;
	String path_tagger = null;
	MaxentTagger tagger = null;

	public Configuration(String configFilePath){
		Properties props = null;
		
		try {
			InputStream file = new FileInputStream(new File(configFilePath)) ;
			props = new Properties();
			props.load(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		classificatore_path = props.getProperty("classificatore").toString();
		wikipediaDump_path = props.getProperty("articles_path").toString();
		analysis_folder = props.getProperty("analysis_folder").toString();
		version = readVersion(props);
		freebase_searcher = createSearcher(props.getProperty("freebase_index").toString());
		classifier = createClassifier();
		log_file = props.getProperty("log_file").toString();
		redirect_searcher = createRedirectSearcher(props.getProperty("redirect_index").toString());
		output_file = props.getProperty("output_file").toString();
		output_such_as_file = props.getProperty("output_such_as_file").toString();
		path_tagger = props.getProperty("path_tagger").toString();
		tagger = createTagger();
	}


	/**
	 * @return the version
	 */
	public Version getVersion() {
		return version;
	}


	/**
	 * @return the freebase_searcher
	 */
	public FreebaseSearcher getFreebase_searcher() {
		return freebase_searcher;
	}


	/**
	 * read the versione from the config file and creates version enum.
	 * @param props
	 * @return
	 */
	public Version readVersion(Properties props){
		String version_name = props.getProperty("version").toString();
		Version version2use = null;
		switch(version_name){
		case "Base":
			version2use = Version.Base;
			break;
		case "Intermedia":
			version2use = Version.Intermedia;
			break;
		case "IntermediaBis":
			version2use = Version.IntermediaBis;
			break;
		case "Completa":
			version2use = Version.Completa;
			break;
		default:
			System.out.println("Versione errata nel config file.");
			break;
		}
		return version2use;
	}
	
	/**
	 * 
	 * @param indexPath
	 * @return
	 */
	public FreebaseSearcher createSearcher(String indexPath){
		FreebaseSearcher freebaseSearcher = null;
		try {
			freebaseSearcher = new FreebaseSearcher(indexPath);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Index path errato nel config file.");
		}
		return freebaseSearcher;
		
	}
	
	/**
	 * 
	 * @param indexPath
	 * @return
	 */
	public RedirectSearcher createRedirectSearcher(String indexPath){
		RedirectSearcher redirectSearcher = null;
		try {
			redirectSearcher  = new RedirectSearcher(indexPath);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Index redirect path errato nel config file.");
		}
		
		return redirectSearcher;
	}
	
	public AbstractSequenceClassifier<CoreLabel> createClassifier (){
		AbstractSequenceClassifier<CoreLabel> classifier = null;
		try {
			classifier = CRFClassifier.getClassifier(classificatore_path);
		} catch (ClassCastException | ClassNotFoundException | IOException e1) {
			e1.printStackTrace();
		}
		return classifier;
	}
	public MaxentTagger createTagger (){
		MaxentTagger tagger = new MaxentTagger(path_tagger);
		return tagger;
	}


	/**
	 * @return the classificatore_path
	 */
	public String getClassificatore_path() {
		return classificatore_path;
	}



	/**
	 * @param classificatore_path the classificatore_path to set
	 */
	public void setClassificatore_path(String classificatore_path) {
		this.classificatore_path = classificatore_path;
	}



	/**
	 * @return the classifier
	 */
	public AbstractSequenceClassifier<CoreLabel> getClassifier() {
		return classifier;
	}



	/**
	 * @return the analysis_folder
	 */
	public String getAnalysis_folder() {
		return analysis_folder;
	}



	/**
	 * @param analysis_folder the analysis_folder to set
	 */
	public void setAnalysis_folder(String analysis_folder) {
		this.analysis_folder = analysis_folder;
	}



	/**
	 * @param freebase_searcher the freebase_searcher to set
	 */
	public void setFreebase_searcher(FreebaseSearcher freebase_searcher) {
		this.freebase_searcher = freebase_searcher;
	}
	
	/**
	 * @return the wikipediaDump_path
	 */
	public String getWikipediaDump_path() {
		return wikipediaDump_path;
	}


	/**
	 * @return the log_file
	 */
	public String getLog_file() {
		return log_file;
	}


	/**
	 * @return the redirect_searcher
	 */
	public RedirectSearcher getRedirect_searcher() {
		return redirect_searcher;
	}


	/**
	 * @param redirect_searcher the redirect_searcher to set
	 */
	public void setRedirect_searcher(RedirectSearcher redirect_searcher) {
		this.redirect_searcher = redirect_searcher;
	}


	/**
	 * @return the output_file
	 */
	public String getOutput_file() {
		return output_file;
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
	 * @return the tagger
	 */
	public MaxentTagger getTagger() {
		return tagger;
	}


	/**
	 * @return the output_such_as_file
	 */
	public String getOutput_such_as_file() {
		return output_such_as_file;
	}
	
	

	
}
