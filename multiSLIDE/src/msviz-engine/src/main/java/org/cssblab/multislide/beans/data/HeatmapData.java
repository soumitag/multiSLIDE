/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.FilteredSortedData;

public class HeatmapData {
    
    private transient final int min = 0;
    private transient final int max = 255;
    
    private transient final int nBins;
    private transient final int nSamples;
    private transient final int nEntrez;
    //private transient final String[] selected_genetags;
    //private transient final String[] selected_phenotypes;
    
    //private transient final int NUM_DISPLAY_SAMPLES = 40;
    //private transient final int NUM_DISPLAY_FEATURES = 30;
    
    public String title;
    public double bin_colors[][];
    public int cell_bin_indices[][];
    public String column_headers[];
    public String entrez[];
    public String row_names[];
    public int gene_tags[];
    public double gene_tag_colors[][];
    public double phenotypes[][][];
    public String phenotype_labels[];
    public String gene_group_keys[];
    public String colorbar_keys[];
    
    public HeatmapData(int r_start, int c_start, int nSamples, int nEntrez, FilteredSortedData data, Heatmap heatmap, String dataset_name, AnalysisContainer analysis) {
        
        this.title = dataset_name;
        
        r_start = (r_start < 0) ? 0 : r_start;
        int current_start = analysis.global_map_config.current_sample_start;
        r_start = (r_start >= data.nSamples) ? current_start : r_start;
        
        c_start = (c_start < 0) ? 0 : c_start;
        current_start = analysis.global_map_config.current_feature_start;
        c_start = (c_start >= data.nGenes) ? current_start : c_start;
        
        int r_end = r_start + nSamples;
        r_end = Math.min(r_end, data.nSamples-1);
        
        int c_end = c_start + nEntrez;
        c_end = Math.min(c_end, data.nGenes-1);
        
        this.nBins = heatmap.hist.nBins;
        this.nSamples = r_end - r_start;
        this.nEntrez = c_end - c_start;
        
        String[] geneGroupNames = analysis.data_selection_state.getGeneGroupNames();
        String[] geneGroupTypes = analysis.data_selection_state.getGeneGroupNames();
        
        bin_colors = new double[heatmap.hist.nBins][3];
        cell_bin_indices = new int[nSamples][nEntrez];
        column_headers = new String[nEntrez];
        entrez = new String[nEntrez];
        row_names = new String[nSamples];
        gene_tags = new int[nEntrez];
        gene_tag_colors = new double[geneGroupNames.length][3];
        phenotypes = new double[nSamples][data.phenotypes.size()][3];
        phenotype_labels = new String[data.phenotypes.size()];
        gene_group_keys = new String[geneGroupNames.length];
        colorbar_keys = new String[5];
        
        this.bin_colors = heatmap.hist.rgb;
        
        int[][] bin_nos = data.getExpressionBinNos(dataset_name);
        String[] rownames = data.getRowNames(dataset_name);
        String[] colheaders = data.getColumnHeaders(dataset_name, heatmap.column_label);
        
        int r_count = 0;
        for (int r=r_start; r<r_end; r++) {
            int c_count = 0;
            for (int c=c_start; c<c_end; c++) {
                cell_bin_indices[r_count][c_count++] = bin_nos[r][c];
            }
            row_names[r_count++] = rownames[r];
        }
        
        int c_count = 0;
        for (int c=c_start; c<c_end; c++) {
            column_headers[c_count] = colheaders[c];
            entrez[c_count] = data.entrez.get(c);
            gene_tags[c_count++] = data.gene_tags.get(c);
        }
        
        int p_count = 0;
        for (Map.Entry pair : data.phenotypes.entrySet()) {
            phenotype_labels[p_count] = (String)pair.getKey();
            String[] phenotype_values = data.getPhenotypes((String)pair.getKey());
            r_count = 0;
            for (int r=r_start; r<r_end; r++) {
                phenotypes[r_count++][p_count] = analysis.data.clinical_info.getPhenotypeColor((String)pair.getKey(), phenotype_values[r]);
            }
            p_count++;
        }
        
        for (int i=0; i<geneGroupNames.length; i++) {
            gene_tag_colors[i] = analysis.group_colors.get(geneGroupNames[i]);
            gene_group_keys[i] = geneGroupTypes[i] + "_" + geneGroupNames[i];
        }
        
        analysis.global_map_config.current_sample_start = r_start;
        analysis.global_map_config.current_feature_start = c_start;
        
        double tick_space = (heatmap.hist.MAX_VAL-heatmap.hist.MIN_VAL)/4.0;
        for (int i=0; i<5; i++) {
            double v = heatmap.hist.MIN_VAL + i*tick_space;
            this.colorbar_keys[i] = String.format("%5.2e",v);
        }

    }
    
    public String heatmapDataAsJSON () {
        return new Gson().toJson(this);
    }
    
    /*
    public HeatmapData(int nSamples, int nEntrez, int nBins, String[] selected_phenotypes, String[] selected_genetags) {
        this.nBins = nBins;
        this.nSamples = nSamples;
        this.nEntrez = nEntrez;
        this.selected_genetags = selected_genetags;
        this.selected_phenotypes = selected_phenotypes;
        
        bin_colors = new double[nBins][3];
        cell_bin_indices = new int[nSamples][nEntrez];
        column_headers = new String[nEntrez];
        row_names = new String[nSamples];
        gene_tags = new int[nEntrez][selected_genetags.length][3];
        phenotypes = new int[nSamples][selected_phenotypes.length][3];
    }
    
    public String heatmapDataAsJSON () {
        Random r = new Random();
        for(int i = 0; i < this.bin_colors.length; i++){
            this.bin_colors[i][0] = r.nextInt(this.max-this.min) + this.min;
            this.bin_colors[i][1] = r.nextInt(this.max-this.min) + this.min;
            this.bin_colors[i][2] = r.nextInt(this.max-this.min) + this.min;
        }
        for(int i = 0; i < this.cell_bin_indices.length; i++){
            for(int j = 0; j < this.cell_bin_indices[i].length; j++){
                this.cell_bin_indices[i][j] = r.nextInt(nBins);
            }
        }
        for(int i = 0; i < this.column_headers.length; i++){
            this.column_headers[i] = "gene" + i;
        }
        for(int i = 0; i < this.row_names.length; i++){
            this.row_names[i] = "patient" + i;
        }
        
        for (int gt=0; gt < selected_genetags.length; gt++) {
            for(int i = 0; i < this.column_headers.length; i++){
                this.gene_tags[i][gt][0] = r.nextInt(this.max-this.min) + this.min;
                this.gene_tags[i][gt][1] = r.nextInt(this.max-this.min) + this.min;
                this.gene_tags[i][gt][2] = r.nextInt(this.max-this.min) + this.min;
            }
        }
        
        for (int pt=0; pt < selected_phenotypes.length; pt++) {
            for(int i = 0; i < this.row_names.length; i++){
                this.phenotypes[i][pt][0] = r.nextInt(this.max-this.min) + this.min;
                this.phenotypes[i][pt][1] = r.nextInt(this.max-this.min) + this.min;
                this.phenotypes[i][pt][2] = r.nextInt(this.max-this.min) + this.min;
            }
        }
        
        String json = new Gson().toJson(this);
        
        return json;

    }
    */
    
}
