/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.graphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.cssblab.multislide.structure.MultiSlideException;

/**
 *
 * @author abhikdatta
 */

enum ColorMaps {
    BBKY, BLWR, BYR, GBR, BRBG, BWR, CIVIDIS, COOLWARM, INFERNO, MAGMA, PIYG, PLASMA, PRGN, RDBU, RDYLBU, RDYLGN, SEISMIC, SPECTRAL, VIRIDIS;
}

public class PyColorMaps implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    String db_path;
    public HashMap <ColorMaps, ArrayList<short[][]>> palette;
    
    public PyColorMaps(String path) {
        this.db_path = path;
        this.parseColorMap(this.db_path);
    }
    
    public final void parseColorMap(String path){
        
        palette = new HashMap <ColorMaps, ArrayList<short[][]>>();
        File directory = new File(path);
        File[] fList = directory.listFiles();
        
        for (int i = 0; i < fList.length; i++) {
            if (fList[i].isFile()) {
                String file_name = fList[i].getName().substring(0,fList[i].getName().length()-4);
                
                try {
                    
                    BufferedReader br = new BufferedReader(new FileReader(fList[i].getPath()));
                    String line;
                    
                    ArrayList<short[][]> global_color_arr = new ArrayList<short[][]>();
                    while((line = br.readLine())!= null){
                        String[] colors = line.split(";", -1);
                        short[][] line_color_arr = new short[colors.length][3];
                        
                        for(int x = 0; x < colors.length; x++){
                            
                            String[] rgb = colors[x].split(",", -1);
                            short[] rgb_val = new short[rgb.length];
                            for(int y = 0; y < rgb.length; y++){
                                rgb_val[y] = Short.parseShort(rgb[y]);
                            }
                            line_color_arr[x] = rgb_val;
                        }
                        global_color_arr.add(line_color_arr);
                    } 
                    palette.put(ColorMaps.valueOf(file_name.toUpperCase()), global_color_arr);
                
                } catch (Exception e) {
                    System.out.println(e);
                }
            
            }
        }
    }
    
    private boolean checkMapname(String mapname) {
        for (ColorMaps c : ColorMaps.values()) {
            if (c.name().equals(mapname)) {
                return true;
            }
        }
        return false;
    }
    
    public short[][] getColors(String mapname, short nColors) throws MultiSlideException {
        mapname = mapname.toUpperCase();
        if (nColors < 3 || nColors > 255) {
            throw new MultiSlideException("Number of allowed colors is between 3 and 255, but requested number of colors is: " + nColors);
        } else if (!this.checkMapname(mapname)) {
            throw new MultiSlideException("Invalid color map name: " + mapname);
        } else {
            return palette.get(ColorMaps.valueOf(mapname)).get(nColors-3);
        }
    }
    
}
