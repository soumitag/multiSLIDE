/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import com.google.gson.Gson;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cssblab.multislide.beans.data.BipartiteLinkageGraph;
import org.cssblab.multislide.beans.data.EnrichmentAnalysisResult;
import org.cssblab.multislide.beans.data.FunctionalGroupContainer;
import org.cssblab.multislide.beans.data.SearchResultSummary;
import org.cssblab.multislide.beans.data.SearchResults;
import org.cssblab.multislide.searcher.GeneObject;
import org.cssblab.multislide.searcher.GoObject;
import org.cssblab.multislide.searcher.PathwayObject;
import org.cssblab.multislide.searcher.SearchResultObject;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class DataSelectionState implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static int ADD_GENES_SOURCE_TYPE_SEARCH = 0;
    public static int ADD_GENES_SOURCE_TYPE_ENRICHMENT = 1;
    public static int ADD_GENES_SOURCE_TYPE_UPLOAD = 2;
    
    // selected from map container (otherwise defaults)
    public String[] selected_datasets;
    public String[] selected_phenotypes;  // the main phenotype data is stored in filtered_sorted_data, here only the keys are maintained
    
    // current selections in add genes panel
    public SearchResultSummary[] selected_searches;
    public EnrichmentAnalysisResult[] selected_enriched_groups;
    public FunctionalGroupContainer[] selected_functional_groups;
    
    public HashMap <String, NetworkNeighbor> network_neighbors;
    public HashMap <String, BipartiteLinkageGraph> user_defined_between_omics_linkages;
    
    // add genes panel state (except selection): search results, enrichment params
    public int add_genes_source_type;
    public List <SearchResults> current_search_results;
    public EnrichmentAnalysisParams enrichment_analysis_params;
    public List <EnrichmentAnalysisResult> current_enrichment_analysis_results;
    public String uploaded_filename;
    public FunctionalGroupContainer[] functional_groups_in_file;
    
    private HashMap <String, DataSelectionState> history;
    
    public DataSelectionState() {
        this.add_genes_source_type = DataSelectionState.ADD_GENES_SOURCE_TYPE_SEARCH;
        this.selected_datasets = new String[0];
        this.selected_phenotypes = new String[0];
        this.selected_searches = new SearchResultSummary[0];
        this.selected_enriched_groups = new EnrichmentAnalysisResult[0];
        this.network_neighbors = new HashMap <> ();
        this.history = new HashMap <> ();
        this.current_search_results = new ArrayList <> ();
        this.enrichment_analysis_params = new EnrichmentAnalysisParams("", "");
        this.selected_functional_groups = new FunctionalGroupContainer[0];
        this.uploaded_filename = "";
        this.functional_groups_in_file = new FunctionalGroupContainer[0];
        this.user_defined_between_omics_linkages = new HashMap <> ();
    }
    
    public void adduserSpecifiedInterOmicsConnections(BipartiteLinkageGraph  linkage) {
        user_defined_between_omics_linkages.put(linkage.dataset_name_1 + "_" + linkage.dataset_name_2, linkage);
    }
    
    public String[] getSelectedDatasets() {
        return selected_datasets;
    }

    public void setSelectedDatasets(String[] datasets) {
        this.selected_datasets = datasets;
        for (String s: this.selected_datasets)
            Utils.log_info("datasets: " + s);
    }

    public void setSelectedSearchResults(AnalysisContainer analysis, String[] search_ids) throws MultiSlideException {
        
        this.selected_searches = new SearchResultSummary[search_ids.length];
        
        int count = 0;
        for (String _id: search_ids) {
            String[] parts = _id.split("_", 2);
            byte type = Byte.parseByte(parts[0]);
            String grp_id = parts[1];
            SearchResultObject ob = this.recreateSearchResultObject(
                    analysis.searcher, grp_id, SearchResultSummary.mapGeneGroupCodeToType(type)
            );
            ob.computeNumGeneIntersections(analysis.data);
            this.selected_searches[count++] = ob.getSummary();
        }
    }
    
    public void setSelectedUploadedFunctionalGroups(String[] functional_group_ids) {
        
        this.selected_functional_groups = new FunctionalGroupContainer[functional_group_ids.length];
        
        int count = 0;
        for (FunctionalGroupContainer fgc : this.functional_groups_in_file) {
            for (String grp_id : functional_group_ids) {
                if (fgc._id.equals(grp_id)) {
                    this.selected_functional_groups[count++] = fgc;
                }
            }
        }
    }
    
    public void setSelectedEnrichedFunctionalGroups(String[] functional_group_ids) {
        
        this.selected_enriched_groups = new EnrichmentAnalysisResult[functional_group_ids.length];
        
        int count = 0;
        for (EnrichmentAnalysisResult ear : this.current_enrichment_analysis_results) {
            for (String grp_id : functional_group_ids) {
                if (ear.pathid.equals(grp_id)) {
                    this.selected_enriched_groups[count++] = ear;
                }
            }
        }
    }

    /*
    public void setSearchResults(
            String[] group_ids, 
            byte[] group_types, 
            String[] group_names, 
            int[][] intersection_counts_per_dataset, 
            int[] background_counts
    )  {
        
        this.selected_searches = new SearchResultSummary[group_ids.length];
        for (int i=0; i<group_ids.length; i++) {
            SearchResultSummary summary = new SearchResultSummary();
            if (group_types[i] == SearchResultSummary.TYPE_GENE_SUMMARY) {
                summary.createSearchResultSummary(SearchResultSummary.TYPE_GENE_SUMMARY, group_names[i], group_ids[i], background_counts[i], intersection_counts_per_dataset[i]);
            } else if (group_types[i] == SearchResultSummary.TYPE_GO_SUMMARY) {
                summary.createSearchResultSummary(SearchResultSummary.TYPE_GO_SUMMARY, group_names[i], group_ids[i], background_counts[i], intersection_counts_per_dataset[i]);
            } else if (group_types[i] == SearchResultSummary.TYPE_PATH_SUMMARY) {
                summary.createSearchResultSummary(SearchResultSummary.TYPE_PATH_SUMMARY, group_names[i], group_ids[i], background_counts[i], intersection_counts_per_dataset[i]);
            }
            this.selected_searches[i] = summary;
        }
    }
    */
    
    public String[] getGeneGroupTypes() throws MultiSlideException {
        String[] geneGroupTypes = new String[this.selected_searches.length];
        for (int i=0; i<this.selected_searches.length; i++) {
            geneGroupTypes[i] = this.selected_searches[i].mapGeneGroupCodeToType();
        }
        return geneGroupTypes;
    }
    
    public String[] getGeneGroupNames() {
        String[] geneGroupNames = new String[this.selected_searches.length];
        for (int i=0; i<this.selected_searches.length; i++) {
            geneGroupNames[i] = this.selected_searches[i]._id;
        }
        return geneGroupNames;
    }
    
    public String getGeneGroupDisplayName (String group_id) throws MultiSlideException {
        for (int i=0; i<this.selected_searches.length; i++) {
            if (group_id.equals(this.selected_searches[i]._id)) {
                return this.selected_searches[i].display_tag;
            }
        }
        throw new MultiSlideException("No such group id: " + group_id + " in current search results.");
    }

    public String[] getSelectedPhenotypes() {
        return selected_phenotypes;
    }

    public void setSelectedPhenotypes(String[] selectedPhenotypes) {
        this.selected_phenotypes = selectedPhenotypes;
    }
        
    public EnrichmentAnalysisParams getEnrichmentAnalysisParams() {
        return this.enrichment_analysis_params;
    }
    
    public void setEnrichmentAnalysisParams(EnrichmentAnalysisParams enrichment_analysis_params) {
        this.enrichment_analysis_params = enrichment_analysis_params;
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
    
    public void setDefaultDatasetAndPhenotypes(String[] datasets, String[] phenotypes) {
        this.setSelectedDatasets(datasets);
        int N = Math.min(phenotypes.length, 5);
        String[] selected_phenotypes = new String[N];
        System.arraycopy(phenotypes, 0, selected_phenotypes, 0, N);
        this.setSelectedPhenotypes(selected_phenotypes);
        if (this.enrichment_analysis_params.dataset.equals("")) {
            this.enrichment_analysis_params.dataset = this.selected_datasets[0];
        }
        if (this.enrichment_analysis_params.phenotype.equals("")) {
            this.enrichment_analysis_params.phenotype = this.selected_phenotypes[0];
        }
    }
    
    public String asJSON () {
        return new Gson().toJson(this);
    }
    
    public boolean isDatasetSelected(String dataset_name) {
        for (String dataset : this.selected_datasets) {
            if (dataset.equals(dataset_name)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasPhenotype(String phenotype_name) {
        for (String phenotype : this.selected_phenotypes) {
            if (phenotype.equals(phenotype_name)) {
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
        this.selected_datasets = state.selected_datasets;
        this.selected_phenotypes = state.selected_phenotypes;
        this.add_genes_source_type = state.add_genes_source_type;
        this.selected_searches = state.selected_searches;
        this.selected_enriched_groups = state.selected_enriched_groups;
        this.network_neighbors = state.network_neighbors;
        this.current_search_results = state.current_search_results;
        this.enrichment_analysis_params = state.enrichment_analysis_params;
        this.selected_functional_groups = state.selected_functional_groups;
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
    
    public void setCurrentSearchResults(List <SearchResults> current_search_results) {
        this.current_search_results = current_search_results;
    }
    
    public void setCurrentEnrichmentAnalysisResults(List <EnrichmentAnalysisResult> current_enrichment_analysis_results) {
        this.current_enrichment_analysis_results = current_enrichment_analysis_results;
    }
    
    public void setFunctionalGroupsInFile(HashMap <String, HashMap <List<String>, List<String>>> user_sepcified_pathways) {
        
        int count = 0;
        this.functional_groups_in_file = new FunctionalGroupContainer[user_sepcified_pathways.size()];
        for (String func_grp_name: user_sepcified_pathways.keySet()) {
            this.functional_groups_in_file[count] = new FunctionalGroupContainer(
                    count + "", func_grp_name, user_sepcified_pathways.get(func_grp_name));
            count++;
        }
        /*
        int count = 0;
        this.functional_groups_in_file = new FunctionalGroupContainer[multilist_data.size()];
        for (Map.Entry<List<String>, String[]> entry : multilist_data.entrySet()) {
            this.functional_groups_in_file[count] = new FunctionalGroupContainer(count + "", entry.getKey(), entry.getValue());
            count++;
        }
        */
    }
    
    public HashMap <String, ArrayList <String>> getFeatureSelection() {
        HashMap <String, ArrayList <String>> feature_selection = new HashMap <> ();
        return feature_selection;
    }
    
    public HashMap <String, ArrayList <GeneGroup>> getEntrezGroupMap() {
        HashMap <String, ArrayList <GeneGroup>> entrez_group_map = new HashMap <> ();
        return entrez_group_map;
    }
    
    /*
        group_id can be entrez, pathid or goid
        returns the group name (display name)
    */
    private SearchResultObject recreateSearchResultObject(Searcher searcher, String group_id, String queryType) throws MultiSlideException{
        
        if (queryType.equals("entrez") || queryType.equals("gene_group_entrez")) {
            ArrayList<GeneObject> genes = searcher.processGeneQuery(group_id, "exact", "entrez");            
            for (int i = 0; i < genes.size(); i++) {
                GeneObject gene = genes.get(i);
                return SearchResultObject.makeSearchResultObject(queryType, gene);
            }
            return null;
        } else if (queryType.equals("pathid")) {
            ArrayList<PathwayObject> part_paths = searcher.processPathQuery(group_id, "exact", queryType);
            for (int i = 0; i < part_paths.size(); i++) {
                PathwayObject path = part_paths.get(i);
                return SearchResultObject.makeSearchResultObject(queryType, path);
            }
            return null;
        } else if (queryType.equals("goid")) {
            ArrayList<GoObject> part_go_terms = searcher.processGOQuery(group_id, "exact", queryType);
            for (int i = 0; i < part_go_terms.size(); i++) {
                GoObject go = part_go_terms.get(i);
                return SearchResultObject.makeSearchResultObject(queryType, go);
            }
            return null;
        } else {
            throw new MultiSlideException("Bad queryType in call to DataSelectionState.recreateSearchResultObject()");
        }
        
    }
}
