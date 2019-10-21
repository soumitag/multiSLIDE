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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.algorithms.statistics.SignificanceTester;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.Data;
import org.cssblab.multislide.graphics.PyColorMaps;
import org.cssblab.multislide.utils.MultiSlideConfig;
import org.cssblab.multislide.utils.SessionManager;

/**
 *
 * @author Soumita
 */
public class LoadDemo extends HttpServlet {

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
            parser.addParam("demo_id", RequestParam.DATA_TYPE_STRING, RequestParam.PARAM_TYPE_REQUIRED);
            parser.addParam("demo_number", RequestParam.DATA_TYPE_INT, RequestParam.PARAM_TYPE_REQUIRED);
            if (!parser.parse()) {
                returnMessage(new ServerResponse(0, "demo_id missing", parser.error_msg), response);
                return;
            }
            
            String analysis_name = "demo_2021158607524066_" + parser.getString("demo_id");
            int demo_number = parser.getInt("demo_number");
            
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
                    ServerResponse resp = new ServerResponse(0, "Failed to load demo:", detailed_reason);
                    returnMessage(resp, response);
                    return;
                }

            }
            
            // create session directory
            SessionManager.createSessionDir(installPath, session_id);
            
            // log
            Logger logger = LogManager.getRootLogger();
            logger.info("Load Demo called");
            
            // create a new analysis
            AnalysisContainer analysis = new AnalysisContainer("demo_2021158607524066_" + parser.getString("demo_id"), "human");
            
             // set base path for analysis
            analysis.setBasePath(SessionManager.getBasePath(installPath, request.getSession().getId(), analysis.analysis_name));
            
            // create analysis directory
            SessionManager.createAnalysisDirs(analysis);
            
            /*
            String path = installPath + "/demo_data/";
            String[] data_types = new String[]{"CNA", "DNA Methylation", "mRNA", "Protein"};
            String[] data_filenames = new String[]{path + "formatted_data_CNA_2.txt", 
                                                   path + "formatted_data_methylation_hm450_row_centered_2.txt",
                                                   path + "formatted_data_RNA_Seq_v2_mRNA_median_Zscores_2.txt",
                                                   path + "formatted_data_rppa_mapped_2.txt"};
            Data database = new Data(path + "clinical_info_blca.txt", data_types, data_filenames);
            */
            
            HashMap <String, DatasetSpecs> specs = new HashMap <String, DatasetSpecs> ();
            
            //DatasetSpecs spec_0 = new DatasetSpecs("demo_" + parser.getString("demo_id"), "clinical_info_blca.txt", "tab", "clinical-info", "");
            //specs.put(spec_0.expanded_filename, spec_0);
            
            /*
            DatasetSpecs spec_1 = new DatasetSpecs("demo_" + parser.getString("demo_id"), "formatted_data_CNA_2.txt", "tab", "CNA", "");
            spec_1.setMetaDataColumns(new String[]{"ENTREZ","Hugo_Symbol","Entrez_Gene_Id"}, 
                                      new String[]{"ENTREZ","Hugo_Symbol"}, 
                                      new String[]{"entrez_2021158607524066","genesymbol_2021158607524066"});
            specs.put(spec_1.expanded_filename, spec_1);
            
            DatasetSpecs spec_2 = new DatasetSpecs("demo_" + parser.getString("demo_id"), "formatted_data_methylation_hm450_row_centered_2.txt", "tab", "DNA Methylation", "");
            spec_2.setMetaDataColumns(new String[]{"ENTREZ","Hugo_Symbol","Entrez_Gene_Id"}, 
                                      new String[]{"ENTREZ","Hugo_Symbol"}, 
                                      new String[]{"entrez_2021158607524066","genesymbol_2021158607524066"});
            specs.put(spec_2.expanded_filename, spec_2);
            
            DatasetSpecs spec_3 = new DatasetSpecs("demo" + parser.getString("demo_id"), "formatted_data_RNA_Seq_v2_mRNA_median_Zscores_2.txt", "tab", "mRNA", "");
            spec_3.setMetaDataColumns(new String[]{"ENTREZ","Hugo_Symbol","Entrez_Gene_Id"}, 
                                      new String[]{"ENTREZ","Hugo_Symbol"}, 
                                      new String[]{"entrez_2021158607524066","genesymbol_2021158607524066"});
            specs.put(spec_3.expanded_filename, spec_3);
            
            DatasetSpecs spec_4 = new DatasetSpecs("demo" + parser.getString("demo_id"), "formatted_data_rppa_mapped_2.txt", "tab", "Protein", "");
            spec_4.setMetaDataColumns(new String[]{"ENTREZ","GENE_ID","ENTREZ_ID"}, 
                                      new String[]{"ENTREZ","GENE_ID"}, 
                                      new String[]{"entrez_2021158607524066","genesymbol_2021158607524066"});
            specs.put(spec_4.expanded_filename, spec_4);
            */
            
            /*
            DatasetSpecs spec_1 = new DatasetSpecs("demo_" + parser.getString("demo_id"), "formatted_data_CNA_2.txt", "tab", "CNA", "");
            spec_1.setMetaDataColumns(new String[]{"ENTREZ","Hugo_Symbol","Entrez_Gene_Id"}, 
                                      new String[]{"Hugo_Symbol"}, 
                                      new String[]{"genesymbol_2021158607524066"});
            specs.put(spec_1.expanded_filename, spec_1);
            
            DatasetSpecs spec_2 = new DatasetSpecs("demo_" + parser.getString("demo_id"), "formatted_data_methylation_hm450_row_centered_2.txt", "tab", "DNA Methylation", "");
            spec_2.setMetaDataColumns(new String[]{"ENTREZ","Hugo_Symbol","Entrez_Gene_Id"}, 
                                      new String[]{"Hugo_Symbol"}, 
                                      new String[]{"genesymbol_2021158607524066"});
            specs.put(spec_2.expanded_filename, spec_2);
            
            DatasetSpecs spec_3 = new DatasetSpecs("demo" + parser.getString("demo_id"), "formatted_data_RNA_Seq_v2_mRNA_median_Zscores_2.txt", "tab", "mRNA", "");
            spec_3.setMetaDataColumns(new String[]{"ENTREZ","Hugo_Symbol","Entrez_Gene_Id"}, 
                                      new String[]{"Hugo_Symbol"}, 
                                      new String[]{"genesymbol_2021158607524066"});
            specs.put(spec_3.expanded_filename, spec_3);
            
            DatasetSpecs spec_4 = new DatasetSpecs("demo" + parser.getString("demo_id"), "formatted_data_rppa_mapped_2.txt", "tab", "Protein", "");
            spec_4.setMetaDataColumns(new String[]{"ENTREZ","GENE_ID","ENTREZ_ID"}, 
                                      new String[]{"GENE_ID"}, 
                                      new String[]{"genesymbol_2021158607524066"});
            specs.put(spec_4.expanded_filename, spec_4);
            */
            if (demo_number == 2) {
                
                DatasetSpecs spec_0 = new DatasetSpecs("demo_2021158607524066_" + parser.getString("demo_id"), "BRCA_groupinfo_example_new.txt", "tab", "clinical-info", "");
                specs.put(spec_0.expanded_filename, spec_0);

                /*
                DatasetSpecs spec_1 = new DatasetSpecs("demo_" + parser.getString("demo_id"), "DNA_imputedDat_new.txt", "tab", "CNA", "");
                spec_1.setMetaDataColumns(new String[]{"Genesym"},
                        new String[]{"Genesym"},
                        new String[]{"genesymbol_2021158607524066"});
                specs.put(spec_1.expanded_filename, spec_1);
                */

                DatasetSpecs spec_2 = new DatasetSpecs("demo_2021158607524066_" + parser.getString("demo_id"), "PROT_imputedDat_rounded_73_filtered.txt", "tab", "Protein", "");
                spec_2.setMetaDataColumns(new String[]{"genesym"},
                        new String[]{"genesym"},
                        new String[]{"genesymbol_2021158607524066"});
                specs.put(spec_2.expanded_filename, spec_2);

                DatasetSpecs spec_3 = new DatasetSpecs("demo" + parser.getString("demo_id"), "mRNA_imputedDat_centered_rounded_73_filtered.txt", "tab", "mRNA", "");
                spec_3.setMetaDataColumns(new String[]{"Genesym"},
                        new String[]{"Genesym"},
                        new String[]{"genesymbol_2021158607524066"});
                specs.put(spec_3.expanded_filename, spec_3);
                
            } else if (demo_number == 1){
                
                DatasetSpecs spec_0 = new DatasetSpecs("demo_2021158607524066_" + parser.getString("demo_id"), "Sample_grouping.txt", "tab", "clinical-info", "");
                specs.put(spec_0.expanded_filename, spec_0);

                DatasetSpecs spec_1 = new DatasetSpecs("demo_2021158607524066_" + parser.getString("demo_id"), "mRNA_baselined_removeB.txt", "tab", "mRNA", "");
                spec_1.setMetaDataColumns(new String[]{"Ensembl", "GeneSymbol"},
                        new String[]{"GeneSymbol"},
                        new String[]{"genesymbol_2021158607524066"});
                specs.put(spec_1.expanded_filename, spec_1);

                DatasetSpecs spec_2 = new DatasetSpecs("demo_2021158607524066_" + parser.getString("demo_id"), "protein_baselined_removeB.txt", "tab", "Protein", "");
                spec_2.setMetaDataColumns(new String[]{"Ensembl", "GeneSymbol"},
                        new String[]{"GeneSymbol"},
                        new String[]{"genesymbol_2021158607524066"});
                specs.put(spec_2.expanded_filename, spec_2);
                
            } else if (demo_number == 0) {
                
                DatasetSpecs spec_0 = new DatasetSpecs("demo_2021158607524066_" + parser.getString("demo_id"), "Classification_of_TCGA_IDH-WT_GBMs.txt", "tab", "clinical-info", "");
                specs.put(spec_0.expanded_filename, spec_0);

                DatasetSpecs spec_1 = new DatasetSpecs("demo_2021158607524066_" + parser.getString("demo_id"), "mRNA_microarray_U133A_366_IDH_WT_subtyping_row_centered_post_filter.txt", "tab", "mRNA", "");
                spec_1.setMetaDataColumns(new String[]{"Gene"},
                        new String[]{"Gene"},
                        new String[]{"genesymbol_2021158607524066"});
                specs.put(spec_1.expanded_filename, spec_1);
                
            }

            // move input file into analysis directory
            SessionManager.moveInputFilesToAnalysisDir_ForDemo(installPath + File.separator + "demo_data", analysis.base_path, specs);
            
            // add searcher (must be done before setting database)
            Searcher searcher = new Searcher(analysis.species);
            analysis.setSearcher(searcher);
            
            ColorPalette categorical_palette = (ColorPalette)context.getAttribute("categorical_palatte");
            ColorPalette continuous_palette = (ColorPalette)context.getAttribute("continuous_palatte");
            HashMap <String, Integer> identifier_index_map = (HashMap <String, Integer>)context.getAttribute("identifier_index_map");
            Data database = new Data(analysis.base_path, specs, categorical_palette, continuous_palette, analysis.searcher, identifier_index_map);
            analysis.setDatabase(database);
            
            // load system configuration details
            HashMap <String, String> multislide_config = MultiSlideConfig.getMultiSlideConfig(installPath);
            
            // create clusterer and significance tester
            String py_module_path = multislide_config.get("py-module-path");
            String py_home = multislide_config.get("python-dir");
            HierarchicalClusterer clusterer = new HierarchicalClusterer(analysis.base_path + File.separator + "data", py_module_path, py_home);
            analysis.setClusterer(clusterer);
            SignificanceTester significance_tester = new SignificanceTester(analysis.base_path + File.separator + "data",py_module_path, py_home);
            analysis.setSignificanceTester(significance_tester);
            
            // Finally add analysis to session
            session.setAttribute(analysis.analysis_name, analysis);
            session.setAttribute("multislide_config", multislide_config);
            
            returnMessage(new ServerResponse(1, "Demo Loaded.", ""), response);
            return;
            
        } catch (Exception e) {
            System.out.println(e);
            returnMessage(new ServerResponse(0, "Failed to load demo. Please contact administrator. Error:", e.getMessage()), response);
            return;
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
