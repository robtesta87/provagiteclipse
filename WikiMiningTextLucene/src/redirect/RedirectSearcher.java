package redirect;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
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

public class RedirectSearcher {
	
	private static final String field = "redirect";
	private static Analyzer analyzer;
	private static IndexSearcher searcher;
	private static QueryParser parser;
	
	/**
	 * 
	 * @param index_path
	 * @throws IOException
	 */
	public RedirectSearcher(String index_path) throws IOException{
		searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File(index_path))));
		analyzer = new KeywordAnalyzer();
		parser = new QueryParser(Version.LUCENE_47, field, analyzer);
	}
	
	public  Pair<String, String> getRedirect(String text) throws ParseException, IOException{
		Pair<String, String> result = new Pair<String, String>(null, null);
		int maxHits = 1;
		text=text.replaceAll(" ", "_");
		if (!(text.equals(""))&&(text!=null)){
			ScoreDoc[] hits = null;
			synchronized (searcher) {
				Query query = parser.parse(QueryParser.escape(text));
				TopDocs results = searcher.search(query, maxHits);
				hits = results.scoreDocs;
			}
			if (hits.length > 0){
				int docId = hits[0].doc;
				Document d = searcher.doc(docId);
				result = new Pair<String, String>(d.get("redirect"),  d.get("wikID"));
			}
			else
				result = null;
		}else{
			System.out.println("WikiID vuoto");
			result = null;
		}
		
		
		return result;
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		RedirectSearcher rs = new RedirectSearcher("/home/roberto/Scrivania/TesiMagistrale/indici/index_redirect");
		Pair<String,String> pair = rs.getRedirect("AS Roma");
		if (pair!=null)
			System.out.println(pair.toString());
		else
			System.out.println("null");
	}
}
