package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.utils.SessionManager;

/**
 *
 * @author soumitag
 */
public class CreateSession extends HttpServlet {

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
            
            DataParser parser = new DataParser(request);
            parser.addParam("analysis_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                return;
            }
            String analysis_name = parser.getString("analysis_name");

            // get installation directory
            ServletContext context = request.getServletContext();
            String installPath = (String)context.getAttribute("install_path");

            // get the current session's id
            String session_id;
            HttpSession session = request.getSession(true);
            // check if a session already exists
            if (request.getSession(false) == null) {
                // if not, create new session
                session = request.getSession(true);
                session_id = session.getId();
            } else {
                // if it does, check if an analysis of same name exists in the session
                if (session.getAttribute(analysis_name) == null) {
                    // if not, create required temp folders for this analysis
                    session_id = session.getId();
                } else {
                    // if it does, send back to previous page with error message
                    //return failure
                    String detailed_reason = "An analysis with the name " + analysis_name + " already exists";
                    ServerResponse resp = new ServerResponse(0, "Analysis exists", detailed_reason);
                    returnMessage(resp, response);
                    return;
                }

            }

            // create session directory
            SessionManager.createSessionDir(installPath, session_id);

            // set base url
            String url = request.getRequestURL().toString();
            String base_url = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
            session.setAttribute("base_url", base_url);
            
            String detailed_reason = "Analysis with the name " + analysis_name + " created";
            ServerResponse resp = new ServerResponse(1, "Analysis created", detailed_reason);
            returnMessage(resp, response);
        
        } catch (DataParsingException dpe) {
            returnMessage(new ServerResponse(0, "A data parsing exception occured when creating session", dpe.getMessage()), response);
        } catch (Exception e) {
            returnMessage(new ServerResponse(0, "A unknown exception occured when creating session", e.getMessage()), response);
        }

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
