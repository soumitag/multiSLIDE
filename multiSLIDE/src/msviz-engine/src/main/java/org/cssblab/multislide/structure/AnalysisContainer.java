/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.beans.data.SearchResults;
import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.searcher.Searcher;

/**
 *
 * @author soumitag
 */
public class AnalysisContainer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    public String base_path;
    public String analysis_name;
    public String species;
    public Data data;
    public Searcher searcher;
    public HashMap <String, Heatmap> heatmaps;
    public ArrayList <SearchResults> current_search_results;

    //public String[] selected_group_ids;
    //public String[] selected_group_types;
    
    public HashMap <String, double[]> group_colors;
    
    //public ArrayList <DatasetProperties> datasets = new ArrayList <DatasetProperties> ();
    
    public DataSelectionState data_selection_state;
    public ClusteringParams row_clustering_params;
    public ClusteringParams col_clustering_params;
    public HierarchicalClusterer clusterer;

    public Lists lists;
    
    public GlobalMapConfig global_map_config;
    
    public AnalysisContainer (String analysis_name, String species) { 
        this.analysis_name = analysis_name;
        this.species = species;
        global_map_config = new GlobalMapConfig();
        data_selection_state = new DataSelectionState();
        lists = new Lists();
        current_search_results = new ArrayList <SearchResults> ();
    }
    
    public void setBasePath (String base_path) {
        this.base_path = base_path;
    }
    
    public void setDatabase(Data data) {
        this.data = data;
    }
    
    public void setSearcher(Searcher searcher) {
        this.searcher = searcher;
    }
    
    public boolean selectedGroupsChanged (ArrayList <String> new_group_ids, ArrayList <String> new_group_types) {
        return false;
    }
    
    public void setRowClusteringParams(ClusteringParams row_clustering_params) {
        this.row_clustering_params = row_clustering_params;
    }

    public void setColClusteringParams(ClusteringParams col_clustering_params) {
        this.col_clustering_params = col_clustering_params;
    }
    
    public ClusteringParams getClusteringParams(int type) throws MultiSlideException {
        if (type == ClusteringParams.CLUSTERING_PARAM_TYPE_ROW) {
            return this.row_clustering_params;
        } else if (type == ClusteringParams.CLUSTERING_PARAM_TYPE_COL) {
            return this.col_clustering_params;
        } else {
            throw new MultiSlideException("Illegal argument for clustering type");
        }
    }
    
    public void generateGeneGroupColors() {
        int min = 0;
        int max = 255;
        Random r = new Random();
        group_colors = new HashMap <String, double[]> ();
        String[] geneGroupNames = this.data_selection_state.getGeneGroupNames();
        for (int i = 0; i < geneGroupNames.length; i++) {
            double[] color = new double[3];
            color[0] = r.nextInt(max - min) + min;
            color[1] = r.nextInt(max - min) + min;
            color[2] = r.nextInt(max - min) + min;
            this.group_colors.put(geneGroupNames[i], color);
        }
    }
    
    public HashMap <String, double[]> getGeneGroupColors() {
        return this.group_colors;
    }
    
    public HierarchicalClusterer getClusterer() {
        return clusterer;
    }

    public void setClusterer(HierarchicalClusterer clusterer) {
        this.clusterer = clusterer;
    }
    
    public void setCurrentSearchResults(ArrayList <SearchResults> current_search_results) {
        this.current_search_results = current_search_results;
    }
    
    public ArrayList<ArrayList<Integer>> getSearchTags() {
        ArrayList<ArrayList<Integer>> search_tags = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> search_tags_0 = new ArrayList<Integer>();
        search_tags_0.add(4);
        search_tags_0.add(5);
        search_tags_0.add(6);
        search_tags_0.add(7);
        ArrayList<Integer> search_tags_1 = new ArrayList<Integer>();
        search_tags_1.add(6);
        search_tags_1.add(7);
        search_tags_1.add(8);
        search_tags_1.add(9);
        search_tags_1.add(10);
        ArrayList<Integer> search_tags_2 = new ArrayList<Integer>();
        search_tags_2.add(18);
        search_tags_2.add(19);
        search_tags_2.add(20);
        search_tags_2.add(21);
        search_tags_2.add(22);
        search_tags.add(search_tags_0);
        search_tags.add(search_tags_1);
        search_tags.add(search_tags_2);
        return search_tags;
    }
    
    /*
    public void addDataset (DatasetProperties dataset) {
        datasets.add(dataset);
        
    }
    */
    
}
