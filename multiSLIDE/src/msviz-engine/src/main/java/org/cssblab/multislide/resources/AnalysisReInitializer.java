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
import java.util.Arrays;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.beans.data.SearchResultSummary;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.graphics.PyColorMaps;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.ClusteringParams;

/**
 *
 * @author Soumita
 */
public class AnalysisReInitializer extends HttpServlet {

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
            
            HttpSession session = request.getSession(false);
            if (session == null) {
                ServerResponse resp = new ServerResponse(0, "Session not found", "Possibly due to time-out");
                returnMessage(resp, response);
                return;
            }
            
            DataParser parser = new DataParser(request);
            parser.addParam("analysis_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
            }
            String analysis_name = parser.getString("analysis_name");
            
            AnalysisContainer analysis = (AnalysisContainer)session.getAttribute(analysis_name);
            if (analysis == null) {
                ServerResponse resp = new ServerResponse(0, "Analysis not found", "Analysis name '" + analysis_name + "' does not exist");
                returnMessage(resp, response);
                return;
            }
            
            parser.addParam("source", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            parser.addListParam("dataset_names", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_OPTIONAL, ",");
            parser.addListParam("clinicalFilters", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_OPTIONAL, ",");
            parser.addListParam("groupNames", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_OPTIONAL, ",");
            parser.addListParam("groupTypes", RequestParam.DATA_TYPE_BYTE, RequestParam.PARAM_TYPE_OPTIONAL, ",", 
                    new String[]{SearchResultSummary.TYPE_GENE_SUMMARY + "",SearchResultSummary.TYPE_PATH_SUMMARY + "",SearchResultSummary.TYPE_GO_SUMMARY + ""}
            );
            parser.addParam("sortByForRows", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_OPTIONAL);
            parser.setDefaultValueForParam("sortByForRows", "No_Sort");
            parser.addParam("sortByForCols", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_OPTIONAL);
            parser.setDefaultValueForParam("sortByForCols", "Gene_Group");
            parser.addParam("row_clustering", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_OPTIONAL);
            parser.addParam("col_clustering", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_OPTIONAL);
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                return;
            }
            
            if (parser.getString("source").equals("init")) {
                String[] selected_datasets = parser.getStringArray("dataset_names");
                String[] selected_phenotypes = parser.getStringArray("clinicalFilters");
                String[] group_ids = parser.getStringArray("groupNames");
                byte[] group_types = parser.getByteArray("groupTypes");
                analysis.data_selection_state.setDatasets(selected_datasets);
                analysis.data_selection_state.setSelectedPhenotypes(selected_phenotypes);
                analysis.data_selection_state.setSearchResults(group_ids, group_types, analysis.current_search_results);
            }
            
            ClusteringParams row_clustering_params;
            if (parser.getBool("row_clustering")) {
                parser.addParam("dataset_name_row", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("useDefaults_row", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                if (parser.getBool("useDefaults_row")) {
                    row_clustering_params = new ClusteringParams(ClusteringParams.CLUSTERING_PARAM_TYPE_ROW, true, parser.getString("dataset_name_row"));
                } else {
                    parser.addParam("linkage_function_row", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                    parser.addParam("distance_function_row", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                    parser.addParam("leaf_ordering_row", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                    if (!parser.parse()) {
                        returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                        return;
                    }
                    row_clustering_params = new ClusteringParams(
                            ClusteringParams.CLUSTERING_PARAM_TYPE_ROW, 
                            parser.getString("linkage_function_row"), 
                            parser.getString("distance_function_row"), 
                            parser.getInt("leaf_ordering_row"),
                            parser.getString("dataset_name_row")
                    );
                }
            } else {
                row_clustering_params = new ClusteringParams(ClusteringParams.CLUSTERING_PARAM_TYPE_ROW);
            }
            analysis.setColClusteringParams(row_clustering_params);
            
            ClusteringParams col_clustering_params;
            if (parser.getBool("col_clustering")) {
                parser.addParam("dataset_name_col", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("useDefaults_col", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                if (parser.getBool("useDefaults_col")) {
                    col_clustering_params = new ClusteringParams(ClusteringParams.CLUSTERING_PARAM_TYPE_ROW, true, parser.getString("dataset_name_col"));
                } else {
                    parser.addParam("linkage_function_col", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                    parser.addParam("distance_function_col", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                    parser.addParam("leaf_ordering_col", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                    if (!parser.parse()) {
                        returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                        return;
                    }
                    col_clustering_params = new ClusteringParams(
                            ClusteringParams.CLUSTERING_PARAM_TYPE_COL, 
                            parser.getString("linkage_function_col"), 
                            parser.getString("distance_function_col"), 
                            parser.getInt("leaf_ordering_col"),
                            parser.getString("dataset_name_col")
                    );
                }
            } else {
                col_clustering_params = new ClusteringParams(ClusteringParams.CLUSTERING_PARAM_TYPE_ROW);
            }
            
            analysis.setColClusteringParams(col_clustering_params);
            
            analysis.data.prepareDataForGroup(
                    analysis, 
                    "genesymbol_2021158607524066",
                    parser.getString("sortByForRows"), 
                    parser.getString("sortByForCols"), 
                    row_clustering_params, 
                    col_clustering_params
            );
            analysis.generateGeneGroupColors();

            analysis.heatmaps = new HashMap <String, Heatmap> ();
            for (int i = 0; i < analysis.data.nDatasets; i++) {
                if (analysis.data_selection_state.isDatasetSelected(analysis.data.dataset_names[i])) {
                    Heatmap heatmap = new Heatmap(analysis.data.fs_data, analysis.data.dataset_names[i], 50, "data_bins", null, "SEISMIC", "genesymbol_2021158607524066", -1, 1);
                    ServletContext context = request.getServletContext();
                    heatmap.genColorData((PyColorMaps)context.getAttribute("colormaps"));
                    heatmap.assignBinsToRows();
                    analysis.heatmaps.put(analysis.data.dataset_names[i], heatmap);
                }
            }
            
            returnMessage(new ServerResponse(1, "Done", ""), response);
            return;
            
        } catch (Exception e) {
            System.out.println(e);
            ServerResponse resp = new ServerResponse(0, "Error in AnalysisReInitializer.", e.getMessage());
            returnMessage(resp, response);
            return;
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
