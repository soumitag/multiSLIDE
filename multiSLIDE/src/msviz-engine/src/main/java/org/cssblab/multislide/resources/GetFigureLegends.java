/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.beans.data.LegendGroup;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.structure.AnalysisContainer;

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
            
            HashMap <String, ArrayList<LegendGroup>> legends = new HashMap <String, ArrayList<LegendGroup>> ();
            
            LegendGroup legend = new LegendGroup();
            ArrayList <LegendGroup> L = legend.createPhenotypeLegendGroups(analysis);
            legends.put("Phenotypes",L);
            
            LegendGroup legend_g = new LegendGroup();
            legend_g.createGenetagLegendGroup(analysis);
            ArrayList <LegendGroup> t = new ArrayList <LegendGroup> ();
            t.add(legend_g);
            legends.put("Gene Groups",t);
            
            String json = new Gson().toJson(legends);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
            
        } catch (Exception e) {
            System.out.println(e);
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
