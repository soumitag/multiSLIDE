/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.layouts;

import java.io.Serializable;
import com.google.gson.Gson;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.GlobalMapConfig;
import org.cssblab.multislide.structure.MultiSlideException;

/**
 *
 * @author Soumita
 */
public class MapContainerLayout implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public int nMaps;
    public double map_tops[];
    public double map_lefts[];
    public double map_height;
    public double map_width;
    public double legend_left_x;
    public String dataset_names[];
    
    private transient double gap_x;
    private transient double gap_y;
    private transient int n_maps_per_row;
    
    public MapContainerLayout (int nMaps) {
        this.nMaps = nMaps;
        this.gap_x = 15;
        this.gap_y = 15;
    }
    
    public void computeMapLayout (AnalysisContainer analysis, HeatmapLayout heatmapLayout, int gridLayout, int mapOrientation) 
    throws MultiSlideException {
        
        if (gridLayout < -2) {
            this.n_maps_per_row = 1;
        } else if (gridLayout == -2) {
            this.n_maps_per_row = 1;
        } else if (gridLayout == -1) {
            this.n_maps_per_row = this.nMaps;
        } else if (gridLayout > this.nMaps) {
            this.n_maps_per_row = this.nMaps;
        } else {
            this.n_maps_per_row = gridLayout;
        }
        
        if (mapOrientation == GlobalMapConfig.MAP_ORIENTATION_GENES_ALONG_X) {
            this.map_height = heatmapLayout.svg_height;
            this.map_width = heatmapLayout.svg_width;
        } else if (mapOrientation == GlobalMapConfig.MAP_ORIENTATION_SAMPLES_ALONG_X) {
            this.map_height = heatmapLayout.svg_width;
            this.map_width = heatmapLayout.svg_height;
        } else {
            throw new MultiSlideException("Non standard map orientation: " + mapOrientation);
        }

        map_tops = new double[nMaps];
        map_lefts = new double[nMaps];
        int col_no = 0;
        for (int i = 0; i < nMaps; i++) {
            map_lefts[i] = (map_width + this.gap_x)*col_no;
            col_no++;
            if ((col_no%n_maps_per_row) == 0) {
                col_no = 0;
            }
        }
        int row_no = -1;
        for (int i = 0; i < nMaps; i++) {
            if ((i%n_maps_per_row) == 0) {
                row_no++;
            }
            map_tops[i] = (map_height + this.gap_y)*row_no;
        }
        
        this.legend_left_x = (map_width + gap_x) * n_maps_per_row;

        dataset_names = analysis.data.fs_data.getDatasetNames();
    }
    
    public String mapContainerLayoutAsJSON () {
        String json = new Gson().toJson(this);
        return json;
    }
}
