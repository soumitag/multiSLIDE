/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.cssblab.multislide.beans.data.DatasetSpecs;

/**
 *
 * @author Soumita
 */
public class MetaData {
    
    public HashMap <String, HashMap <String, String[]>> values;
    //public HashMap <String, HashMap <String, Integer>> metadata_colname_pos_map;
    public HashMap <String, HashMap <String, Integer>> entrezPosMaps;
    public HashMap <String, boolean[]> metadata_col_ind;
    public HashMap <String, Boolean> entrezMaster;
    public HashMap <String, Integer> entrezColIndices;
    
    MetaData() {
        
        values = new HashMap <String, HashMap <String, String[]>> ();
        //metadata_colname_pos_map = new HashMap <String, HashMap <String, Integer>> ();
        entrezPosMaps = new HashMap <String, HashMap <String, Integer>> ();
        metadata_col_ind = new HashMap <String, boolean[]> ();
        entrezMaster = new HashMap <String, Boolean> ();
        entrezColIndices = new HashMap <String, Integer> ();
    }
    
    public String[] getMetaDataColumnNames(String dataset_name) {
        HashMap <String, String[]> dataset_metadata = this.values.get(dataset_name);
        String[] a = new String[dataset_metadata.keySet().size()];
        dataset_metadata.keySet().toArray(a);
        return a;
    }

    
    public void extract(String[] dataset_names, ArrayList <String[][]> raw_datasets, DatasetSpecs[] dataset_specs) {
        for (int i=0; i<raw_datasets.size(); i++) {
            this.extract(dataset_names[i], raw_datasets.get(i), dataset_specs[i]);
        }
    }
    
    public void extract(String dataset_name, String[][] raw_data, DatasetSpecs specs) {
        
        raw_data = trim(raw_data);
        
        HashMap <String, String[]> values_i = new HashMap <String, String[]> ();
        //HashMap <String, Integer> metadata_colname_pos_map_i = new HashMap <String, Integer> ();
        HashMap <String, Integer> entrezPosMap_i = new HashMap <String, Integer>();
        boolean[] metadata_col_ind_i = new boolean[raw_data[0].length];
        int entrez_col_index = -1;
        String entrez_colname = specs.getMappedColname("entrez_2021158607524066");

        for (int col = 0; col < raw_data[0].length; col++) {
            metadata_col_ind_i[col] = false;
            for (int i = 0; i < specs.metadata_columns.length; i++) {
                if (raw_data[0][col].equals(specs.metadata_columns[i])) {
                    metadata_col_ind_i[col] = true;
                    //metadata_colname_pos_map_i.put(specs.metadata_columns[i], col);
                    break;
                }
            }
            
            if (metadata_col_ind_i[col]) {
                String[] metadat_col_i_values = new String[raw_data.length-1];
                for (int row=1; row<raw_data.length; row++) {
                    metadat_col_i_values[row-1] = raw_data[row][col];
                }
                values_i.put(raw_data[0][col], metadat_col_i_values);
                
                if (raw_data[0][col].equals(entrez_colname)) {
                    entrez_col_index = col;
                }
            }
        }
        
        for (int row=1; row<raw_data.length; row++) {
            String entrez_id = raw_data[row][entrez_col_index];
            this.entrezMaster.put(entrez_id, true);
            entrezPosMap_i.put(entrez_id, row-1);
        }
        
        values.put(dataset_name, values_i);
        //metadata_colname_pos_map.put(dataset_name, metadata_colname_pos_map_i);
        entrezPosMaps.put(dataset_name, entrezPosMap_i);
        metadata_col_ind.put(dataset_name, metadata_col_ind_i);
        entrezColIndices.put(dataset_name, entrez_col_index);
        
    }
    
    public ArrayList <String[][]> getNonMetaDataColumns(String[] dataset_names, ArrayList <String[][]> raw_datasets) {
        ArrayList <String[][]> data = new ArrayList <String[][]> ();
        for (int i=0; i<raw_datasets.size(); i++) {
            data.add(getNonMetaDataColumns(dataset_names[i], raw_datasets.get(i)));
        }
        return data;
    }
    
    public String[][] getNonMetaDataColumns(String dataset_name, String[][] raw_data) {
        String[][] data = new String[raw_data.length][raw_data[0].length-this.values.get(dataset_name).size()];
        boolean[] metadata_column_ind = metadata_col_ind.get(dataset_name);
        int col_index = 0;
        for (int col = 0; col < raw_data[0].length; col++) {
            if (!metadata_column_ind[col]) {
                for (int row=0; row<raw_data.length; row++) {
                    data[row][col_index] = raw_data[row][col];
                }
                col_index++;
            }
        }
        return data;
    }
    
    public HashMap <String, String> getIdentifiers(String dataset_name, String metadata_column_name, ArrayList <String> entrez) {
        String[] col_values = values.get(dataset_name).get(metadata_column_name);
        HashMap <String, Integer> entrezPosMap_i = entrezPosMaps.get(dataset_name);
        HashMap <String, String> identifiers = new HashMap <String, String> ();
        for (int i=0; i<entrez.size(); i++) {
            System.out.println(entrez.get(i) + ":" + i);
            if(!entrezPosMap_i.containsKey(entrez.get(i))){
                identifiers.put(entrez.get(i), "-");
            } else {
                int row_index = entrezPosMap_i.get(entrez.get(i));
                //System.out.println(entrez.get(i) + ":" + row_index);
                identifiers.put(entrez.get(i), col_values[row_index]);
            }
        }
        return identifiers;
    }
    
    public String[][] trim(String[][] raw_data) {
        String[][] cleaned_data = new String[raw_data.length][raw_data[0].length];
        for (int i=0; i<raw_data.length; i++) {
            for (int j=0; j<raw_data[0].length; j++) {
                cleaned_data[i][j] = raw_data[i][j].trim();
            }
        }
        return cleaned_data;
    }
    
}
