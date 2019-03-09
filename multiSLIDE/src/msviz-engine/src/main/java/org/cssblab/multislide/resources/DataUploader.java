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
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.utils.FormElementMapper;
import org.cssblab.multislide.utils.SessionManager;


/**
 *
 * @author soumitag
 */
public class DataUploader extends HttpServlet {
    
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
        String installPath = (String)context.getAttribute("install_path");
        
        // data will be uploaded temporarily to session-dir. session-dir is already created in newExperiment.jsp
        // here just get the path
        HttpSession session = request.getSession(false);
        String session_id = session.getId();
        //String uploadFolder = SessionManager.getSessionDir(installPath, session.getId());
        String uploadFolder = SessionManager.getSessionDir(installPath, session_id);
        //String uploadFolder = "F:\\code_multislide\\Multislide_30_Jun_2018";
        
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        
        // Set overall request size constraint
        upload.setSizeMax(MAX_REQUEST_SIZE);
        
        try {
            
            HashMap <String, String> formData = new HashMap <String, String> ();
            
            List items = upload.parseRequest(request);
            Iterator iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (item.isFormField()) {
                    String name = item.getFieldName();
                    if (!name.trim().equalsIgnoreCase("delimiter")) {
                        String value = FormElementMapper.parseFormData(name, item.getString());
                        formData.put(name, value);
                    } else {
                        formData.put(name, item.getString().trim().toLowerCase());
                    }
                }
            }
        
            String filePath = "";
            String fileName = "";
        
            // Parse the request
            iter = items.iterator();

            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (!item.isFormField()) {
                    fileName = new File(item.getName()).getName();

                    if (fileName.toLowerCase().endsWith(".xls")
                            || fileName.toLowerCase().endsWith(".xlsx")) {

                        String msg = "Upload Failed! Multi-SLIDE cannot process Excel files. Please provide a delimited file in tsv, csv or txt format.";
                        
                        try (PrintWriter out = response.getWriter()) {
                            /* TODO output your page here. You may use following sample code. */
                            ServerResponse resp = new ServerResponse(0, "Error", msg);
                            String json = new Gson().toJson(resp);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(json);
                        }

                        //return failure
                        return;

                    } else {

                        /*
                        String expanded_filename = formData.get("analysis_name") + "_" + 
                                                   formData.get("upload_type") + "_" + 
                                                   formData.get("delimiter") + "_" + 
                                                   formData.get("identifier_type") + "_" + 
                                                   fileName;
                        */
                        DatasetSpecs specs = new DatasetSpecs(formData);
                        
                        filePath = uploadFolder + File.separator + specs.expanded_filename;
                        File uploadedFile = new File(filePath);
                        item.write(uploadedFile);
                        item.delete();
                        
                        specs.addToAnalysis(uploadFolder);
                    }
                }
            }
            
            // success
            try (PrintWriter out = response.getWriter()) {
                
                ServerResponse resp = new ServerResponse(1, "Success", "File " + fileName + " uploaded successfully");
                String json = new Gson().toJson(resp);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(json);
            }  
            
            
            // return success
            
        }  catch (FileUploadException ex) {
            
            //return failure
            
            try (PrintWriter out = response.getWriter()) {
                /* TODO output your page here. You may use following sample code. */
                ServerResponse resp = new ServerResponse(0, "Fatal Error", "File upload is unsuccessful");
                String json = new Gson().toJson(resp);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(json);
            }
        }  catch (Exception ex) {
            
            //return failure
            try (PrintWriter out = response.getWriter()) {
                /* TODO output your page here. You may use following sample code. */
                ServerResponse resp = new ServerResponse(0, "Fatal Error", "File upload is unsuccessful");
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
