/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author soumitaghosh
 */
public class EnrichmentAnalysisParams implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String phenotype;
    public String dataset;
    public double significance_level_d;
    public String testtype;
    public boolean use_pathways;
    public boolean use_ontologies;
    public boolean apply_fdr_d;
    public double fdr_threshold_d;
    public double significance_level_e;
    public boolean apply_fdr_e;
    public double fdr_threshold_e;
    
    public final boolean apply_pathway_sz_filter = true;
    public final int min_pathway_sz = 10;
    
    public EnrichmentAnalysisParams(String phenotype, String dataset) {
        this.phenotype = phenotype;
        this.dataset = dataset;
        this.significance_level_d = 0.05;
        this.testtype = "Parametric";
        this.use_pathways = true;
        this.use_ontologies = false;
        this.apply_fdr_d = false;
        this.fdr_threshold_d = 0.01;
        this.significance_level_e = 0.05;
        this.apply_fdr_e = false;
        this.fdr_threshold_e = 0.01;
    }
    
    public EnrichmentAnalysisParams(
            String phenotype, String dataset, double significance_level_d, String testtype, boolean use_pathways, boolean use_ontologies,
            boolean apply_fdr_d, double fdr_threshold_d, double significance_level_e, boolean apply_fdr_e, double fdr_threshold_e
    ) {
        this.phenotype = phenotype;
        this.dataset = dataset;
        this.significance_level_d = significance_level_d;
        this.testtype = testtype;
        this.use_pathways = use_pathways;
        this.use_ontologies = use_ontologies;
        this.apply_fdr_d = apply_fdr_d;
        this.fdr_threshold_d = fdr_threshold_d;
        this.significance_level_e = significance_level_e;
        this.apply_fdr_e = apply_fdr_e;
        this.fdr_threshold_e = fdr_threshold_e;
    }
    
    public String getPhenotype() {
        return phenotype;
    }

    public void setPhenotype(String phenotype) {
        this.phenotype = phenotype;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }
    
    public String makeCacheKey() {
        return "" + this.phenotype + 
                    this.dataset + 
                    this.significance_level_d + 
                    this.testtype + 
                    this.use_pathways + 
                    this.use_ontologies + 
                    this.apply_fdr_d + 
                    this.fdr_threshold_d + 
                    this.significance_level_e + 
                    this.apply_fdr_e + 
                    this.fdr_threshold_e;
    }
    
    public HashMap <String, String> asMap() {
        
        HashMap <String, String> map = new HashMap <> ();
        map.put("phenotype", this.getPhenotype());
        map.put("dataset_name", this.getDataset());
        map.put("significance_level_d", this.significance_level_d + "");
        map.put("testtype", this.testtype);
        map.put("use_pathways", this.use_pathways + "");
        map.put("use_ontologies", this.use_ontologies + "");
        map.put("apply_fdr_d", this.apply_fdr_d + "");
        map.put("fdr_threshold_d", this.fdr_threshold_d + "");
        map.put("significance_level_e", this.significance_level_e + "");
        map.put("apply_fdr_e", this.apply_fdr_e + "");
        map.put("fdr_threshold_e", this.fdr_threshold_e + "");
        
        return map;
        
    }

}
