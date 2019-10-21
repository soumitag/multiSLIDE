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
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.beans.data.SearchResultSummary;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.beans.data.SignificanceTestingParams;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.graphics.PyColorMaps;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.ClusteringParams;
import org.cssblab.multislide.structure.GlobalMapConfig;
import org.cssblab.multislide.structure.PhenotypeSortingParams;

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
            parser.addListParam("groupIDs", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_OPTIONAL, ",");
            parser.addListParam("groupNames", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_OPTIONAL, ",");
            parser.addListParam("groupTypes", RequestParam.DATA_TYPE_BYTE, RequestParam.PARAM_TYPE_OPTIONAL, ",", 
                    new String[]{SearchResultSummary.TYPE_GENE_SUMMARY + "",SearchResultSummary.TYPE_PATH_SUMMARY + "",SearchResultSummary.TYPE_GO_SUMMARY + ""}
            );
            parser.addListParam("intersection_counts", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_OPTIONAL, ",");
            parser.addParam("intersection_counts_per_dataset", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_OPTIONAL);
            parser.addListParam("background_counts", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_OPTIONAL, ",");

            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                return;
            }
            
            if (parser.getString("source").equals("init")) {
                
                String[] selected_datasets = parser.getStringArray("dataset_names");
                String[] selected_phenotypes = parser.getStringArray("clinicalFilters");
                String[] group_ids = parser.getStringArray("groupIDs");
                String[] group_names = parser.getStringArray("groupNames");
                byte[] group_types = parser.getByteArray("groupTypes");
                int[][] intersection_counts_per_dataset = parseIntersectionCounts(parser.getString("intersection_counts_per_dataset"));
                int[] background_counts = parser.getIntArray("background_counts");
                
                analysis.data_selection_state.setDatasets(selected_datasets);
                analysis.data_selection_state.setSelectedPhenotypes(selected_phenotypes);
                analysis.data_selection_state.setSearchResults(group_ids, group_types, group_names, intersection_counts_per_dataset, background_counts);
                analysis.data_selection_state.clearNetworkNeighbors();
                analysis.setRowClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_ROW_CLUSTERING, selected_datasets[0]));
                analysis.setColClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_COL_CLUSTERING, selected_datasets[0]));
                analysis.global_map_config.resetSignificanceTestingParameters(selected_datasets, selected_phenotypes);
                analysis.global_map_config.setPhenotypeSortingParams(new PhenotypeSortingParams(selected_phenotypes));
                analysis.global_map_config.setGeneFilteringOn(false);
                analysis.global_map_config.setColumnOrderingScheme(GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING);
                analysis.global_map_config.setSampleOrderingScheme(GlobalMapConfig.PHENOTYPE_SAMPLE_ORDERING);
                analysis.global_map_config.setCurrentFeatureStart(0);
                analysis.global_map_config.setCurrentSampleStart(0);
                
            } else if (parser.getString("source").equals("list")) {
                
                parser.addParam("feature_list", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_OPTIONAL);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                
                analysis.data_selection_state.pushHistory();
                
                analysis.setRowClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_ROW_CLUSTERING, analysis.data_selection_state.datasets[0]));
                analysis.setColClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_COL_CLUSTERING, analysis.data_selection_state.datasets[0]));
                analysis.global_map_config.resetSignificanceTestingParameters(analysis.data_selection_state.datasets, analysis.data_selection_state.selectedPhenotypes);
                analysis.global_map_config.setPhenotypeSortingParams(new PhenotypeSortingParams(analysis.data_selection_state.selectedPhenotypes));
                analysis.global_map_config.setGeneFilteringOn(false);
                analysis.global_map_config.setColumnOrderingScheme(GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING);
                analysis.global_map_config.setSampleOrderingScheme(GlobalMapConfig.PHENOTYPE_SAMPLE_ORDERING);
                analysis.global_map_config.setCurrentFeatureStart(0);
                analysis.global_map_config.setCurrentSampleStart(0);

            } else if (parser.getString("source").equals("history")) {
                
                analysis.data_selection_state.popHistory();
                
                analysis.setRowClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_ROW_CLUSTERING, analysis.data_selection_state.datasets[0]));
                analysis.setColClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_COL_CLUSTERING, analysis.data_selection_state.datasets[0]));
                analysis.global_map_config.resetSignificanceTestingParameters(analysis.data_selection_state.datasets, analysis.data_selection_state.selectedPhenotypes);
                analysis.global_map_config.setPhenotypeSortingParams(new PhenotypeSortingParams(analysis.data_selection_state.selectedPhenotypes));
                analysis.global_map_config.setGeneFilteringOn(false);
                analysis.global_map_config.setColumnOrderingScheme(GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING);
                analysis.global_map_config.setSampleOrderingScheme(GlobalMapConfig.PHENOTYPE_SAMPLE_ORDERING);
                analysis.global_map_config.setCurrentFeatureStart(0);
                analysis.global_map_config.setCurrentSampleStart(0);
                
            } else if (parser.getString("source").equals("savepoint")) {
                
                String savepoint_id = parser.getString("savepoint_id");
                if (!analysis.data_selection_state.hasCurrent()) {
                    analysis.data_selection_state.pushHistory();
                }
                analysis.data_selection_state.loadState(savepoint_id);
                        
                analysis.setRowClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_ROW_CLUSTERING, analysis.data_selection_state.datasets[0]));
                analysis.setColClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_COL_CLUSTERING, analysis.data_selection_state.datasets[0]));
                analysis.global_map_config.resetSignificanceTestingParameters(analysis.data_selection_state.datasets, analysis.data_selection_state.selectedPhenotypes);
                analysis.global_map_config.setPhenotypeSortingParams(new PhenotypeSortingParams(analysis.data_selection_state.selectedPhenotypes));
                analysis.global_map_config.setGeneFilteringOn(false);
                analysis.global_map_config.setColumnOrderingScheme(GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING);
                analysis.global_map_config.setSampleOrderingScheme(GlobalMapConfig.PHENOTYPE_SAMPLE_ORDERING);
                analysis.global_map_config.setCurrentFeatureStart(0);
                analysis.global_map_config.setCurrentSampleStart(0);
                
            } else if (parser.getString("source").equals("demo")) {
                
                analysis.global_map_config.setCurrentFeatureStart(0);
                analysis.global_map_config.setCurrentSampleStart(0);
                
            }
            
            ServletContext context = request.getServletContext();
            ColorPalette gene_group_color_palette = (ColorPalette)context.getAttribute("gene_group_color_palette");
            analysis.clearCaches();
            
            if (parser.getString("source").equals("init") || parser.getString("source").equals("reinit") || parser.getString("source").equals("history")) {
                analysis.data.prepareDataForGroup(
                        analysis, 
                        "genesymbol_2021158607524066",
                        gene_group_color_palette
                );
            } else if (parser.getString("source").equals("list")) {
                analysis.data.prepareFeatureListDataForGroup(
                        parser.getString("feature_list"),
                        analysis, 
                        "genesymbol_2021158607524066",
                        gene_group_color_palette
                );
            }

            analysis.global_map_config.setAvailableRows(analysis.data.fs_data.nSamples);
            analysis.global_map_config.setAvailableCols(analysis.data.fs_data.nFilteredGenes);
            //analysis.global_map_config.setGeneFilteringOn(false);
            
            HashMap<String, Heatmap> heatmaps = new HashMap<String, Heatmap>();
            for (int i = 0; i < analysis.data.nDatasets; i++) {
                String dataset_name = analysis.data.dataset_names[i];
                if (analysis.data_selection_state.isDatasetSelected(dataset_name)) {
                    Heatmap heatmap;
                    if (analysis.heatmaps.containsKey(dataset_name)) {
                        Heatmap reference_heatmap = analysis.heatmaps.get(dataset_name);
                        heatmap = new Heatmap(analysis.data.fs_data, analysis.data.dataset_names[i], reference_heatmap);
                    } else {
                        heatmap = new Heatmap(analysis.data.fs_data, analysis.data.dataset_names[i], 20, "data_bins", null, "SEISMIC", "genesymbol_2021158607524066", -1, 1);
                    }
                    heatmap.genColorData((PyColorMaps) context.getAttribute("colormaps"));
                    heatmap.assignBinsToRows(analysis.global_map_config);
                    heatmaps.put(analysis.data.dataset_names[i], heatmap);
                }
            }
            analysis.heatmaps = heatmaps;
            
            returnMessage(new ServerResponse(1, "Done", ""), response);
            return;
            
        } catch (Exception e) {
            System.out.println(e);
            ServerResponse resp = new ServerResponse(0, "Error in AnalysisReInitializer.", e.getMessage());
            returnMessage(resp, response);
            return;
        }
            
    }
    
    private int[][] parseIntersectionCounts(String s) {
        String[] ss = s.split(";");
        String[] sss = ss[0].split(",", -1);
        int[][] intersection_counts = new int[ss.length][sss.length];
        for (int i=0; i<ss.length; i++) {
            sss = ss[i].split(",", -1);
            for (int j=0; j<sss.length; j++) {
                intersection_counts[i][j] = Integer.parseInt(sss[j]);
            }
        }
        return intersection_counts;
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
