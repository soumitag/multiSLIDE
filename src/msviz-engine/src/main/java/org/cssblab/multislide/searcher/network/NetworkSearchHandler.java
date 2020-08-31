/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.searcher.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.cssblab.multislide.beans.data.NeighborhoodSearchResults;
import org.cssblab.multislide.searcher.network.NetworkSearcher;
import org.cssblab.multislide.searcher.GeneObject;
import org.cssblab.multislide.searcher.network.NetworkSearchResultObject;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class NetworkSearchHandler implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public HashMap <String, ArrayList<String>> processQuery(String query) throws MultiSlideException {
        try {
            if (query.contains("=")) {
                String[] parts = query.split("=", -1);
                String keyword = parts[0].trim().toLowerCase();
                String search_type = "exact";
                String keyword_str = "";
                if (keyword.contains("-")) {
                    String[] keyword_parts = keyword.split("-", -1);
                    keyword_str = keyword_parts[0].trim().toLowerCase() + "_" + keyword_parts[1].trim().toLowerCase();
                } else {
                    keyword_str = keyword;
                }
                String search_term = parts[1].trim().toLowerCase();
                ArrayList<String> neighbor_entrez_list = doSearch(keyword_str, search_type, search_term);
                
                HashMap <String, ArrayList<String>> retval = new HashMap <String, ArrayList<String>> ();
                ArrayList<String> t1 = new ArrayList<String>();
                t1.add(keyword_str);
                retval.put("query_type", t1);
                ArrayList<String> t2 = new ArrayList<String>();
                t2.add(search_term);
                retval.put("query_string", t2);
                retval.put("entrez_list", neighbor_entrez_list);
                
                return retval;
                
            } else {
                throw new MultiSlideException("Exception in NetworkSearchHandler.processQuery(): query must contain '='");
            }
        } catch (Exception e) {
            Utils.log_exception(e, "");
            throw new MultiSlideException("Exception in NetworkSearchHandler.processQuery()");
        }
    }

    public ArrayList <String> doSearch(String query_type, String search_type, String search_term) {
        NetworkSearcher network_search = new NetworkSearcher("human");        
        ArrayList<String> all_results = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(search_term, ",");

        if (query_type != null) {
            if (query_type.equals("tf_entrez")) {

                while (st.hasMoreTokens()) {
                    String qString = st.nextToken();
                    ArrayList<String> TFtargets = network_search.searchTFDB(query_type, search_type, qString);
                    all_results.addAll(TFtargets);
                }

            } else if (query_type.equals("mirna_id")) {

                while (st.hasMoreTokens()) {
                    String qString = st.nextToken();
                    ArrayList<String> miRNATargets = network_search.searchmiRNAToFamilyToTargets(query_type, search_type, qString);
                    all_results.addAll(miRNATargets);
                }

            } else if (query_type.equals("ppi_entrez")) {

                while (st.hasMoreTokens()) {
                    String qString = st.nextToken();
                    ArrayList<String> ppi_interactors = network_search.searchPPI(query_type, search_type, qString);
                    all_results.addAll(ppi_interactors);
                }
            }

        }
        
        return all_results;
    }

}
