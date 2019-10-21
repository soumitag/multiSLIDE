/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.cssblab.multislide.beans.data.SearchResultSummary;
import org.cssblab.multislide.beans.data.SearchResults;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.searcher.SearchResultObject;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class DataSelectionState implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String[] datasets;
    public SearchResultSummary[] searches;
    public String[] selectedPhenotypes;  // the main phenotype data is stored in filtered_sorted_data, here only the keys are maintained
    public int nGeneTags;
    public String[] geneTagKeys;         // the main tags are stored in filtered_sorted_data, here only the keys are maintained
    public HashMap <String, NetworkNeighbor> network_neighbors;
    
    HashMap <String, DataSelectionState> history;
    
    public DataSelectionState() {
        this.nGeneTags = 1;
        this.datasets = new String[0];
        this.selectedPhenotypes = new String[0];
        this.searches = new SearchResultSummary[0];
        this.network_neighbors = new HashMap <String, NetworkNeighbor> ();
        this.history = new HashMap <String, DataSelectionState> ();
    }
    
    public String[] getDatasets() {
        return datasets;
    }

    public void setDatasets(String[] datasets) {
        this.datasets = datasets;
    }

    public void setSearchResults(
            String[] group_ids, 
            byte[] group_types, 
            String[] group_names, 
            int[][] intersection_counts_per_dataset, 
            int[] background_counts
    )  {
        
        this.searches = new SearchResultSummary[group_ids.length];
        for (int i=0; i<group_ids.length; i++) {
            SearchResultSummary summary = new SearchResultSummary();
            if (group_types[i] == SearchResultSummary.TYPE_GENE_SUMMARY) {
                summary.createSearchResultSummary(SearchResultSummary.TYPE_GENE_SUMMARY, group_names[i], group_ids[i], background_counts[i], intersection_counts_per_dataset[i]);
            } else if (group_types[i] == SearchResultSummary.TYPE_GO_SUMMARY) {
                summary.createSearchResultSummary(SearchResultSummary.TYPE_GO_SUMMARY, group_names[i], group_ids[i], background_counts[i], intersection_counts_per_dataset[i]);
            } else if (group_types[i] == SearchResultSummary.TYPE_PATH_SUMMARY) {
                summary.createSearchResultSummary(SearchResultSummary.TYPE_PATH_SUMMARY, group_names[i], group_ids[i], background_counts[i], intersection_counts_per_dataset[i]);
            }
            this.searches[i] = summary;
        }

    }
    
    public String[] getGeneGroupTypes() throws MultiSlideException {
        String[] geneGroupTypes = new String[this.searches.length];
        for (int i=0; i<this.searches.length; i++) {
            geneGroupTypes[i] = this.searches[i].mapGeneGroupCodeToType();
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
    
    public String getGeneGroupDisplayName (String group_id) throws MultiSlideException {
        for (int i=0; i<this.searches.length; i++) {
            if (group_id.equals(this.searches[i]._id)) {
                return this.searches[i].display_tag;
            }
        }
        throw new MultiSlideException("No such group id: " + group_id + " in current search results.");
    }

    public String[] getSelectedPhenotypes() {
        return selectedPhenotypes;
    }

    public void setSelectedPhenotypes(String[] selectedPhenotypes) {
        this.selectedPhenotypes = selectedPhenotypes;
    }
    
    public void addNetworkNeighbor(NetworkNeighbor nn) {
        this.network_neighbors.put(nn.id, nn);
    }
    
    public boolean removeNetworkNeighbor(String neighbor_id) {
        if (this.network_neighbors.containsKey(neighbor_id)) {
            this.network_neighbors.remove(neighbor_id);
            return true;
        } else {
            return false;
        }
    }
    
    public ArrayList <NetworkNeighbor> getNetworkNeighbors() {
        return new ArrayList(this.network_neighbors.values());
    }
    
    public void clearNetworkNeighbors() {
        this.network_neighbors = new HashMap <String, NetworkNeighbor> ();
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
    
    public void pushHistory() throws MultiSlideException {
        DataSelectionState state = (DataSelectionState)Utils.deepCopy(this);
        this.history.put("current", state);
    }
    
    public void popHistory() throws MultiSlideException {
        this.loadState("current");
        this.history.put("current", null);
    }
    
    public void loadState(String name) throws MultiSlideException {
        DataSelectionState state = this.history.get(name);
        this.datasets = state.datasets;
        this.searches = state.searches;
        this.selectedPhenotypes = state.selectedPhenotypes;
        this.nGeneTags = state.nGeneTags;
        this.geneTagKeys = state.geneTagKeys;
        this.network_neighbors = state.network_neighbors;
    }
    
    public void saveState(String name) throws MultiSlideException {
        DataSelectionState state = (DataSelectionState)Utils.deepCopy(this);
        this.history.put(name, state);
    }
    
    public boolean hasCurrent() {
        if (this.history.containsKey("current")) {
            if (this.history.get("current") != null) {
                return true;
            }
        }
        return false;
    }
    
}
