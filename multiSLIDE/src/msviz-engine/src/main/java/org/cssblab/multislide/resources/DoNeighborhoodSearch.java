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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.beans.data.SearchResults;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.utils.FileHandler;

/**
 *
 * @author Soumita
 */
public class DoNeighborhoodSearch extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        try {
            
            String analysis_name = request.getParameter("analysis_name");
            
            ArrayList <SearchResults> search_results = new ArrayList <SearchResults> ();
            HttpSession session = request.getSession(false);
            AnalysisContainer analysis = (AnalysisContainer)session.getAttribute(analysis_name);
            
            /*
            String path = "D:/ad/sg/code_multislide_03_Nov_2018/multiSLIDE/demo_data/PTEN";
            String[][] genes = FileHandler.loadDelimData(path, "\t", false);
            
            for (int i=0; i<genes.length; i++) {
                SearchResultContainer_ToBeDeleted c = new SearchResultContainer_ToBeDeleted();
                c.createGeneSearchResult(genes[i][0] + " (Marbach2016)", genes[i][0] + " (Marbach2016)");
                search_result_container.add(c);
            }
            */
            
            String json = new Gson().toJson(search_results);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
            
        } catch (Exception e) {
            
            ServerResponse resp = new ServerResponse(0, "Encountered error during search", e.getMessage());
            String json = new Gson().toJson(resp);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
            
        }
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
