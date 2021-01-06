/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

import com.google.gson.Gson;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.utils.FileHandler;


/**
 *
 * @author soumitaghosh
 */
public class EnrichmentAnalysisResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String type;
    public int big_k;
    public int small_k;
    public int big_N;
    public int small_N;
    public String pathid;
    public String pathname;
    public double p_value;
    
    HashMap <String, Boolean> TYPES = new HashMap<String, Boolean>()
    {{
         put("entrez", Boolean.TRUE);
         put("pathway", Boolean.TRUE);
         put("gene-ontology", Boolean.TRUE);
    }};
    
    /*
    private static HashMap <String, Boolean> TYPES = new HashMap <> ();
    this.TYPES.put("entrez", Boolean.TRUE);
    */
    
    public EnrichmentAnalysisResult (
            String type, String pathid, String pathname, 
            int big_k, int small_k, int big_N, int small_N, double p_value
    ) throws MultiSlideException {
        
        if (!TYPES.containsKey(type)) {
            throw new MultiSlideException("Unknown type for Enrichment Analysis Result");
        }

        this.big_k = big_k;
        this.small_k = small_k;
        this.big_N = big_N;
        this.small_N = small_N;
        this.pathid = pathid;
        if (type.equals("pathway")) {
            this.type = "pathid";
        } else if (type.equals("gene-ontology")) {
            this.type = "goid";
        }
        this.pathname = pathname;
        this.p_value = p_value;
    }
    
    public static EnrichmentAnalysisResult[] makeEnrichmentAnalysisResults(String[][] data) 
    throws NullPointerException, NumberFormatException, MultiSlideException {
        EnrichmentAnalysisResult[] results = new EnrichmentAnalysisResult[data.length];
        int count = 0;
        for(String[] row : data) {
            results[count] = new EnrichmentAnalysisResult(
                    row[0]+"", 
                    row[1]+"", 
                    row[2]+"", 
                    Integer.parseInt(row[3]), 
                    Integer.parseInt(row[4]), 
                    0, 
                    0, 
                    Double.parseDouble(row[5]));
            count++;
        }
        return results;
    }
    
    public static EnrichmentAnalysisResult[] loadFromFile(String filename) throws NullPointerException, NumberFormatException, MultiSlideException {
        
        EnrichmentAnalysisResult[] results = new EnrichmentAnalysisResult[5];
        results[0] = new EnrichmentAnalysisResult("entrez", "1", "abc", 5, 6, 7, 8, 0.01);
        results[1] = new EnrichmentAnalysisResult("pathid", "2", "def", 6, 7, 8, 9, 0.01);
        results[2] = new EnrichmentAnalysisResult("pathid", "3", "ghi", 7, 8, 9, 10, 0.01);
        results[3] = new EnrichmentAnalysisResult("goid", "4", "jkl", 8, 9, 10, 11, 0.01);
        results[4] = new EnrichmentAnalysisResult("goid", "5", "mno", 9, 10, 11, 12, 0.01);
        
        /*
        EnrichmentAnalysisResult[] results = null;
        try {
            String[][] enriched_results_data = FileHandler.loadDelimData(filename, "\t", false);
            results = new EnrichmentAnalysisResult[enriched_results_data.length];

            for(int row = 0; row < enriched_results_data.length; row++) {
                EnrichmentAnalysisResult result = new EnrichmentAnalysisResult(enriched_results_data[row][0], 
                                                                enriched_results_data[row][1], Integer.parseInt(enriched_results_data[row][2]), 
                                                                Integer.parseInt(enriched_results_data[row][3]), Integer.parseInt(enriched_results_data[row][4]), 
                                                                Integer.parseInt(enriched_results_data[row][5]), Float.parseFloat(enriched_results_data[row][6]));
                results[row] = result;
                
            }
            
        } catch (Exception e) {
            
            Utils.log_info("Error reading input data:");
            Utils.log_exception(e, "");
        }
        */
        return results;
    }
    
    public String makeJSON() {
        String json = new Gson().toJson(this);
        return json;
    }
    
    public String makeJSONFromArray(EnrichmentAnalysisResult[] results) {
        String json = new Gson().toJson(results);
        return json;
    }
    
}
