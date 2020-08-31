/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.utils.FileHandler;

/**
 *
 * @author Soumita Ghosh and Abhik Datta
 */
public class AnalysisState implements Serializable {
    
    public String analysis_name;
    public String species;
    
    public ListOrderedMap <String, DatasetSpecs> dataset_specs_map;
    public DataSelectionState data_selection_state;
    public GlobalMapConfig global_map_config;
    public Map <String, MapConfig> map_configs;
    
    public Map <String, String[][]> raw_data_map;
    
    
    public AnalysisState(AnalysisContainer analysis, String session_folder_path) throws DataParsingException, IOException {
        
        String timestamp = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss").format(new java.util.Date());
        this.analysis_name = analysis.analysis_name + "_" + timestamp;
        this.species = analysis.species;
        
        this.data_selection_state = analysis.data_selection_state;
        this.global_map_config = analysis.global_map_config;
        
        // load dataset specs
        dataset_specs_map = DatasetSpecs.loadSpecsMap(session_folder_path, analysis.analysis_name);
        
        // get map configs
        map_configs = new HashMap <> ();
        for (String df_name: analysis.data.datasets.keySet()) {
            if (analysis.heatmaps.containsKey(df_name)) {
                map_configs.put(df_name, analysis.heatmaps.get(df_name).getMapConfig());
            }
        }
        
        // load raw data
        raw_data_map = new HashMap <> ();
        for (String key : dataset_specs_map.keyList()) {
            DatasetSpecs specs = dataset_specs_map.get(key);
            String f = specs.getFilenameWithinAnalysisFolder();
            String filepath = analysis.base_path + File.separator + f;
            raw_data_map.put(f, FileHandler.loadDelimData(filepath, specs.delimiter, false));
        }
        
        // update analysis name with timestamp
        for (String key: dataset_specs_map.keyList()) {
            DatasetSpecs specs = dataset_specs_map.get(key);
            specs.changeAnalysisName(this.analysis_name);
        }
    }
    
    public List <DatasetSpecs> getDatasetSpecs() {
        List <DatasetSpecs> a = new ArrayList <> ();
        for (String name: dataset_specs_map.keyList()) {
            a.add(dataset_specs_map.get(name));
        }
        return a;
    }
    
    public String asJSON() {
        return new Gson().toJson(this);
    }
}
