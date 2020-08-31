/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
import org.cssblab.multislide.beans.data.BipartiteLinkageGraph;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.utils.FormElementMapper;
import org.cssblab.multislide.utils.SessionManager;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.utils.Utils;


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
            returnMessage(new ServerResponse(0, "Fatal Error", "File upload is unsuccessful"), response);
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
            
            String[] actions = new String[]{
                "upload",
                "upload_add_genes",
                "upload_connections"
            };
            if (!formData.containsKey("analysis_name")) {
                returnMessage(new ServerResponse(0, "Bad param", "Missing analysis name"), response);
                return;
            }
            if (!formData.containsKey("data_action")) {
                returnMessage(new ServerResponse(0, "Bad param", "Missing action"), response);
                return;
            }
            String analysis_name = formData.get("analysis_name");
            String action = formData.get("data_action");
            if (!Arrays.asList(actions).contains(action)) {
                returnMessage(new ServerResponse(0, "Bad param", "Unknown action"), response);
                return;
            }

            HttpSession session = request.getSession(false);
            if (session == null) {
                ServerResponse resp = new ServerResponse(0, "Session not found", "Possibly due to time-out");
                returnMessage(resp, response);
                return;
            }
            
            /* 
            For dataset upload (action=upload)
            data will be uploaded temporarily to session-dir. session-dir should be already created in crete_experiment
            here just get the path again
            */
            String session_id = session.getId();
            String uploadFolder = SessionManager.getSessionDir(installPath, session_id);

            if (action.equals("upload")) {
                
                uploadDatasets(formData, items, response, uploadFolder);
                
            } else if (action.equals("upload_add_genes")) {
                
                AnalysisContainer analysis = (AnalysisContainer) session.getAttribute(analysis_name);
                if (analysis == null) {
                    ServerResponse resp = new ServerResponse(0, "Analysis not found", "Analysis name '" + analysis_name + "' does not exist");
                    returnMessage(resp, response);
                    return;
                }
                
                handleAddGenesPathwayUpload(analysis, formData, items, response, uploadFolder);
                
            } else if (action.equals("upload_connections")) {
                
                AnalysisContainer analysis = (AnalysisContainer) session.getAttribute(analysis_name);
                if (analysis == null) {
                    ServerResponse resp = new ServerResponse(0, "Analysis not found", "Analysis name '" + analysis_name + "' does not exist");
                    returnMessage(resp, response);
                    return;
                }
                
                handleAddConnections(analysis, formData, items, response, uploadFolder);
                
            }

        }  catch (FileUploadException ex) {
            Utils.log_exception(ex, "File Upload Exception");
            returnMessage(new ServerResponse(0, "File upload is unsuccessful", "File upload is unsuccessful"), response);
        }  catch (Exception ex) {
            Utils.log_exception(ex, "");
            returnMessage(new ServerResponse(0, "File upload is unsuccessful", "File upload is unsuccessful"), response);
        }
    }
    
    public void uploadDatasets(
            HashMap <String, String> formData, List items, HttpServletResponse response, String uploadFolder)
    throws IOException, Exception {
        
        String filePath = "";
        String fileName = "";

        // Parse the request
        Iterator iter = items.iterator();

        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            if (!item.isFormField()) {
                fileName = new File(item.getName()).getName();

                if (fileName.toLowerCase().endsWith(".xls")
                        || fileName.toLowerCase().endsWith(".xlsx")) {

                    String msg = "Upload Failed! multi-SLIDE cannot process Excel files. Please provide a delimited file in tsv, csv or txt format.";
                    returnMessage(new ServerResponse(0, "Error", msg), response);
                    
                } else {
                    DatasetSpecs specs = new DatasetSpecs(formData, uploadFolder);
                    filePath = uploadFolder + File.separator + specs.getExpandedFilename();
                    File uploadedFile = new File(filePath);
                    item.write(uploadedFile);
                    item.delete();
                    specs.addToAnalysis(uploadFolder);
                }
            }
        }

        // success
        returnMessage(new ServerResponse(1, "Success", "File " + fileName + " uploaded successfully"), response);
    }
    
    public void handleAddConnections(
            AnalysisContainer analysis, 
            HashMap <String, String> formData, List items, HttpServletResponse response, String session_dir)
    throws IOException, Exception {
        
        String filePath = "";
        String fileName = "";

        // Parse the request
        Iterator iter = items.iterator();

        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            if (!item.isFormField()) {
                fileName = new File(item.getName()).getName();

                if (fileName.toLowerCase().endsWith(".xls")
                        || fileName.toLowerCase().endsWith(".xlsx")) {

                    String msg = "Upload Failed! multi-SLIDE cannot process Excel files. Please provide a delimited file in tsv, csv or txt format.";
                    returnMessage(new ServerResponse(0, "Error", msg), response);
                    
                } else {
                    filePath = session_dir + File.separator + formData.get("analysis_name");
                    String filename = formData.get("filename");
                    String fqpath = filePath + File.separator + filename;
                    File uploadedFile = new File(fqpath);
                    item.write(uploadedFile);
                    item.delete();
                    try {
                        
                        HashMap <String, String> filename_map = new HashMap <> ();
                        for (String dataset_name: analysis.data.datasets.keySet()) {
                            String fname = analysis.data.datasets.get(dataset_name).specs.filename;
                            filename_map.put(fname.toLowerCase(), dataset_name);
                        }
                        
                        BipartiteLinkageGraph linkage = new BipartiteLinkageGraph(
                                formData.get("identifier_type"), formData.get("filename"), filePath, formData.get("delimiter"), filename_map);
                        
                        analysis.data_selection_state.adduserSpecifiedInterOmicsConnections(linkage);
                        
                    } catch (Exception e) {
                        Utils.log_exception(e, "");
                        returnMessage(new ServerResponse(0, "Error parsing file.", Arrays.toString(e.getStackTrace())), response);
                        return;
                    }
                }
            }
        }

        // success
        returnMessage(new ServerResponse(1, "File " + fileName + " uploaded successfully", ""), response);
        
    }
    
    public void handleAddGenesPathwayUpload(
            AnalysisContainer analysis, 
            HashMap <String, String> formData, List items, HttpServletResponse response, String session_dir)
    throws IOException, DataParsingException, Exception {
        
        String filePath = "";
        String fileName = "";

        // Parse the request
        Iterator iter = items.iterator();

        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            if (!item.isFormField()) {
                fileName = new File(item.getName()).getName();

                if (fileName.toLowerCase().endsWith(".xls")
                        || fileName.toLowerCase().endsWith(".xlsx")) {

                    String msg = "Upload Failed! multi-SLIDE cannot process Excel files. Please provide a delimited file in tsv, csv or txt format.";
                    returnMessage(new ServerResponse(0, "Error", msg), response);
                    
                } else {
                    filePath = session_dir + File.separator + 
                            formData.get("analysis_name") + File.separator + 
                            formData.get("filename");
                    File uploadedFile = new File(filePath);
                    item.write(uploadedFile);
                    item.delete();
                    try {
                        HashMap <String, String> filename_map = new HashMap <> ();
                        for (String dataset_name: analysis.data.datasets.keySet()) {
                            String filename = analysis.data.datasets.get(dataset_name).specs.filename;
                            filename_map.put(filename.toLowerCase(), dataset_name);
                        }
                        
                        analysis.data_selection_state.setFunctionalGroupsInFile(
                            FileHandler.parseUserSpecifiedPathways(filePath, formData.get("delimiter"), filename_map, analysis)
                        );
                        analysis.data_selection_state.uploaded_filename = formData.get("filename");
                    } catch (DataParsingException e) {
                        Utils.log_exception(e, "");
                        returnMessage(new ServerResponse(0, e.getMessage(), ""), response);
                        return;
                    } catch (Exception e) {
                        Utils.log_exception(e, "");
                        returnMessage(new ServerResponse(0, "Error parsing file.", Arrays.toString(e.getStackTrace())), response);
                        return;
                    }
                }
            }
        }

        // success
        returnMessage(new ServerResponse(1, "File " + fileName + " uploaded successfully", ""), response);
        
    }
    
    protected void returnMessage(ServerResponse resp, HttpServletResponse response) throws IOException {
        String json = new Gson().toJson(resp);
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
