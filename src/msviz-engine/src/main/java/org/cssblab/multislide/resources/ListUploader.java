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
import java.util.ArrayList;
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
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.Lists;
import org.cssblab.multislide.utils.FormElementMapper;
import org.cssblab.multislide.utils.SessionManager;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.structure.MultiSlideException;

/**
 *
 * @author soumitag
 */
public class ListUploader extends HttpServlet {
    
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
            String analysis_name = "";
            String list_name = "";
            String delimiter = "";
            String identifier_type = "";
            int identifier_index = 0;
        
            // Parse the request
            iter = items.iterator();

            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (!item.isFormField()) {
                    fileName = new File(item.getName()).getName();

                    /*
                        String expanded_filename = formData.get("analysis_name") + "_" + 
                                                   formData.get("upload_type") + "_" + 
                                                   formData.get("delimiter") + "_" + 
                                                   formData.get("identifier_type") + "_" + 
                                                   fileName;
                     */
                    
                    analysis_name = formData.get("analysis_name");
                    list_name = formData.get("data_action");
                    delimiter = formData.get("delimiter");
                    identifier_type = formData.get("identifier_type");
                    filePath = uploadFolder + File.separator + analysis_name + File.separator + fileName;
                    File uploadedFile = new File(filePath);
                    item.write(uploadedFile);
                    item.delete();

                    
                }

            }

            AnalysisContainer analysis = (AnalysisContainer) session.getAttribute(analysis_name);
            if (analysis == null) {
                ServerResponse resp = new ServerResponse(0, "Analysis not found", "Analysis name '" + analysis_name + "' does not exist");
                returnMessage(resp, response);
            }
            HashMap <String, Integer> identifier_index_map = (HashMap <String, Integer>)context.getAttribute("identifier_index_map");
            identifier_index = identifier_index_map.get(identifier_type);

            Lists lists = analysis.lists;
            lists.createEmptyFeatureList(list_name);
            ArrayList<String> list_data = FileHandler.loadListData(filePath, FormElementMapper.parseDelimiter(delimiter));
            ArrayList<String> converted_entrez_list = new ArrayList<String>();
            Searcher searcher = new Searcher(analysis.species);
            if(!identifier_type.equalsIgnoreCase("entrez_2021158607524066")){
                for(int i = 0; i < list_data.size(); i++){
                    ArrayList <String> result_entrez = searcher.getEntrezFromDB(list_data.get(i), identifier_index);
                    converted_entrez_list.addAll(result_entrez);
                }
                lists.addToFeatureList(list_name, converted_entrez_list, "");   
                
            } else {
                lists.addToFeatureList(list_name, list_data, "");       
            }
            
            // success
            try (PrintWriter out = response.getWriter()) {
                
                ServerResponse resp = new ServerResponse(1, "Success", "List with name " + list_name + " for file " + fileName + " generated successfully");
                returnMessage(resp, response);
                
            }  
            
            
            // return success
            
        }  catch (FileUploadException ex) {
            
            //return failure
            ServerResponse resp = new ServerResponse(0, "List upload is unsuccessful", ex.getMessage());
            returnMessage(resp, response);
           
        }  catch (MultiSlideException ex) {
            
            //return failure
            ServerResponse resp = new ServerResponse(0, "List upload is unsuccessful", ex.getMessage());
            returnMessage(resp, response);
            
        } catch (Exception ex){
            
            ServerResponse resp = new ServerResponse(0, "List upload is unsuccessful", ex.getMessage());
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
