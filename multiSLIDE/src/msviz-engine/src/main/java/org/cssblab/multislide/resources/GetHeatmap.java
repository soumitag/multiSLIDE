/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.beans.data.HeatmapData;
import org.cssblab.multislide.beans.data.PreviewData;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.beans.layouts.HeatmapLayout;
import org.cssblab.multislide.beans.layouts.MapContainerLayout;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.graphics.PyColorMaps;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.GlobalMapConfig;
import org.cssblab.multislide.structure.MapConfig;
import org.cssblab.multislide.structure.MultiSlideException;

/**
 *
 * @author abhik
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
            parser.addParam("action", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, new String[]{"get_data", "get_layout", "get_container_layout", "reset_heatmap", "get_map_config", "get_global_map_config"});
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
                parser.addParam("sample_start", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("feature_start", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }

                Heatmap heatmap = analysis.heatmaps.get(parser.getString("dataset_name"));
                HeatmapData hd = new HeatmapData(
                        parser.getInt("sample_start"), 
                        parser.getInt("feature_start"), 
                        analysis.global_map_config.getRowsPerPage(), 
                        analysis.global_map_config.getColsPerPage(), 
                        analysis.data.fs_data, 
                        heatmap, 
                        heatmap.dataset_name, 
                        analysis
                );
                json = hd.heatmapDataAsJSON();
                
                analysis.global_map_config.setCurrentFeatureStart(parser.getInt("feature_start"));
                analysis.global_map_config.setCurrentSampleStart(parser.getInt("sample_start"));
                
                sendData(response, json);

            } else if (action.equalsIgnoreCase("get_layout")) {

                parser.addParam("dataset_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }

                int nPhenotypes = analysis.data_selection_state.selectedPhenotypes.length;
                int nGeneTags = analysis.data_selection_state.nGeneTags;

                ArrayList<ArrayList<Integer>> search_tags = analysis.getSearchTags();
                Heatmap heatmap = analysis.heatmaps.get(parser.getString("dataset_name"));
                
                try {
                    HeatmapLayout map_layout = new HeatmapLayout(
                            analysis.global_map_config.getRowsPerPage(), 
                            analysis.global_map_config.getColsPerPage(), 
                            nPhenotypes, 
                            nGeneTags, 
                            analysis.global_map_config.getMapResolution(), 
                            analysis.global_map_config.getMapResolution(), 
                            search_tags,
                            heatmap.hist.nBins+1,
                            analysis.global_map_config.getColHeaderHeight(), 
                            analysis.global_map_config.getRowLabelWidth()
                    );
                    json = map_layout.heatmapLayoutAsJSON();
                    sendData(response, json);
                } catch (MultiSlideException mse) {
                    returnMessage(new ServerResponse(0, "Error getting layout.", mse.getMessage()), response);
                }

            } else if (action.equalsIgnoreCase("get_container_layout")) {

                parser.addParam("nSamples", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("nEntrez", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("mapResolution", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, new String[]{"XS", "S", "M", "L", "XL"});
                parser.addParam("colHeaderHeight", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("rowLabelWidth", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("gridLayout", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }

                int nPhenotypes = analysis.data_selection_state.selectedPhenotypes.length;
                int nGeneTags = analysis.data_selection_state.nGeneTags;
                
                ArrayList<ArrayList<Integer>> search_tags = analysis.getSearchTags();

                try {
                    MapContainerLayout container_layout = new MapContainerLayout(analysis.data.fs_data.nDatasets);
                    container_layout.computeMapLayout(analysis, 
                            new HeatmapLayout(
                                parser.getInt("nSamples"), 
                                parser.getInt("nEntrez"), 
                                nPhenotypes, 
                                nGeneTags, 
                                parser.getString("mapResolution"), 
                                analysis.global_map_config.getMapResolution(), 
                                search_tags,
                                51,
                                parser.getDouble("colHeaderHeight"), 
                                parser.getDouble("rowLabelWidth")
                            ), parser.getInt("gridLayout")
                    );
                    json = container_layout.mapContainerLayoutAsJSON();
                    
                    analysis.global_map_config.setMapResolution(parser.getString("mapResolution"));
                    analysis.global_map_config.setRowsPerPage(parser.getInt("nSamples"));
                    analysis.global_map_config.setColsPerPage(parser.getInt("nEntrez"));
                    analysis.global_map_config.setGridLayout(parser.getInt("gridLayout"));
                    analysis.global_map_config.setRowLabelWidth(parser.getDouble("rowLabelWidth"));
                    analysis.global_map_config.setColHeaderHeight(parser.getDouble("colHeaderHeight"));
                    
                    sendData(response, json);
                } catch (MultiSlideException mse) {
                    returnMessage(new ServerResponse(0, "Error getting layout.", mse.getMessage()), response);
                }

            } else if (action.equalsIgnoreCase("reset_heatmap")) {

                parser.addParam("dataset_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("numColors", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("binning_range", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, new String[]{"data_bins", "symmetric_bins", "user_specified"});
                parser.addParam("color_scheme", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("column_label", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("binning_range_start", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_OPTIONAL);
                parser.addParam("binning_range_end", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_OPTIONAL);
                
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                
                Heatmap heatmap = new Heatmap (
                        analysis.data.fs_data, 
                        parser.getString("dataset_name"), 
                        parser.getInt("numColors")-1, 
                        parser.getString("binning_range"),
                        null, 
                        parser.getString("color_scheme"),
                        parser.getString("column_label"),
                        parser.getDouble("binning_range_start"),
                        parser.getDouble("binning_range_end")
                );
                ServletContext context = request.getServletContext();
                heatmap.genColorData((PyColorMaps)context.getAttribute("colormaps"));
                heatmap.assignBinsToRows();
                
                analysis.heatmaps.put(parser.getString("dataset_name"), heatmap);
                returnMessage(new ServerResponse(1, "Done", "Heat map settings updated"), response);

            } else if (action.equalsIgnoreCase("get_map_config")) {
            
                parser.addParam("dataset_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                
                MapConfig config = analysis.heatmaps.get(parser.getString("dataset_name")).getMapConfig(analysis);
                json = config.mapConfigAsJSON();
                sendData(response, json);
                
            } else if (action.equalsIgnoreCase("get_global_map_config")) {
            
                GlobalMapConfig global_config = analysis.global_map_config;
                json = global_config.mapConfigAsJSON();
                sendData(response, json);
                
            } else {
                json = null;
            }
        
        } catch (DataParsingException dpe) {
            returnMessage(new ServerResponse(0, "Data parsing exception", dpe.getMessage()), response);
        } catch (Exception e) {
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
