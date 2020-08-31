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
import org.cssblab.multislide.structure.data.Selection;
import org.cssblab.multislide.structure.GeneGroup;
import org.cssblab.multislide.structure.GlobalMapConfig;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.NetworkNeighbor;

public class GlobalHeatmapData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private transient final int nSamples;

    public String row_names[];
    public double gene_tag_colors[][];
    public double gene_tag_background_color[];
    public String gene_tag_names[];
    public double phenotypes[][][];
    public String phenotype_labels[];
    public String gene_group_keys[];
    //public String colorbar_keys[];
    
    public double[][] search_tag_colors;
    public double[][] search_tag_stroke_colors;
    public int[] search_tag_color_indices;
    //public boolean[] search_tag_origin_map_ind;
    public String[] search_tag_ids;
    
    
    public GlobalHeatmapData(
            GlobalMapConfig global_map_config,
            Selection selection, 
            String column_label, 
            AnalysisContainer analysis
    ) throws MultiSlideException {
        
        /*
        r_start = (r_start < 0) ? 0 : r_start;
        int current_start = analysis.global_map_config.current_sample_start;
        r_start = (r_start >= fs_data.nSamples) ? current_start : r_start;
        
        c_start = (c_start < 0) ? 0 : c_start;
        current_start = analysis.global_map_config.current_feature_start;
        c_start = (c_start >= fs_data.nFilteredGenes) ? current_start : c_start;
        
        int r_end = r_start + nSamples;
        r_end = Math.min(r_end, fs_data.nSamples);
        
        int c_end = c_start + nEntrez;
        c_end = Math.min(c_end, fs_data.nFilteredGenes);
        
        this.nSamples = r_end - r_start;
        this.nEntrez = c_end - c_start;
        */
        
        //int r_start = global_map_config.getCurrentSampleStart();
        this.nSamples = analysis.data.selected.getNumSamples();
        
        //int c_start = global_map_config.getCurrentFeatureStart();
        //this.nEntrez = analysis.data.selected.getNumFilteredFeatures(dataset_name, analysis.global_map_config);
        
        int nGeneGroups = selection.getGeneGroups().size();
        ArrayList <NetworkNeighbor> network_neighbors = analysis.data_selection_state.getNetworkNeighbors();

        //column_headers = new String[this.nEntrez];
        //entrez = new String[this.nEntrez];
        row_names = new String[this.nSamples];
        //gene_tags = new ArrayList <ArrayList <Integer>> ();
        gene_tag_colors = new double[nGeneGroups][3];
        /*
        phenotypes = new double[this.nSamples][fs_data.getNumSelectedPhenotype()][3];
        phenotype_labels = new String[fs_data.getNumSelectedPhenotype()];
        */
        String[] selected_phenotypes = analysis.data_selection_state.selected_phenotypes;
        phenotypes = new double[this.nSamples][selected_phenotypes.length][3];
        phenotype_labels = new String[selected_phenotypes.length];
        
        gene_group_keys = new String[nGeneGroups];
        gene_tag_names = new String[nGeneGroups];
        //colorbar_keys = new String[5];
        
        //this.bin_colors = heatmap.hist.rgb;
        //int[][] bin_nos = data.getExpressionBinNos(dataset_name, analysis.global_map_config);
        
        Map.Entry <String,Heatmap> entry = analysis.heatmaps.entrySet().iterator().next();
        String dataset_name = entry.getKey();
        this.row_names = selection.getSampleIDs(dataset_name, analysis.global_map_config);
        //this.column_headers = selection.getFeatureIDs(dataset_name, column_label, analysis.global_map_config, analysis.heatmaps);
        
        /*
        int r_count = 0;
        for (int r=r_start; r<r_end; r++) {
            row_names[r_count++] = rownames[r];
        }
        */
        
        //int c_count = 0;
        /*
        for (int c=0; c<nEntrez; c++) {
            //column_headers[c_count] = colheaders[c];
            entrez[c] = selection.getEntrezAt(c_start+c);
            ArrayList <GeneGroup> GGs = selection.getEntrezGroupMap().get(entrez[c]);
            ArrayList <Integer> t = new ArrayList <Integer> ();
            for (GeneGroup g : GGs) {
                t.add(g.tag);
            }
            gene_tags.add(t);
        }
        */

        for (int p_count=0; p_count<analysis.data_selection_state.selected_phenotypes.length; p_count++) {
            phenotype_labels[p_count] = selected_phenotypes[p_count];
            String[] phenotype_values = selection.getPhenotypeValues(selected_phenotypes[p_count], analysis.global_map_config);
            for (int r=0; r<nSamples; r++) {
                phenotypes[r][p_count] = analysis.data.clinical_info.getPhenotypeColor(selected_phenotypes[p_count], phenotype_values[r]);
            }
        }
        
        int g = 0;
        for (GeneGroup unique_gene_group : selection.getGeneGroups().values()) {
            gene_tag_colors[g] = unique_gene_group.color;
            gene_group_keys[g] = unique_gene_group.getID();
            gene_tag_names[g] = unique_gene_group.display_tag;
            g++;
        }
        
        gene_tag_background_color = new double[]{235,235,235};
        
        /*
        for (int i=0; i<nGeneGroups; i++) {
            gene_tag_colors[i] = analysis.data_selection_state.group_colors.get(geneGroupNames[i]);
            gene_group_keys[i] = geneGroupTypes[i] + "_" + geneGroupNames[i];
        }
        */
        
        //analysis.global_map_config.current_sample_start = r_start;
        //analysis.global_map_config.current_feature_start = c_start;
        
        /*
        double tick_space = (heatmap.hist.MAX_VAL-heatmap.hist.MIN_VAL)/4.0;
        for (int i=0; i<5; i++) {
            double v = heatmap.hist.MIN_VAL + i*tick_space;
            this.colorbar_keys[i] = String.format("%5.2e",v);
        }
        */

        this.search_tag_colors = new double[3][3];
        this.search_tag_colors[0] = NetworkNeighbor.PPI_ENTREZ_NEIGHBOR_COLOR;
        this.search_tag_colors[1] = NetworkNeighbor.MIRNA_ID_NEIGHBOR_COLOR;
        this.search_tag_colors[2] = NetworkNeighbor.TF_ENTREZ_NEIGHBOR_COLOR;
        
        this.search_tag_stroke_colors = new double[3][3];
        this.search_tag_stroke_colors[0] = NetworkNeighbor.PPI_ENTREZ_NEIGHBOR_STROKE_COLOR;
        this.search_tag_stroke_colors[1] = NetworkNeighbor.MIRNA_ID_NEIGHBOR_STROKE_COLOR;
        this.search_tag_stroke_colors[2] = NetworkNeighbor.TF_ENTREZ_NEIGHBOR_STROKE_COLOR;
        

        this.search_tag_color_indices = new int[network_neighbors.size()];
        //this.search_tag_origin_map_ind = new boolean[network_neighbors.size()];
        this.search_tag_ids = new String[network_neighbors.size()];
        for (int i=0; i<network_neighbors.size(); i++) {
            NetworkNeighbor nn = network_neighbors.get(i);
            if (nn.getNetworkType().equals(NetworkNeighbor.NETWORK_TYPE_PPI_ENTREZ)) {
                this.search_tag_color_indices[i] = 0;
            } else if (nn.getNetworkType().equals(NetworkNeighbor.NETWORK_TYPE_MIRNA_ID)) {
                this.search_tag_color_indices[i] = 1;
            } else if (nn.getNetworkType().equals(NetworkNeighbor.NETWORK_TYPE_TF_ENTREZ)) {
                this.search_tag_color_indices[i] = 2;
            }
            /*
            if (nn.getDatasetName().equalsIgnoreCase(dataset_name)) {
                search_tag_origin_map_ind[i] = true;
            }
            */
            search_tag_ids[i] = nn.getID();
        }
        
        
        /*
        HashMap <String, ArrayList<Integer>> entrezSortPositionMap = selection.getEntrezSortPositionMap(analysis.global_map_config);
        
        for (int i=0; i<network_neighbors.size(); i++) {
            NetworkNeighbor nn = network_neighbors.get(i);
            ArrayList <Integer> qtag_i = nn.getQueryEntrezPositions(entrezSortPositionMap);
            HashMap <Integer, Boolean> tags_i = nn.getNeighborEntrezPositions(entrezSortPositionMap);
            
            ArrayList <Integer> search_tag_positions_i = new ArrayList <Integer> ();
            ArrayList <Integer> is_search_query_i = new ArrayList <Integer> ();
            
            for (int gene_col_index : qtag_i) {
                if (gene_col_index >= c_start && gene_col_index < (c_start+nEntrez)) {
                    int gene_col_index_at_scroll = gene_col_index - c_start;
                    search_tag_positions_i.add(gene_col_index_at_scroll);
                    is_search_query_i.add(1);
                }
            }
            
            for (int gene_col_index : tags_i.keySet()) {
                if (gene_col_index >= c_start && gene_col_index < (c_start+nEntrez)) {
                    int gene_col_index_at_scroll = gene_col_index - c_start;
                    search_tag_positions_i.add(gene_col_index_at_scroll);
                    is_search_query_i.add(0);
                }
            }
            this.search_tag_positions.add(search_tag_positions_i);
            this.is_search_query.add(is_search_query_i);
        }
        */

    }
    
    public String globalHeatmapDataAsJSON () {
        return new Gson().toJson(this);
    }
 
}
