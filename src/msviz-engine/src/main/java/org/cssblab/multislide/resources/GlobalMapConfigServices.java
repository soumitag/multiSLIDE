package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.beans.data.MappedData;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.beans.data.SignificanceTestingParams;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.graphics.PyColorMaps;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.ClusteringParams;
import org.cssblab.multislide.structure.GlobalMapConfig;
import org.cssblab.multislide.structure.PhenotypeSortingParams;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class GlobalMapConfigServices extends HttpServlet {

    protected void processRequest (
            HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException {
        
        /*
        set_map_resolution	            server_response	
        set_grid_layout                     server_response	
        set_col_header_height               server_response	
        set_row_label_width	            server_response	
        set_rows_per_page	            mapped_data_response	
        set_cols_per_page	            mapped_data_response
        set_row_ordering	            server_response	        current_feature_start = 0
        set_col_ordering	            server_response	        current_feature_start = 0
        set_gene_filtering	            mapped_data_response	current_feature_start = 0
        set_sample_filtering	            mapped_data_response	current_feature_start = 0
        set_current_sample_start	    mapped_data_response	
        set_current_feature_start	    mapped_data_response
        set_clustering_params               server_response	        current_sample_start = 0
        set_significance_testing_params     mapped_data_response	current_feature_start = 0
        set_phenotype_sorting_params        server_response	        current_sample_start = 0
        */
        
        /*
        HashMap <String, Integer> standard_response_actions = new HashMap <String, Integer> ();
        standard_response_actions.put("set_map_resolution", RequestParam.DATA_TYPE_STRING);
        standard_response_actions.put("set_grid_layout", RequestParam.DATA_TYPE_INT);
        standard_response_actions.put("set_col_header_height", RequestParam.DATA_TYPE_DOUBLE);
        standard_response_actions.put("set_row_label_width", RequestParam.DATA_TYPE_DOUBLE);
        standard_response_actions.put("set_col_ordering", RequestParam.DATA_TYPE_INT);
        standard_response_actions.put("set_row_ordering", RequestParam.DATA_TYPE_INT);
        standard_response_actions.put("set_map_orientation", RequestParam.DATA_TYPE_INT);
        
        HashMap <String, Integer> mapped_data_response_actions = new HashMap <String, Integer> ();
        mapped_data_response_actions.put("set_rows_per_page", RequestParam.DATA_TYPE_INT);
        mapped_data_response_actions.put("set_cols_per_page", RequestParam.DATA_TYPE_INT);
        mapped_data_response_actions.put("set_gene_filtering", RequestParam.DATA_TYPE_BOOLEAN);
        mapped_data_response_actions.put("set_sample_filtering", RequestParam.DATA_TYPE_BOOLEAN);
        mapped_data_response_actions.put("set_current_sample_start", RequestParam.DATA_TYPE_INT);
        mapped_data_response_actions.put("set_current_feature_start", RequestParam.DATA_TYPE_INT);
        */
        
        boolean _roll_back = false;
        GlobalMapConfig _cached_global_map_config = null;
        AnalysisContainer analysis = null;
        
        try {
            
            /*
            Removed:
            "set_rows_per_page", 
            "set_cols_per_page", 
            "set_current_sample_start",
            "set_current_feature_start",
            */
            
            String[] actions = new String[]{
                "set_selected_phenotypes",
                "set_selected_datasets",
                "get_global_map_config", 
                "set_map_resolution", 
                "set_grid_layout", 
                "set_col_header_height", 
                "set_row_label_width", 
                "set_clustering_params", 
                "set_row_ordering",
                "set_col_ordering", 
                "set_gene_filtering", 
                "set_sample_filtering",
                "set_significance_testing_params", 
                "set_phenotype_sorting_params", 
                "set_map_orientation",
                "set_is_dataset_linking_on",
                "set_database_linkings",
                "reset_config",
                "set_show_cluster_labels",
                "set_num_cluster_labels"
            };
            
            HashMap <String, Boolean> _atomic_transactions = new HashMap <> ();
            _atomic_transactions.put("set_selected_phenotypes", true);
            _atomic_transactions.put("set_selected_datasets", true);
            _atomic_transactions.put("set_map_resolution", true);
            _atomic_transactions.put("set_grid_layout", true);
            _atomic_transactions.put("set_col_header_height", true);
            _atomic_transactions.put("set_row_label_width", true);
            _atomic_transactions.put("set_clustering_params", true);
            _atomic_transactions.put("set_row_ordering", true);
            _atomic_transactions.put("set_col_ordering", true);
            _atomic_transactions.put("set_gene_filtering", true);
            _atomic_transactions.put("set_sample_filtering", true);
            _atomic_transactions.put("set_significance_testing_params", true);
            _atomic_transactions.put("set_phenotype_sorting_params", true);
            _atomic_transactions.put("set_map_orientation", true);
            _atomic_transactions.put("set_is_dataset_linking_on", true);
            _atomic_transactions.put("set_database_linkings", true);
            _atomic_transactions.put("reset_config", true);
            _atomic_transactions.put("set_show_cluster_labels", true);
            _atomic_transactions.put("set_num_cluster_labels", true);
            
            DataParser parser = new DataParser(request);
            parser.addParam("analysis_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            parser.addParam("action", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, actions);
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                return;
            }
            String analysis_name = parser.getString("analysis_name");
            String action = parser.getString("action");

            HttpSession session = request.getSession(false);
            if (session == null) {
                ServerResponse resp = new ServerResponse(0, "Session not found", "Possibly due to time-out");
                returnMessage(resp, response);
                return;
            }

            analysis = (AnalysisContainer)session.getAttribute(analysis_name);
            if (analysis == null) {
                ServerResponse resp = new ServerResponse(0, "Analysis not found", "Analysis name '" + analysis_name + "' does not exist");
                returnMessage(resp, response);
                return;
            }
            
            /*
            Create a savepoint to restore to in case transaction fails 
            */
            if (_atomic_transactions.containsKey(action)) {
                _roll_back = true;
                _cached_global_map_config = analysis.global_map_config.clone();
            }

            String json = "";

            if(action.equalsIgnoreCase("set_map_resolution")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setMapResolution(parser.getString("param_value"));
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
            } else if(action.equalsIgnoreCase("set_grid_layout")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setGridLayout(parser.getInt("param_value"));
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
            } else if(action.equalsIgnoreCase("set_col_header_height")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setColHeaderHeight(parser.getDouble("param_value"));
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
            } else if(action.equalsIgnoreCase("set_row_label_width")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setRowLabelWidth(parser.getDouble("param_value"));
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
            } else if(action.equalsIgnoreCase("set_col_ordering")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setColumnOrderingScheme(parser.getInt("param_value"));
                analysis.data.selected.recomputeFeatureOrdering(analysis);
                //analysis.global_map_config.setCurrentFeatureStart(0);
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
            } else if(action.equalsIgnoreCase("set_row_ordering")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setSampleOrderingScheme(parser.getInt("param_value"));
                analysis.data.selected.recomputeSampleOrdering(analysis);
                //analysis.global_map_config.setCurrentSampleStart(0);
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
            } else if(action.equalsIgnoreCase("set_map_orientation")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setMapOrientation(parser.getInt("param_value"));
                returnMessage(new ServerResponse(1, "", ""), response); return;
   
            } else if(action.equalsIgnoreCase("set_gene_filtering")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setGeneFilteringOn(parser.getBool("param_value"));
                analysis.data.selected.recomputeFeatureOrdering(analysis);
                
                /*
                analysis.global_map_config.setAvailableCols(analysis.data.selected.getNumFeatures(analysis.global_map_config));
                analysis.global_map_config.setUserSpecifiedColsPerPage(analysis.global_map_config.getUserSpecifiedColsPerPage());
                analysis.global_map_config.setCurrentFeatureStart(0);
                */
                
                /*
                MappedData md = new MappedData(1, "", "");
                md.addNameValuePair("available_cols", analysis.global_map_config.available_cols+"");
                md.addNameValuePair("colsPerPageDisplayed", analysis.global_map_config.getColsPerPageDisplayed()+"");
                md.addNameValuePair("userSpecifiedColsPerPage", analysis.global_map_config.getUserSpecifiedColsPerPage()+"");
                md.addNameValuePair("current_feature_start", analysis.global_map_config.getCurrentFeatureStart()+"");
                json = md.asJSON();
                sendData(response, json);
                */
                
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
            } else if(action.equalsIgnoreCase("set_sample_filtering")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setSampleFilteringOn(parser.getBool("param_value"));
         
            } else if(action.equalsIgnoreCase("set_significance_testing_params")) {
                
                parser.addParam("dataset", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("phenotype", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("significance_level", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("testtype", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("apply_fdr", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }

                if(!parser.getBool("apply_fdr")){
                    analysis.global_map_config.setSignificanceTestingParameters(new SignificanceTestingParams(
                            parser.getString("dataset"), parser.getString("phenotype"), parser.getDouble("significance_level"),
                            parser.getString("testtype"), false, 1.0
                    ));
                } else {
                    parser.addParam("fdr_threshold", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_REQUIRED);
                    if (!parser.parse()) {
                        returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                        return;
                    }
                    analysis.global_map_config.setSignificanceTestingParameters(new SignificanceTestingParams(
                            parser.getString("dataset"), parser.getString("phenotype"), parser.getDouble("significance_level"),
                            parser.getString("testtype"), true, parser.getDouble("fdr_threshold")
                    ));
                }
                
                if (analysis.global_map_config.isGeneFilteringOn) {
                    
                    analysis.data.selected.recomputeFeatureOrdering(analysis);
                    
                    /*
                    analysis.global_map_config.setAvailableCols(analysis.data.selected.getNumFeatures(analysis.global_map_config));
                    analysis.global_map_config.setUserSpecifiedColsPerPage(analysis.global_map_config.getUserSpecifiedColsPerPage());
                    analysis.global_map_config.setCurrentFeatureStart(0);
                    */
                    
                    /*
                    MappedData md = new MappedData(1, "", "");
                    md.addNameValuePair("available_cols", analysis.global_map_config.available_cols+"");
                    md.addNameValuePair("colsPerPageDisplayed", analysis.global_map_config.getColsPerPageDisplayed()+"");
                    md.addNameValuePair("userSpecifiedColsPerPage", analysis.global_map_config.getUserSpecifiedColsPerPage()+"");
                    md.addNameValuePair("current_feature_start", analysis.global_map_config.getCurrentFeatureStart()+"");
                    json = md.asJSON();
                    sendData(response, json);
                    */
                    
                    returnMessage(new ServerResponse(1, "", ""), response); return;
                    
                } else if (analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.SIGNIFICANCE_COLUMN_ORDERING) {
                    
                    analysis.data.selected.recomputeFeatureOrdering(analysis);
                    
                    /*
                    analysis.global_map_config.setCurrentFeatureStart(0);
                    MappedData md = new MappedData(1, "", "");
                    json = md.asJSON();
                    sendData(response, json);
                    */
                    
                    returnMessage(new ServerResponse(1, "", ""), response); return;
                    
                } else {
                    /*
                    MappedData md = new MappedData(1, "", "");
                    json = md.asJSON();
                    sendData(response, json);
                    */
                    returnMessage(new ServerResponse(1, "", ""), response); return;
                }
                
            }  else if(action.equalsIgnoreCase("set_clustering_params")) {
                
                /*
                parser.addParam("is_joint", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("use_aggregate", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("aggregate_func", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                */
                parser.addParam("type", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("dataset", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("use_defaults", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("linkage_function", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("distance_function", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("leaf_ordering", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("numClusterLabels", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                if (parser.getInt("type") == ClusteringParams.TYPE_SAMPLE_CLUSTERING) {
                    if (parser.getBool("use_defaults")) {
                        analysis.setRowClusteringParams(new ClusteringParams(
                            ClusteringParams.TYPE_SAMPLE_CLUSTERING,
                            parser.getString("dataset")
                        ));
                    } else {
                        analysis.setRowClusteringParams(new ClusteringParams(
                            ClusteringParams.TYPE_SAMPLE_CLUSTERING, 
                            parser.getInt("linkage_function"), 
                            parser.getInt("distance_function"), 
                            parser.getInt("leaf_ordering"),
                            parser.getString("dataset"),
                            parser.getInt("numClusterLabels")
                        ));
                        
                        /*
                        parser.getBool("is_joint"),
                        parser.getBool("use_aggregate"),
                        parser.getString("aggregate_func")
                        */
                    }
                    analysis.data.selected.recomputeSampleOrdering(analysis);
                    //analysis.global_map_config.setCurrentSampleStart(0);
                } else if (parser.getInt("type") == ClusteringParams.TYPE_FEATURE_CLUSTERING) {
                    
                    if (parser.getBool("use_defaults")) {
                        analysis.setColClusteringParams(new ClusteringParams(
                            ClusteringParams.TYPE_FEATURE_CLUSTERING, 
                            parser.getString("dataset")
                        ));
                    } else {
                        analysis.setColClusteringParams(new ClusteringParams(
                            ClusteringParams.TYPE_FEATURE_CLUSTERING, 
                            parser.getInt("linkage_function"), 
                            parser.getInt("distance_function"), 
                            parser.getInt("leaf_ordering"),
                            parser.getString("dataset"),
                            parser.getInt("numClusterLabels")
                        ));
                        
                        /*
                        parser.getBool("is_joint"),
                        parser.getBool("use_aggregate"),
                        parser.getString("aggregate_func")
                        */
                    }
                    analysis.data.selected.recomputeFeatureOrdering(analysis);
                    //analysis.global_map_config.setCurrentFeatureStart(0);
                }
                
                returnMessage(new ServerResponse(1, "", ""), response);
                
                
            }  else if(action.equalsIgnoreCase("set_phenotype_sorting_params")) {
                
                parser.addListParam("phenotypes", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, ",");
                parser.addListParam("sort_orders", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED, ",");
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                analysis.global_map_config.setPhenotypeSortingParams(
                    new PhenotypeSortingParams(
                        parser.getStringArray("phenotypes"),
                        parser.getBoolArray("sort_orders")
                    )
                );
                analysis.data.selected.recomputeSampleOrdering(analysis);
                //analysis.global_map_config.setCurrentSampleStart(0);
                returnMessage(new ServerResponse(1, "", ""), response);
                
            } else if (action.equalsIgnoreCase("get_global_map_config")) {
            
                GlobalMapConfig global_config = analysis.getGlobalMapConfig();
                json = global_config.mapConfigAsJSON();
                sendData(response, json);
                
            } else if (action.equalsIgnoreCase("set_selected_datasets")) {
                
                parser.addListParam("selected_datasets", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, ",");
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                Utils.log_info("setting selected datasets");
                analysis.data_selection_state.setSelectedDatasets(parser.getStringArray("selected_datasets"));
                analysis.clearCaches();
                ServletContext context = request.getServletContext();
                ColorPalette gene_group_color_palette = (ColorPalette)context.getAttribute("gene_group_color_palette");
                analysis.data.createSelection(
                        analysis,
                        gene_group_color_palette
                );
                /*
                analysis.global_map_config.setAvailableRows(analysis.data.selected.getNumSamples());
                analysis.global_map_config.setAvailableCols(analysis.data.selected.getNumFeatures(analysis.global_map_config));
                */
                //analysis.global_map_config.setGeneFilteringOn(false);

                Map<String, Heatmap> heatmaps = new ListOrderedMap <> ();
                for (String dataset_name : analysis.data_selection_state.selected_datasets) {

                    Heatmap heatmap;
                    if (analysis.heatmaps.containsKey(dataset_name)) {
                        Heatmap reference_heatmap = analysis.heatmaps.get(dataset_name);
                        heatmap = new Heatmap(analysis.data.selected, dataset_name, reference_heatmap);
                    } else {
                        heatmap = new Heatmap(
                                analysis, dataset_name,
                                analysis.data.datasets.get(dataset_name).specs,
                                20, "data_bins", "SEISMIC", -1, 1);
                    }
                    heatmap.genColorData((PyColorMaps) context.getAttribute("colormaps"));
                    heatmap.assignBinsToRows(analysis.spark_session, analysis.data.selected);
                    heatmaps.put(dataset_name, heatmap);

                }
                analysis.heatmaps = heatmaps;

                returnMessage(new ServerResponse(1, "Done", ""), response);
                return;
                
            } else if (action.equalsIgnoreCase("set_selected_phenotypes")) {
                
                parser.addListParam("selected_phenotypes", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_OPTIONAL, ",");
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                analysis.data_selection_state.setSelectedPhenotypes(parser.getStringArray("selected_phenotypes"));
                analysis.global_map_config.phenotype_sorting_params.updateOnPhenSelectionChange(parser.getStringArray("selected_phenotypes"));
                analysis.data.selected.updateSelectedPhenotypes(analysis, analysis.data.clinical_info);
                returnMessage(new ServerResponse(1, "Done", ""), response);
                return;
                
            } else if(action.equalsIgnoreCase("set_is_dataset_linking_on")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setDatasetLinkingOn(parser.getBool("param_value"));
                analysis.data.selected.updateDatasetLinkages(analysis);
                returnMessage(new ServerResponse(1, "Done", ""), response);
                
            } else if(action.equalsIgnoreCase("set_database_linkings")) {
                
                parser.addListParam("dataset_names", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, ",");
                parser.addListParam("is_dataset_linked_arr", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED, ",");
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                String[] dataset_names_arr = parser.getStringArray("dataset_names");
                boolean[] is_dataset_linked_arr = parser.getBoolArray("is_dataset_linked_arr");
                for(int i = 0; i < dataset_names_arr.length; i++) {
                    analysis.global_map_config.updateDatasetLinkings(dataset_names_arr[i], is_dataset_linked_arr[i]);
                }
                
                analysis.data.selected.updateDatasetLinkages(analysis);
                
                Utils.log_info("dataset linkages:");
                HashMap <String, Boolean> t = analysis.global_map_config.getDatasetLinkages();
                for(String s: t.keySet())
                    Utils.log_info("dataset: " + s + "\tis_linked=" + t.get(s));
                
                returnMessage(new ServerResponse(1, "Done", ""), response);
                
            } else if(action.equalsIgnoreCase("reset_config")){
                analysis.global_map_config = new GlobalMapConfig();
                analysis.setRowClusteringParams(
                        new ClusteringParams(
                                ClusteringParams.TYPE_SAMPLE_CLUSTERING, 
                                analysis.data_selection_state.selected_datasets[0]
                        )
                );
                analysis.setColClusteringParams(
                        new ClusteringParams(
                                ClusteringParams.TYPE_FEATURE_CLUSTERING, 
                                analysis.data_selection_state.selected_datasets[0]
                        )
                );
                analysis.global_map_config.resetSignificanceTestingParameters(
                        analysis.data_selection_state.selected_datasets, 
                        analysis.data_selection_state.selected_phenotypes
                );
                analysis.global_map_config.setPhenotypeSortingParams(
                        new PhenotypeSortingParams(analysis.data_selection_state.selected_phenotypes)
                );
                analysis.global_map_config.setDefaultDatasetLinking(analysis.data.datasets);
                
                ServletContext context = request.getServletContext();
                ColorPalette gene_group_color_palette = (ColorPalette)context.getAttribute("gene_group_color_palette");
                analysis.clearCaches();
            
                analysis.data.createSelection(
                        analysis, 
                        gene_group_color_palette
                );
                
                Map<String, Heatmap> heatmaps = new ListOrderedMap <> ();
                for (String dataset_name : analysis.data_selection_state.selected_datasets) {

                    Heatmap heatmap = new Heatmap(analysis, dataset_name,
                                analysis.data.datasets.get(dataset_name).specs,
                                20, "data_bins", "SEISMIC", -1, 1);
                    heatmap.genColorData((PyColorMaps) context.getAttribute("colormaps"));
                    heatmap.assignBinsToRows(analysis.spark_session, analysis.data.selected);
                    heatmaps.put(dataset_name, heatmap);
                }
                analysis.heatmaps = heatmaps;
                
                returnMessage(new ServerResponse(1, "Done", ""), response);
                
            } else if(action.equalsIgnoreCase("set_show_cluster_labels")){
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setShowClusterLabelsOn(parser.getBool("param_value"));
                returnMessage(new ServerResponse(1, "Done", ""), response);
                
            }
            
        } catch (DataParsingException dpe) {
            
            rollbackSettings(_roll_back, analysis, _cached_global_map_config);
            
            Utils.log_exception(dpe, "Data parsing exception");
            returnMessage(new ServerResponse(0, "Data parsing exception", dpe.getMessage()), response);
            
        } catch (Exception e) {
            
            rollbackSettings(_roll_back, analysis, _cached_global_map_config);
            
            Utils.log_exception(e, "Error in GlobalMapConfigServices");
            returnMessage(new ServerResponse(0, "Error in GlobalMapConfigServices", e.getMessage()), response);
            
        }
    }
    
    private void rollbackSettings(boolean _roll_back, AnalysisContainer analysis, GlobalMapConfig _cached_global_map_config) {
        if (_roll_back) {
            if (analysis != null) {
                if (_cached_global_map_config != null) {
                    analysis.global_map_config = _cached_global_map_config;
                } else {
                    Utils.log_info("Warning: trying to roll back settings with null global map config");
                }
            } else {
                Utils.log_info("Warning: trying to roll back settings on null analysis");
            }
        }
    }

    protected void returnMessage(ServerResponse resp, HttpServletResponse response) throws IOException {
        String json = new Gson().toJson(resp);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
    
    protected void sendData(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
