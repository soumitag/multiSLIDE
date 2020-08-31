/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.layouts;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cssblab.multislide.beans.data.BipartiteLinkageGraph;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.GlobalMapConfig;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.NetworkNeighbor;
import org.cssblab.multislide.structure.data.FeatureIndex;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author Soumita Ghosh and Abhik Datta
 */
public class MapLinkLayout implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static double MAP_LINKS_HEIGHT_XS = 200.0;
    public static double MAP_LINKS_HEIGHT_S = 250.0;
    public static double MAP_LINKS_HEIGHT_M = 300.0;
    public static double MAP_LINKS_HEIGHT_L = 350.0;
    public static double MAP_LINKS_HEIGHT_XL = 400.0;
    
    public String dataset_name_1;
    public String dataset_name_2;
    public double start;
    public double end;
    public double[] gene_centres_1;
    public double[] gene_centres_2;
    public List <int[]> mappings;
    public List <int[]> mapping_colors;
    public double cell_width_height;
    
    public MapLinkLayout(String dataset_name_1,
                         String dataset_name_2,
                         int nPhenotypes, 
                         String mapResolution, 
                         int mapOrientation,
                         FeatureIndex feature_manifest_1,
                         FeatureIndex feature_manifest_2,
                         AnalysisContainer analysis
    ) throws MultiSlideException {
        
        this.dataset_name_1 = dataset_name_1;
        this.dataset_name_2 = dataset_name_2;
        this.start = 0;
        
        double info_hist_panel_width;
        if (mapOrientation == GlobalMapConfig.MAP_ORIENTATION_GENES_ALONG_X) {
            info_hist_panel_width = HeatmapLayout.INFO_HIST_PANEL_WIDTH_GENES_ALONG_X;
        } else if (mapOrientation == GlobalMapConfig.MAP_ORIENTATION_SAMPLES_ALONG_X) {
            info_hist_panel_width = HeatmapLayout.INFO_HIST_PANEL_WIDTH_SAMPLES_ALONG_X;
        } else {
            throw new MultiSlideException("Non standard map orientation: " + mapOrientation);
        }
        
        if (mapResolution.equalsIgnoreCase("XS")) {
            cell_width_height = HeatmapLayout.HEATMAP_CELL_SIZE_XS;
            this.end = this.start + MAP_LINKS_HEIGHT_XS;
            
        } else if (mapResolution.equalsIgnoreCase("S")) {
            cell_width_height = HeatmapLayout.HEATMAP_CELL_SIZE_S;
            this.end = this.start + MAP_LINKS_HEIGHT_S;
            
        } else if (mapResolution.equalsIgnoreCase("M")) {
            cell_width_height = HeatmapLayout.HEATMAP_CELL_SIZE_M;
            this.end = this.start + MAP_LINKS_HEIGHT_M;
            
        } else if (mapResolution.equalsIgnoreCase("L")) {
            cell_width_height = HeatmapLayout.HEATMAP_CELL_SIZE_L;
            this.end = this.start + MAP_LINKS_HEIGHT_L;
            
        } else if (mapResolution.equalsIgnoreCase("XL")) {
            cell_width_height = HeatmapLayout.HEATMAP_CELL_SIZE_XL;
            this.end = this.start + MAP_LINKS_HEIGHT_XL;
            
        } else {
            throw new MultiSlideException("Non standard map resolution: " + mapResolution);
        }
        
        double map_start_x = HeatmapLayout.LEFT_BUFFER + info_hist_panel_width + HeatmapLayout.INFO_HIST_PANEL_MAP_GAP 
                            + nPhenotypes*cell_width_height + HeatmapLayout.PHENOTYPE_MAP_BUFFER;
        
        gene_centres_1 = new double[feature_manifest_1.count()];
        for (int i=0; i<feature_manifest_1.count(); i++) {
            gene_centres_1[i] = map_start_x + (i+0.5)*cell_width_height;
        }
        
        gene_centres_2 = new double[feature_manifest_2.count()];
        for (int i=0; i<feature_manifest_2.count(); i++) {
            gene_centres_2[i] = map_start_x + (i+0.5)*cell_width_height;
        }
        
        /*
            create mappings
        */
        mappings = new ArrayList <> ();
        mapping_colors = new ArrayList <> ();
        
        /*
            check if any user provided mapping exists
        */
        BipartiteLinkageGraph linkage = null;
        boolean isFlipped = false;
        if (analysis.data_selection_state.user_defined_between_omics_linkages.containsKey(dataset_name_1 + "_" + dataset_name_2)) {
            linkage = analysis.data_selection_state.user_defined_between_omics_linkages.get(dataset_name_1 + "_" + dataset_name_2);
            isFlipped = false;
        } else if (analysis.data_selection_state.user_defined_between_omics_linkages.containsKey(dataset_name_2 + "_" + dataset_name_1)) {
            linkage = analysis.data_selection_state.user_defined_between_omics_linkages.get(dataset_name_2 + "_" + dataset_name_1);
            isFlipped = true;
        }
        
        /*
            if user provided mapping is present, it will override default 
            (linker inferred) mappings
        */
        if (linkage != null) {
            
            List <List<String>> feature_ids_1 = analysis.data.selected.getFeatureIDs(
                        linkage.dataset_name_1, analysis.global_map_config, new String[]{linkage.column_name_1});
            
            List <List<String>> feature_ids_2 = analysis.data.selected.getFeatureIDs(
                        linkage.dataset_name_2, analysis.global_map_config, new String[]{linkage.column_name_2});
            
            for (int i=0; i<feature_ids_1.size(); i++) {
                for (int j=0; j<feature_ids_2.size(); j++) {
                    if (linkage.isConnected(feature_ids_1.get(i).get(0), feature_ids_2.get(j).get(0))) {
                        int[] color = linkage.getColor(feature_ids_1.get(i).get(0), feature_ids_2.get(j).get(0));
                        mapping_colors.add(color);
                        if (isFlipped) {
                            mappings.add(new int[]{j,i});
                        } else {
                            mappings.add(new int[]{i,j});
                        }
                    }
                }
            }
            
        } else {
            
            DatasetSpecs spec_1 = analysis.data.datasets.get(dataset_name_1).specs;
            DatasetSpecs spec_2 = analysis.data.datasets.get(dataset_name_2).specs;

            if (spec_1.has_linker && spec_2.has_linker) {
                /*
                    mappings will be there only if both datasets have linker columns
                */
                List<List<String>> d1 = analysis.data.selected.views.get(dataset_name_1)
                        .getLinkers(analysis.global_map_config);

                List<List<String>> d2 = analysis.data.selected.views.get(dataset_name_2)
                        .getLinkers(analysis.global_map_config);

                /*
                    create hashmap on dataset 2 for quick lookup: linker_value -> ArrayList of _id
                    each linker can be linked to multiple _ids, we will draw all lines
                */
                Map <String, List <Long>> look_up_table = feature_manifest_2.getLinkerIdMap();


                for (List<String> _id_linker: d1) {

                    String linker_value = _id_linker.get(1);

                    /*
                    Only add a line if the linker exists in dataset 2
                    */
                    if (look_up_table.containsKey(linker_value)) {    
                        /*
                        Get the position of the linker in the first dataset
                        */
                        Long _id_1 = Long.parseLong(_id_linker.get(0));
                        Long pos_1 = feature_manifest_1.indexOf(_id_1);

                        /*
                        Get the position(s) of the linker in the second dataset
                        */
                        for (Long _id_2: look_up_table.get(linker_value)) {
                            Long pos_2 = feature_manifest_2.indexOf(_id_2);
                            if (pos_2 != null) {
                                mappings.add(new int[]{Math.toIntExact(pos_1), Math.toIntExact(pos_2)});
                                mapping_colors.add(new int[]{175,175,175});
                            }
                        }
                    }
                }

            }
        }
    }
    
    public String asJSON () {
        String json = new Gson().toJson(this);
        return json;
    }
}
