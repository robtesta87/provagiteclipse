package freebase;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import util.Pair;



public class FreebaseSearcher {

	private static final String field = "title";
	private static StandardAnalyzer analyzer;
	private static IndexSearcher searcher;
	private static QueryParser parser;

	/**
	 * 
	 * @param index_path
	 * @throws IOException 
	 */
	public FreebaseSearcher(String index_path) throws IOException{
		searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File(index_path))));
		analyzer = new StandardAnalyzer(Version.LUCENE_47);
		parser = new QueryParser(Version.LUCENE_47, field, analyzer);
	}

	/**
	 * 
	 * @param wikid
	 * @return
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public  Pair<String, String> getMid(String wikid) throws ParseException, IOException{
		Pair<String, String> result = new Pair<String, String>(null, null);
		int maxHits = 1;
		if (!(wikid.equals(""))&&(wikid!=null)){
			ScoreDoc[] hits = null;
			synchronized (searcher) {
				Query query = parser.parse(QueryParser.escape(wikid));
				TopDocs results = searcher.search(query, maxHits);
				hits = results.scoreDocs;
			}
			if (hits.length > 0){
				int docId = hits[0].doc;
				Document d = searcher.doc(docId);
				result = new Pair<String, String>(d.get("title"),  d.get("mid"));
			}
			else
				result = null;
		}else{
			System.out.println("WikiID vuoto");
			result = null;
		}
		return result;
	}


}
