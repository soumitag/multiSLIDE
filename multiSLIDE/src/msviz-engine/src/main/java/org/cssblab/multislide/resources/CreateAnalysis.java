/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.Data;
import org.cssblab.multislide.utils.MultiSlideConfig;
import org.cssblab.multislide.utils.SessionManager;

/**
 *
 * @author Soumita
 */
public class CreateAnalysis extends HttpServlet {

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
            
            // get session
            HttpSession session = request.getSession(false);
            if (session == null) {
                ServerResponse resp = new ServerResponse(0, "Session not found", "Possibly due to time-out");
                returnMessage(resp, response);
                return;
            }
            
            DataParser parser = new DataParser(request);
            parser.addParam("analysis_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            parser.addParam("species", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "Analysis name or species is missing", parser.error_msg), response);
            }
            String analysis_name = parser.getString("analysis_name");
            String species = parser.getString("species");
            
            ServletContext context = request.getServletContext();
            String installPath = (String)context.getAttribute("install_path");
            
            Logger logger = LogManager.getRootLogger();
            logger.info("Creating analysis: " + analysis_name);
            
            // create a new analysis
            AnalysisContainer analysis = new AnalysisContainer(analysis_name, species);
            
             // set base path for analysis
            analysis.setBasePath(SessionManager.getBasePath(installPath, request.getSession().getId(), analysis.analysis_name));
            
            // create analysis directory
            SessionManager.createAnalysisDirs(analysis);
            
            // load dataset specs
            String uploadFolder = SessionManager.getSessionDir(installPath, request.getSession().getId());
            HashMap <String, DatasetSpecs> dataset_specs_map = (new DatasetSpecs(analysis_name)).loadSpecsMap(uploadFolder);
            
            // move input file into analysis directory
            SessionManager.moveInputFilesToAnalysisDir(uploadFolder, analysis.base_path, dataset_specs_map);
            
            /*
            String path = installPath + "/demo_data/";
            String[] data_types = new String[]{"CNA", "DNA Methylation", "mRNA", "Protein"};
            String[] data_filenames = new String[]{path + "formatted_data_CNA_2.txt", 
                                                   path + "formatted_data_methylation_hm450_row_centered_2.txt",
                                                   path + "formatted_data_RNA_Seq_v2_mRNA_median_Zscores_2.txt",
                                                   path + "formatted_data_rppa_mapped_2.txt"};
            */
            
            Data database = new Data(analysis.base_path, dataset_specs_map);
            analysis.setDatabase(database);
            
            Searcher searcher = new Searcher(analysis.species);
            analysis.setSearcher(searcher);
            
            // load system configuration details
            HashMap <String, String> multislide_config = MultiSlideConfig.getMultiSlideConfig(installPath);
            
            // create clusterer
            String py_module_path = multislide_config.get("py-module-path");
            String py_home = multislide_config.get("python-dir");
            HierarchicalClusterer clusterer = new HierarchicalClusterer(analysis.base_path + File.separator + "data",py_module_path, py_home);
            analysis.setClusterer(clusterer);
            
            // Finally add analysis to session
            session.setAttribute(analysis.analysis_name, analysis);
            session.setAttribute("multislide_config", multislide_config);
            
            returnMessage(new ServerResponse(1, "Analysis created", ""), response);
            return;
            
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
