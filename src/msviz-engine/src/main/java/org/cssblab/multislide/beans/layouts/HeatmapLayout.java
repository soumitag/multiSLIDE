/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.layouts;

import com.google.gson.Gson;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.io.Serializable;
import org.cssblab.multislide.structure.GlobalMapConfig;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.NetworkNeighbor;

/**
 *
 * @author Soumita
 */
public class HeatmapLayout implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public static byte HEATMAP_CELL_SIZE_XS = 6;
    public static byte HEATMAP_CELL_SIZE_S = 9;
    public static byte HEATMAP_CELL_SIZE_M = 12;
    public static byte HEATMAP_CELL_SIZE_L = 15;
    public static byte HEATMAP_CELL_SIZE_XL = 18;

    public static byte MIN_COL_HEADER_HEIGHT = 10;
    public static byte MIN_ROW_LABEL_WIDTH = 10;
    public static int MIN_MAP_HEIGHT = 100;
    public static int MIN_MAP_WIDTH = 100;
    public static int MAX_COLORBAR_HEIGHT = 600;

    public static byte GENE_TAG_HEIGHT = 10;
    public static byte BETWEEN_GENE_TAG_BUFFER = 4;
    
    public static double LEFT_BUFFER = 10;
    public static double INFO_HIST_PANEL_WIDTH_GENES_ALONG_X = 300;
    public static double INFO_HIST_PANEL_WIDTH_SAMPLES_ALONG_X = 190;
    public static double PHENOTYPE_MAP_BUFFER = 10;
    public static double INFO_HIST_PANEL_MAP_GAP = 15;

    public String name;
    public byte cell_width_height;
    public byte gene_tag_height = HeatmapLayout.GENE_TAG_HEIGHT;
    public double gene_tag_background_width;
    //public double map_orientation = 1.0;
    public double column_left_x[];
    public double row_top_y[];
    public double header_height;
    public double row_name_width;
    public double column_header_text_anchor_x[];
    public double column_header_text_anchor_y;
    public double row_name_text_anchor_x;
    public double row_name_text_anchor_y[];
    public double phenotype_tag_anchor_x[];
    public double phenotype_tag_anchor_y[];
    public double phenotype_label_anchor_x[];
    public double phenotype_label_anchor_y;
    public double genetag_anchor_y[];
    public double svg_height;
    public double svg_width;
    public double plot_title_x;
    public double plot_title_y;
    public double row_label_font_size;
    public double column_label_font_size;
    public double phenotype_label_font_size;
    public double settings_icon_left;
    public double settings_icon_size;
    

    public double search_tag_radius;
    public double[][] search_tag_background_rects;
    public double[][] search_tag_lines_xxy;
    public double colorbar_cell_x;
    public double colorbar_cell_y[];
    public double colorbar_cell_width = 12;
    public double colorbar_cell_height;
    public double colorbar_tick_width = 5;
    public double colorbar_tick_y[];
    public double colorbar_tick_x1_x2[];
    public double colorbar_tick_text_x;
    public double missing_value_colorbar_tick_y;
    
    public double info_hist_panel_width;
    public double info_panel_top;
    public double info_panel_left;
    public double hist_left;
    public double hist_top;
    public double hist_width;
    public double hist_height;

    private transient double bottom_buffer = 0;
    private transient final double map_feature_label_buffer = 5;
    private transient final double header_genetag_buffer = 6;
    private transient final double genetag_map_buffer = 6;
    private transient final double plot_title_height_genes_along_x = 0;
    private transient final double plot_title_height_samples_along_x = 0;
    private transient final double map_search_tag_buffer = 4;
    private transient final double between_search_tags_buffer = 2;
    private transient final double row_label_color_bar_buffer = 6;
    private transient final double colorbar_width;
    private transient final double search_tag_bar_height;
    private transient final double font_sz;
    private transient final double plot_title_height;
    
    private transient double colorbar_height;
    
    
    public HeatmapLayout(String name,
                         int nSamples, 
                         int nEntrez, 
                         int nPhenotypes, 
                         int nGeneTags, 
                         String mapResolution, 
                         int mapOrientation,
                         ArrayList <NetworkNeighbor> network_neighbors,
                         int nColors, 
                         double colHeaderHeight, 
                         double rowLabelWidth
    ) throws MultiSlideException {
        
        this.name = name;
        
        if (mapResolution.equalsIgnoreCase("XS")) {
            cell_width_height = HEATMAP_CELL_SIZE_XS;
            font_sz = 6.0;
            settings_icon_size = 30.0;
            colorbar_width = 55.0;
            
        } else if (mapResolution.equalsIgnoreCase("S")) {
            cell_width_height = HEATMAP_CELL_SIZE_S;
            font_sz = 8.0;
            settings_icon_size = 35.0;
            colorbar_width = 60.0;
            
        } else if (mapResolution.equalsIgnoreCase("M")) {
            cell_width_height = HEATMAP_CELL_SIZE_M;
            font_sz = 10.0;
            settings_icon_size = 40.0;
            colorbar_width = 65.0;
            
        } else if (mapResolution.equalsIgnoreCase("L")) {
            cell_width_height = HEATMAP_CELL_SIZE_L;
            font_sz = 12.0;
            settings_icon_size = 50.0;
            colorbar_width = 75.0;
            
        } else if (mapResolution.equalsIgnoreCase("XL")) {
            cell_width_height = HEATMAP_CELL_SIZE_XL;
            font_sz = 14.0;
            settings_icon_size = 50.0;
            colorbar_width = 85.0;
            
        } else {
            throw new MultiSlideException("Non standard map resolution: " + mapResolution);
        }

        search_tag_bar_height = this.cell_width_height*1.5;
        
        if (mapOrientation == GlobalMapConfig.MAP_ORIENTATION_GENES_ALONG_X) {
            this.plot_title_height = this.plot_title_height_genes_along_x;
            
            this.info_hist_panel_width = INFO_HIST_PANEL_WIDTH_GENES_ALONG_X;
            this.info_panel_top = 50.0;
            this.info_panel_left = 25.0;
            this.hist_left = 10.0;
            this.hist_top = 275.0;
            this.hist_width = 270.0;
            this.hist_height = 150.0;
            
            this.createHeatmapLayout_GenesAlongX (nSamples, nEntrez, nPhenotypes, nGeneTags, mapResolution, network_neighbors, nColors, colHeaderHeight, rowLabelWidth);
            
        } else if (mapOrientation == GlobalMapConfig.MAP_ORIENTATION_SAMPLES_ALONG_X) {
            
            this.plot_title_height = this.plot_title_height_samples_along_x;
            
            this.info_hist_panel_width = INFO_HIST_PANEL_WIDTH_SAMPLES_ALONG_X;
            this.info_panel_top = 50.0;
            this.info_panel_left = 10.0;
            this.hist_width = 270.0;
            this.hist_height = 150.0;
            this.hist_left = this.hist_width + 20.0;    // 10 + hist_width + 10 (info panel width = hist_width)
            this.hist_top = 50.0;
            
            this.createHeatmapLayout_SamplesAlongX (nSamples, nEntrez, nPhenotypes, nGeneTags, mapResolution, network_neighbors, nColors, colHeaderHeight, rowLabelWidth);
            
        } else {
            throw new MultiSlideException("Non standard map orientation: " + mapOrientation);
        }
           
    }

    public final void createHeatmapLayout_GenesAlongX(
            int nSamples,
            int nEntrez,
            int nPhenotypes,
            int nGeneTags,
            String mapResolution,
            ArrayList<NetworkNeighbor> network_neighbors,
            int nColors,
            double colHeaderHeight,
            double rowLabelWidth
    ) {
        
        double search_tag_height = this.search_tag_bar_height;
        //this.search_tag_radius = (search_tag_height/2.0);

        if (colHeaderHeight >= HeatmapLayout.MIN_COL_HEADER_HEIGHT) {
            header_height = colHeaderHeight;
        }
        if (rowLabelWidth >= HeatmapLayout.MIN_ROW_LABEL_WIDTH) {
            row_name_width = rowLabelWidth;
        }
        
        int nSearchTags = network_neighbors.size();

        double map_height = cell_width_height * nSamples;
        double map_width = cell_width_height * nEntrez;
        
        map_height = Math.max(map_height, HeatmapLayout.MIN_MAP_HEIGHT);
        
        column_left_x = new double[nEntrez];
        row_top_y = new double[nSamples];
        column_header_text_anchor_x = new double[nEntrez];
        column_header_text_anchor_y = plot_title_height + header_height + header_genetag_buffer - 5;
        row_name_text_anchor_x = LEFT_BUFFER + INFO_HIST_PANEL_WIDTH_GENES_ALONG_X + INFO_HIST_PANEL_MAP_GAP 
                                    + nPhenotypes*cell_width_height + PHENOTYPE_MAP_BUFFER + map_width + map_feature_label_buffer;
        row_name_text_anchor_y = new double[nSamples];        
        phenotype_tag_anchor_x = new double[nPhenotypes];
        phenotype_tag_anchor_y = new double[nSamples];
        phenotype_label_anchor_x = new double[nPhenotypes];
        phenotype_label_anchor_y = plot_title_height + header_height + header_genetag_buffer + 
                                    nGeneTags*HeatmapLayout.GENE_TAG_HEIGHT + (nGeneTags-1)*HeatmapLayout.BETWEEN_GENE_TAG_BUFFER;
        
        genetag_anchor_y = new double[nGeneTags];
        gene_tag_background_width = nEntrez * this.cell_width_height;
        
        svg_height = plot_title_height + header_height + header_genetag_buffer 
                        + nGeneTags * HeatmapLayout.GENE_TAG_HEIGHT 
                        + (nGeneTags - 1) * HeatmapLayout.BETWEEN_GENE_TAG_BUFFER
                        + genetag_map_buffer + map_height + map_search_tag_buffer 
                        + (this.search_tag_bar_height+this.between_search_tags_buffer) * nSearchTags + bottom_buffer;

        svg_width = LEFT_BUFFER + INFO_HIST_PANEL_WIDTH_GENES_ALONG_X + INFO_HIST_PANEL_MAP_GAP + nPhenotypes * cell_width_height + PHENOTYPE_MAP_BUFFER
                                + map_width + map_feature_label_buffer + row_name_width + row_label_color_bar_buffer + colorbar_width;

        plot_title_x = (LEFT_BUFFER + INFO_HIST_PANEL_WIDTH_GENES_ALONG_X + INFO_HIST_PANEL_MAP_GAP)/2.0;
        plot_title_y = 90.0;
        
        this.row_label_font_size = font_sz;
        this.column_label_font_size = font_sz;
        this.phenotype_label_font_size = font_sz+1;
        
        this.colorbar_height = Math.min(Math.max(map_height-12, HeatmapLayout.MIN_MAP_HEIGHT), HeatmapLayout.MAX_COLORBAR_HEIGHT);
        this.colorbar_cell_y = new double[nColors];
        this.colorbar_cell_height = new BigDecimal(this.colorbar_height/(nColors-1)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        
        this.colorbar_cell_x = LEFT_BUFFER + nPhenotypes*cell_width_height + PHENOTYPE_MAP_BUFFER + 
                               map_width + map_feature_label_buffer + row_name_width + row_label_color_bar_buffer;

        colorbar_tick_y = new double[5];
        this.search_tag_background_rects = new double[nSearchTags][4];
        
        this.colorbar_tick_x1_x2 = new double[2];
        colorbar_tick_x1_x2[0] = this.colorbar_cell_x + this.colorbar_cell_width;
        colorbar_tick_x1_x2[1] = this.colorbar_cell_x + this.colorbar_cell_width + this.colorbar_tick_width;
        
        this.colorbar_tick_text_x = this.colorbar_cell_x + 19;

        double map_start_x = LEFT_BUFFER + INFO_HIST_PANEL_WIDTH_GENES_ALONG_X + INFO_HIST_PANEL_MAP_GAP 
                                + nPhenotypes*this.cell_width_height + PHENOTYPE_MAP_BUFFER;
        double map_start_y = plot_title_height + this.header_height + this.header_genetag_buffer + 
                          nGeneTags*HeatmapLayout.GENE_TAG_HEIGHT + (nGeneTags-1)*HeatmapLayout.BETWEEN_GENE_TAG_BUFFER + this.genetag_map_buffer;

        for(int i = 0; i < this.column_left_x.length; i++){
            this.column_left_x[i] = map_start_x + i*this.cell_width_height; 
        }
        
        for(int i = 0; i < this.row_top_y.length; i++){
            this.row_top_y[i] = map_start_y + i*this.cell_width_height; 
        }
        
        for(int i = 0; i < this.column_header_text_anchor_x.length; i++){
            this.column_header_text_anchor_x[i] = map_start_x + (i+0.7)*this.cell_width_height;
        }
        
        for(int i = 0; i < this.row_name_text_anchor_y.length; i++){
            this.row_name_text_anchor_y[i] = map_start_y + (i+0.5)*this.cell_width_height;
            this.phenotype_tag_anchor_y[i] = map_start_y + (i+0.5)*this.cell_width_height;
        }
        
        for(int i = 0; i < nPhenotypes; i++) {
            this.phenotype_tag_anchor_x[i] = LEFT_BUFFER + INFO_HIST_PANEL_WIDTH_GENES_ALONG_X + INFO_HIST_PANEL_MAP_GAP + (i+0.5)*this.cell_width_height; 
            this.phenotype_label_anchor_x[i] = LEFT_BUFFER + INFO_HIST_PANEL_WIDTH_GENES_ALONG_X + INFO_HIST_PANEL_MAP_GAP + (i+0.5)*this.cell_width_height; 
        }
        
        for(int i = 0; i < nGeneTags; i++) {
            if (i==0) {
                this.genetag_anchor_y[i] = plot_title_height + this.header_height + this.header_genetag_buffer + i*HeatmapLayout.GENE_TAG_HEIGHT; 
            } else {
                this.genetag_anchor_y[i] = plot_title_height + this.header_height + this.header_genetag_buffer + i*(HeatmapLayout.GENE_TAG_HEIGHT+HeatmapLayout.BETWEEN_GENE_TAG_BUFFER); 
            }
        }

        this.search_tag_lines_xxy = new double[nSearchTags][3];     //line is from x1,y to x2,y
        for (int i=0; i<network_neighbors.size(); i++) {
            this.search_tag_lines_xxy[i][0] = map_start_x;
            this.search_tag_lines_xxy[i][1] = map_start_x + map_width;
            double line_y = map_start_y + map_height + map_search_tag_buffer + i*(this.search_tag_bar_height+this.between_search_tags_buffer) + 0.5*search_tag_bar_height;
            this.search_tag_lines_xxy[i][2] = line_y;
            
            this.search_tag_background_rects[i][0] = map_start_x;
            this.search_tag_background_rects[i][1] = map_start_y + map_height + this.map_search_tag_buffer + i*(this.search_tag_bar_height+this.between_search_tags_buffer);
            this.search_tag_background_rects[i][2] = map_width;
            this.search_tag_background_rects[i][3] = this.search_tag_bar_height;
            
        }
        
        for (int i = 0; i < nColors; i++) {
            this.colorbar_cell_y[i] = map_start_y + i * this.colorbar_cell_height;
            this.colorbar_cell_y[i] = new BigDecimal(this.colorbar_cell_y[i]).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        double tick_space = this.colorbar_height / 4.0;
        for (int i = 0; i < 5; i++) {
            this.colorbar_tick_y[i] = map_start_y + i * tick_space;
        }

        this.missing_value_colorbar_tick_y = this.colorbar_tick_y[4] + 10.0 + 0.5*this.cell_width_height;
    }
    
    
    public final void createHeatmapLayout_SamplesAlongX(
            int nSamples,
            int nEntrez,
            int nPhenotypes,
            int nGeneTags,
            String mapResolution,
            ArrayList<NetworkNeighbor> network_neighbors,
            int nColors,
            double colHeaderHeight,
            double rowLabelWidth
    ) {
        
        double search_tag_height = this.search_tag_bar_height;
        
        if (colHeaderHeight >= HeatmapLayout.MIN_COL_HEADER_HEIGHT) {
            header_height = colHeaderHeight;
        }
        if (rowLabelWidth >= HeatmapLayout.MIN_ROW_LABEL_WIDTH) {
            row_name_width = rowLabelWidth;
        }
        
        int nSearchTags = network_neighbors.size();
        if (nSearchTags == 0) {
            bottom_buffer = 0;
        } else {
            bottom_buffer = 1;
        }
        
        double map_height = cell_width_height * nSamples;
        double map_width = cell_width_height * nEntrez;
        map_height = Math.max(map_height, HeatmapLayout.MIN_MAP_HEIGHT);
        
        svg_height = header_height + 
                     header_genetag_buffer + nGeneTags * HeatmapLayout.GENE_TAG_HEIGHT + (nGeneTags - 1) * HeatmapLayout.BETWEEN_GENE_TAG_BUFFER + 
                     genetag_map_buffer + map_height + map_search_tag_buffer + search_tag_height * nSearchTags + bottom_buffer;

        svg_width = LEFT_BUFFER + INFO_HIST_PANEL_WIDTH_SAMPLES_ALONG_X + INFO_HIST_PANEL_MAP_GAP  + 
                    plot_title_height + nPhenotypes * cell_width_height + PHENOTYPE_MAP_BUFFER + 
                    map_width + map_feature_label_buffer + row_name_width;
        
        double map_start_x = LEFT_BUFFER + INFO_HIST_PANEL_WIDTH_SAMPLES_ALONG_X + INFO_HIST_PANEL_MAP_GAP  + 
                                plot_title_height + nPhenotypes*this.cell_width_height + PHENOTYPE_MAP_BUFFER;
        double map_start_y = bottom_buffer + search_tag_height * nSearchTags + map_search_tag_buffer;
        
        plot_title_x = 90;
        plot_title_y = LEFT_BUFFER + INFO_HIST_PANEL_WIDTH_SAMPLES_ALONG_X/2.0 + INFO_HIST_PANEL_MAP_GAP;
        
        this.row_label_font_size = font_sz;
        this.column_label_font_size = font_sz;
        this.phenotype_label_font_size = font_sz+1;
        
        /*=============Cells============*/
        column_left_x = new double[nEntrez];
        row_top_y = new double[nSamples];
        for(int i = 0; i < this.column_left_x.length; i++){
            this.column_left_x[i] = map_start_x + i*this.cell_width_height; 
        }
        
        for(int i = 0; i < this.row_top_y.length; i++){
            this.row_top_y[i] = map_start_y + i*this.cell_width_height; 
        }
        /*=============Cells============*/
        
        /*=============Column Headers============*/
        column_header_text_anchor_x = new double[nEntrez];
        column_header_text_anchor_y = map_start_y + map_height + genetag_map_buffer + 
                                      nGeneTags * HeatmapLayout.GENE_TAG_HEIGHT + (nGeneTags - 1) * HeatmapLayout.BETWEEN_GENE_TAG_BUFFER + 
                                      header_genetag_buffer;
        //column_header_text_anchor_y = plot_title_height + header_height + header_genetag_buffer - 5;
        for(int i = 0; i < this.column_header_text_anchor_x.length; i++){
            this.column_header_text_anchor_x[i] = map_start_x + (i+0.5)*this.cell_width_height;
        }
        /*=============Column Headers============*/
        
        /*=============Row Names and Phenotypes============*/
        row_name_text_anchor_x = map_start_x + map_width + map_feature_label_buffer;
        row_name_text_anchor_y = new double[nSamples];
        
        phenotype_tag_anchor_x = new double[nPhenotypes];
        phenotype_tag_anchor_y = new double[nSamples];
        phenotype_label_anchor_x = new double[nPhenotypes];
        phenotype_label_anchor_y = map_start_y + map_height + 5;
        /*phenotype_label_anchor_y = plot_title_height + header_height + header_genetag_buffer + 
                                   nGeneTags*HeatmapLayout.GENE_TAG_HEIGHT + (nGeneTags-1)*HeatmapLayout.BETWEEN_GENE_TAG_BUFFER;
        */
        for(int i = 0; i < nSamples; i++){
            this.row_name_text_anchor_y[i] = map_start_y + (i+0.5)*this.cell_width_height;
            this.phenotype_tag_anchor_y[i] = map_start_y + (i+0.5)*this.cell_width_height;
        }
        
        for(int i = 0; i < nPhenotypes; i++) {
            this.phenotype_tag_anchor_x[i] = LEFT_BUFFER + INFO_HIST_PANEL_WIDTH_SAMPLES_ALONG_X + INFO_HIST_PANEL_MAP_GAP  + 
                                                this.plot_title_height + (i+0.5)*this.cell_width_height; 
            this.phenotype_label_anchor_x[i] = LEFT_BUFFER + INFO_HIST_PANEL_WIDTH_SAMPLES_ALONG_X + INFO_HIST_PANEL_MAP_GAP  + 
                                                this.plot_title_height + (i+0.5)*this.cell_width_height; 
        }
        /*=============Row Names and Phenotypes============*/
        
        /*=============Gene Tags============*/
        genetag_anchor_y = new double[nGeneTags];
        gene_tag_background_width = nEntrez * this.cell_width_height;
        double t = map_start_y + map_height + genetag_map_buffer;
        for(int i = 0; i < nGeneTags; i++) {
            if (i==0) {
                this.genetag_anchor_y[i] = t; 
            } else {
                this.genetag_anchor_y[i] = t + i*(HeatmapLayout.GENE_TAG_HEIGHT+HeatmapLayout.BETWEEN_GENE_TAG_BUFFER); 
            }
        }
        /*=============Gene Tags============*/

        /*================Color Bar==============*/
        this.colorbar_height = Math.min(Math.max(map_width-12, HeatmapLayout.MIN_MAP_WIDTH), HeatmapLayout.MAX_COLORBAR_HEIGHT);
        this.colorbar_cell_y = new double[nColors];
        this.colorbar_cell_height = new BigDecimal(this.colorbar_height/(nColors-1)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        this.colorbar_cell_x = row_label_color_bar_buffer + header_height + 
                               header_genetag_buffer + nGeneTags * HeatmapLayout.GENE_TAG_HEIGHT + (nGeneTags - 1) * HeatmapLayout.BETWEEN_GENE_TAG_BUFFER + 
                               genetag_map_buffer + map_height + map_search_tag_buffer + search_tag_height * nSearchTags + bottom_buffer;
        /*
        this.colorbar_cell_x = left_buffer + nPhenotypes*cell_width_height + phenotype_map_buffer + 
                               map_height + map_feature_label_buffer + header_height + row_label_color_bar_buffer;
        */
        colorbar_tick_y = new double[5];
        
        this.colorbar_tick_x1_x2 = new double[2];
        colorbar_tick_x1_x2[0] = this.colorbar_cell_x + this.colorbar_cell_width;
        colorbar_tick_x1_x2[1] = this.colorbar_cell_x + this.colorbar_cell_width + this.colorbar_tick_width;
       
        this.colorbar_tick_text_x = this.colorbar_cell_x + 19;
        
        for (int i = 0; i < nColors; i++) {
            this.colorbar_cell_y[i] = map_start_x + 50.0 + i * this.colorbar_cell_height;
            this.colorbar_cell_y[i] = new BigDecimal(this.colorbar_cell_y[i]).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        double tick_space = this.colorbar_height / 4.0;
        for (int i = 0; i < 5; i++) {
            this.colorbar_tick_y[i] = map_start_x + 50.0 + i * tick_space;
        }

        this.missing_value_colorbar_tick_y = this.colorbar_tick_y[4] + 10.0 + 0.5*this.cell_width_height;
        /*================Color Bar==============*/
        
        /*======Neighbourhood Search Tags=======*/
        //this.search_tag_radius = (search_tag_height/2.0);
        this.search_tag_background_rects = new double[nSearchTags][4];
        this.search_tag_lines_xxy = new double[nSearchTags][3];     //line is from x1,y to x2,y
        
        for (int i=0; i<network_neighbors.size(); i++) {
            this.search_tag_lines_xxy[i][0] = map_start_x;
            this.search_tag_lines_xxy[i][1] = map_start_x + map_width;
            this.search_tag_lines_xxy[i][2] = bottom_buffer + i*(this.search_tag_bar_height+this.between_search_tags_buffer) + 0.5*search_tag_bar_height;
            
            this.search_tag_background_rects[i][0] = map_start_x;
            this.search_tag_background_rects[i][1] = bottom_buffer + i*(this.search_tag_bar_height+this.between_search_tags_buffer);
            this.search_tag_background_rects[i][2] = map_width;
            this.search_tag_background_rects[i][3] = this.search_tag_bar_height;  
            //this.search_tag_background_rects[i][3] = this.search_tag_radius * 3;  
        }
        /*======Neighbourhood Search Tags=======*/
    }
    
    public String heatmapLayoutAsJSON () {
        String json = new Gson().toJson(this);
        return json;
    }

}