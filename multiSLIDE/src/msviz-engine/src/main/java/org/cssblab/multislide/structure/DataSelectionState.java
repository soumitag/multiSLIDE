/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.cssblab.multislide.beans.data.SearchResultSummary;
import org.cssblab.multislide.beans.data.SearchResults;

/**
 *
 * @author soumitag
 */
public class DataSelectionState {
    
    public String[] datasets;
    public SearchResultSummary[] searches;
    public String[] selectedPhenotypes;  // the main phenotype data is stored in filtered_sorted_data, here only the keys are maintained
    public int nGeneTags;
    public String[] geneTagKeys;         // the main tags are stored in filtered_sorted_data, here only the keys are maintained
    
    public DataSelectionState() {
        this.nGeneTags = 1;
        this.datasets = new String[0];
        this.selectedPhenotypes = new String[0];
        this.searches = new SearchResultSummary[0];
    }
    
    public String[] getDatasets() {
        return datasets;
    }

    public void setDatasets(String[] datasets) {
        this.datasets = datasets;
    }

    public void setSearchResults(String[] group_ids, byte[] group_types, ArrayList <SearchResults> current_search_results) {
        
        HashMap <String, SearchResultSummary> srs_map = new HashMap <String, SearchResultSummary> ();
        for (int i=0; i<current_search_results.size(); i++) {
            ArrayList<SearchResultSummary> summaries_i = current_search_results.get(i).getSummaries();
            for (int j=0; j<summaries_i.size(); j++) {
                SearchResultSummary srs = summaries_i.get(j);
                srs_map.put(srs._id + ":" + srs.type, srs);
            }
        }
        
        int count = 0;
        for (int i=0; i<group_ids.length; i++) {
            if (srs_map.containsKey(group_ids[i] + ":" + group_types[i])) {
                count++;
            }
        }
        
        this.searches = new SearchResultSummary[count];
        count = 0;
        for (int i=0; i<group_ids.length; i++) {
            if (srs_map.containsKey(group_ids[i] + ":" + group_types[i])) {
                this.searches[count++] = srs_map.get(group_ids[i] + ":" + group_types[i]);
            }
        }
        
    }
    
    public String[] getGeneGroupTypes() throws MultiSlideException {
        String[] geneGroupTypes = new String[this.searches.length];
        for (int i=0; i<this.searches.length; i++) {
            geneGroupTypes[i] = mapGeneGroupCodeToType(this.searches[i].type);
        }
        return geneGroupTypes;
    }
    
    public String[] getGeneGroupNames() {
        String[] geneGroupNames = new String[this.searches.length];
        for (int i=0; i<this.searches.length; i++) {
            geneGroupNames[i] = this.searches[i]._id;
        }
        return geneGroupNames;
    }

    public String[] getSelectedPhenotypes() {
        return selectedPhenotypes;
    }

    public void setSelectedPhenotypes(String[] selectedPhenotypes) {
        this.selectedPhenotypes = selectedPhenotypes;
    }
    
    public String mapGeneGroupCodeToType(int geneGroupTypeCode)
            throws MultiSlideException {

        switch (geneGroupTypeCode) {
            case SearchResultSummary.TYPE_GENE_SUMMARY:
                return "gene";
            case SearchResultSummary.TYPE_PATH_SUMMARY:
                return "pathid";
            case SearchResultSummary.TYPE_GO_SUMMARY:
                return "goid";
            default:
                throw new MultiSlideException("Invalid Gene Group Type Code: " + geneGroupTypeCode + " valid values are: 4, 5, and 6");
        }
    }
    
    public String asJSON () {
        return new Gson().toJson(this);
    }
    
    public boolean isDatasetSelected(String dataset_name) {
        for (String dataset : this.datasets) {
            if (dataset.equals(dataset_name)) {
                return true;
            }
        }
        return false;
    }
}
