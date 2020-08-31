/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.beans.data.BipartiteLinkageGraph;
import org.cssblab.multislide.beans.data.EnrichmentAnalysisResult;
import org.cssblab.multislide.beans.data.FunctionalGroupContainer;
import org.cssblab.multislide.beans.data.MappedData;
import org.cssblab.multislide.beans.data.SelectionPanelData;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.DataSelectionState;
import org.cssblab.multislide.structure.EnrichmentAnalysisParams;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author Soumita
 */
public class GetPhenotypes extends HttpServlet {

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

            parser.addParam("action", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, 
                    new String[]{ 
                        "panel_data", 
                        "panel_state", 
                        "set_enrichment_analysis_params",
                        "get_functional_grp_names",
                        "download_enrichment_analysis_results",
                        "get_user_specified_connections",
                        "delete_user_specified_connections"
                    });
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                return;
            }
            
            if (parser.getString("action").equals("panel_data")) {
                
                SelectionPanelData data = new SelectionPanelData(analysis);
                String json = data.asJSON();
                sendData(response, json);
                
            } else if (parser.getString("action").equals("panel_state")) {
                
                DataSelectionState state = analysis.data_selection_state;
                String json = state.asJSON();
                sendData(response, json);
                
            } else if (parser.getString("action").equals("set_enrichment_analysis_params")) {
                
                parser.addParam("dataset", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("phenotype", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("testtype", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("use_pathways", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("use_ontologies", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("significance_level_d", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("apply_fdr_d", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("significance_level_e", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("apply_fdr_e", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                
                double fdr_threshold_d = 1.0;
                if (parser.getBool("apply_fdr_d")) {
                    parser.addParam("fdr_threshold_d", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_REQUIRED);
                    if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                    fdr_threshold_d = parser.getDouble("fdr_threshold_d");
                }
                
                double fdr_threshold_e = 1.0;
                if (parser.getBool("apply_fdr_e")) {
                    parser.addParam("fdr_threshold_e", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_REQUIRED);
                    if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                    fdr_threshold_e = parser.getDouble("fdr_threshold_e");
                }
                
                analysis.data_selection_state.enrichment_analysis_params = new EnrichmentAnalysisParams(
                    parser.getString("phenotype"), 
                    parser.getString("dataset"), 
                    parser.getDouble("significance_level_d"),
                    parser.getString("testtype"), 
                    parser.getBool("use_pathways"),
                    parser.getBool("use_ontologies"),
                    parser.getBool("apply_fdr_d"),
                    fdr_threshold_d,
                    parser.getDouble("significance_level_e"),
                    parser.getBool("apply_fdr_e"),  
                    fdr_threshold_e);
                
                //EnrichmentAnalysisResult[] results = EnrichmentAnalysisResult.loadFromFile("");
                EnrichmentAnalysisResult[] results = analysis.enrichment_analyzer.doEnrichmentAnalysis(analysis, true);
                analysis.data_selection_state.setCurrentEnrichmentAnalysisResults(Arrays.asList(results));
                
                MappedData md = new MappedData(1, "", "");
                md.addNameValuePair("results", new Gson().toJson(results));
                String json = md.asJSON();
                sendData(response, json);
                return;
                
            } else if (parser.getString("action").equals("get_functional_grp_names")) {
                //FunctionalGroupContainer[] containers = FunctionalGroupContainer.populateList();
                FunctionalGroupContainer[] containers = new FunctionalGroupContainer[analysis.data_selection_state.functional_groups_in_file.length];
                int count = 0;
                for (FunctionalGroupContainer c: analysis.data_selection_state.functional_groups_in_file) {
                    containers[count++] = c.getSummary();
                }
                
                MappedData md = new MappedData(1, "", "");
                md.addNameValuePair("functional_group_containers", new Gson().toJson(containers));
                String json = md.asJSON();
                sendData(response, json);
                return;
                
            } else if (parser.getString("action").equals("download_enrichment_analysis_results")) {
                analysis.enrichment_analyzer.doEnrichmentAnalysis(analysis, false);
                String ea_out_file_name = analysis.enrichment_analyzer.getLastOutputFile();
                File f = new File(ea_out_file_name);
                java.io.InputStream is = new FileInputStream(f);
                byte[] bytes = new byte[(int)f.length()];
                int read = is.read(bytes, 0, bytes.length);
                
                response.setContentType("application/download");
                response.setHeader("Content-Disposition", "attachment;filename=" + "Enrichment_Analysis_Results.txt");
                OutputStream os = response.getOutputStream();
                os.write(bytes, 0, read);
                os.flush();
                os.close();
                return;
                
            }  else if (parser.getString("action").equals("get_user_specified_connections")) {
                
                List <String[]> usp_conns = new ArrayList <> ();
                for (String s: analysis.data_selection_state.user_defined_between_omics_linkages.keySet()) {
                    BipartiteLinkageGraph blg = analysis.data_selection_state.user_defined_between_omics_linkages.get(s);
                    usp_conns.add(new String[]{blg.display_name, blg.filename});
                }
                
                String json = new Gson().toJson(usp_conns);
                sendData(response, json);
                return;
                
            }   else if (parser.getString("action").equals("delete_user_specified_connections")) {
                
                parser.addParam("filename", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                String filename = parser.getString("filename");
                
                for (String s: analysis.data_selection_state.user_defined_between_omics_linkages.keySet()) {
                    BipartiteLinkageGraph blg = analysis.data_selection_state.user_defined_between_omics_linkages.get(s);
                    if (blg.filename.equals(filename)) {
                        analysis.data_selection_state.user_defined_between_omics_linkages.remove(s);
                        returnMessage(new ServerResponse(1, "Connections '" + filename + "' deleted.", ""), response);
                        return;
                    }
                }
                
                returnMessage(new ServerResponse(0, "Connections '" + filename + "' not found.", ""), response);
                return;
            }
            
        } catch (Exception e) {
            Utils.log_exception(e, "");
            ServerResponse resp = new ServerResponse(0, "Could not open analyis, perhaps session has expired.", e.getMessage());
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
