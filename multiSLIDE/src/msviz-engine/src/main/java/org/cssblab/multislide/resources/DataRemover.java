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
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.utils.FormElementMapper;
import org.cssblab.multislide.utils.SessionManager;

/**
 *
 * @author soumitag
 */
public class DataRemover extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final String DATA_DIRECTORY = "data";
    private static final int MAX_MEMORY_SIZE = 1024 * 1024 * 1024;
    private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 1024;

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
        
        ServletContext context = request.getServletContext();
        String installPath = (String)context.getAttribute("install_path");
        
        String uploadFolder = SessionManager.getSessionDir(installPath, request.getSession().getId());

        //String uploadFolder = "F:\\code_multislide\\Multislide_30_Jun_2018";

        try {

            String analysis_name = request.getParameter("analysis_name");
            String filename = request.getParameter("filename");
            String delimiter = request.getParameter("delimiter");
            String upload_type = FormElementMapper.parseDataUploadType(request.getParameter("upload_type"));
            String identifier_type = FormElementMapper.parseRowIdentifierType(request.getParameter("identifier_type"));
            
            DatasetSpecs specs = new DatasetSpecs (analysis_name, filename, delimiter, upload_type, identifier_type);
            
            String filePath = uploadFolder + File.separator + specs.expanded_filename;
            File uploadedFile = new File(filePath);

            if (uploadedFile.exists() && !uploadedFile.isDirectory()) {
                
                uploadedFile.delete();
                specs.removeFromAnalysis(uploadFolder);
                
                // success
                try (PrintWriter out = response.getWriter()) {

                    ServerResponse resp = new ServerResponse(1, "Success", "File" + filename + "has been removed successfully");
                    String json = new Gson().toJson(resp);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(json);
                }
            } else {
                // failure
                try (PrintWriter out = response.getWriter()) {

                    ServerResponse resp = new ServerResponse(2, "Error", "File" + filename + "does not exist on the server");
                    String json = new Gson().toJson(resp);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(json);
                }

            }
            
        } catch (Exception ex) {

            //return failure
            try (PrintWriter out = response.getWriter()) {
                /* TODO output your page here. You may use following sample code. */
                ServerResponse resp = new ServerResponse(0, "Fatal Error", "File deletion is unsuccessful");
                String json = new Gson().toJson(resp);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(json);
            }

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
