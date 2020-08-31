/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.layouts;

import java.io.Serializable;
import com.google.gson.Gson;
import java.util.ArrayList;
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
    public double map_heights[];
    public double map_widths[];
    public double map_links_tops[];
    public double map_links_lefts[];
    public double map_links_heights[];
    public double map_links_widths[];
    public double legend_left_x;
    public String dataset_names[];
    
    private static final transient double GAP_X = 15;
    private static final transient double GAP_Y = 5;
    private static final transient double LEGEND_WIDTH = 250;
    
    public MapContainerLayout (int nMaps) {
        this.nMaps = nMaps;
    }
    
    public void computeMapLayout (
            AnalysisContainer analysis, ArrayList <HeatmapLayout> heatmapLayouts, int gridLayout, String mapResolution, int mapOrientation) 
    throws MultiSlideException {
        
        dataset_names = new String[nMaps];
        map_tops = new double[nMaps];
        map_lefts = new double[nMaps];
        map_heights = new double[nMaps];
        map_widths = new double[nMaps];
        
        map_links_tops = new double[nMaps-1];
        map_links_lefts = new double[nMaps-1];
        map_links_heights = new double[nMaps-1];
        map_links_widths = new double[nMaps-1];
        
        legend_left_x = 10;
        
        double map_links_height;
        if (mapResolution.equalsIgnoreCase("XS")) {
            map_links_height = MapLinkLayout.MAP_LINKS_HEIGHT_XS;
        } else if (mapResolution.equalsIgnoreCase("S")) {
            map_links_height = MapLinkLayout.MAP_LINKS_HEIGHT_S;
        } else if (mapResolution.equalsIgnoreCase("M")) {
            map_links_height = MapLinkLayout.MAP_LINKS_HEIGHT_M;
        } else if (mapResolution.equalsIgnoreCase("L")) {
            map_links_height = MapLinkLayout.MAP_LINKS_HEIGHT_L;
        } else if (mapResolution.equalsIgnoreCase("XL")) {
            map_links_height = MapLinkLayout.MAP_LINKS_HEIGHT_XL;
        } else {
            throw new MultiSlideException("Non standard map resolution: " + mapResolution);
        }
        
        if (mapOrientation == GlobalMapConfig.MAP_ORIENTATION_GENES_ALONG_X) {

            double y_pos = 0;
            for (int i = 0; i < nMaps; i++) {
                map_lefts[i] = legend_left_x + LEGEND_WIDTH + MapContainerLayout.GAP_X;
                dataset_names[i] = heatmapLayouts.get(i).name;
                map_heights[i] = heatmapLayouts.get(i).svg_height;
                map_widths[i] = heatmapLayouts.get(i).svg_width;
                map_tops[i] = y_pos;
                y_pos = y_pos + map_heights[i] + map_links_height;
                
                if (i < nMaps-1) {
                    map_links_lefts[i] = legend_left_x + LEGEND_WIDTH + MapContainerLayout.GAP_X;
                    map_links_heights[i] = map_links_height;
                    map_links_tops[i] = map_tops[i] + map_heights[i];
                    map_links_widths[i] = Math.max(map_widths[i], heatmapLayouts.get(i+1).svg_width);
                }
            }
            
        }  else if (mapOrientation == GlobalMapConfig.MAP_ORIENTATION_SAMPLES_ALONG_X) {

            double x_pos = legend_left_x + LEGEND_WIDTH + MapContainerLayout.GAP_X;
            for (int i = 0; i < nMaps; i++) {
                map_tops[i] = MapContainerLayout.GAP_Y;
                dataset_names[i] = heatmapLayouts.get(i).name;
                map_heights[i] = heatmapLayouts.get(i).svg_width;
                map_widths[i] = heatmapLayouts.get(i).svg_height;
                map_lefts[i] = x_pos;
                x_pos = x_pos + map_widths[i] + map_links_height;
                
                if (i < nMaps-1) {
                    map_links_tops[i] = MapContainerLayout.GAP_Y;
                    map_links_widths[i] = map_links_height;
                    map_links_heights[i] = Math.max(map_heights[i], heatmapLayouts.get(i+1).svg_width);
                    map_links_lefts[i] = map_lefts[i] + map_widths[i];
                }
            }
        }
        
        //legend_left_x = map_lefts[nMaps-1] + map_widths[nMaps-1] + GAP_X;
        
    }
    
    public String mapContainerLayoutAsJSON () {
        String json = new Gson().toJson(this);
        return json;
    }
}
