package org.cssblab.multislide.beans.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.cssblab.multislide.searcher.SearchResultObject;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.data.Data;
import org.cssblab.multislide.structure.data.Selection;

/**
 *
 * @author soumitag
 */
public class SearchResults implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String query;
    public String[] dataset_names;
    public ArrayList <SearchResultSummary> search_result_summaries;
    
    public SearchResults() {
        this.query = "";
        this.dataset_names = new String[0];
        this.search_result_summaries = new ArrayList <SearchResultSummary> ();
    }
    
    public SearchResults(
            String query, 
            String[] dataset_names,
            ArrayList <SearchResultSummary> search_result_summaries
    ) {
        this.query = query;
        this.dataset_names = dataset_names;
        this.search_result_summaries = search_result_summaries;
    }
    
    public static ArrayList <SearchResults> compileSearchResults (
            HashMap <String, ArrayList <SearchResultObject>> search_results_map,
            Data data
    ) throws MultiSlideException {
        ArrayList<SearchResults> search_result_groups = new ArrayList<SearchResults>();
        for (Map.Entry pair : search_results_map.entrySet()) {
            String query = (String) pair.getKey();
            ArrayList<SearchResultObject> search_results_i_ranked = SearchResultObject.sortSearchResultObjects(
                    (ArrayList<SearchResultObject>) pair.getValue(), 
                    data
            );
            ArrayList<SearchResultSummary> search_result_summaries_i = SearchResultSummary.summarize(search_results_i_ranked);
            SearchResults result_group = new SearchResults(query, data.dataset_names, search_result_summaries_i);
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
