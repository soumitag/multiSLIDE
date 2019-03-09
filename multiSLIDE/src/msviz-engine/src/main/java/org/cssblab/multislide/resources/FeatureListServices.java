/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.Lists;
import org.cssblab.multislide.structure.MultiSlideException;

/*
Provides the following feature list related services:
1. Get Details (Metadata: name, type, size) of all Feature Lists
2. Creation
3. Deletion
4. Create and Add to List
5. Add to Existing List
6. Remove from List
7. Save List as File
8. Create List from File
*/


/**
 *
 * @author abhikdatta
 */
public class FeatureListServices extends HttpServlet {

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
        String list_name = request.getParameter("list_name");
        String action = request.getParameter("action");
        
        if (analysis_name == null || analysis_name.equals("")) {
            ServerResponse resp = new ServerResponse(0, "Bad param", "Missing or bad analysis name");
            returnMessage(resp, response);
        }
        
        if (action == null || action.equals("")) {
            ServerResponse resp = new ServerResponse(0, "Bad param", "Missing or bad action");
            returnMessage(resp, response);
        }
        
        String[] allowed_actions = {"create_list", "delete_list", "create_list_and_add", "add_features", "remove_features", "get_metadata", "save"};
        action = action.trim().toLowerCase();
        boolean isValidAction = false;
        for (int i=0; i<allowed_actions.length; i++) {
            if (action.equalsIgnoreCase(allowed_actions[i])) {
                isValidAction = true;
                break;
            }
        }
        if (!isValidAction) {
            ServerResponse resp = new ServerResponse(0, "Bad param", "Action '" + action + "' is not supported.");
            returnMessage(resp, response);
        }
        
        if (!action.equalsIgnoreCase("get_metadata")) {
            if (list_name == null || list_name.equals("")) {
                ServerResponse resp = new ServerResponse(0, "Bad param", "Missing or bad feature list name");
                returnMessage(resp, response);
            }
        }
        
        // get session
        HttpSession session = request.getSession(false);
        if (session == null) {
            ServerResponse resp = new ServerResponse(0, "Session not found", "Possibly due to time-out");
            returnMessage(resp, response);
        }
        
        AnalysisContainer analysis = (AnalysisContainer)session.getAttribute(analysis_name);
        if (analysis == null) {
            ServerResponse resp = new ServerResponse(0, "Analysis not found", "Analysis name '" + analysis_name + "' does not exist");
            returnMessage(resp, response);
        }
        
        Lists lists = analysis.lists;
        
        if (action.equalsIgnoreCase("create_list")) {
            try {
                lists.createEmptyFeatureList(list_name);
                returnMessage(new ServerResponse(1, "Feature list '" + list_name + "' created.", ""), response);
            } catch (MultiSlideException mse) {
                ServerResponse resp = new ServerResponse(0, "Create feature list FAILED.", mse.getMessage());
                returnMessage(resp, response);
            }
        }
        
        if (action.equalsIgnoreCase("create_list_and_add")) {
            
            try {
                list_name = lists.generateFeatureListName();
                lists.createEmptyFeatureList(list_name);
            } catch (MultiSlideException mse) {
                ServerResponse resp = new ServerResponse(0, "Create feature list FAILED.", mse.getMessage());
                returnMessage(resp, response);
            }
            
            try {
                String add_type = request.getParameter("add_type");
                if (add_type == null || add_type.equals("")) {
                    ServerResponse resp = new ServerResponse(0, "Add feature(s) FAILED.", "Missing or bad value for 'add_type'.");
                    returnMessage(resp, response);
                }
                
                String data = request.getParameter("list_data");
                if (data == null || data.equals("")) {
                    ServerResponse resp = new ServerResponse(0, "Add feature(s) FAILED.", "Missing data.");
                    returnMessage(resp, response);
                }
                
                ArrayList<String> list_data = new ArrayList <String>();
                if (add_type.equalsIgnoreCase("single_feature")) {
                    list_data.add(data);
                    
                } else if (add_type.equalsIgnoreCase("feature_group")) {
                    list_data.addAll(analysis.data.fs_data.getGeneGroup(data));
                } else {
                    ServerResponse resp = new ServerResponse(0, "Add feature(s) FAILED.", "Bad value for 'add_type'.");
                    returnMessage(resp, response);
                }
                
                lists.addToFeatureList(list_name, list_data, data);
                returnMessage(new ServerResponse(1, "Feature list '" + list_name + "' created.", ""), response);
                
            } catch (MultiSlideException mse) {
                ServerResponse resp = new ServerResponse(0, "Add feature(s) FAILED.", mse.getMessage());
                returnMessage(resp, response);
            }
            
        }
        
        if (action.equalsIgnoreCase("delete_list")) {
            try {
                lists.removeFeatureList(list_name);
                returnMessage(new ServerResponse(1, "Feature list '" + list_name + "' deleted.", ""), response);
            } catch (MultiSlideException mse) {
                ServerResponse resp = new ServerResponse(0, "Remove feature list FAILED.", mse.getMessage());
                returnMessage(resp, response);
            }
        }
        
        if (action.equalsIgnoreCase("add_features")) {
            try {
                String add_type = request.getParameter("add_type");
                if (add_type == null || add_type.equals("")) {
                    ServerResponse resp = new ServerResponse(0, "Add feature(s) FAILED.", "Missing or bad value for 'add_type'.");
                    returnMessage(resp, response);
                }
                
                String data = request.getParameter("list_data");
                if (data == null || data.equals("")) {
                    ServerResponse resp = new ServerResponse(0, "Add feature(s) FAILED.", "Missing data.");
                    returnMessage(resp, response);
                }
                
                ArrayList<String> list_data = new ArrayList <String>();
                if (add_type.equalsIgnoreCase("single_feature")) {
                    list_data.add(data);
                    lists.addToFeatureList(list_name, list_data, "single_feature");
                    returnMessage(new ServerResponse(1, "Added features to list '" + list_name + "'.", ""), response);
                    
                } else if (add_type.equalsIgnoreCase("feature_group")) {
                    list_data.addAll(analysis.data.fs_data.getGeneGroup(data));
                    lists.addToFeatureList(list_name, list_data, data);
                    returnMessage(new ServerResponse(1, "Added features to list '" + list_name + "'.", ""), response);
                    
                } else {
                    ServerResponse resp = new ServerResponse(0, "Add feature(s) FAILED.", "Bad value for 'add_type'.");
                    returnMessage(resp, response);
                }
                
            } catch (MultiSlideException mse) {
                ServerResponse resp = new ServerResponse(0, "Add feature(s) FAILED.", mse.getMessage());
                returnMessage(resp, response);
            }
        }
        
        if (action.equalsIgnoreCase("remove_features")) {
            try {
                String remove_type = request.getParameter("remove_type");
                if (remove_type == null || remove_type.equals("")) {
                    ServerResponse resp = new ServerResponse(0, "Remove feature(s) FAILED.", "Missing or bad value for 'remove_type'.");
                    returnMessage(resp, response);
                }
                String data = request.getParameter("data");
                if (data == null || data.equals("")) {
                    ServerResponse resp = new ServerResponse(0, "Remove feature(s) FAILED.", "Missing data.");
                    returnMessage(resp, response);
                }
                
                if (remove_type.equalsIgnoreCase("single_feature")) {
                    lists.removeFeatureFromList(list_name, data);
                } else if (remove_type.equalsIgnoreCase("feature_group")) {
                    lists.removeGroupFromList(list_name, data);
                } else {
                    ServerResponse resp = new ServerResponse(0, "Remove feature(s) FAILED.", "Bad value for 'remove_type'.");
                    returnMessage(resp, response);
                }
                
                returnMessage(new ServerResponse(1, "Removed features from list '" + list_name + "'.", ""), response);
                
            } catch (MultiSlideException mse) {
                ServerResponse resp = new ServerResponse(0, "Remove feature list FAILED.", mse.getMessage());
                returnMessage(resp, response);
            }
        }
        
        if (action.equalsIgnoreCase("get_metadata")) {
            String json = lists.getListsMetadata();
            //String json = new Gson().toJson(lists_metadata);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        }
        
        if (action.equalsIgnoreCase("save")) {
            
            String filename = request.getParameter("filename");
            String identifier = request.getParameter("identifier");
            String delimval = "\t";
            
            try {
                response.setContentType("application/download");
                response.setHeader("Content-Disposition", "attachment;filename=" + filename);
                String str = lists.serializeFeatureList(list_name, identifier, delimval);
                byte[] bytes = str.getBytes();
                OutputStream os = response.getOutputStream();
                os.write(bytes, 0, bytes.length);
                os.flush();
                os.close();
                return;
            } catch (MultiSlideException mse) {
                ServerResponse resp = new ServerResponse(0, "Save feature list FAILED.", mse.getMessage());
                returnMessage(resp, response);
            }  catch (Exception e) {
                ServerResponse resp = new ServerResponse(0, "Save feature list FAILED.", e.getMessage());
                returnMessage(resp, response);
            }
        }

        
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            
            
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
