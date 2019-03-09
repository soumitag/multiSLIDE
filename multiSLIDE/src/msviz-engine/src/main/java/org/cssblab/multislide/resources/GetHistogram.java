/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cssblab.multislide.beans.data.HistogramBean;

/**
 *
 * @author soumitag
 */
public class GetHistogram extends HttpServlet {

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
        
        String analysis_name = request.getParameter("analysis_name");
        String data_type = request.getParameter("data_type");
        String img_width = request.getParameter("img_width");
        String img_height = request.getParameter("img_height");
        
        HistogramBean hist_data = new HistogramBean(5);
        int[][] hist_colors = new int[4][3];
        hist_data.hist_colors[0][0] = 25;
        hist_data.hist_colors[0][1] = 25;
        hist_data.hist_colors[0][2] = 255;
        
        hist_data.hist_colors[1][0] = 51;
        hist_data.hist_colors[1][1] = 51;
        hist_data.hist_colors[1][2] = 255;
        
        hist_data.hist_colors[2][0] = 76;
        hist_data.hist_colors[2][1] = 76;
        hist_data.hist_colors[2][2] = 255;
        
        hist_data.hist_colors[3][0] = 102;
        hist_data.hist_colors[3][1] = 102;
        hist_data.hist_colors[3][2] = 255;
        
        //response.setContentType("text/html;charset=UTF-8");
        //try (PrintWriter out = response.getWriter()) {
        /* TODO output your page here. You may use following sample code. */
        /*
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet GetHistogram</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet GetHistogram at " + request.getContextPath() + "</h1>");
        out.println("</body>");
        out.println("</html>");
         
        }
        */
        
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            //ServerResponse resp = new ServerResponse(5, "Great", "Brilliantly executed");
            
            String json = new Gson().toJson(hist_data);
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
