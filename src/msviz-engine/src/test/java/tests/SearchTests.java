/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.util.ArrayList;
import java.util.HashMap;
import org.cssblab.multislide.beans.data.SearchResults;
import org.cssblab.multislide.beans.data.SearchResultSummary;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.searcher.SearchHandler;
import org.cssblab.multislide.searcher.SearchResultObject;
import org.cssblab.multislide.searcher.Searcher;

/**
 *
 * @author abhikdatta
 */
public class SearchTests {

    public static void main(String[] args) {
        
        //doSearch_Test("pathname:immune");
        //doSearch_Test("genesymbol: cdk1; goterm: metabolic");
        doSearch_Test("genesymbol=cdk1");
        //doSearch_Test("genesymbol=cdk1,cdk2");
        //doSearch_Test("genesymbol:cdk1, cdk2");
        doSearch_Test("cdk1");
        //doSearch_Test("cdk1,cdk2");
        
    }
    
    public static void doSearch_Test(String query) {
        
        Searcher searcher = new Searcher("human");
        HashMap <String, Boolean> entrezMaster = new HashMap <String, Boolean> ();

        try {
            HashMap <String, ArrayList <SearchResultObject>> search_results_map = SearchHandler.processSearchQuery(query, searcher, null, false);
            
            ArrayList <SearchResults> search_result_groups = SearchResults.compileSearchResults(search_results_map, entrezMaster, null);
        
            for(int i=0; i<search_result_groups.size(); i++) {
                SearchResults srg = search_result_groups.get(i);
                System.out.println(srg.getQuery());
                ArrayList <SearchResultSummary> summaries = srg.getSummaries();
                for (int j=0; j<summaries.size(); j++) {
                    System.out.println(summaries.get(j).html_display_tag_pre + "<" + summaries.get(j).html_display_tag_mid + ">" + summaries.get(j).html_display_tag_post);
                }
            }
            
        } catch (Exception e) {
            System.out.println(e);
        }
        
    }
    
}
