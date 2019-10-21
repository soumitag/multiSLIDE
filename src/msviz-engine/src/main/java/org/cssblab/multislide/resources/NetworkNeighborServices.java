/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.NetworkNeighbor;
import org.cssblab.multislide.utils.FormElementMapper;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class NetworkNeighborServices extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
        
            HttpSession session = request.getSession(false);
            if (session == null) {
                ServerResponse resp = new ServerResponse(0, "Session not found", "Possibly due to time-out");
                returnMessage(resp, response);
                return;
            }
            
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            
            if (isMultipart) {
            
                // Create a factory for disk-based file items
                DiskFileItemFactory factory = new DiskFileItemFactory();

                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);

                HashMap <String, String> formData = new HashMap <String, String> ();

                String analysis_name = "";
                String action = "";
                String dataset_name = "";
                String query_entrez = "";
                String network_type = "";
                String neighbor_entrez_list = "";
                
                List items = upload.parseRequest(request);
                Iterator iter = items.iterator();
                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();
                    if (item.isFormField()) {
                        String name = item.getFieldName().trim();
                        if (name.equals("analysis_name")) {
                            analysis_name = FormElementMapper.parseFormData(name, item.getString());
                            if (analysis_name == null || analysis_name.equals("")) {
                                returnMessage(new ServerResponse(0, "Analysis name is missing", ""), response);
                            }
                        } else if (name.equals("action")) {
                            action = FormElementMapper.parseFormData(name, item.getString());
                            if (action == null || action.equals("")) {
                                returnMessage(new ServerResponse(0, "Action name is missing", ""), response);
                            } else {
                                if (!action.equalsIgnoreCase("add")) {
                                    returnMessage(new ServerResponse(0, "For multipart data action can only be 'Add'", ""), response);
                                }
                            }
                        } else if (name.equals("dataset_name")) {
                            dataset_name = FormElementMapper.parseFormData(name, item.getString());
                            if (dataset_name == null || dataset_name.equals("")) {
                                returnMessage(new ServerResponse(0, "Dataset name is missing", ""), response);
                            }
                        } else if (name.equals("query_entrez")) {
                            query_entrez = FormElementMapper.parseFormData(name, item.getString());
                            if (query_entrez == null || query_entrez.equals("")) {
                                returnMessage(new ServerResponse(0, "Query entrez is missing", ""), response);
                            }
                        } else if (name.equals("network_type")) {
                            network_type = FormElementMapper.parseFormData(name, item.getString());
                            if (network_type == null || network_type.equals("")) {
                                returnMessage(new ServerResponse(0, "Network type is missing", ""), response);
                            }
                        } else if (name.equals("neighbor_entrez_list")) {
                            neighbor_entrez_list = FormElementMapper.parseFormData(name, item.getString());
                            if (neighbor_entrez_list == null || neighbor_entrez_list.equals("")) {
                                returnMessage(new ServerResponse(0, "Neighbor entrez list name is missing or empty", ""), response);
                            }
                        }
                    }
                }
                
                AnalysisContainer analysis = (AnalysisContainer)session.getAttribute(analysis_name);
                if (analysis == null) {
                    ServerResponse resp = new ServerResponse(0, "Analysis not found", "Analysis name '" + analysis_name + "' does not exist");
                    returnMessage(resp, response);
                    return;
                }
                
                String[] neighbor_entrezs = neighbor_entrez_list.split(",", -1);
                
                NetworkNeighbor nn = new NetworkNeighbor(
                    dataset_name, query_entrez, network_type, neighbor_entrezs
                );
                analysis.data_selection_state.addNetworkNeighbor(nn);
                returnMessage(new ServerResponse(1, "Network neighbors added.", ""), response);
                
            } else {
                
                DataParser parser = new DataParser(request);
                parser.addParam("analysis_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Analysis name is missing", parser.error_msg), response);
                    return;
                }
                String analysis_name = parser.getString("analysis_name");

                AnalysisContainer analysis = (AnalysisContainer)session.getAttribute(analysis_name);
                if (analysis == null) {
                    ServerResponse resp = new ServerResponse(0, "Analysis not found", "Analysis name '" + analysis_name + "' does not exist");
                    returnMessage(resp, response);
                    return;
                }

                parser.addParam("action", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, new String[]{"add", "remove"});
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Unknown action. Allowed actions are 'add' and 'remove'.", parser.error_msg), response);
                    return;
                }

                if (parser.getString("action").equalsIgnoreCase("add")) { 

                    parser.addParam("dataset_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                    parser.addParam("query_entrez", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                    parser.addParam("network_type", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, new String[]{"tf_entrez", "mirna_id", "ppi_entrez"});
                    parser.addListParam("neighbor_entrez_list", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_OPTIONAL, ",");
                    if (!parser.parse()) {
                        returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                        return;
                    }

                    NetworkNeighbor nn = new NetworkNeighbor(
                        parser.getString("dataset_name"),
                        parser.getString("query_entrez"),
                        parser.getString("network_type"),
                        parser.getStringArray("neighbor_entrez_list")
                    );
                    analysis.data_selection_state.addNetworkNeighbor(nn);

                    returnMessage(new ServerResponse(1, "Network neighbors added.", ""), response);

                } else if (parser.getString("action").equalsIgnoreCase("remove")) { 

                    parser.addParam("id", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                    if (!parser.parse()) {
                        returnMessage(new ServerResponse(0, "'id' is required parameter when action='remove'.", parser.error_msg), response);
                        return;
                    }

                    analysis.data_selection_state.removeNetworkNeighbor(parser.getString("id"));
                    returnMessage(new ServerResponse(1, "Network neighbors removed.", ""), response);

                }
                
            }
        
        } catch (DataParsingException dpe) {
            returnMessage(new ServerResponse(0, "Data parsing exception", dpe.getMessage()), response);
        } catch (Exception e) {
            returnMessage(new ServerResponse(0, "Data parsing exception", e.getMessage()), response);
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
