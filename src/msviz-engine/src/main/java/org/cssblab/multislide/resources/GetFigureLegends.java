/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.beans.data.LegendData;
import org.cssblab.multislide.beans.data.LegendGroup;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author Soumita
 */
public class GetFigureLegends extends HttpServlet {

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
            
            HttpSession session = request.getSession(false);
            AnalysisContainer analysis = (AnalysisContainer)session.getAttribute(analysis_name);
            
            /*
            ArrayList <LegendGroup> legends = new ArrayList <LegendGroup> ();
            LegendGroup legend = new LegendGroup();
            ArrayList <LegendGroup> L = legend.createPhenotypeLegendGroups(analysis);
            legends.addAll(L);
            LegendGroup legend_g = new LegendGroup();
            legend_g.createGenetagLegendGroup(analysis);
            legends.add(legend_g);
            */
            
            LegendData legend_data = new LegendData();
            
            ArrayList <LegendGroup> Lp = LegendGroup.createPhenotypeLegendGroups(analysis);
            legend_data.addLegendGroup("Phenotypes", Lp, LegendData.LEGEND_GROUP_TYPE_PHENOTYPE);
            
            ArrayList <LegendGroup> Lg = LegendGroup.createGenetagLegendGroup(analysis);
            legend_data.addLegendGroup("Gene Groups", Lg, LegendData.LEGEND_GROUP_TYPE_GENE_GROUP);
            
            if (!analysis.data_selection_state.network_neighbors.isEmpty()) {
                ArrayList <LegendGroup> Ln = LegendGroup.createNetworkNeighborhoodLegendGroup(analysis);
                legend_data.addLegendGroup("Network Neighbors", Ln, LegendData.LEGEND_GROUP_TYPE_NETWORK_NEIGHBOR);
            }
            
            String json = legend_data.legendDataAsJSON();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
            
        } catch (Exception e) {
            Utils.log_exception(e, "");
            ServerResponse resp = new ServerResponse(0, "Error generating figure legends", e.getMessage());
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
