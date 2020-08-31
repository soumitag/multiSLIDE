/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.GlobalHeatmapData;
import org.cssblab.multislide.beans.data.HeatmapData;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.beans.layouts.HeatmapLayout;
import org.cssblab.multislide.beans.layouts.MapContainerLayout;
import org.cssblab.multislide.beans.layouts.MapLinkLayout;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.graphics.PyColorMaps;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.MapConfig;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.data.Data;
import org.cssblab.multislide.utils.CollectionUtils;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class GetHeatmap extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
        
            DataParser parser = new DataParser(request);
            parser.addParam("analysis_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            parser.addParam("action", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, 
                    new String[]{
                        "get_data", 
                        "get_global_data", 
                        "get_layout", 
                        "get_container_layout", 
                        "reset_heatmap", 
                        "get_map_config", 
                        "get_global_map_config", 
                        "get_link_layout"});
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

            AnalysisContainer analysis = (AnalysisContainer)session.getAttribute(analysis_name);
            if (analysis == null) {
                ServerResponse resp = new ServerResponse(0, "Analysis not found", "Analysis name '" + analysis_name + "' does not exist");
                returnMessage(resp, response);
                return;
            }

            String json = "";

            if(action.equalsIgnoreCase("get_data")){

                parser.addParam("dataset_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                //parser.addParam("sample_start", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                //parser.addParam("feature_start", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }

                long startTime = System.nanoTime();
                Heatmap heatmap = analysis.heatmaps.get(parser.getString("dataset_name"));
                HeatmapData hd = new HeatmapData(
                        analysis.global_map_config, 
                        analysis.data.selected, 
                        heatmap, 
                        heatmap.dataset_name, 
                        analysis
                );
                
                String dataset_name = parser.getString("dataset_name");
                Utils.log_info(dataset_name + ": get_data heatmap creation (time) " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
                
                startTime = System.nanoTime();
                json = hd.heatmapDataAsJSON();
                Utils.log_info(dataset_name + ": get_data json creation (time) " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
                
                //analysis.global_map_config.setCurrentFeatureStart(parser.getInt("feature_start"));
                //analysis.global_map_config.setCurrentSampleStart(parser.getInt("sample_start"));
                
                startTime = System.nanoTime();
                sendData(response, json);
                Utils.log_info(dataset_name + ": get_data response sendData (time) " + (System.nanoTime() - startTime)/1000000 + " milliseconds");

            } else if(action.equalsIgnoreCase("get_global_data")){

                /*
                parser.addParam("sample_start", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("feature_start", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                */

                //Heatmap heatmap = analysis.heatmaps.get(parser.getString("dataset_name"));
                GlobalHeatmapData global_hd = new GlobalHeatmapData(
                        analysis.global_map_config, 
                        analysis.data.selected, 
                        "genesymbol_2021158607524066",
                        analysis
                );
                json = global_hd.globalHeatmapDataAsJSON();
                
                //analysis.global_map_config.setCurrentFeatureStart(parser.getInt("feature_start"));
                //analysis.global_map_config.setCurrentSampleStart(parser.getInt("sample_start"));
                
                sendData(response, json);

            } else if (action.equalsIgnoreCase("get_layout")) {

                parser.addParam("dataset_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                String dataset_name = parser.getString("dataset_name");

                int nSamples = analysis.data.selected.getNumSamples();
                int nEntrez = analysis.data.selected.getNumFeatures(dataset_name, analysis.global_map_config);
                int nPhenotypes = analysis.data_selection_state.selected_phenotypes.length;
                int nGeneTags = analysis.data.selected.getGeneGroups().size();

                Map.Entry <String,Heatmap> entry = analysis.heatmaps.entrySet().iterator().next();
                Heatmap heatmap = entry.getValue();
                
                try {
                    
                    HeatmapLayout map_layout = new HeatmapLayout(
                            dataset_name,
                            nSamples, 
                            nEntrez, 
                            nPhenotypes, 
                            nGeneTags, 
                            analysis.global_map_config.getMapResolution(), 
                            analysis.global_map_config.getMapOrientation(),
                            analysis.data_selection_state.getNetworkNeighbors(),
                            heatmap.hist.nBins+1,
                            analysis.global_map_config.getColHeaderHeight(), 
                            analysis.global_map_config.getRowLabelWidth()
                    );
                    json = map_layout.heatmapLayoutAsJSON();
                    sendData(response, json);
                } catch (MultiSlideException mse) {
                    Utils.log_exception(mse, "Error getting layout");
                    returnMessage(new ServerResponse(0, "Error getting layout.", mse.getMessage()), response);
                }

            } else if (action.equalsIgnoreCase("get_link_layout")) {

                parser.addParam("dataset_name_1", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("dataset_name_2", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                String dataset_name_1 = parser.getString("dataset_name_1");
                String dataset_name_2 = parser.getString("dataset_name_2");

                try {
                    
                    MapLinkLayout link_layout = new MapLinkLayout(
                            dataset_name_1,
                            dataset_name_2,
                            analysis.data_selection_state.selected_phenotypes.length, 
                            analysis.global_map_config.getMapResolution(), 
                            analysis.global_map_config.getMapOrientation(),
                            analysis.data.selected.views.get(dataset_name_1).getFeatureManifest(analysis.global_map_config),
                            analysis.data.selected.views.get(dataset_name_2).getFeatureManifest(analysis.global_map_config),
                            analysis
                    );
                    json = link_layout.asJSON();
                    sendData(response, json);
                    
                } catch (MultiSlideException mse) {
                    
                    Utils.log_exception(mse, "Error getting layout");
                    returnMessage(new ServerResponse(0, "Error getting layout.", mse.getMessage()), response);
                    
                }

            } else if (action.equalsIgnoreCase("get_container_layout")) {

                int nSamples = analysis.data.selected.getNumSamples();
                int nPhenotypes = analysis.data_selection_state.selected_phenotypes.length;
                int nGeneTags = analysis.data.selected.getGeneGroups().size();
                
                try {
                    
                    ArrayList <HeatmapLayout> heatmap_layouts = new ArrayList <> ();
                    
                    for (String dataset_name: analysis.data_selection_state.selected_datasets) {
                        
                        int nEntrez = analysis.data.selected.getNumFeatures(dataset_name, analysis.global_map_config);
                        
                        HeatmapLayout heatmap_layout = new HeatmapLayout(
                                dataset_name,
                                nSamples,
                                nEntrez,
                                nPhenotypes,
                                nGeneTags,
                                analysis.global_map_config.getMapResolution(),
                                analysis.global_map_config.getMapOrientation(),
                                analysis.data_selection_state.getNetworkNeighbors(),
                                20,
                                analysis.global_map_config.getColHeaderHeight(),
                                analysis.global_map_config.getRowLabelWidth()
                        );
                        heatmap_layouts.add(heatmap_layout);
                        
                    }
                    
                    MapContainerLayout container_layout = new MapContainerLayout(analysis.data.selected.getNumDatasets());
                    container_layout.computeMapLayout(
                            analysis,
                            heatmap_layouts,
                            analysis.global_map_config.getGridLayout(),
                            analysis.global_map_config.getMapResolution(), 
                            analysis.global_map_config.getMapOrientation()
                    );
                    
                    json = container_layout.mapContainerLayoutAsJSON();

                    /*
                    analysis.global_map_config.setMapResolution(parser.getString("mapResolution"));
                    analysis.global_map_config.setUserSpecifiedRowsPerPage(parser.getInt("nSamples"));
                    analysis.global_map_config.setUserSpecifiedColsPerPage(parser.getInt("nEntrez"));
                    analysis.global_map_config.setGridLayout(parser.getInt("gridLayout"));
                    analysis.global_map_config.setRowLabelWidth(parser.getDouble("rowLabelWidth"));
                    analysis.global_map_config.setColHeaderHeight(parser.getDouble("colHeaderHeight"));
                    */
                    
                    sendData(response, json);
                } catch (MultiSlideException mse) {
                    returnMessage(new ServerResponse(0, "Error getting layout.", mse.getMessage()), response);
                }

            } else if (action.equalsIgnoreCase("reset_heatmap")) {

                parser.addParam("dataset_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("numColors", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("binning_range", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, 
                        new String[]{"data_bins", "symmetric_bins", "user_specified"});
                parser.addParam("color_scheme", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("binning_range_start", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_OPTIONAL);
                parser.addParam("binning_range_end", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_OPTIONAL);
                parser.addParam("binning_range_end", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_OPTIONAL);
                
                parser.addListParam("selected_feature_identifiers", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, ",");
                parser.addParam("show_aggregated", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("aggregate_function", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                
                /*
                Get the old values of show_aggregated and aggregate_function
                if these have changed, the view will have to be recreated
                */
                String dataset_name = parser.getString("dataset_name");
                MapConfig mc = analysis.heatmaps.get(dataset_name).getMapConfig();
                boolean prev_show_aggregated = mc.show_aggregated;
                String prev_agg_func = mc.aggregate_function;
                
                /*
                Create a new heatmap and set it to analysis
                */
                Heatmap heatmap = new Heatmap (
                        analysis, 
                        dataset_name, 
                        analysis.data.datasets.get(dataset_name).specs,
                        parser.getInt("numColors")-1, 
                        parser.getString("binning_range"),
                        parser.getString("color_scheme"),
                        parser.getDouble("binning_range_start"),
                        parser.getDouble("binning_range_end")
                );
                
                /*
                Do not use Array.asList it returns an immutable list that does not support add()
                */
                List <String> _identifiers = CollectionUtils.asList(parser.getStringArray("selected_feature_identifiers"));
                
                DatasetSpecs specs = analysis.data.datasets.get(dataset_name).specs;
                if (_identifiers.size() < 1) {
                    if (specs.has_linker) {
                        if (!_identifiers.contains(specs.getLinkerColname())) {
                            _identifiers.add(specs.getLinkerColname());
                        }
                    } else {
                        String[] _ids = specs.getIdentifierMetadataColumns();
                        _identifiers.add(_ids[0]);
                    }
                }
                
                heatmap.getMapConfig().setSelectedFeatureIdentifiers(CollectionUtils.asArray(_identifiers));
                heatmap.getMapConfig().setShowAggregated(parser.getBool("show_aggregated"));
                heatmap.getMapConfig().setAggregateFunction(parser.getString("aggregate_function"));
                
                ServletContext context = request.getServletContext();
                heatmap.genColorData((PyColorMaps)context.getAttribute("colormaps"));
                analysis.heatmaps.put(dataset_name, heatmap);
                
                /*
                    Now check if show_aggregated and aggregate_function have changed
                    if so, updated view
                */
                Utils.log_info(heatmap.getMapConfig().mapConfigAsJSON());
                
                if (heatmap.getMapConfig().show_aggregated != prev_show_aggregated || 
                        !prev_agg_func.equalsIgnoreCase(heatmap.getMapConfig().aggregate_function)) {
                    
                    analysis.data.selected.updateDatasetAggregation(dataset_name, analysis);
                }
                
                /*
                    Finallym reset bin numbers
                */
                heatmap.assignBinsToRows(analysis.spark_session, analysis.data.selected);
                
                returnMessage(new ServerResponse(1, "Done", "Heat map settings updated"), response);

            } else if (action.equalsIgnoreCase("get_map_config")) {
            
                parser.addParam("dataset_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                
                MapConfig config = analysis.heatmaps.get(parser.getString("dataset_name")).getMapConfig();
                json = config.mapConfigAsJSON();
                sendData(response, json);
                
            } else {
                json = null;
            }
        
        } catch (DataParsingException dpe) {
            Utils.log_exception(dpe, "Data parsing exception");
            returnMessage(new ServerResponse(0, "Data parsing exception", dpe.getMessage()), response);
        } catch (Exception e) {
            Utils.log_exception(e, "");
            returnMessage(new ServerResponse(0, "Data parsing exception", e.getMessage()), response);
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
