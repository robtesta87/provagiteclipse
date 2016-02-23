package redirect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class InvertedIndexRedirect {
	static final String IndexPath = "util/index_redirect/";
	static final String TitleMidPath = "util/redirect.txt";

	public static void main(String[] args) throws IOException {

		FileReader f = new FileReader(TitleMidPath);
		BufferedReader b = new BufferedReader(f);

		System.out.println("Creazione Indice inverso nella direcory: " +IndexPath + "'...");
		//Analyzer analyzer = new StopAnalyzer(Version.LUCENE_47);
		Analyzer analyzer = new KeywordAnalyzer();

		Directory index = FSDirectory.open(new File((IndexPath)));
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);

		IndexWriter writer = new IndexWriter(index, config);
		Date start = new Date();
		String [] fieldsText;
		String redirect = "";
		String wikID = "";
		String line = "";
		int i=0;
		while((line=b.readLine())!=null){
			fieldsText = line.split("\t");
			redirect = fieldsText[0];
			redirect = redirect.replaceAll(" ", "_");
			wikID = fieldsText[1];
			wikID = wikID.replaceAll(" ", "_");
			System.out.println(redirect+"###"+wikID);

			//creazione del Document con i relativi campi d'interesse
			Document doc = new Document();

			Field redirectFiled = new TextField("redirect", redirect, Field.Store.YES);
			redirectFiled.setBoost(2f);

			Field wikIDField = new TextField("wikID", wikID,Field.Store.YES);
			wikIDField.setBoost(1.5f);

			doc.add(redirectFiled);
			doc.add(wikIDField);

			writer.addDocument(doc);
			i++;
		}

		writer.close();
		b.close();
		f.close();

		System.out.println("CONCLUSA. ");
		System.out.println("Creazione del dizionario in corso...");

		Date end = new Date();
		System.out.println(end.getTime() - start.getTime() + " total milliseconds");
		System.out.println("CONCLUSA");

	}
}
