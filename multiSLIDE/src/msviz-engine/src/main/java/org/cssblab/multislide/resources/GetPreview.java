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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.PreviewData;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.utils.FormElementMapper;
import org.cssblab.multislide.utils.SessionManager;

/**
 *
 * @author soumitag
 */
@WebServlet(name = "GetPreview", urlPatterns = {"/GetPreview"})
public class GetPreview extends HttpServlet {

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
        
        HttpSession session = request.getSession(false);
        String session_id = session.getId();
        String uploadFolder = SessionManager.getSessionDir(installPath, session_id);

        //String uploadFolder = "F:\\code_multislide\\Multislide_30_Jun_2018";

        try {

            String analysis_name = request.getParameter("analysis_name");
            String filename = request.getParameter("filename");
            String delimiter = request.getParameter("delimiter");
            String upload_type = FormElementMapper.parseDataUploadType(request.getParameter("upload_type"));
            String identifier_type = FormElementMapper.parseRowIdentifierType(request.getParameter("identifier_type"));

            DatasetSpecs specs = new DatasetSpecs(analysis_name, filename, delimiter, upload_type, identifier_type);

            String filePath = uploadFolder + File.separator + specs.expanded_filename;
            File uploadedFile = new File(filePath);

            if (uploadedFile.exists() && !uploadedFile.isDirectory()) {
                try {
                    String[][] data = FileHandler.previewFile(filePath, delimiter, 11);
                    PreviewData previewData = new PreviewData(data, new ServerResponse(1, "Success", "File preview successful"));
                    // success
                    try (PrintWriter out = response.getWriter()) {
                        String json = new Gson().toJson(previewData);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(json);
                    }

                } catch (DataParsingException dpex) {

                    //return failure
                    try (PrintWriter out = response.getWriter()) {                        
                        PreviewData prevData = new PreviewData (new ServerResponse(0, "Data parsing error", dpex.getMessage()));
                        String json = new Gson().toJson(prevData);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(json);
                    }
                } catch (IOException ioex) {

                    //return failure
                    try (PrintWriter out = response.getWriter()) {
                        PreviewData prevData = new PreviewData (new ServerResponse(0, "IO error", ioex.getMessage()));
                        String json = new Gson().toJson(prevData);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(json);
                    }
                }
            }
        } catch (Exception ex) {

            //return failure
            try (PrintWriter out = response.getWriter()) {
                /* TODO output your page here. You may use following sample code. */
                PreviewData prevData = new PreviewData (new ServerResponse(0, "Fatal Error", "File preview is unsuccessful"));
                String json = new Gson().toJson(prevData);
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
