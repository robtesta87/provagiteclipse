package KB;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class PredicateSearcher {

	private static final String field1 = "mid1";
	private static final String field2 = "mid2";
	private static Analyzer analyzer;
	private static IndexSearcher searcher;
	private static QueryParser parser;

	/**
	 * 
	 * @param index_path
	 * @throws IOException 
	 */
	public PredicateSearcher(String index_path) throws IOException {
		searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File(index_path))));
		analyzer = new KeywordAnalyzer();
		String[] fields = new String[2];
		fields[0] = field1;
		fields[1] = field2;
		parser = new MultiFieldQueryParser(Version.LUCENE_47, fields, analyzer);
	}


	/**
	 * 
	 * @param mid1
	 * @param mid2
	 * @return
	 * @throws IOException
	 */
	public List<String> getPredicate (String mid1,String mid2) throws IOException{
		List<String> result = new ArrayList<String>();
		int maxHits = 5;
		ScoreDoc[] hits = null;

		Query query1 = new TermQuery(new Term("mid1", mid1));
		Query query2 = new TermQuery(new Term("mid2",mid2));

		BooleanQuery bq = new BooleanQuery();
		bq.add(query1, Occur.MUST);
		bq.add(query2, Occur.MUST);

		TopDocs results = searcher.search(bq, maxHits);
		hits = results.scoreDocs;

		for(int i=0;i<hits.length;++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			String s = (d.get("mid1")+"\t"+d.get("mid2")+"\t"+d.get("predicate")+"\t"+d.get("title1")+
					"\t"+d.get("title2")+"\t"+d.get("types1")+"\t"+d.get("types2"));
			result.add(s);
		}


		return result;
	}
	
	public static void main(String[] args) throws IOException {
		String index_path = "/home/roberto/workspace/TesiMagistraleMultiThread/util/index_KB";
		PredicateSearcher predicateSearcher = new PredicateSearcher(index_path);
		List<String> result = predicateSearcher.getPredicate("m.010_gw_", "m.01xmnx");
		
		for (String r : result) {
			System.out.println(r);
		}
		
	}
}
