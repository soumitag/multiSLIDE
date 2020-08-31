/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.cssblab.multislide.searcher.Searcher;

/**
 *
 * @author soumitag
 */
public class NeighborhoodSearchResults implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String query_type;
    public String query_string;
    public int neighbor_count;
    public int neighbors_in_dataset;
    public String[] neighbor_entrez;
    public String[] neighbor_display_tag;
    public String identifier_type;
    public boolean[] neighbor_in_dataset_ind;
    public String message;
    public int status;
    
    public NeighborhoodSearchResults() {
        this.status = 0;
    }
    
    public NeighborhoodSearchResults makeNeighborhoodSearchResultObject (
            Searcher searcher,
            String identifier_type, 
            int identifier_index, 
            HashMap <String, ArrayList<String>> search_results,
            HashMap <String, Integer> entrezMaster
    ) {
        this.query_type = search_results.get("query_type").get(0);
        this.query_string = search_results.get("query_string").get(0);
        this.identifier_type = identifier_type;
        ArrayList <String> entrez_list = search_results.get("entrez_list");
        this.neighbor_count = entrez_list.size();
        this.neighbors_in_dataset = 0;
        this.neighbor_entrez = new String[entrez_list.size()];
        this.neighbor_display_tag = new String[entrez_list.size()];
        this.neighbor_in_dataset_ind = new boolean[entrez_list.size()];
        for (int i=0; i<entrez_list.size(); i++) {
            neighbor_entrez[i] = entrez_list.get(i);
            neighbor_display_tag[i] = "Entrez: " + neighbor_entrez[i] + ", Aliases: ";
            if (entrezMaster.containsKey(neighbor_entrez[i])) {
                this.neighbors_in_dataset++;
                neighbor_in_dataset_ind[i] = true;
            }
            ArrayList <String> identifier_values = searcher.getIdentifiersFromDB(neighbor_entrez[i], identifier_index);
            if (identifier_values.size() > 1) {
                for (int j=0; j<identifier_values.size()-1; j++) {
                    neighbor_display_tag[i] += identifier_values.get(j).toUpperCase() + ", ";
                }
                neighbor_display_tag[i] += identifier_values.get(identifier_values.size()-1).toUpperCase();
            }
        }
        this.message = "";
        this.status = 1;
        return this;
    }
    
        
    public String asJSON() {
        String json = new Gson().toJson(this);
        return json;
    }
}
