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
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.Serializer;
import org.cssblab.multislide.utils.FormElementMapper;
import org.cssblab.multislide.utils.SessionManager;

/**
 *
 * @author soumitag
 */
public class LoadAnalysis extends HttpServlet {

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

        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (!isMultipart) {
            ServerResponse resp = new ServerResponse(0, "Analysis upload failed", "Not multipart data.");
            returnMessage(resp, response);
            return;
        }

        // Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();

        // Sets the size threshold beyond which files are written directly to
        // disk.
        factory.setSizeThreshold(MAX_MEMORY_SIZE);

        // Sets the directory used to temporarily store files that are larger
        // than the configured size threshold. We use temporary directory for
        // java
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        // get base path for analysis
        ServletContext context = request.getServletContext();
        String installPath = (String) context.getAttribute("install_path");
        
        // check if a session already exists
        HttpSession session = request.getSession(true);
        if (request.getSession(false) == null) {
            // if not, create new session
            session = request.getSession(true);
        }
        
        if (session == null) {
            //return failure
            ServerResponse resp = new ServerResponse(0, "Analysis upload failed", "Could not create a session");
            returnMessage(resp, response);
            return;
        }
        
        // get the current session's id        
        String session_id = session.getId();
        
        // create session directory
        SessionManager.createSessionDir(installPath, session_id);

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Set overall request size constraint
        upload.setSizeMax(MAX_REQUEST_SIZE);

        try {

            List items = upload.parseRequest(request);
            Iterator iter = items.iterator();
            
            String uploadFolder = installPath + File.separator + "temp" + File.separator + session_id;
            String filePath = "";
            String fileName = "";

            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (!item.isFormField()) {
                    fileName = new File(item.getName()).getName();

                    if (!fileName.toLowerCase().endsWith(".mslide")) {
                        //return failure
                        ServerResponse resp = new ServerResponse(0, "Analysis upload failed", "Only '.mslide' are allowed");
                        returnMessage(resp, response);
                        return;
                    }

                    filePath = uploadFolder + File.separator + fileName;
                    File uploadedFile = new File(filePath);
                    item.write(uploadedFile);
                    item.delete();

                }
            }
            
            Serializer serializer = new Serializer();
            AnalysisContainer analysis = serializer.loadAnalysis(filePath);
            
            if (analysis == null) {
                ServerResponse resp = new ServerResponse(0, "Analysis could not be loaded", "Incorrect '.mslide' file");
                returnMessage(resp, response);
                return;
            }
            
            if (session.getAttribute(analysis.analysis_name) != null) {
                ServerResponse resp = new ServerResponse(0, "Analysis could not be loaded", "An analysis with the name '" + analysis.analysis_name + "' is already open");
                returnMessage(resp, response);
                return;
            }

            // log
            Logger logger = LogManager.getRootLogger();
            logger.info("Load Analysis called");
            
            // set base path for analysis
            analysis.setBasePath(SessionManager.getBasePath(installPath, request.getSession().getId(), analysis.analysis_name));
            
             // create analysis directory
            SessionManager.createAnalysisDirs(analysis);
            
            // set searcher for analysis as it was nullified when saving
            analysis.setSearcher(new Searcher(analysis.species));
            
            // Finally add analysis to session
            session.setAttribute(analysis.analysis_name, analysis);
            
            ServerResponse resp = new ServerResponse(1, analysis.analysis_name, "Analysis loaded");
            returnMessage(resp, response);

        } catch (FileUploadException ex) {

            //return failure
            ServerResponse resp = new ServerResponse(0, "Analysis upload is unsuccessful", ex.getMessage());
            returnMessage(resp, response);

        } catch (MultiSlideException ex) {

            //return failure
            ServerResponse resp = new ServerResponse(0, "Analysis upload is unsuccessful", ex.getMessage());
            returnMessage(resp, response);

        } catch (Exception ex) {

            ServerResponse resp = new ServerResponse(0, "Analysis upload is unsuccessful", ex.getMessage());
            returnMessage(resp, response);

        }
    }

    protected void returnMessage(ServerResponse resp, HttpServletResponse response) throws IOException {
        String json = new Gson().toJson(resp);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
        return;
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
