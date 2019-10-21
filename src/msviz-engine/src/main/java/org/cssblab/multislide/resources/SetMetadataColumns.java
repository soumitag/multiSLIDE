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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.PreviewData;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.utils.FormElementMapper;
import org.cssblab.multislide.utils.SessionManager;

/**
 *
 * @author abhikdatta
 */
public class SetMetadataColumns extends HttpServlet {

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
        try {
            
            // get session
            HttpSession session = request.getSession(false);
            if (session == null) {
                ServerResponse resp = new ServerResponse(0, "Session not found", "Possibly due to time-out");
                returnMessage(resp, response);
                return;
            }
            
            DataParser parser = new DataParser(request);
            parser.addParam("action", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, new String[]{"set_metadata", "get_column_headers"});
            parser.addParam("analysis_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            parser.addParam("expanded_filename", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
            }
            
            String action = parser.getString("action");
            String analysis_name = parser.getString("analysis_name");
            String expanded_filename = parser.getString("expanded_filename");
            
            ServletContext context = request.getServletContext();
            String installPath = (String)context.getAttribute("install_path");
            
            if (action.equalsIgnoreCase("set_metadata")) {
                
                parser.addListParam("metadata_columns", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, ",");
                parser.addListParam("metadata_columns_with_identifier_mappings", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, ",");
                parser.addListParam("identifiers", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, ",");
                
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                }
                
                String uploadFolder = SessionManager.getSessionDir(installPath, request.getSession().getId());
                HashMap <String, DatasetSpecs> dataset_specs_map = (new DatasetSpecs(analysis_name)).loadSpecsMap(uploadFolder);
                DatasetSpecs specs = dataset_specs_map.get(expanded_filename);
                if (specs.omics_type.equals("mi_rna")) {
                    String[] identifiers = parser.getStringArray("identifiers");
                    boolean hasMIRNA_mapping = false;
                    for (int i=0; i<identifiers.length; i++) {
                        if (identifiers[i].equals("mirna_id_2021158607524066")) {
                            hasMIRNA_mapping = true;
                            break;
                        }
                    }
                    if (!hasMIRNA_mapping) {
                        returnMessage(new ServerResponse(0, "Failed to update column mappings", "miRNA datasets must contain a column containing miRNA IDs"), response);
                        return;
                    }
                }
                
                (new DatasetSpecs(analysis_name)).setMetaDatColumns(
                        uploadFolder, analysis_name, expanded_filename, 
                        parser.getStringArray("metadata_columns"), 
                        parser.getStringArray("metadata_columns_with_identifier_mappings"), 
                        parser.getStringArray("identifiers"));
                returnMessage(new ServerResponse(1, "Metadata column information updated", ""), response);
                
            } else if (action.equalsIgnoreCase("get_column_headers")) {
                
                String uploadFolder = SessionManager.getSessionDir(installPath, request.getSession().getId());
                HashMap <String, DatasetSpecs> dataset_specs_map = (new DatasetSpecs(analysis_name)).loadSpecsMap(uploadFolder);
                DatasetSpecs specs = dataset_specs_map.get(expanded_filename);
                
                String filepath = uploadFolder + File.separator + specs.expanded_filename;
                String[][] data = FileHandler.previewFile(filepath, specs.delimiter, 1);
                PreviewData previewData = new PreviewData(data, new ServerResponse(1, "Success", "File preview successful"));
                
                String json = previewData.mapConfigAsJSON();
                sendData(response, json);
            }
            
        } catch (MultiSlideException e) {
            returnMessage(new ServerResponse(0, "Unknown MultiSlideException", e.getMessage()), response);
        } catch (Exception e) {
            returnMessage(new ServerResponse(0, "Unknown Exception", e.getMessage()), response);
        }
    }
    
    protected void returnMessage(ServerResponse resp, HttpServletResponse response) throws IOException {
        String json = new Gson().toJson(resp);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
    
    protected void sendData(HttpServletResponse response, String json) throws IOException {
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
