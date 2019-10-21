package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.beans.data.MappedData;
import org.cssblab.multislide.beans.data.SearchResultSummary;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.beans.data.SignificanceTestingParams;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.ClusteringParams;
import org.cssblab.multislide.structure.GlobalMapConfig;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.PhenotypeSortingParams;

/**
 *
 * @author soumitag
 */
public class GlobalMapConfigServices extends HttpServlet {

    protected void processRequest (
            HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException {
        
        /*
        set_map_resolution	            server_response	
        set_grid_layout                     server_response	
        set_col_header_height               server_response	
        set_row_label_width	            server_response	
        set_rows_per_page	            mapped_data_response	
        set_cols_per_page	            mapped_data_response
        set_row_ordering	            server_response	        current_feature_start = 0
        set_col_ordering	            server_response	        current_feature_start = 0
        set_gene_filtering	            mapped_data_response	current_feature_start = 0
        set_sample_filtering	            mapped_data_response	current_feature_start = 0
        set_current_sample_start	    mapped_data_response	
        set_current_feature_start	    mapped_data_response
        set_clustering_params               server_response	        current_sample_start = 0
        set_significance_testing_params     mapped_data_response	current_feature_start = 0
        set_phenotype_sorting_params        server_response	        current_sample_start = 0
        */
        
        /*
        HashMap <String, Integer> standard_response_actions = new HashMap <String, Integer> ();
        standard_response_actions.put("set_map_resolution", RequestParam.DATA_TYPE_STRING);
        standard_response_actions.put("set_grid_layout", RequestParam.DATA_TYPE_INT);
        standard_response_actions.put("set_col_header_height", RequestParam.DATA_TYPE_DOUBLE);
        standard_response_actions.put("set_row_label_width", RequestParam.DATA_TYPE_DOUBLE);
        standard_response_actions.put("set_col_ordering", RequestParam.DATA_TYPE_INT);
        standard_response_actions.put("set_row_ordering", RequestParam.DATA_TYPE_INT);
        standard_response_actions.put("set_map_orientation", RequestParam.DATA_TYPE_INT);
        
        HashMap <String, Integer> mapped_data_response_actions = new HashMap <String, Integer> ();
        mapped_data_response_actions.put("set_rows_per_page", RequestParam.DATA_TYPE_INT);
        mapped_data_response_actions.put("set_cols_per_page", RequestParam.DATA_TYPE_INT);
        mapped_data_response_actions.put("set_gene_filtering", RequestParam.DATA_TYPE_BOOLEAN);
        mapped_data_response_actions.put("set_sample_filtering", RequestParam.DATA_TYPE_BOOLEAN);
        mapped_data_response_actions.put("set_current_sample_start", RequestParam.DATA_TYPE_INT);
        mapped_data_response_actions.put("set_current_feature_start", RequestParam.DATA_TYPE_INT);
        */
        
        try {
            
            String[] actions = new String[]{
                "get_global_map_config", 
                "set_map_resolution", 
                "set_grid_layout", 
                "set_col_header_height", 
                "set_row_label_width", 
                "set_rows_per_page", 
                "set_cols_per_page", 
                "set_clustering_params", 
                "set_row_ordering",
                "set_col_ordering", 
                "set_gene_filtering", 
                "set_sample_filtering",
                "set_significance_testing_params", 
                "set_phenotype_sorting_params", 
                "set_current_sample_start",
                "set_current_feature_start",
                "set_map_orientation"
            };
            DataParser parser = new DataParser(request);
            parser.addParam("analysis_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            parser.addParam("action", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, actions);
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

            String json = "";

            if(action.equalsIgnoreCase("set_map_resolution")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setMapResolution(parser.getString("param_value"));
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
            } else if(action.equalsIgnoreCase("set_grid_layout")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setGridLayout(parser.getInt("param_value"));
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
            } else if(action.equalsIgnoreCase("set_col_header_height")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setColHeaderHeight(parser.getDouble("param_value"));
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
            } else if(action.equalsIgnoreCase("set_row_label_width")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setRowLabelWidth(parser.getDouble("param_value"));
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
            } else if(action.equalsIgnoreCase("set_col_ordering")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setColumnOrderingScheme(parser.getInt("param_value"));
                analysis.data.fs_data.recomputeColOrdering(analysis);
                analysis.global_map_config.setCurrentFeatureStart(0);
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
            } else if(action.equalsIgnoreCase("set_row_ordering")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setSampleOrderingScheme(parser.getInt("param_value"));
                analysis.data.fs_data.recomputeRowOrdering(analysis);
                analysis.global_map_config.setCurrentSampleStart(0);
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
            } else if(action.equalsIgnoreCase("set_map_orientation")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setMapOrientation(parser.getInt("param_value"));
                returnMessage(new ServerResponse(1, "", ""), response); return;
                
                
                
                
            } else if(action.equalsIgnoreCase("set_rows_per_page")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setUserSpecifiedRowsPerPage(parser.getInt("param_value"));
                analysis.global_map_config.setCurrentSampleStart(analysis.global_map_config.getCurrentSampleStart());
                MappedData md = new MappedData(1, "", "");
                md.addNameValuePair("rowsPerPageDisplayed", analysis.global_map_config.getRowsPerPageDisplayed()+"");
                md.addNameValuePair("userSpecifiedRowsPerPage", analysis.global_map_config.getUserSpecifiedRowsPerPage()+"");
                md.addNameValuePair("current_sample_start", analysis.global_map_config.getCurrentSampleStart()+"");
                json = md.asJSON();
                sendData(response, json);
                
            } else if(action.equalsIgnoreCase("set_cols_per_page")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setUserSpecifiedColsPerPage(parser.getInt("param_value"));
                analysis.global_map_config.setCurrentFeatureStart(analysis.global_map_config.getCurrentFeatureStart());
                MappedData md = new MappedData(1, "", "");
                md.addNameValuePair("colsPerPageDisplayed", analysis.global_map_config.getColsPerPageDisplayed()+"");
                md.addNameValuePair("userSpecifiedColsPerPage", analysis.global_map_config.getUserSpecifiedColsPerPage()+"");
                md.addNameValuePair("current_feature_start", analysis.global_map_config.getCurrentFeatureStart()+"");
                json = md.asJSON();
                sendData(response, json);
                
            } else if(action.equalsIgnoreCase("set_current_sample_start")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("scrollBy", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setScrollBy(parser.getInt("scrollBy"));
                analysis.global_map_config.setCurrentSampleStart(parser.getInt("param_value"));
                MappedData md = new MappedData(1, "", "");
                md.addNameValuePair("current_sample_start", analysis.global_map_config.getCurrentSampleStart()+"");
                json = md.asJSON();
                sendData(response, json);
                
            } else if(action.equalsIgnoreCase("set_current_feature_start")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("scrollBy", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setCurrentFeatureStart(parser.getInt("param_value"));
                analysis.global_map_config.setScrollBy(parser.getInt("scrollBy"));
                MappedData md = new MappedData(1, "", "");
                md.addNameValuePair("current_feature_start", analysis.global_map_config.getCurrentFeatureStart()+"");
                json = md.asJSON();
                sendData(response, json);
                
            } else if(action.equalsIgnoreCase("set_gene_filtering")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setGeneFilteringOn(parser.getBool("param_value"));
                analysis.data.fs_data.recomputeColOrdering(analysis);
                analysis.global_map_config.setAvailableCols(analysis.data.fs_data.nFilteredGenes);
                analysis.global_map_config.setUserSpecifiedColsPerPage(analysis.global_map_config.getUserSpecifiedColsPerPage());
                analysis.global_map_config.setCurrentFeatureStart(0);
                MappedData md = new MappedData(1, "", "");
                md.addNameValuePair("available_cols", analysis.global_map_config.available_cols+"");
                md.addNameValuePair("colsPerPageDisplayed", analysis.global_map_config.getColsPerPageDisplayed()+"");
                md.addNameValuePair("userSpecifiedColsPerPage", analysis.global_map_config.getUserSpecifiedColsPerPage()+"");
                md.addNameValuePair("current_feature_start", analysis.global_map_config.getCurrentFeatureStart()+"");
                json = md.asJSON();
                sendData(response, json);
                
            } else if(action.equalsIgnoreCase("set_sample_filtering")) {
                
                parser.addParam("param_value", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) { returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response); return; }
                analysis.global_map_config.setSampleFilteringOn(parser.getBool("param_value"));
                
            
                
                
                
            } else if(action.equalsIgnoreCase("set_significance_testing_params")) {
                
                parser.addParam("dataset", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("phenotype", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("significance_level", RequestParam.DATA_TYPE_DOUBLE, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                analysis.global_map_config.setSignificanceTestingParameters(new SignificanceTestingParams(
                        parser.getString("dataset"), parser.getString("phenotype"), parser.getDouble("significance_level")
                ));
                
                if (analysis.global_map_config.isGeneFilteringOn) {
                    analysis.data.fs_data.recomputeColOrdering(analysis);
                    analysis.global_map_config.setAvailableCols(analysis.data.fs_data.nFilteredGenes);
                    analysis.global_map_config.setUserSpecifiedColsPerPage(analysis.global_map_config.getUserSpecifiedColsPerPage());
                    analysis.global_map_config.setCurrentFeatureStart(0);
                    MappedData md = new MappedData(1, "", "");
                    md.addNameValuePair("available_cols", analysis.global_map_config.available_cols+"");
                    md.addNameValuePair("colsPerPageDisplayed", analysis.global_map_config.getColsPerPageDisplayed()+"");
                    md.addNameValuePair("userSpecifiedColsPerPage", analysis.global_map_config.getUserSpecifiedColsPerPage()+"");
                    md.addNameValuePair("current_feature_start", analysis.global_map_config.getCurrentFeatureStart()+"");
                    json = md.asJSON();
                    sendData(response, json);
                } else if (analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.SIGNIFICANCE_COLUMN_ORDERING) {
                    analysis.data.fs_data.recomputeColOrdering(analysis);
                    analysis.global_map_config.setCurrentFeatureStart(0);
                    MappedData md = new MappedData(1, "", "");
                    json = md.asJSON();
                    sendData(response, json);
                } else {
                    MappedData md = new MappedData(1, "", "");
                    json = md.asJSON();
                    sendData(response, json);
                }
                
            }  else if(action.equalsIgnoreCase("set_clustering_params")) {
                
                parser.addParam("type", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("dataset", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("use_defaults", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("linkage_function", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("distance_function", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("leaf_ordering", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                if (parser.getInt("type") == HierarchicalClusterer.TYPE_ROW_CLUSTERING) {
                    if (parser.getBool("use_defaults")) {
                        analysis.setRowClusteringParams(new ClusteringParams(
                            HierarchicalClusterer.TYPE_ROW_CLUSTERING, 
                            parser.getString("dataset")
                        ));
                    } else {
                        analysis.setRowClusteringParams(new ClusteringParams(
                            HierarchicalClusterer.TYPE_ROW_CLUSTERING, 
                            parser.getInt("linkage_function"), 
                            parser.getInt("distance_function"), 
                            parser.getInt("leaf_ordering"),
                            parser.getString("dataset")
                        ));
                    }
                    analysis.data.fs_data.recomputeRowOrdering(analysis);
                    analysis.global_map_config.setCurrentSampleStart(0);
                } else if (parser.getInt("type") == HierarchicalClusterer.TYPE_COL_CLUSTERING) {
                    if (parser.getBool("use_defaults")) {
                        analysis.setColClusteringParams(new ClusteringParams(
                            HierarchicalClusterer.TYPE_COL_CLUSTERING, 
                            parser.getString("dataset")
                        ));
                    } else {
                        analysis.setColClusteringParams(new ClusteringParams(
                            HierarchicalClusterer.TYPE_COL_CLUSTERING, 
                            parser.getInt("linkage_function"), 
                            parser.getInt("distance_function"), 
                            parser.getInt("leaf_ordering"),
                            parser.getString("dataset")
                        ));
                    }
                    analysis.data.fs_data.recomputeColOrdering(analysis);
                    analysis.global_map_config.setCurrentFeatureStart(0);
                }
                
                returnMessage(new ServerResponse(1, "", ""), response);
                
                
            }  else if(action.equalsIgnoreCase("set_phenotype_sorting_params")) {
                
                parser.addListParam("phenotypes", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, ",");
                parser.addListParam("sort_orders", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED, ",");
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                    return;
                }
                analysis.global_map_config.setPhenotypeSortingParams(
                    new PhenotypeSortingParams(
                        parser.getStringArray("phenotypes"),
                        parser.getBoolArray("sort_orders")
                    )
                );
                analysis.data.fs_data.recomputeRowOrdering(analysis);
                analysis.global_map_config.setCurrentSampleStart(0);
                returnMessage(new ServerResponse(1, "", ""), response);
                
            } else if (action.equalsIgnoreCase("get_global_map_config")) {
            
                GlobalMapConfig global_config = analysis.getGlobalMapConfig();
                json = global_config.mapConfigAsJSON();
                sendData(response, json);
                
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
