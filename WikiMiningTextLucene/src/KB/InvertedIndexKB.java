package KB;

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
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class InvertedIndexKB {

	static final String IndexPath = "util/index_KB/";
	static final String KBPath = "/home/roberto/Scrivania/TesiMagistrale/1000_annotated.tsv";

	public static void main(String[] args) throws IOException {

		FileReader f = new FileReader(KBPath);
		BufferedReader b = new BufferedReader(f);

		System.out.println("Creazione Indice inverso nella direcory: " +IndexPath + "'...");
//		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
		Analyzer analyzer = new KeywordAnalyzer();

		Directory index = FSDirectory.open(new File((IndexPath)));
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);

		IndexWriter writer = new IndexWriter(index, config);
		Date start = new Date();
		String [] fieldsRecord ;
		String mid1 = "";
		String mid2 = "";
		String predicate = "";
		String title1 = "";
		String title2 = "";
		String types1 = "";
		String types2 = "";
		
		String line = "";
		
		
		while((line=b.readLine())!=null){
			fieldsRecord = line.split("\t");
			mid1 = fieldsRecord[0];
			mid2 = fieldsRecord[1];
			predicate = fieldsRecord[2];
			title1 = fieldsRecord[3];
			title2 = fieldsRecord[4];
			types1 = fieldsRecord[5];
			types2 = fieldsRecord[6];
			
			System.out.println(mid1+" "+mid2+" "+predicate+" "+title1+" "+title2+" "+types1+" "+types2);

			//creazione del Document con i relativi campi d'interesse
			Document doc = new Document();

			/*
			String mid1_mid2 = mid1+","+mid2;
			Field coupleOfMidField = new TextField("coupleOfMid",mid1_mid2,Field.Store.YES);
			coupleOfMidField.setBoost(2.0f);
			*/
			
			
			
			Field mid1Field= new TextField("mid1",mid1,Field.Store.YES);
			mid1Field.setBoost(2.0f);
			Field mid2Field= new TextField("mid2",mid2,Field.Store.YES);
			mid2Field.setBoost(2.0f);
			
			Field predicateField = new StringField("predicate",predicate,Field.Store.YES);
			Field title1Field = new StringField("title1",title1,Field.Store.YES);
			Field title2Field = new StringField("title2",title2,Field.Store.YES);
			Field types1Field = new StringField("types1",types1,Field.Store.YES);
			Field types2Field = new StringField("types2",types2,Field.Store.YES);
			
			
			
			doc.add(mid1Field);
			doc.add(mid2Field);
//			doc.add(coupleOfMidField);
			doc.add(predicateField);
			doc.add(title1Field);
			doc.add(title2Field);
			doc.add(types1Field);
			doc.add(types2Field);

			writer.addDocument(doc);
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
