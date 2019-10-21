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
import org.cssblab.multislide.beans.data.NeighborhoodSearchResults;
import org.cssblab.multislide.beans.data.SearchResults;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.searcher.network.NetworkSearchHandler;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.utils.FileHandler;

/**
 *
 * @author Soumita
 */
public class DoNeighborhoodSearch extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
        
            DataParser parser = new DataParser(request);
            parser.addParam("analysis_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "Analysis name is missing", parser.error_msg), response);
                return;
            }
            String analysis_name = parser.getString("analysis_name");

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

            parser.addParam("dataset_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            parser.addParam("query", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                return;
            }
            
            NetworkSearchHandler searchP = new NetworkSearchHandler();
            HashMap <String, ArrayList<String>> search_results = searchP.processQuery(parser.getString("query"));
            
            Heatmap heatmap = analysis.heatmaps.get(parser.getString("dataset_name"));
            
            ServletContext context = request.getServletContext();
            HashMap <String, Integer> identifier_index_map = (HashMap <String, Integer> )context.getAttribute("identifier_index_map");
            int idenitifier_index = identifier_index_map.get(heatmap.column_label);
            
            NeighborhoodSearchResults nsr = new NeighborhoodSearchResults();
            nsr.makeNeighborhoodSearchResultObject(analysis.searcher, heatmap.column_label, idenitifier_index, search_results, analysis.data.metadata.entrezMaster);
            sendData(response, nsr.asJSON());
        
        } catch (DataParsingException dpe) {
            System.out.println(dpe);
            NeighborhoodSearchResults nsr = new NeighborhoodSearchResults();
            nsr.message = dpe.getMessage();
            sendData(response, nsr.asJSON());
        } catch (Exception e) {
            System.out.println(e);
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
