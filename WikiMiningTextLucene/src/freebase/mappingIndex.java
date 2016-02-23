package freebase;

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

public class mappingIndex {
	static final String IndexPath = "/media/roberto/ROB/Università(Roma Tre)/TesiMagistrale/index_lucene_mapping2";
//	static final String TitleMidPath = "/home/roberto/Scrivania/TesiMagistrale/mapping1000.txt";
	static final String TitleMidPath = "/media/roberto/ROB/Università(Roma Tre)/TesiMagistrale/mapping wikid mid (types e keywords)/mapping.tsv";

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
		String wikid = "";
		String mid = "";
		String types = "";
		String keywords = "";

		String line = "";
		int i=0;
		while((line=b.readLine())!=null){
			//line=b.readLine();
			fieldsText = line.split("\t");
			wikid = fieldsText[0];
			mid = fieldsText[1];
			types = fieldsText[2];
			keywords = fieldsText[3];
			System.out.println(wikid+" "+mid);

			//creazione del Document con i relativi campi d'interesse
			Document doc = new Document();

			Field wikidField = new TextField("wikid", wikid, Field.Store.YES);
			wikidField.setBoost(2f);

			Field midField = new TextField("mid", mid,Field.Store.YES);
			midField.setBoost(1.5f);
			
			Field typesField = new TextField("types", types,Field.Store.YES);
			typesField.setBoost(1.5f);
			
			Field keywordsField = new TextField("keywords", keywords,Field.Store.YES);
			keywordsField.setBoost(1.5f);

			doc.add(wikidField);
			doc.add(midField);
			doc.add(typesField);
			doc.add(keywordsField);

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
