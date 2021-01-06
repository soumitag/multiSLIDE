/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.utils.FileHandler;

/**
 *
 * @author Soumita Ghosh and Abhik Datta
 */
public class BipartiteLinkageGraph implements Serializable {
    
    class LinkerColorName {
    
        public int[] color; 
        public String name;
        
        LinkerColorName(int[] color, String name) {
            this.color = color;
            this.name = name;
        }
    }
    
    private static final long serialVersionUID = 1L;
    
    public String display_name;
    public String filename;
    public String delimiter;
    
    public String dataset_name_1;
    public String dataset_name_2;
    public String column_name_1;
    public String column_name_2;
    
    //public HashMap <String, HashMap <String, int[]>> linkage;
    private final HashMap <String, HashMap <String, LinkerColorName>> linkage;
    
    public BipartiteLinkageGraph(
            String display_name, String filename, String filepath, String delimiter, HashMap <String, String> filename_map
    ) throws DataParsingException, IOException {
        this.display_name = display_name;
        this.filename = filename;
        this.delimiter = delimiter;
        
        linkage = new HashMap <> ();
        String fqpath = filepath + File.separator + filename;
        this.loadFromFile(fqpath, filename_map);
    }
    
    public boolean isConnected(String column_value_1, String column_value_2) {
        if (linkage.containsKey(column_value_1)) {
            if (linkage.get(column_value_1).containsKey(column_value_2)) {
                return true;
            }
        }
        return false;
    }
    
    public int[] getColor(String column_value_1, String column_value_2) {
        return linkage.get(column_value_1).get(column_value_2).color;
    }
    
    public String getName(String column_value_1, String column_value_2) {
        return linkage.get(column_value_1).get(column_value_2).name;
    }
    
    public HashMap <String, int[]> getNameColorMap() {
        HashMap <String, int[]> map = new HashMap <> ();
        for (String column_value_1: this.linkage.keySet()) {
            for (String column_value_2: this.linkage.keySet()) {
                LinkerColorName cn = this.linkage.get(column_value_1).get(column_value_2);
                map.put(cn.name, cn.color);
            }
        }
        return map;
    }
    
    private void loadFromFile(String filename, HashMap <String, String> filename_map) 
    throws DataParsingException, IOException {
        
        List<List<String>> d = FileHandler.loadDelimListData (filename, "\t", false);
        
        String[] parts_1 = d.get(0).get(0).split(",");
        
        String fname_1 = parts_1[0].toLowerCase();
        if (!filename_map.containsKey(fname_1)) {
            String err_msg = "The given filename '" + fname_1 + "' has no match. Available filenames are:";
            int i = 1;
            for (String f : filename_map.keySet()) {
                err_msg += " " + i + ")" + f;
                i++;
            }
            throw new DataParsingException(err_msg);
        }
        this.dataset_name_1 = filename_map.get(fname_1);
        
        this.column_name_1 = parts_1[1];
        
        String[] parts_2 = d.get(0).get(1).split(",");
        
        String fname_2 = parts_2[0].toLowerCase();
        if (!filename_map.containsKey(fname_2)) {
            String err_msg = "The given filename '" + fname_2 + "' has no match. Available filenames are:";
            int i = 1;
            for (String f : filename_map.keySet()) {
                err_msg += " " + i + ")" + f;
                i++;
            }
            throw new DataParsingException(err_msg);
        }
        this.dataset_name_2 = filename_map.get(fname_2);
        
        this.column_name_2 = parts_2[1];
        
        for (int i=1; i<d.size(); i++) {
            
            int[] colors = new int[3];
            String name = "";
            
            if (d.get(i).size() == 4) {
                
                String[] rgb = d.get(i).get(2).split(",");
                if (rgb.length != 3) {
                    throw new DataParsingException("Invalid RGB string '" + d.get(i).get(2) + "'. Must contain three comma separated values between 0-255.");
                }
                colors[0] = Integer.parseInt(rgb[0]);
                colors[1] = Integer.parseInt(rgb[1]);
                colors[2] = Integer.parseInt(rgb[2]);
                name = d.get(i).get(3);
                
            } else if (d.get(i).size() == 3) {
                
                String[] rgb = d.get(i).get(2).split(",");
                if (rgb.length != 3) {
                    throw new DataParsingException("Invalid RGB string '" + d.get(i).get(2) + "'. Must contain three comma separated values between 0-255.");
                }
                colors[0] = Integer.parseInt(rgb[0]);
                colors[1] = Integer.parseInt(rgb[1]);
                colors[2] = Integer.parseInt(rgb[2]);
                
            } else {
                colors[0] = 175;
                colors[1] = 175;
                colors[2] = 175;
            }
            
            if (linkage.containsKey(d.get(i).get(0))) {
                linkage.get(d.get(i).get(0)).put(d.get(i).get(1), new LinkerColorName(colors, name));
            } else {
                HashMap <String, LinkerColorName> h = new HashMap <> ();
                h.put(d.get(i).get(1), new LinkerColorName(colors, name));
                linkage.put(d.get(i).get(0), h);
            }
        }
    }
    
    
}
