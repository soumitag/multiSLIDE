package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.Serializer;

/**
 *
 * @author soumitag
 */
public class SerializeAnalysis extends HttpServlet {

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
            parser.addParam("action", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                return;
            }
            String analysis_name = parser.getString("analysis_name");
            String action = parser.getString("action");
           
            HttpSession session = request.getSession(false);
            if (session == null) {
                ServerResponse resp = new ServerResponse(0, "Session not found", "Possibly due to time-out");
                returnMessage(resp, response);
                return;
            }

            AnalysisContainer analysis = (AnalysisContainer)session.getAttribute(analysis_name);
            if (analysis == null) {
                ServerResponse resp = new ServerResponse(0, "Analysis not found", "Analysis name '" + analysis_name + "' does not exist");
                returnMessage(resp, response);
                return;
            }
            
            if (action.equalsIgnoreCase("generate")) {
                
                String filename = analysis_name + ".mslide";
                String analysis_filepath = analysis.base_path + File.separator  + "data";
                Serializer serializer = new Serializer();
                serializer.serializeAnalysis(analysis, analysis_filepath);
                returnMessage(new ServerResponse(1, filename, ""), response);
                
            } else if (action.equalsIgnoreCase("download")) {
                
                parser.addParam("filename", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                String filename = parser.getString("filename");
                
                String analysis_filepath = analysis.base_path + File.separator  + "data";
                File f = new File(analysis_filepath + File.separator + filename);
                InputStream is = new FileInputStream(f);
                byte[] bytes = new byte[(int)f.length()];
                int read = is.read(bytes, 0, bytes.length);
                
                response.setContentType("application/download");
                response.setHeader("Content-Disposition", "attachment;filename=" + filename);
                OutputStream os = response.getOutputStream();
                os.write(bytes, 0, read);
                os.flush();
                os.close();
                
            } else if (action.equalsIgnoreCase("delete")) {
                
                session.removeAttribute(analysis_name);
                returnMessage(new ServerResponse(1, "done", ""), response);
                
            }

        } catch (DataParsingException dpe) {
            System.out.println(dpe);
            returnMessage(new ServerResponse(0, "Data parsing exception", dpe.getMessage()), response);
        } catch (Exception e) {
            System.out.println(e);
            returnMessage(new ServerResponse(0, "Data parsing exception", e.getMessage()), response);
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
