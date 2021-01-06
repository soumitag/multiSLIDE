/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.algorithms.statistics;

import com.google.gson.Gson;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Random;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.cssblab.multislide.beans.data.EnrichmentAnalysisResult;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.EnrichmentAnalysisParams;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.data.table.Table;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.utils.HttpClientManager;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitaghosh
 */
public class EnrichmentAnalysis implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String CACHE_PATH;
    public String message;
    public int status;
    private String last_output_file;
    HttpClientManager analytics_engine_comm;
    
    HashMap <String, EnrichmentAnalysisResult[]> cache;
    
    public EnrichmentAnalysis(String cache_path, HttpClientManager analytics_engine_comm){
        this.CACHE_PATH = cache_path;
        this.status = 0;
        this.analytics_engine_comm = analytics_engine_comm;
        cache = new HashMap <> ();
    }
    
    public boolean isCached (EnrichmentAnalysisParams params) {
        String cache_key = params.makeCacheKey();
        return this.cache.containsKey(cache_key);
    }
    
    public EnrichmentAnalysisResult[] doEnrichmentAnalysis (AnalysisContainer analysis, boolean return_array
    ) throws MultiSlideException, DataParsingException {
        
        Table expression_data = analysis.data.getDataForEnrichmentAnalysis(
                analysis.data_selection_state.enrichment_analysis_params.dataset);
        
        Dataset <Row> phenotype_data = analysis.data.clinical_info.getDataForEnrichmentAnalysis(
                analysis.data_selection_state.enrichment_analysis_params.phenotype);
        
        /*
        In case of empty input, return empty
        */
        if (expression_data.count() == 0 || phenotype_data.count() == 0) {
            EnrichmentAnalysisResult[] result = new EnrichmentAnalysisResult[0];
            return result;
        }
        
        /*
        Create a unique request id
        */
        String id = System.currentTimeMillis() + "";
        Random rand = new Random();
        String request_id = "req_" + id + "." + rand.nextInt();
        
        /*
        Get folder and file paths
        */
        String request_path = CACHE_PATH + File.separator + request_id;
        
        EnrichmentAnalysisParams params = analysis.data_selection_state.enrichment_analysis_params;
        
        try {
            
            phenotype_data.repartition(1)
                           .write()
                           .mode("overwrite")
                           .format("csv")
                           .option("delimiter", "\t")
                           .option("header", "true")
                           .save(request_path + File.separator + "phenotypes");
            
            /*
            expression_data.repartition(1)
                           .write()
                           .mode("overwrite")
                           .format("csv")
                           .option("delimiter", "\t")
                           .option("header", "true")
                           .save(request_path + File.separator + "expressions");
            */
            
            expression_data.save(request_path + File.separator + "expressions", "data.csv", "\t", true);
            
            HashMap <String, String> props = params.asMap();
            props.put("operation", "enrichment-analysis");
            FileHandler.savePropertiesFile(request_path + File.separator + "_params", props);
            
        } catch (Exception e) {
            Utils.log_exception(e, "");
            throw new MultiSlideException("Failed to start enrichment analysis: Could not create cache file");
        }
        
        String phenotype_datatype_string = 
                analysis.data.clinical_info.getPhenotypeDatatypeString(analysis.data_selection_state.enrichment_analysis_params.phenotype);
        
        HashMap<String, String> p = new HashMap <> ();
        p.put("request_id", request_id);
        p.put("n_rows", expression_data.count()+"");
        p.put("n_cols", expression_data.columns().size()+"");
        p.put("dtype", "float");
        p.put("species", analysis.species);
        p.put("phenotype_datatype", phenotype_datatype_string);
        p.put("significance_level_d", params.significance_level_d+"");
        if (params.testtype.equalsIgnoreCase("Parametric")) {
            p.put("use_parametric", "True");
        } else {
            p.put("use_parametric", "False");
        }
        p.put("apply_fdr_d", params.apply_fdr_d+"");
        p.put("fdr_rate_d", params.fdr_threshold_d+"");
        p.put("include_pathways", params.use_pathways+"");
        p.put("include_ontologies", params.use_ontologies+"");
        p.put("significance_level_e", params.significance_level_e+"");
        p.put("apply_fdr_e", params.apply_fdr_e+"");
        p.put("fdr_rate_e", params.fdr_threshold_e+"");
        if(return_array) {
            p.put("display_headers", "False");
        } else {
            p.put("display_headers", "True");
        }
        if (params.apply_pathway_sz_filter) {
            p.put("apply_pathway_sz_filter", "True");
        } else {
            p.put("apply_pathway_sz_filter", "False");
        }
        p.put("min_pathway_sz", params.min_pathway_sz + "");

        ServerResponse resp = analytics_engine_comm.doGet("do_enrichment_analysis", p);

        if (resp.status == 0) {
            throw new MultiSlideException(resp.message + ". " + resp.detailed_reason);
        } else {
            try {
                
                String out_fname = request_path + File.separator + "result.txt";
                if (!return_array) {
                    this.last_output_file = out_fname;
                    return null;
                }
                String[][] d = FileHandler.loadDelimData(out_fname, "\t", false);
                EnrichmentAnalysisResult[] ea_results = EnrichmentAnalysisResult.makeEnrichmentAnalysisResults(d);
                return ea_results;
                
            } catch (Exception e) {
                throw new MultiSlideException("Failed to read enrichment analysis results");
            }
        }

    }
    
    public String getLastOutputFile() {
        return this.last_output_file;
    }

    public String asJSON() {
        String json = new Gson().toJson(this);
        return json;
    }

}
