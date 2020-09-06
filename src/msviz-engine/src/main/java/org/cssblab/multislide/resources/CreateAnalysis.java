/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.sql.SparkSession;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.algorithms.statistics.EnrichmentAnalysis;
import org.cssblab.multislide.algorithms.statistics.SignificanceTester;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.PreviewData;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.data.Data;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.utils.MultiSlideConfig;
import org.cssblab.multislide.utils.SessionManager;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author Soumita
 */
public class CreateAnalysis extends HttpServlet {

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
            parser.addParam("action", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, 
                    new String[]{"create_analysis", "get_dataset_specs", "set_dataset_specs", "get_column_headers", "remove_dataset", "get_preview"});
            parser.addParam("analysis_name", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "Action or Analysis name is missing", parser.error_msg), response);
            }
            
            String analysis_name = parser.getString("analysis_name");
            
            // load dataset specs
            ServletContext context = request.getServletContext();
            String installPath = (String)context.getAttribute("install_path");
            String uploadFolder = SessionManager.getSessionDir(installPath, request.getSession().getId());
            ListOrderedMap <String, DatasetSpecs> dataset_specs_map = DatasetSpecs.loadSpecsMap(uploadFolder, analysis_name);
            
            ArrayList <DatasetSpecs> dataset_list = new ArrayList <> ();
            for (String name: dataset_specs_map.keySet())
                dataset_list.add(dataset_specs_map.get(name));

            // get logger
            Logger logger = LogManager.getRootLogger();
            
            if (parser.getString("action").equals("create_analysis")) {
                
                parser.addParam("species", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Species is missing", parser.error_msg), response);
                }
                
                String species = parser.getString("species");

                logger.info("Creating analysis: " + analysis_name);

                // create a new analysis
                AnalysisContainer analysis = new AnalysisContainer(analysis_name, species);

                // set base path for analysis
                analysis.setBasePath(SessionManager.getBasePath(installPath, request.getSession().getId(), analysis.analysis_name));

                // set analytics_server for analysis
                analysis.setAnalyticsServer((String)context.getAttribute("analytics_server_address"));
                
                // add spark session
                analysis.setSparkSession((SparkSession)context.getAttribute("spark"));

                // create analysis directory
                SessionManager.createAnalysisDirs(analysis);

                // move input file into analysis directory
                SessionManager.moveInputFilesToAnalysisDir(uploadFolder, analysis.base_path, dataset_list);

                /*
                String path = installPath + "/demo_data/";
                String[] data_types = new String[]{"CNA", "DNA Methylation", "mRNA", "Protein"};
                String[] data_filenames = new String[]{path + "formatted_data_CNA_2.txt", 
                                                       path + "formatted_data_methylation_hm450_row_centered_2.txt",
                                                       path + "formatted_data_RNA_Seq_v2_mRNA_median_Zscores_2.txt",
                                                       path + "formatted_data_rppa_mapped_2.txt"};
                */

                // add searcher (must be done before setting database)
                Searcher searcher = new Searcher(analysis.species);
                analysis.setSearcher(searcher);

                // create data
                ColorPalette categorical_palette = (ColorPalette)context.getAttribute("categorical_palatte");
                ColorPalette continuous_palette = (ColorPalette)context.getAttribute("continuous_palatte");
                //HashMap <String, Integer> identifier_index_map = (HashMap <String, Integer>)context.getAttribute("identifier_index_map");
                Data data = new Data(
                        analysis.spark_session, analysis.base_path, dataset_list, 
                        categorical_palette, continuous_palette, analysis.searcher);
                analysis.setDatabase(data);

                //initialize data_selection_state
                analysis.data_selection_state.setDefaultDatasetAndPhenotypes(data.dataset_names, data.clinical_info.getPhenotypeNames());
                analysis.global_map_config.setDefaultDatasetLinking(data.datasets);

                // load system configuration details
                //HashMap <String, String> multislide_config = MultiSlideConfig.getMultiSlideConfig(installPath);

                // create clusterer
                /*
                String py_module_path = multislide_config.get("py-module-path");
                String py_home = multislide_config.get("python-dir");
                */
                String cache_path = installPath + File.separator + "temp" + File.separator + "cache";
                HierarchicalClusterer clusterer = new HierarchicalClusterer(cache_path, analysis.analytics_engine_comm);
                analysis.setClusterer(clusterer);

                // create significance tester
                SignificanceTester significance_tester = new SignificanceTester(cache_path, analysis.analytics_engine_comm);
                analysis.setSignificanceTester(significance_tester);

                // create enrichment analyzer
                EnrichmentAnalysis enrichment_analyzer = new EnrichmentAnalysis(cache_path, analysis.analytics_engine_comm);
                analysis.setEnrichmentAnalyzer(enrichment_analyzer);

                // Finally add analysis to session
                session.setAttribute(analysis.analysis_name, analysis);
                //session.setAttribute("multislide_config", multislide_config);

                returnMessage(new ServerResponse(1, "Analysis created", ""), response);
                return;
                
            } else if (parser.getString("action").equals("get_dataset_specs")) {
                
                // load dataset specs
                ArrayList <DatasetSpecs> dataset_specs = new ArrayList <> ();
                int i=0;
                for (DatasetSpecs spec : dataset_specs_map.values()) {
                    if (!spec.getOmicsType().equals("clinical-info")) {
                        dataset_specs.add(spec);
                    }
                }
                String json = new Gson().toJson(dataset_specs);
                sendData(response, json);
                
            } else if (parser.getString("action").equals("set_dataset_specs")) {
                
                parser.addParam("expanded_filename", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addListParam("metadata_columns", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, ",");
                parser.addParam("has_linker", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                parser.addParam("has_additional_identifiers", RequestParam.DATA_TYPE_BOOLEAN, RequestParam.PARAM_TYPE_REQUIRED);
                
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                }
                
                if (parser.getBool("has_linker")) {
                    parser.addParam("linker_colname", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                    parser.addParam("linker_identifier_type", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                }
                
                if (parser.getBool("has_additional_identifiers")) {
                    parser.addListParam("identifier_metadata_columns", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED, ",");
                }
                
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Bad param", parser.error_msg), response);
                }
                
                String expanded_filename = parser.getString("expanded_filename");
                DatasetSpecs specs = dataset_specs_map.get(expanded_filename);
                
                String linker_colname = "";
                String linker_identifier_type = "";
                if (parser.getBool("has_linker")) {
                    linker_colname = parser.getString("linker_colname");
                    linker_identifier_type = parser.getString("linker_identifier_type");
                }
                
                String[] identifier_metadata_columns = new String[0];
                if (parser.getBool("has_additional_identifiers")) {
                    identifier_metadata_columns = parser.getStringArray("identifier_metadata_columns");
                }
                
                specs.update(
                        uploadFolder, analysis_name, expanded_filename, 
                        parser.getStringArray("metadata_columns"), 
                        parser.getBool("has_linker"), 
                        linker_colname,
                        linker_identifier_type,
                        parser.getBool("has_additional_identifiers"), 
                        identifier_metadata_columns
                );
                
                returnMessage(new ServerResponse(1, "Metadata column information updated", ""), response);
                
            } else if (parser.getString("action").equals("get_column_headers")) {
                
                parser.addParam("expanded_filename", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Expanded filename is missing", parser.error_msg), response);
                }
                String expanded_filename = parser.getString("expanded_filename");
                
                DatasetSpecs specs = dataset_specs_map.get(expanded_filename);
                
                String filepath = uploadFolder + File.separator + specs.getExpandedFilename();
                String[][] data = FileHandler.previewFile(filepath, specs.getDelimiter(), 1);
                PreviewData previewData = new PreviewData(data, new ServerResponse(1, "Success", "File preview successful"));
                
                String json = previewData.mapConfigAsJSON();
                sendData(response, json);
                
            } else if (parser.getString("action").equals("remove_dataset")) {
            
                parser.addParam("expanded_filename", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Expanded filename is missing", parser.error_msg), response);
                }
                String expanded_filename = parser.getString("expanded_filename");
            
                DatasetSpecs specs = dataset_specs_map.get(expanded_filename);

                String filePath = uploadFolder + File.separator + expanded_filename;
                File uploadedFile = new File(filePath);
                
                if (uploadedFile.exists() && !uploadedFile.isDirectory()) {

                    uploadedFile.delete();
                    DatasetSpecs.removeFromAnalysis(uploadFolder, analysis_name, expanded_filename);
                    returnMessage(new ServerResponse(1, "Success", "File " + specs.getFilename() + " has been removed successfully"), response);
                    
                } else {
                    returnMessage(new ServerResponse(1, "Error", "File " + specs.getFilename() + " does not exist on the server"), response);
                }
                
            } else if (parser.getString("action").equals("get_preview")) {
                
                parser.addParam("expanded_filename", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
                if (!parser.parse()) {
                    returnMessage(new ServerResponse(0, "Expanded filename is missing", parser.error_msg), response);
                }
                String expanded_filename = parser.getString("expanded_filename");
            
                DatasetSpecs specs = dataset_specs_map.get(expanded_filename);
                
                String filePath = uploadFolder + File.separator + specs.getExpandedFilename();
                File uploadedFile = new File(filePath);

                if (uploadedFile.exists() && !uploadedFile.isDirectory()) {
                    try {
                        String[][] data = FileHandler.previewFile(filePath, specs.getDelimiter(), 11);
                        PreviewData previewData = new PreviewData(data, new ServerResponse(1, "Success", "File preview successful"));
                        String json = new Gson().toJson(previewData);
                        sendData(response, json);
                        
                    } catch (DataParsingException dpex) {

                        returnMessage(new ServerResponse(0, "Data parsing error", dpex.getMessage()), response);

                    } catch (IOException ioex) {

                        returnMessage(new ServerResponse(0, "IO error", ioex.getMessage()), response);

                    }
                }
                
            }
            
            
        } catch (Exception e) {
            Utils.log_exception(e, "");
            returnMessage(new ServerResponse(0, "Exception", e.getMessage()), response);
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
