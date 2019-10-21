/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.graphics;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 *
 * @author Soumita
 */
public class HeatmapContainer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static int TYPE_IMAGE = 0;
    public static int TYPE_ARRAY = 1;
    
    public int type;
    public BufferedImage current_raster_as_image;
    public short[][][] current_raster_as_array;
    
    public HeatmapContainer(BufferedImage current_raster_as_image) {
        this.current_raster_as_image = current_raster_as_image;
        this.type = HeatmapContainer.TYPE_IMAGE;
    }
    
    public HeatmapContainer(short[][][] current_raster_as_array) {
        this.current_raster_as_array = current_raster_as_array;
        this.type = HeatmapContainer.TYPE_ARRAY;
    }
    
}
