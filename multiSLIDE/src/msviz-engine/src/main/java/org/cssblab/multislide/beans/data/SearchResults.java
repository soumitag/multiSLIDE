package org.cssblab.multislide.beans.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.cssblab.multislide.searcher.SearchResultObject;

/**
 *
 * @author soumitag
 */
public class SearchResults {
    
    String query;
    ArrayList <SearchResultSummary> search_result_summaries;
    
    public SearchResults(String query, ArrayList <SearchResultSummary> search_result_summaries) {
        this.query = query;
        this.search_result_summaries = search_result_summaries;
    }
    
    public static ArrayList <SearchResults> compileSearchResults (
            HashMap <String, ArrayList <SearchResultObject>> search_results_map,
            HashMap <String, Boolean> entrezMaster
    ) {
        ArrayList<SearchResults> search_result_groups = new ArrayList<SearchResults>();
        for (Map.Entry pair : search_results_map.entrySet()) {
            String query = (String) pair.getKey();
            ArrayList<SearchResultObject> search_results_i_ranked = SearchResultObject.sortSearchResultObjects((ArrayList<SearchResultObject>) pair.getValue(), entrezMaster);
            ArrayList<SearchResultSummary> search_result_summaries_i = SearchResultSummary.summarize(search_results_i_ranked);
            SearchResults result_group = new SearchResults(query, search_result_summaries_i);
            search_result_groups.add(result_group);
        }
        return search_result_groups;
    }

    public String getQuery() {
        return query;
    }

    public ArrayList<SearchResultSummary> getSummaries() {
        return search_result_summaries;
    }
    
}
