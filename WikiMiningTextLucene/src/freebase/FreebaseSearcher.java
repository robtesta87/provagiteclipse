package freebase;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
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

import bean.MappingObject;
import util.Pair;



public class FreebaseSearcher {

	private static final String field = "wikid";
	private static KeywordAnalyzer analyzer;
	private static IndexSearcher searcher;
	private static QueryParser parser;

	/**
	 * 
	 * @param index_path
	 * @throws IOException 
	 */
	public FreebaseSearcher(String index_path) throws IOException{
		searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File(index_path))));
		analyzer = new KeywordAnalyzer();
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
				result = new Pair<String, String>(d.get("wikid"),  d.get("mid"));
			}
			else
				result = null;
		}else{
			System.out.println("WikiID vuoto");
			result = null;
		}
		return result;
	}

	/**
	 * 
	 * @param wikid
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public MappingObject getMapping (String wikid) throws ParseException, IOException{
		int maxHits = 1;
		MappingObject result;
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
				result = new MappingObject(wikid, d.get("mid"),d.get("types"), d.get("keywords"));
			}
			else
				result = new MappingObject("null", "null","null","null");
		}else{
			System.out.println("WikiID vuoto");
			result = new MappingObject("null", "null","null","null");
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		String index_path = "/media/roberto/ROB/Università(Roma Tre)/TesiMagistrale/index_lucene_mapping2";
		String wikid = ".paris";
		FreebaseSearcher searcher = new FreebaseSearcher(index_path);
		System.out.println(searcher.getMapping(wikid).getMid());
		System.out.println(searcher.getMapping(wikid).getTypes());
		System.out.println(searcher.getMapping(wikid).getKeywords());
		System.out.println(searcher.getMapping(wikid).getWikid());
	}

}
