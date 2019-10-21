/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.cssblab.multislide.graphics.ColorPalette;

import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.FilteredSortedData;
import org.cssblab.multislide.structure.GeneGroup;
import org.cssblab.multislide.structure.GlobalMapConfig;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.NetworkNeighbor;

public class HeatmapData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private transient final int nSamples;
    private transient final int nEntrez;
    
    public String title;
    public double bin_colors[][];
    public int cell_bin_indices[][];
    public String colorbar_keys[];
    public boolean[] search_tag_origin_map_ind;

    
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
        
        cell_bin_indices = data.getExpressionBinNos(dataset_name, analysis.global_map_config);
        
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
