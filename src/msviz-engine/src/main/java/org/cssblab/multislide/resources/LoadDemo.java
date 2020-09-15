/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.sql.SparkSession;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.algorithms.statistics.EnrichmentAnalysis;
import org.cssblab.multislide.algorithms.statistics.SignificanceTester;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.data.Data;
import org.cssblab.multislide.graphics.PyColorMaps;
import org.cssblab.multislide.utils.MultiSlideConfig;
import org.cssblab.multislide.utils.SessionManager;
import org.cssblab.multislide.utils.Utils;
import org.cssblab.multislide.resources.LoadAnalysis;
import org.cssblab.multislide.structure.AnalysisState;
import org.cssblab.multislide.structure.DataSelectionState;
import org.cssblab.multislide.structure.MapConfig;
import org.cssblab.multislide.structure.Serializer;
import org.cssblab.multislide.utils.FileHandler;

/**
 *
 * @author Soumita
 */
public class LoadDemo extends HttpServlet {

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
            parser.addParam("demo_id", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            parser.addParam("demo_number", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "demo_id missing", parser.error_msg), response);
                return;
            }
            
            String analysis_name = "demo_2021158607524066_" + parser.getString("demo_id");
            int demo_number = parser.getInt("demo_number");
            
            ServletContext context = request.getServletContext();
            String installPath = (String)context.getAttribute("install_path");
            
            // get the current session's id
            String session_id;
            HttpSession session = request.getSession(true);
            // check if a session already exists
            if (request.getSession(false) == null) {
                // if not, create new session
                session = request.getSession(true);
                session_id = session.getId();
            } else {
                // if it does, check if an analysis of same name exists in the session
                if (session.getAttribute(analysis_name) == null) {
                    // if not, create required temp folders for this analysis
                    session_id = session.getId();
                } else {
                    // if it does, send back to previous page with error message
                    //return failure
                    String detailed_reason = "An analysis with the name " + analysis_name + " already exists";
                    ServerResponse resp = new ServerResponse(0, "Failed to load demo:", detailed_reason);
                    returnMessage(resp, response);
                    return;
                }

            }
            
            // create session directory
            SessionManager.createSessionDir(installPath, session_id);
            String uploadFolder = SessionManager.getSessionDir(installPath, request.getSession().getId());
            
            // log
            Logger logger = LogManager.getRootLogger();
            logger.info("Load Demo called");
            
            String mslide_filename = "";
            switch (demo_number) {
                case 0:
                    mslide_filename = "UPR_On_ER_Stress_12-Sep-2020_17-12-50.mslide";
                    break;
                case 1:
                    mslide_filename = "CPTAC_Ovarian_Cancer_12-Sep-2020_22-32-11.mslide";
                    break;
                case 2:
                    mslide_filename = "Plasma_Protein_and_MicroRNA_IR_Markers_12-Sep-2020_17-43-53.mslide";
                    break;
                default:
                    Utils.log("Failed in load demo: invalid demo_number");
                    ServerResponse resp = new ServerResponse(0, "Load demo is unsuccessful", "Invalid demo_number");
                    returnMessage(resp, response);
                    break;
            }
            
            String demoDataFolder = installPath + File.separator + "demo_data";
            String filePath = demoDataFolder + File.separator + mslide_filename;
            
            // load state from mslide file
            Serializer serializer = new Serializer();
            AnalysisState analysis_state = serializer.loadAnalysis(filePath, Serializer.TYPE_JSON);
            
            // rename analysis
            analysis_state.renameAnalysis(analysis_name);
            
            // create a new analysis
            AnalysisContainer analysis = new AnalysisContainer(analysis_state.analysis_name, analysis_state.species);
            
            // set base path for analysis
            analysis.setBasePath(SessionManager.getBasePath(installPath, request.getSession().getId(), analysis_state.analysis_name));
            
            // create analysis directory
            SessionManager.createAnalysisDirs(analysis);
            
            // copy analysis data to files
            for (String key: analysis_state.dataset_specs_map.keyList()) {
                DatasetSpecs specs = analysis_state.dataset_specs_map.get(key);
                String filepath = analysis.base_path + File.separator + specs.getFilenameWithinAnalysisFolder();
                FileHandler.saveDataMatrix(filepath, specs.delimiter, analysis_state.raw_data_map.get(specs.unique_name));
            }
            DatasetSpecs.serializeSpecsMap(uploadFolder, analysis_state.analysis_name, analysis_state.dataset_specs_map);
            
            // set state
            analysis.data_selection_state = analysis_state.data_selection_state;
            analysis.global_map_config = analysis_state.global_map_config;
            
            // set analytics_server for analysis
            analysis.setAnalyticsServer((String)context.getAttribute("analytics_server_address"));
            
            // add spark session
            analysis.setSparkSession((SparkSession)context.getAttribute("spark"));
            
            // set searcher for analysis as it was nullified when saving
            analysis.setSearcher(new Searcher(analysis.species));
            
            // create clusterer and significance tester
            String cache_path = installPath + File.separator + "temp" + File.separator + "cache";
            HierarchicalClusterer clusterer = new HierarchicalClusterer(cache_path, analysis.analytics_engine_comm);
            analysis.setClusterer(clusterer);
            
            // create significance tester
            SignificanceTester significance_tester = new SignificanceTester(cache_path, analysis.analytics_engine_comm);
            analysis.setSignificanceTester(significance_tester);
            
            // create enrichment analyzer
            EnrichmentAnalysis enrichment_analyzer = new EnrichmentAnalysis(cache_path, analysis.analytics_engine_comm);
            analysis.setEnrichmentAnalyzer(enrichment_analyzer);
            
            // create database
            ColorPalette categorical_palette = (ColorPalette)context.getAttribute("categorical_palatte");
            ColorPalette continuous_palette = (ColorPalette)context.getAttribute("continuous_palatte");
            Data database = new Data(
                    analysis.spark_session, analysis.base_path, analysis_state.getDatasetSpecs(), 
                    categorical_palette, continuous_palette, analysis.searcher);
            analysis.setDatabase(database);
            
            // create selection (to bypass AnalysisReInitializer)
            boolean has_selected_data = false;
            if (analysis.data_selection_state.add_genes_source_type == DataSelectionState.ADD_GENES_SOURCE_TYPE_SEARCH
                    && analysis.data_selection_state.selected_searches.length > 0) {
                has_selected_data = true;
            }
            if (analysis.data_selection_state.add_genes_source_type == DataSelectionState.ADD_GENES_SOURCE_TYPE_ENRICHMENT
                    && analysis.data_selection_state.selected_enriched_groups.length > 0) {
                has_selected_data = true;
            }
            if (analysis.data_selection_state.add_genes_source_type == DataSelectionState.ADD_GENES_SOURCE_TYPE_UPLOAD
                    && analysis.data_selection_state.selected_functional_groups.length > 0) {
                has_selected_data = true;
            }
            
            ColorPalette gene_group_color_palette = (ColorPalette)context.getAttribute("gene_group_color_palette");
            if (has_selected_data) {
                analysis.data.createSelection(
                            analysis, 
                            gene_group_color_palette
                );
                
                // create heatmaps (to bypass AnalysisReInitializer)
                Map<String, Heatmap> heatmaps = new ListOrderedMap <> ();
                for (String dataset_name : analysis.data_selection_state.selected_datasets) {
                    MapConfig map_config = analysis_state.map_configs.get(dataset_name);
                    Heatmap heatmap = new Heatmap(
                            analysis, dataset_name,
                            analysis.data.datasets.get(dataset_name).specs,
                            map_config.getnColors()-1, 
                            map_config.getBinningRange(),
                            map_config.getColorScheme(), 
                            map_config.getBinningRangeStart(), 
                            map_config.getBinningRangeEnd());
                    heatmap.genColorData((PyColorMaps) context.getAttribute("colormaps"));
                    heatmap.assignBinsToRows(analysis.spark_session, analysis.data.selected);
                    heatmaps.put(dataset_name, heatmap);
                }
                analysis.heatmaps = heatmaps;
            }
            
            // finally add analysis to session
            session.setAttribute(analysis.analysis_name, analysis);
            
            returnMessage(new ServerResponse(1, "Demo Loaded.", ""), response);
            return;
            
        } catch (Exception e) {
            Utils.log_exception(e, "Failed in load demo");
            returnMessage(new ServerResponse(0, "Failed to load demo. Please contact administrator. Error:", e.getMessage()), response);
            return;
        }
        
    }
    
    protected void returnMessage(ServerResponse resp, HttpServletResponse response) throws IOException {
        String json = new Gson().toJson(resp);
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
