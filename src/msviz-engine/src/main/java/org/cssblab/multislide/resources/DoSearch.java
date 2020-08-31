package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.beans.data.SearchResults;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.searcher.SearchHandler;
import org.cssblab.multislide.searcher.SearchResultObject;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author Soumita
 */
public class DoSearch extends HttpServlet {

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
            parser.addParam("query", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_OPTIONAL);
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                return;
            }
            
            String analysis_name = parser.getString("analysis_name");
            AnalysisContainer analysis = (AnalysisContainer)session.getAttribute(analysis_name);
            if (analysis == null) {
                ServerResponse resp = new ServerResponse(0, "Analysis not found", "Analysis name '" + analysis_name + "' does not exist");
                returnMessage(resp, response);
                return;
            }
            String query = parser.getString("query");
            if (query.trim().equals("")) {
                returnMessage(new ServerResponse(0, "Empty query string", parser.error_msg), response);
            }
            
            /*
                check if at least one dataset has mi_rna
            */
            boolean has_mi_rna = false;
            for (String name: analysis.data.datasets.keySet()) {
                has_mi_rna = has_mi_rna || analysis.data.datasets.get(name).specs.has_mi_rna;
            }
            /*
                search
            */
            Searcher searcher = (Searcher)analysis.searcher;
            HashMap <String, ArrayList <SearchResultObject>> search_results_map = SearchHandler.processSearchQuery(
                    query, searcher, analysis.data, has_mi_rna);
            ArrayList <SearchResults> search_result_groups = SearchResults.compileSearchResults(
                    search_results_map, 
                    analysis.data
            );
            analysis.data_selection_state.setCurrentSearchResults(search_result_groups);
            
            String json = new Gson().toJson(search_result_groups);
            sendData(response, json);
            
        } catch(Exception e) {
            Utils.log_exception(e, "");
            ServerResponse resp = new ServerResponse(0, "Unexpected error occurred during search", e.getMessage());
            String json = new Gson().toJson(resp);
            sendData(response, json);
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
