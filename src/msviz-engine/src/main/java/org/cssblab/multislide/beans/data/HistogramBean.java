/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

import java.io.Serializable;

/**
 *
 * @author Soumita
 */
public class HistogramBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final float x_tick_length;
    private final float y_tick_length;
    private final int num_y_ticks;
    private final float offset_top;
    private final float offset_left;
    
    public int[][] hist_colors;
    public float[] hist_bin_centers;
    public float bar_width;
    public float bar_height;
    public float[] bar_x;
    public float bar_y;
    public float svg_width;
    public float svg_height;
    public float x_tick_ystart;
    public float x_tick_yend;
    public float[] y_tick_ypositions;
    public float y_tick_xstart;
    public float y_tick_xend;
    public String[] x_tick_labels;
    public String[] y_tick_labels;
    
    public HistogramBean(int nBins) {
        
        x_tick_length = 5;
        y_tick_length = 5;
        num_y_ticks = 10;
        
        offset_top = 20;
        offset_left = 20;
        svg_width = 200;
        svg_height = 200;
        bar_width = (float)20.0;
        bar_height = (float)50.0;
        
        hist_colors = new int[nBins][3];
        hist_bin_centers = new float[nBins];
        bar_x = new float[nBins];
        x_tick_labels = new String[nBins];
        y_tick_ypositions = new float[num_y_ticks];
        y_tick_labels = new String[num_y_ticks];
        
        for (int i=0; i<nBins; i++) {
            bar_x[i] = i*bar_width + offset_left;
            hist_bin_centers[i] = i*bar_width + bar_width/(float)2.0 + offset_left;
            x_tick_labels[i] = i + "";
        }
        bar_y = offset_top;
        
        x_tick_ystart = bar_height + offset_top;
        x_tick_yend = x_tick_ystart + x_tick_length;
        
        float y_tick_gap = bar_height/num_y_ticks;
        for (int i=0; i<num_y_ticks; i++) {
            y_tick_ypositions[i] = y_tick_gap*i + offset_top;
            y_tick_labels[i] = i + "";
        }
        y_tick_xstart = offset_left - y_tick_length;
        y_tick_xend = y_tick_xstart + y_tick_length;
    }
}
