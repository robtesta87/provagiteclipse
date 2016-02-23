package loader;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import bean.WikiArticle;


public class Loader {

	private String file2load;

	/**
	 * 
	 * @param file2load
	 */
	public Loader(String file2load){
		this.file2load = file2load;
	}

	/**
	 * 
	 * @param fileIn
	 * @return
	 * @throws FileNotFoundException
	 * @throws CompressorException
	 */
	public static BufferedReader getBufferedReaderForCompressedFile(String fileIn) throws FileNotFoundException, CompressorException {
		FileInputStream fin = new FileInputStream(fileIn);
		BufferedInputStream bis = new BufferedInputStream(fin);
		CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
		BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
		return br2;
	}	


	/**
	 * 
	 * @return
	 * @throws CompressorException 
	 * @throws FileNotFoundException 
	 */
	public List<WikiArticle> getArticles() throws FileNotFoundException, CompressorException{
		List<WikiArticle> articles = new ArrayList<WikiArticle>();
		int contArticles = 0;
		File dir = new File(file2load);
		File[] directoryListing = dir.listFiles();
		for (File segmentDir : directoryListing) {
			System.out.println(segmentDir.toString());
			File[] segmentDirList = segmentDir.listFiles();
			String text = null;
			BufferedReader br=null;
			String[] docs = null;
			String[] docSplitted = null;
			String[] titleText = null;
			String textWikiArticle = null;
			String titleWikiArticle = null;
			if (segmentDirList != null) {
				for (File f : segmentDirList) {
					System.out.println("Analisi del file:"+ f.getName());
					br = new BufferedReader(new FileReader(segmentDir+"/"+f.getName()));
					StringBuilder builder = new StringBuilder();
					String aux = "";
					try {
						while ((aux = br.readLine()) != null) {
							builder.append(aux+"\n");
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					text = builder.toString();
					docs = text.split("<doc id=");
					for (String doc : docs) {
						docSplitted = doc.split("title=\"");
						if (docSplitted.length>1){
							titleText = docSplitted[1].split("\">");
							titleWikiArticle = titleText[0];
							textWikiArticle = titleText[1].split("</doc>")[0].substring(titleWikiArticle.length()+2);
							if ((!(titleWikiArticle.contains("List of")))&&(textWikiArticle.length()>10)){
								WikiArticle wikiArticle = new WikiArticle(titleWikiArticle,titleWikiArticle.replaceAll(" ","_"),textWikiArticle);
								articles.add(wikiArticle);
								contArticles++;
							}
						}
					}
				}
			}
			System.out.println("Articoli caricati nel buffer:\t"+contArticles);
		}
		return articles;
	}

	public List<WikiArticle> getArticlesGIW() throws FileNotFoundException, CompressorException{
		List<WikiArticle> articles = new ArrayList<WikiArticle>();
		int contArticles = 0;
		File dir = new File(file2load);
		File[] directoryListing = dir.listFiles();
		for (File segmentDir : directoryListing) {
			System.out.println(segmentDir.toString());
			File[] segmentDirList = segmentDir.listFiles();
			String text = null;
			BufferedReader br=null;
			String[] docs = null;
			String[] docSplitted = null;
			String[] titleText = null;
			String textWikiArticle = null;
			String titleWikiArticle = null;
			if (segmentDirList != null) {
				for (File f : segmentDirList) {
					System.out.println("Analisi del file:"+ f.getName());
					br = getBufferedReaderForCompressedFile(segmentDir+"/"+f.getName());
					StringBuilder builder = new StringBuilder();
					String aux = "";
					try {
						while ((aux = br.readLine()) != null) {
							if (!(aux.startsWith("==")))
								builder.append(aux+"\n");
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					text = builder.toString();
					docs = text.split("<doc");
					for (String doc : docs) {
						docSplitted = doc.split("title=");
						if (docSplitted.length>1){
							titleWikiArticle = docSplitted[1].split("\n")[0].replaceAll("_", " ");
							textWikiArticle = docSplitted[1].split("</doc>")[0].substring(titleWikiArticle.length()+2);
							WikiArticle wikiArticle = new WikiArticle(titleWikiArticle,titleWikiArticle.replaceAll(" ","_"),textWikiArticle);
							articles.add(wikiArticle);
							contArticles++;

						}
					}
				}
			}
			System.out.println("Articoli caricati nel buffer:\t"+contArticles);
		}
		return articles;
	}



}
