package freebase;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import bean.MappingObject;

public class FreebaseSearcherES {
	private final static String index_name = "index_mapping_wikid_mid";
	private final static String index_type = "wikid_mid";
	QueryBuilder queryBuilder;
	SearchResponse response;
	SearchHit[] results;
	String mid;
	String types;
	String keywords;
	String result;
	MappingObject mapping_object;
	
	public FreebaseSearcherES() {
		queryBuilder=null;
		response = null;
		results = null;
		
	}

	public String getMid (String wikid,Client client){
		queryBuilder = QueryBuilders.matchQuery("wikid",wikid);

		response = client.prepareSearch(index_name)
				.setTypes(index_type)
				.setSearchType(SearchType.DEFAULT)
				.setQuery(queryBuilder)
				.execute().actionGet();

		results = response.getHits().getHits();
		mid="";
		if(results.length > 0) {
			mid = (String)results[0].getSource().get("mid");
			types = (String)results[0].getSource().get("types");
			keywords = (String)results[0].getSource().get("keywords");
		}
		else{
			mid = "null";
			types = "null";
			keywords ="null";
		}
		//result = mid+"\t"+types+"\t"+keywords;
		result = mid;
		return result;
	}
	
	public MappingObject getMapping (String wikid,Client client){
		queryBuilder = QueryBuilders.matchQuery("wikid",wikid);

		response = client.prepareSearch(index_name)
				.setTypes(index_type)
				.setSearchType(SearchType.DEFAULT)
				.setQuery(queryBuilder)
				.execute().actionGet();

		results = response.getHits().getHits();
		mid="";
		if(results.length > 0) {
			mid = (String)results[0].getSource().get("mid");
			types = (String)results[0].getSource().get("types");
			keywords = (String)results[0].getSource().get("keywords");
		}
		else{
			mid = "null";
			types = "null";
			keywords ="null";
		}
		mapping_object = new MappingObject(wikid, mid, types, keywords);
		
		return mapping_object;
	}
}
