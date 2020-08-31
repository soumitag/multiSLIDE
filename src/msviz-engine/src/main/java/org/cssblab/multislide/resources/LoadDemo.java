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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.data.Data;
import org.cssblab.multislide.graphics.PyColorMaps;
import org.cssblab.multislide.utils.MultiSlideConfig;
import org.cssblab.multislide.utils.SessionManager;
import org.cssblab.multislide.utils.Utils;

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
            
            // create a new analysis
            AnalysisContainer analysis = new AnalysisContainer("demo_2021158607524066_" + parser.getString("demo_id"), "human");
            
            // set base path for analysis
            analysis.setBasePath(SessionManager.getBasePath(installPath, request.getSession().getId(), analysis.analysis_name));
            
            // create analysis directory
            SessionManager.createAnalysisDirs(analysis);
            
            // set analytics_server for analysis
            analysis.setAnalyticsServer((String)context.getAttribute("analytics_server_address"));
            
            // add spark session
            analysis.setSparkSession((SparkSession)context.getAttribute("spark"));
            
            //HashMap <String, DatasetSpecs> specs = new HashMap <String, DatasetSpecs> ();
            List <DatasetSpecs> specs = new ArrayList <DatasetSpecs> ();

            if (demo_number == 2) {
                
                DatasetSpecs spec_0 = new DatasetSpecs(
                        "demo_2021158607524066_" + parser.getString("demo_id"), 
                        "BRCA_groupinfo_example_new.txt", 
                        "tab", "clinical-info", uploadFolder);
                //specs.put(spec_0.getExpandedFilename(), spec_0);
                specs.add(spec_0);

                /*
                DatasetSpecs spec_1 = new DatasetSpecs("demo_" + parser.getString("demo_id"), "DNA_imputedDat_new.txt", "tab", "CNA", "");
                spec_1.setMetaDataColumns(new String[]{"Genesym"},
                        new String[]{"Genesym"},
                        new String[]{"genesymbol_2021158607524066"});
                specs.put(spec_1.expanded_filename, spec_1);
                */

                

                DatasetSpecs spec_2 = new DatasetSpecs(
                        "demo" + parser.getString("demo_id"), 
                        "mRNA_imputedDat_centered_rounded_73_filtered.txt", 
                        "tab", "m_rna", uploadFolder
                );
                spec_2.update(new String[]{"Genesym"}, true, "Genesym", "genesymbol_2021158607524066", false, new String[]{});
                //specs.put(spec_3.getExpandedFilename(), spec_3);
                specs.add(spec_2);
                
                DatasetSpecs spec_3 = new DatasetSpecs(
                        "demo_2021158607524066_" + parser.getString("demo_id"), 
                        "PROT_imputedDat_rounded_73_filtered.txt", 
                        "tab", "protein", uploadFolder
                );
                spec_3.update(
                        new String[]{"genesym"}, true, "genesym", "genesymbol_2021158607524066", false, new String[]{}
                );
                //specs.put(spec_2.getExpandedFilename(), spec_2);
                specs.add(spec_3);
                
                DatasetSpecs spec_4 = new DatasetSpecs(
                        "demo" + parser.getString("demo_id"), 
                        "CPTAC_Phosphoproteome_centered_selected_metadata.txt", 
                        "tab", "phosphoproteome", uploadFolder
                );
                spec_4.update(new String[]{"unique_idenitifier", "variableSites", "sequence", "accession_number", "geneName"}, 
                        true, "geneName", "genesymbol_2021158607524066", true, new String[]{"unique_idenitifier"});
                //specs.put(spec_4.getExpandedFilename(), spec_4);
                specs.add(spec_4);
                
            } else if (demo_number == 1){
                
                DatasetSpecs spec_0 = new DatasetSpecs(
                        "demo_2021158607524066_" + parser.getString("demo_id"), 
                        "Sample_grouping.txt", 
                        "tab", "clinical-info", uploadFolder
                );
                //specs.put(spec_0.getExpandedFilename(), spec_0);
                specs.add(spec_0);

                DatasetSpecs spec_1 = new DatasetSpecs(
                        "demo_2021158607524066_" + parser.getString("demo_id"), 
                        "mRNA_baselined_removeB.txt", 
                        "tab", "m_rna", uploadFolder
                );
                spec_1.update(
                        new String[]{"Ensembl", "GeneSymbol"},
                        true, "GeneSymbol", "genesymbol_2021158607524066", true, new String[]{"Ensembl"});
                //specs.put(spec_1.getExpandedFilename(), spec_1);
                specs.add(spec_1);

                DatasetSpecs spec_2 = new DatasetSpecs(
                        "demo_2021158607524066_" + parser.getString("demo_id"), 
                        "protein_baselined_removeB.txt", 
                        "tab", "protein", uploadFolder
                );
                spec_2.update(
                        new String[]{"Ensembl", "GeneSymbol"},
                        true, "GeneSymbol", "genesymbol_2021158607524066", true, new String[]{"Ensembl"});
                //specs.put(spec_2.getExpandedFilename(), spec_2);
                specs.add(spec_2);
                
            } else if (demo_number == 0) {
                
                DatasetSpecs spec_0 = new DatasetSpecs(
                        "demo_2021158607524066_" + parser.getString("demo_id"), 
                        "Classification_of_TCGA_IDH-WT_GBMs.txt", 
                        "tab", "clinical-info", uploadFolder
                );
                //specs.put(spec_0.getExpandedFilename(), spec_0);
                specs.add(spec_0);

                DatasetSpecs spec_1 = new DatasetSpecs(
                        "demo_2021158607524066_" + parser.getString("demo_id"), 
                        "mRNA_microarray_U133A_366_IDH_WT_subtyping_row_centered_post_filter.txt", 
                        "tab", "m_rna", uploadFolder
                );
                spec_1.update(new String[]{"Gene"}, true, "Gene", "genesymbol_2021158607524066", false, new String[]{});
                //specs.put(spec_1.getExpandedFilename(), spec_1);
                specs.add(spec_1);
                
            }

            // move input file into analysis directory
            SessionManager.moveInputFilesToAnalysisDir_ForDemo(installPath + File.separator + "demo_data", analysis.base_path, specs);
            
            // add searcher (must be done before setting database)
            Searcher searcher = new Searcher(analysis.species);
            analysis.setSearcher(searcher);
            
            // create data
            ColorPalette categorical_palette = (ColorPalette)context.getAttribute("categorical_palatte");
            ColorPalette continuous_palette = (ColorPalette)context.getAttribute("continuous_palatte");
            //HashMap <String, Integer> identifier_index_map = (HashMap <String, Integer>)context.getAttribute("identifier_index_map");
            Data database = new Data(
                    analysis.spark_session, analysis.base_path, specs, 
                    categorical_palette, continuous_palette, analysis.searcher);
            analysis.setDatabase(database);
            
            //initialize data_selection_state
            analysis.data_selection_state.setDefaultDatasetAndPhenotypes(database.dataset_names, database.clinical_info.getPhenotypeNames());
            analysis.global_map_config.setDefaultDatasetLinking(database.datasets);
            
            // load system configuration details
            HashMap <String, String> multislide_config = MultiSlideConfig.getMultiSlideConfig(installPath);
            
            // create clusterer and significance tester
            String py_module_path = multislide_config.get("py-module-path");
            String py_home = multislide_config.get("python-dir");
            String cache_path = installPath + File.separator + "temp" + File.separator + "cache";
            HierarchicalClusterer clusterer = new HierarchicalClusterer(cache_path, analysis.analytics_engine_comm);
            analysis.setClusterer(clusterer);
            
            // create significance tester
            SignificanceTester significance_tester = new SignificanceTester(cache_path, analysis.analytics_engine_comm);
            analysis.setSignificanceTester(significance_tester);
            
            // create enrichment analyzer
            EnrichmentAnalysis enrichment_analyzer = new EnrichmentAnalysis(cache_path, analysis.analytics_engine_comm);
            analysis.setEnrichmentAnalyzer(enrichment_analyzer);
            
            // Finally add analysis to session
            session.setAttribute(analysis.analysis_name, analysis);
            session.setAttribute("multislide_config", multislide_config);
            
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
