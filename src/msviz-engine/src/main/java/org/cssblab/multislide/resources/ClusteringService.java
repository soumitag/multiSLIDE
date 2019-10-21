/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.graphics.PyColorMaps;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.ClusteringParams;
import org.cssblab.multislide.structure.MultiSlideException;

/**
 *
 * @author soumita
 */
public class ClusteringService extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
        
            DataParser parser = new DataParser(request);
            parser.addParam("analysis_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            parser.addParam("action", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, new String[]{"set_clustering_params", "get_clustering_params", "get_dataset_names"});
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
            
            if (action.equals("set_clustering_params")) {
        
                /*
                'row_doClustering': row_clustering_params.doClustering.toString(),
                'row_dataset': row_clustering_params.dataset,
                'row_use_defaults': row_clustering_params.use_defaults.toString(),
                'row_linkage_function': row_clustering_params.linkage_function,
                'row_distance_function': row_clustering_params.distance_function,
                'row_leaf_ordering': row_clustering_params.leaf_ordering,
                'col_doClustering': col_clustering_params.doClustering.toString(),
                'col_dataset': col_clustering_params.dataset,
                'col_use_defaults': col_clustering_params.use_defaults.toString(),
                'col_linkage_function': col_clustering_params.linkage_function,
                'col_distance_function': col_clustering_params.distance_function,
                'col_leaf_ordering': col_clustering_params.leaf_ordering
                */
                
                parser.addParam("row_dataset", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("row_use_defaults", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("row_linkage_function", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("row_distance_function", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("row_leaf_ordering", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                
                parser.addParam("col_dataset", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("col_use_defaults", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("col_linkage_function", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("col_distance_function", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("col_leaf_ordering", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                
                if (!parser.parse()) {
                    ServerResponse resp = new ServerResponse(0, "Bad param", parser.error_msg);
                    returnMessage(resp, response);
                    return;
                }
                
                if(!parser.getBool("row_do_clustering")) {
                    analysis.setRowClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_ROW_CLUSTERING));
                } else {
                    if(parser.getBool("row_use_defaults")) {
                        if (analysis.data.fs_data == null) {
                            analysis.setRowClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_ROW_CLUSTERING, parser.getString("row_dataset")));
                        } else {
                            analysis.setRowClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_ROW_CLUSTERING, analysis.data.fs_data.getDatasetNames()[0]));
                        }
                    } else {
                        String row_dataset = parser.getString("row_dataset");
                        boolean datasetIsValid = false;
                        for (String dataset_name : analysis.data.fs_data.getDatasetNames()) {
                            if (row_dataset.equalsIgnoreCase(dataset_name)) {
                                datasetIsValid = true;
                                break;
                            }
                        }
                        if (!datasetIsValid) {
                            ServerResponse resp = new ServerResponse(0, "Failed to updated clustering parametes", "Dataset '" + row_dataset + "' is not selected (in selection panel) for visualization.");
                            returnMessage(resp, response);
                        }
                        analysis.setRowClusteringParams(new ClusteringParams(
                            HierarchicalClusterer.TYPE_ROW_CLUSTERING, 
                            parser.getInt("row_linkage_function"), 
                            parser.getInt("row_distance_function"), 
                            parser.getInt("row_leaf_ordering"),
                            parser.getString("row_dataset")
                        ));
                    }
                }
                
                if(!parser.getBool("col_do_clustering")) {
                    analysis.setColClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_COL_CLUSTERING));
                } else {
                    if (parser.getBool("col_use_defaults")) {
                        if (analysis.data.fs_data == null) {
                            analysis.setColClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_COL_CLUSTERING, parser.getString("col_dataset")));
                        } else {
                            analysis.setColClusteringParams(new ClusteringParams(HierarchicalClusterer.TYPE_COL_CLUSTERING, analysis.data.fs_data.getDatasetNames()[0]));
                        }
                    } else {
                        String col_dataset = parser.getString("col_dataset");
                        boolean datasetIsValid = false;
                        for (String dataset_name : analysis.data.fs_data.getDatasetNames()) {
                            if (col_dataset.equalsIgnoreCase(dataset_name)) {
                                datasetIsValid = true;
                                break;
                            }
                        }
                        if (!datasetIsValid) {
                            ServerResponse resp = new ServerResponse(0, "Failed to updated clustering parametes", "Dataset '" + col_dataset + "' is not selected (in selection panel) for visualization.");
                            returnMessage(resp, response);
                        }
                        analysis.setColClusteringParams(new ClusteringParams(
                            HierarchicalClusterer.TYPE_COL_CLUSTERING, 
                            parser.getInt("col_linkage_function"), 
                            parser.getInt("col_distance_function"), 
                            parser.getInt("col_leaf_ordering"),
                            parser.getString("col_dataset")
                        ));
                    }
                }
                
                ServletContext context = request.getServletContext();
                ColorPalette gene_group_color_palette = (ColorPalette)context.getAttribute("gene_group_color_palette");
                analysis.data.prepareDataForGroup(
                        analysis, 
                        "genesymbol_2021158607524066",
                        gene_group_color_palette
                );

                analysis.global_map_config.setAvailableRows(analysis.data.fs_data.nSamples);
                analysis.global_map_config.setAvailableCols(analysis.data.fs_data.nFilteredGenes);

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
                
                returnMessage(new ServerResponse(1, "Clustering parameters updated.", ""), response);
                
            } else if (action.equals("get_clustering_params")) {
                
                parser.addParam("type", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    ServerResponse resp = new ServerResponse(0, "Bad param", parser.error_msg);
                    returnMessage(resp, response);
                    return;
                }
                
                String type = parser.getString("type");
                
                if (type.equalsIgnoreCase("row")) {
                    String json = analysis.global_map_config.row_clustering_params.asJSON();
                    sendData(response, json);
                } else if (type.equalsIgnoreCase("col")) {
                    String json = analysis.global_map_config.col_clustering_params.asJSON();
                    sendData(response, json);
                }
                
                
            } else if (action.equals("get_dataset_names")) {
                
                if (analysis.data.fs_data == null) {
                    String json = new Gson().toJson(new String[]{});
                    sendData(response, json);
                } else {
                    String json = new Gson().toJson(analysis.data.fs_data.getDatasetNames());
                    sendData(response, json);
                }
                
            }
            
        } catch (MultiSlideException dpe) {
            System.out.println(dpe);
            returnMessage(new ServerResponse(0, "Clustering failed", dpe.getMessage()), response);
        } catch (DataParsingException dpe) {
            System.out.println(dpe);
            returnMessage(new ServerResponse(0, "Data parsing exception", dpe.getMessage()), response);
        } catch (Exception e) {
            System.out.println(e);
            returnMessage(new ServerResponse(0, "Exception", e.getMessage()), response);
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
