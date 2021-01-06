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
import java.util.List;
import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.data.Selection;
import org.cssblab.multislide.structure.GlobalMapConfig;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.NetworkNeighbor;
import org.cssblab.multislide.utils.Utils;

public class HeatmapData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public int nSamples;
    public int nEntrez;
    
    public String title;
    public String description;
    public String entrez[];
    public String column_headers[];
    public List <List <Integer>> gene_tags;
    public double bin_colors[][];
    public int cell_bin_indices[][];
    public String hist_x_values[];
    public double hist_frequencies[];
    public boolean[] search_tag_origin_map_ind;
    public List <List <Integer>> search_tag_positions;
    public ArrayList <ArrayList <Integer>> is_search_query;
    public List <Integer> feature_cluster_labels;
    public List <String[]> network_neighborhood_types;
    public String mi_rna_ids[];
    public String[] cluster_label_message;
    
    /*
    public int rowsPerPageDisplayed;
    public int colsPerPageDisplayed;
    public int current_sample_start;
    public int current_feature_start;
    public int available_rows;
    public int available_cols;
    */
    
    public HeatmapData(
            GlobalMapConfig global_map_config,
            Selection selected_data, 
            Heatmap heatmap, 
            String dataset_name, 
            AnalysisContainer analysis
    ) throws MultiSlideException {
        
        long startTime = System.nanoTime();
        
        this.title = analysis.data.datasets.get(dataset_name).specs.display_name;
        this.nSamples = analysis.data.selected.getNumSamples();
        this.nEntrez = analysis.data.selected.getNumFeatures(dataset_name, global_map_config);
        this.description = global_map_config.makeDescString(nEntrez, nSamples);
        
        ArrayList <NetworkNeighbor> network_neighbors = analysis.data_selection_state.getNetworkNeighbors();

        cell_bin_indices = selected_data.getExpressionBins(dataset_name, analysis.global_map_config, heatmap.hist.nBins);
        
        Utils.log_info("HeatmapData time to 0 " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
        
        bin_colors = new double[heatmap.hist.nBins][3];
        this.bin_colors = heatmap.hist.rgb;
        heatmap.hist.normalizeHist();
        this.hist_frequencies = heatmap.hist.normedFrequencies;
        this.hist_x_values = new String[this.hist_frequencies.length];
        for (int i=0; i<hist_frequencies.length; i++) {
            double v = heatmap.hist.MIN_VAL + i*heatmap.hist.binsize;
            this.hist_x_values[i] = String.format("%5.2e",v);
        }
        
        Utils.log_info("HeatmapData time to 1 " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
        
        List <String> entrez_ids = selected_data.getEntrez(dataset_name, analysis.global_map_config);
        List <List<String>> feature_ids = selected_data.getFeatureIDs(
                dataset_name, analysis.global_map_config, heatmap.getMapConfig().getSelectedFeatureIdentifiers());
        
        this.entrez = new String[entrez_ids.size()];
        this.column_headers = new String[feature_ids.size()];
        for (int i=0; i<feature_ids.size(); i++) {
            this.entrez[i] = entrez_ids.get(i);
            this.column_headers[i] = String.join(", ", feature_ids.get(i));
        }
        
        gene_tags = selected_data.getGeneTags(dataset_name, analysis.global_map_config);
        
        Utils.log_info("HeatmapData time to 2 " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
        
        search_tag_origin_map_ind = new boolean[network_neighbors.size()];
        for (int i=0; i<network_neighbors.size(); i++) {
            NetworkNeighbor nn = network_neighbors.get(i);
            if (nn.getDatasetName().equalsIgnoreCase(dataset_name)) {
                search_tag_origin_map_ind[i] = true;
            }
        }
        
        List <List <Integer>> temp = analysis.data.selected.getSearchTags(dataset_name, analysis.global_map_config);
        
        search_tag_positions = new ArrayList <> ();
        is_search_query = new ArrayList <> ();
        for (int i=0; i<temp.size(); i++) {
            ArrayList <Integer> search_tag_positions_i = new ArrayList <> ();
            ArrayList <Integer> is_search_query_i = new ArrayList <> ();
            for (int j=0; j<temp.get(i).size(); j++) {
                search_tag_positions_i.add(Math.abs(temp.get(i).get(j))-1);
                if (temp.get(i).get(j) < 0) {
                    is_search_query_i.add(1);
                } else {
                    is_search_query_i.add(0);
                }
            }
            this.is_search_query.add(is_search_query_i);
            this.search_tag_positions.add(search_tag_positions_i);
        }
        
        /*
        Temporarily create an indicator variable , will be all 0s
        till a better strategy is designed to differentiate query vs neighbors in the same position list
        
        for (int i=0; i<this.search_tag_positions.size(); i++) {
            ArrayList <Integer> is_search_query_i = new ArrayList <> ();
            for (int j=0; j<this.search_tag_positions.get(i).size(); j++) {
                is_search_query_i.add(0);
            }
            //this.is_search_query.add(is_search_query_i);
        }
        */
        
        this.feature_cluster_labels = new ArrayList <> (feature_ids.size());
        for (List<String> feature_id : feature_ids) {
            this.feature_cluster_labels.add(0);
        }
        
        
        HashMap <String, Boolean> dataset_linkages = analysis.global_map_config.getDatasetLinkages();
        boolean is_linked = dataset_linkages.get(dataset_name);
        if (is_linked) {
            if (dataset_name.equals(analysis.global_map_config.col_clustering_params.getDatasetName())) {
                is_linked = false;
            }
        }
        
        if (analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.HIERARCHICAL_COLUMN_ORDERING && 
                analysis.global_map_config.isShowClusterLabelsOn) {
            
            if (!is_linked && analysis.data.selected.hasClusterLabels(dataset_name)) {

                if (analysis.data.selected.hasWithinLinkerOrdering(dataset_name)) {
                    
                    this.cluster_label_message = new String[]{"*Cluster labels are", "not displayed when", "linkers are nested"};
                } else {
                
                    List <Integer> fc_labels = analysis.data.selected.getClusterLabels(dataset_name, analysis.global_map_config);
                    this.feature_cluster_labels = new ArrayList <> (feature_ids.size());
                    fc_labels.forEach((label) -> {
                        this.feature_cluster_labels.add(label%2);
                    });
                    this.cluster_label_message = new String[0];
                }

            } else {
                this.cluster_label_message = new String[]{"*Cluster labels are", "not displayed for", "linked datasets"};
            }
        } else {
            this.cluster_label_message = new String[0];
        }
        
        network_neighborhood_types = new ArrayList <> ();
        if (analysis.data.datasets.get(dataset_name).specs.has_mi_rna) {
            
            network_neighborhood_types.add(new String[]{"miRNA Targets", "1"});
            
            List <List<String>> mirna_ids = selected_data.getFeatureIDs(
                dataset_name, analysis.global_map_config, new String[]{analysis.data.datasets.get(dataset_name).specs.getMIRNAColname()});
            
            mi_rna_ids = new String[mirna_ids.size()];
            for (int i=0; i<mirna_ids.size(); i++) {
                mi_rna_ids[i] = mirna_ids.get(i).get(0);
            }
            
        } else if (analysis.data.datasets.get(dataset_name).specs.has_linker) {
            
            network_neighborhood_types.add(new String[]{"Protein-Protein Interaction", "0"});
            network_neighborhood_types.add(new String[]{"Transcription Factor Targets", "2"});
        }
        
        /*
        this.feature_cluster_labels = new int[feature_ids.size()];
        for (int i=0; i<this.feature_cluster_labels.length; i++) {
            int r = (int)Math.floor(i/5);
            this.feature_cluster_labels[i] = r%2;
        }
        */
        
        Utils.log_info("HeatmapData time to 3 " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
    }
    
    
    /*
    public HeatmapData(
            GlobalMapConfig global_map_config,
            FilteredSortedData data, 
            Heatmap heatmap, 
            String dataset_name, 
            AnalysisContainer analysis
    ) throws MultiSlideException {
        
        this.title = dataset_name;
        this.nSamples = global_map_config.getRowsPerPageDisplayed();
        this.nEntrez = global_map_config.getColsPerPageDisplayed();
        
        ArrayList <NetworkNeighbor> network_neighbors = analysis.data_selection_state.getNetworkNeighbors();

        bin_colors = new double[heatmap.hist.nBins][3];
        this.bin_colors = heatmap.hist.rgb;
        
        int[][] cell_bin_indices_orig = data.getExpressionBinNos(dataset_name, analysis.global_map_config);
        cell_bin_indices = new int[this.nEntrez][this.nSamples];
        for (int i=0; i<this.nSamples; i++) {
            for (int j=0; j<this.nEntrez; j++) {
                cell_bin_indices[j][i] = cell_bin_indices_orig[i][j];
            }
        }
        
        colorbar_keys = new String[5];
        double tick_space = (heatmap.hist.MAX_VAL-heatmap.hist.MIN_VAL)/4.0;
        for (int i=0; i<5; i++) {
            double v = heatmap.hist.MIN_VAL + i*tick_space;
            this.colorbar_keys[i] = String.format("%5.2e",v);
        }
        
        search_tag_origin_map_ind = new boolean[network_neighbors.size()];
        for (int i=0; i<network_neighbors.size(); i++) {
            NetworkNeighbor nn = network_neighbors.get(i);
            if (nn.getDatasetName().equalsIgnoreCase(dataset_name)) {
                search_tag_origin_map_ind[i] = true;
            }
        }
    }
    */
    
    public String heatmapDataAsJSON () {
        return new Gson().toJson(this);
    }
    
    

}
