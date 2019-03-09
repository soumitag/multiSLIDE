/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.layouts;

import com.google.gson.Gson;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.cssblab.multislide.structure.MultiSlideException;

/**
 *
 * @author Soumita
 */
public class HeatmapLayout {
    
    public static byte HEATMAP_CELL_SIZE_XS = 6;
    public static byte HEATMAP_CELL_SIZE_S = 9;
    public static byte HEATMAP_CELL_SIZE_M = 12;
    public static byte HEATMAP_CELL_SIZE_L = 15;
    public static byte HEATMAP_CELL_SIZE_XL = 18;
    
    public static byte MIN_COL_HEADER_HEIGHT = 10;
    public static byte MIN_ROW_LABEL_WIDTH = 10;
    
    public byte cell_width_height;
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
    public ArrayList <ArrayList <double[]>> search_tags_xy;
    public double[][] search_tag_lines_xxy;
    public double[][] search_tag_background_rects;
    public double colorbar_cell_x;
    public double colorbar_cell_y[];
    public double colorbar_cell_width = 12;
    public double colorbar_cell_height;
    public double colorbar_tick_width = 5;
    public double colorbar_tick_y[];
        public double colorbar_tick_x1_x2[];
        public double colorbar_tick_text_x;
        public double missing_value_colorbar_tick_y;
    
    //private transient final int nGeneTags;
    //private transient final int nPhenotypes;
    //private transient final int nSearchTags;
    private transient final double left_buffer = 10;
    private transient final double bottom_buffer = 5;
    private transient final double map_feature_label_buffer = 5;
    private transient final double phenotype_map_buffer = 10;
    private transient final double header_genetag_buffer = 6;
    private transient final double genetag_map_buffer = 6;
    private transient final double plot_title_height = 0;
        private transient final double map_search_tag_buffer = 4;
            private transient final double between_search_tags_buffer = 2;
        private transient final double row_label_color_bar_buffer = 6;
        private transient final double colorbar_width;
            private transient final double search_tag_bar_height;
        //private transient final int nColors;
        
    public HeatmapLayout(int nSamples, 
                         int nEntrez, 
                         int nPhenotypes, 
                         int nGeneTags, 
                         String mapResolution, 
                         String prevMapResolution, 
                         ArrayList <ArrayList <Integer>> search_tags, 
                         int nColors, 
                         double colHeaderHeight, 
                         double rowLabelWidth) throws MultiSlideException {
     
        double font_sz;
        
        if (mapResolution.equalsIgnoreCase("XS")) {
            cell_width_height = HEATMAP_CELL_SIZE_XS;
            font_sz = 6.0;
            settings_icon_size = 30.0;
            colorbar_width = 55.0;
            //header_height = 80;
            //row_name_width = 80;
        } else if (mapResolution.equalsIgnoreCase("S")) {
            cell_width_height = HEATMAP_CELL_SIZE_S;
            font_sz = 8.0;
            settings_icon_size = 35.0;
            colorbar_width = 60.0;
            //header_height = 90;
            //row_name_width = 90;
        } else if (mapResolution.equalsIgnoreCase("M")) {
            cell_width_height = HEATMAP_CELL_SIZE_M;
            font_sz = 10.0;
            settings_icon_size = 40.0;
            colorbar_width = 65.0;
            //header_height = 100;
            //row_name_width = 100;
        } else if (mapResolution.equalsIgnoreCase("L")) {
            cell_width_height = HEATMAP_CELL_SIZE_L;
            font_sz = 12.0;
            settings_icon_size = 50.0;
            colorbar_width = 75.0;
            //header_height = 100;
            //row_name_width = 100;
        } else if (mapResolution.equalsIgnoreCase("XL")) {
            cell_width_height = HEATMAP_CELL_SIZE_XL;
            font_sz = 14.0;
            settings_icon_size = 50.0;
            colorbar_width = 85.0;
            //header_height = 120;
            //row_name_width = 120;
        } else {
            throw new MultiSlideException("Non standard map resolution: " + mapResolution);
        }
        
        double search_tag_height = this.cell_width_height;
        this.search_tag_radius = (search_tag_height/2.0-1.5);
        
        /*
        if (!mapResolution.equalsIgnoreCase(prevMapResolution)) {
            double scaling_factor = this.getScalingFactor(mapResolution, prevMapResolution);
            colHeaderHeight = scaling_factor*colHeaderHeight;
            rowLabelWidth = scaling_factor*rowLabelWidth;
        }
        */
        
        if (colHeaderHeight >= HeatmapLayout.MIN_COL_HEADER_HEIGHT) {
            header_height = colHeaderHeight;
        }
        if (rowLabelWidth >= HeatmapLayout.MIN_ROW_LABEL_WIDTH) {
            row_name_width = rowLabelWidth;
        }
        
        //this.nGeneTags = nGeneTags;
        //this.nPhenotypes = nPhenotypes;
        int nSearchTags = search_tags.size();

        double map_height = cell_width_height * nSamples;
        double map_width = cell_width_height * nEntrez;
        column_left_x = new double[nEntrez];
        row_top_y = new double[nSamples];
        column_header_text_anchor_x = new double[nEntrez];
        column_header_text_anchor_y = plot_title_height + header_height + header_genetag_buffer - 5;
        row_name_text_anchor_x = left_buffer + nPhenotypes*cell_width_height + phenotype_map_buffer + 
                map_width + map_feature_label_buffer;
        row_name_text_anchor_y = new double[nSamples];        
        phenotype_tag_anchor_x = new double[nPhenotypes];
        phenotype_tag_anchor_y = new double[nSamples];
        phenotype_label_anchor_x = new double[nPhenotypes];
        phenotype_label_anchor_y = plot_title_height + header_height + header_genetag_buffer + nGeneTags*cell_width_height;
        genetag_anchor_y = new double[nGeneTags];
        svg_height = plot_title_height + header_height + header_genetag_buffer + nGeneTags*cell_width_height +
                    genetag_map_buffer + map_height + map_search_tag_buffer + search_tag_height*nSearchTags + bottom_buffer;
        svg_width = left_buffer + nPhenotypes*cell_width_height + phenotype_map_buffer + 
                map_width + map_feature_label_buffer + row_name_width + row_label_color_bar_buffer + colorbar_width;
        
        plot_title_x = svg_width/2.0;
        plot_title_y = 20;
        
        this.row_label_font_size = font_sz;
        this.column_label_font_size = font_sz;
        this.phenotype_label_font_size = font_sz;
        
        search_tag_bar_height = this.cell_width_height - 2;
        
        this.search_tag_lines_xxy = new double[nSearchTags][3];     //line is from x1,y to x2,y
        
        this.colorbar_cell_y = new double[nColors];
        this.colorbar_cell_height = new BigDecimal(map_height/nColors).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();;
        this.colorbar_cell_x = left_buffer + nPhenotypes*cell_width_height + phenotype_map_buffer + 
                map_width + map_feature_label_buffer + row_name_width + row_label_color_bar_buffer;
        
        colorbar_tick_y = new double[5];
        this.search_tag_background_rects = new double[nSearchTags][4];
        
        this.colorbar_tick_x1_x2 = new double[2];
        colorbar_tick_x1_x2[0] = this.colorbar_cell_x + this.colorbar_cell_width;
        colorbar_tick_x1_x2[1] = this.colorbar_cell_x + this.colorbar_cell_width + this.colorbar_tick_width;
        
        this.colorbar_tick_text_x = this.colorbar_cell_x + 19;
        
        createHeatmapLayout(map_height, map_width, search_tags, nGeneTags, nPhenotypes, nSearchTags, nColors);

    }
    
    public final void createHeatmapLayout (double map_height, double map_width, ArrayList <ArrayList <Integer>> search_tags, int nGeneTags, int nPhenotypes, int nSearchTags, int nColors) {
        
        double map_start_x = this.left_buffer + nPhenotypes*this.cell_width_height + this.phenotype_map_buffer;
        double map_start_y = plot_title_height + this.header_height + this.header_genetag_buffer + nGeneTags*this.cell_width_height + this.genetag_map_buffer;
        
        for(int i = 0; i < this.column_left_x.length; i++){
            this.column_left_x[i] = map_start_x + i*this.cell_width_height; 
        }
        
        for(int i = 0; i < this.row_top_y.length; i++){
            this.row_top_y[i] = map_start_y + i*this.cell_width_height; 
        }
        
        for(int i = 0; i < this.column_header_text_anchor_x.length; i++){
            this.column_header_text_anchor_x[i] = map_start_x + (i+0.5)*this.cell_width_height;
        }
        
        for(int i = 0; i < this.row_name_text_anchor_y.length; i++){
            this.row_name_text_anchor_y[i] = map_start_y + (i+0.5)*this.cell_width_height;
            this.phenotype_tag_anchor_y[i] = map_start_y + (i+0.5)*this.cell_width_height;
        }
        
        for(int i = 0; i < nPhenotypes; i++) {
            this.phenotype_tag_anchor_x[i] = this.left_buffer + (i+0.5)*this.cell_width_height; 
            this.phenotype_label_anchor_x[i] = this.left_buffer + (i+0.5)*this.cell_width_height; 
        }
        
        for(int i = 0; i < nGeneTags; i++) {
            this.genetag_anchor_y[i] = plot_title_height + this.header_height + this.header_genetag_buffer + i*this.cell_width_height; 
        }

        this.search_tags_xy = new ArrayList();
        for (int i=0; i<nSearchTags; i++) {
            ArrayList <Integer> tags_i = search_tags.get(i);
            this.search_tag_lines_xxy[i][0] = map_start_x + (tags_i.get(0))*this.cell_width_height;
            this.search_tag_lines_xxy[i][1] = map_start_x + (tags_i.get(tags_i.size()-1) + 1)*this.cell_width_height;
            this.search_tag_lines_xxy[i][2] = map_start_y + map_height + map_search_tag_buffer + i*(this.search_tag_bar_height+this.between_search_tags_buffer) + 0.5*search_tag_bar_height;
            ArrayList <double[]> tag_i_xys = new ArrayList <double[]> ();
            for (int j=0; j<search_tags.get(i).size(); j++) {
                int gene_col_index = search_tags.get(i).get(j);
                double[] xy = new double[2];
                xy[0] = map_start_x + (gene_col_index + 0.5)*this.cell_width_height;
                xy[1] = this.search_tag_lines_xxy[i][2];
                tag_i_xys.add(xy);
            }
            this.search_tags_xy.add(tag_i_xys);
            this.search_tag_background_rects[i][0] = map_start_x;
            this.search_tag_background_rects[i][1] = map_start_y + map_height + this.map_search_tag_buffer + i*(this.search_tag_bar_height+this.between_search_tags_buffer);
            this.search_tag_background_rects[i][2] = map_width;
            this.search_tag_background_rects[i][3] = this.search_tag_bar_height;
        }
        
        for (int i=0; i<nColors; i++) {
            this.colorbar_cell_y[i] = map_start_y + i*this.colorbar_cell_height;
            this.colorbar_cell_y[i] = new BigDecimal(this.colorbar_cell_y[i]).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        
        double tick_space = (map_height-cell_width_height)/4.0;
        for (int i=0; i<5; i++) {
            this.colorbar_tick_y[i] = map_start_y + i*tick_space;
        }
        
        this.missing_value_colorbar_tick_y = this.colorbar_tick_y[4] + 10.0 + 0.5*this.cell_width_height;
    }
    
    public String heatmapLayoutAsJSON () {
        String json = new Gson().toJson(this);
        return json;
    }
    
}