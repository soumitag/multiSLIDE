package org.cssblab.multislide.structure;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cssblab.multislide.beans.data.BipartiteLinkageGraph;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.layouts.MapLinkLayout;
import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.structure.data.FeatureIndex;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class Serializer implements Serializable {
    
    public static String TYPE_OBJ = "object";
    public static String TYPE_JSON = "json";
    
    private static final long serialVersionUID = 1L;
    
    public Serializer(){}
    
    public String serializeAnalysis(AnalysisContainer analysis, String filepath, String session_folder_path, String type) throws Exception {
        
        AnalysisState state = new AnalysisState(analysis, session_folder_path);

        if (type.equals(Serializer.TYPE_OBJ)) {
            
            ObjectOutputStream oos
                    = new ObjectOutputStream(new FileOutputStream(filepath + File.separator + state.analysis_name + ".mslide"));
            
            oos.writeObject(state);
            Utils.log_info("Analysis saved.");
            return state.analysis_name + ".mslide";
            
        } else if (type.equals(Serializer.TYPE_JSON)) {
            
            String json = state.asJSON();
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(filepath + File.separator + state.analysis_name + ".mslide", false));
            writer.append(json);
            writer.close();
            return state.analysis_name + ".mslide";
            
        } else {
            throw new MultiSlideException("Unknown type: " + type + ". Can be 'object' or 'json'.");
        }

    }
    
    public AnalysisState loadAnalysis(String filepath, String type) throws Exception {
        
        if (type.equals(Serializer.TYPE_OBJ)) {
            
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath));
            AnalysisState state = (AnalysisState) ois.readObject();
            return state;
            
        } else if (type.equals(Serializer.TYPE_JSON)) {
            
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            AnalysisState state = new Gson().fromJson(br, AnalysisState.class);  
            return state;
            
        } else {
            throw new MultiSlideException("Unknown type: " + type + ". Can be 'object' or 'json'.");
        }
    }
    
    public static void mslideObjectToJSON(String infile, String outfile) throws Exception {
        
        Serializer s = new Serializer();
        AnalysisState state = s.loadAnalysis(infile, Serializer.TYPE_OBJ);
        
        String json = state.asJSON();
        BufferedWriter writer = new BufferedWriter(new FileWriter(outfile, false));
        writer.append(json);
        writer.close();
    }
    
    public String serializeView(
            AnalysisContainer analysis, String filepath, String session_folder_path
    ) throws MultiSlideException {
        
        HashMap <String, String[]> view = new HashMap <> ();
        List<String> dataset_names = new ArrayList <> (analysis.heatmaps.keySet());
        
        for (int i=0; i<analysis.heatmaps.size(); i++) {
            Heatmap heatmap = analysis.heatmaps.get(dataset_names.get(i));
            String name = analysis.data.datasets.get(dataset_names.get(i)).specs.display_name;
            
            List <List<String>> ids = analysis.data.selected.getFeatureIDs(
                dataset_names.get(i), analysis.global_map_config, heatmap.getMapConfig().getSelectedFeatureIdentifiers());
            
            String[] feature_ids = new String[ids.size()];
            for (int j=0; j<ids.size(); j++) {
                feature_ids[j] = String.join(";", ids.get(j));
            }
            
            view.put(name, feature_ids);
        }
        
        for (int i=0; i<analysis.heatmaps.size()-1; i++) {
            
            Heatmap heatmap_1 = analysis.heatmaps.get(dataset_names.get(i));
            Heatmap heatmap_2 = analysis.heatmaps.get(dataset_names.get(i+1));
            
            String name_1 = analysis.data.datasets.get(dataset_names.get(i)).specs.display_name;
            String name_2 = analysis.data.datasets.get(dataset_names.get(i+1)).specs.display_name;
            
            List <List<String>> ids_1 = analysis.data.selected.getFeatureIDs(
                dataset_names.get(i), analysis.global_map_config, heatmap_1.getMapConfig().getSelectedFeatureIdentifiers());
            
            List <List<String>> ids_2 = analysis.data.selected.getFeatureIDs(
                dataset_names.get(i+1), analysis.global_map_config, heatmap_2.getMapConfig().getSelectedFeatureIdentifiers());
            
            
            HashMap <String, List <int[]>> linkages = MapLinkLayout.getLinkages(
                    analysis, dataset_names.get(i), dataset_names.get(i+1), 
                    analysis.data.selected.views.get(dataset_names.get(i)).getFeatureManifest(analysis.global_map_config), 
                    analysis.data.selected.views.get(dataset_names.get(i+1)).getFeatureManifest(analysis.global_map_config)
            );
            List <int[]> mappings = linkages.get("mappings");
            
            String[] feature_ids = new String[mappings.size()];
            int j=0;
            for (int[] m: mappings) {
                feature_ids[j++] = String.join(";", ids_1.get(m[0])) + ";" + String.join(";", ids_2.get(m[1]));
            }
            
            view.put(name_1 + "; " + name_2, feature_ids);
        }
        
        /*
        for (int i=0; i<analysis.heatmaps.size()-1; i++) {
            
            String name_1 = analysis.data.datasets.get(dataset_names.get(i)).specs.display_name;
            String name_2 = analysis.data.datasets.get(dataset_names.get(i+1)).specs.display_name;
        
            Heatmap heatmap_1 = analysis.heatmaps.get(dataset_names.get(i));
            Heatmap heatmap_2 = analysis.heatmaps.get(dataset_names.get(i+1));
            
            String[] ids_1 = heatmap_1.getMapConfig().getSelectedFeatureIdentifiers();
            String[] ids_2 = heatmap_2.getMapConfig().getSelectedFeatureIdentifiers();
            view.put(dataset_names.get(i), ids_1);
            
            ArrayList <String[]> linkages = get_linkages(analysis, dataset_names.get(i), dataset_names.get(i+1), ids_1, ids_2);
            
            String[] _linkages = new String[linkages.size()];
            for (int j=0; i<linkages.size(); i++) {
                _linkages[i] = linkages.get(i)[0] + "," + linkages.get(i)[1];
            }
            
            view.put(name_1 + "; " + name_2, _linkages);
        }
        */
        
        try {
            
            Gson gson = new Gson(); 
            String json = gson.toJson(view);
            
            String filename = analysis.analysis_name + ".json";
            
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(filepath + File.separator + filename, false));
            writer.append(json);
            writer.close();
            
            return filename;
            
        } catch (Exception e) {
            
            throw new MultiSlideException("Unable be serialize current view as a json.");
        }
    }
    
    public ArrayList <String[]> get_linkages(
            AnalysisContainer analysis, 
            String dataset_name_1, String dataset_name_2, 
            String[] ids_1, String[] ids_2
    ) throws MultiSlideException {
        
        ArrayList <String[]> mapped_samples = new ArrayList <> ();
        
        /*
            check if any user provided mapping exists
        */
        BipartiteLinkageGraph linkage = null;
        if (analysis.data_selection_state.user_defined_between_omics_linkages.containsKey(dataset_name_1 + "_" + dataset_name_2)) {
            linkage = analysis.data_selection_state.user_defined_between_omics_linkages.get(dataset_name_1 + "_" + dataset_name_2);
            
        } else if (analysis.data_selection_state.user_defined_between_omics_linkages.containsKey(dataset_name_2 + "_" + dataset_name_1)) {
            linkage = analysis.data_selection_state.user_defined_between_omics_linkages.get(dataset_name_2 + "_" + dataset_name_1);
            
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
                        mapped_samples.add(new String[]{
                            feature_ids_1.get(i).get(0), 
                            feature_ids_2.get(j).get(0)});
                    }
                }
            }
            
        } else {
            
            List <List<String>> feature_ids_1 = analysis.data.selected.getFeatureIDs(dataset_name_1, analysis.global_map_config, ids_1);
            List <List<String>> feature_ids_2 = analysis.data.selected.getFeatureIDs(dataset_name_2, analysis.global_map_config, ids_2);
            
            DatasetSpecs spec_1 = analysis.data.datasets.get(dataset_name_1).specs;
            DatasetSpecs spec_2 = analysis.data.datasets.get(dataset_name_2).specs;
            
            FeatureIndex feature_manifest_1 = analysis.data.selected.views.get(dataset_name_1).getFeatureManifest(analysis.global_map_config);
            FeatureIndex feature_manifest_2 = analysis.data.selected.views.get(dataset_name_2).getFeatureManifest(analysis.global_map_config);

            if (spec_1.has_linker && spec_2.has_linker) {
                /*
                    mappings will be there only if both datasets have linker columns
                */
                List<List<String>> d1 = analysis.data.selected.views.get(dataset_name_1).getLinkers(analysis.global_map_config);
                List<List<String>> d2 = analysis.data.selected.views.get(dataset_name_2).getLinkers(analysis.global_map_config);

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
                                mapped_samples.add(new String[]{
                                    feature_ids_1.get(pos_1.intValue()).get(0), 
                                    feature_ids_2.get(pos_2.intValue()).get(0)});
                            }
                        }
                    }
                }

            }
        }
        
        return mapped_samples;
    }
    
}
