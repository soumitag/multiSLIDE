package org.cssblab.multislide.beans.data;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author soumitag
 */
public class SignificanceTestingParams implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String dataset;
    private String phenotype;
    private String testtype;
    private double significance_level;
    private boolean apply_fdr;
    private double fdr_threshold;
    
    public SignificanceTestingParams() {
        this.significance_level = 0.05;
        this.testtype = "Parametric";
        this.apply_fdr = false;
        this.fdr_threshold = 1.0;
    }
    
/*
    public SignificanceTestingParams(String dataset, String phenotype) {
        this.dataset = dataset;
        this.phenotype = phenotype;
        this.significance_level = 0.05;
        this.testtype = "Parametric";
        this.apply_fdr = false;
        this.fdr_threshold = 1;
    }
    
    public SignificanceTestingParams(String dataset, String phenotype, double significance_level) {
        this.dataset = dataset;
        this.phenotype = phenotype;
        this.significance_level = significance_level;
        this.testtype = "Parametric";
        this.apply_fdr = false;
        this.fdr_threshold = 1;
    }
*/

    public SignificanceTestingParams(String dataset, String phenotype, double significance_level, String testtype, boolean apply_fdr, double fdr_threshold) {
        this.dataset = dataset;
        this.phenotype = phenotype;
        this.significance_level = significance_level;
        this.testtype = testtype;
        this.apply_fdr = apply_fdr;
        this.fdr_threshold = fdr_threshold;
    }
    
    public boolean isSet() {
        return this.dataset != null && this.phenotype != null;
    }
    
    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public String getPhenotype() {
        return phenotype;
    }

    public void setPhenotype(String phenotype) {
        this.phenotype = phenotype;
    }

    public double getSignificanceLevel() {
        return significance_level;
    }

    public void setSignificanceLevel(double significance_level) {
        this.significance_level = significance_level;
    }
    
    public String getTestType() {
        return testtype;
    }

    public void setTesttype(String testtype) {
        this.testtype = testtype;
    }

    public boolean getApplyFDR() {
        return apply_fdr;
    }

    public void setApplyFDR(boolean apply_fdr) {
        this.apply_fdr = apply_fdr;
    }

    public double getFDRThreshold() {
        return fdr_threshold;
    }

    public void setFDRThreshold(double fdr_threshold) {
        this.fdr_threshold = fdr_threshold;
    }
    
    public String asJSON () {
        return new Gson().toJson(this);
    }
    
    public String makeCacheKey() {
        return "" + this.phenotype + 
                    this.dataset +  
                    this.testtype;
    }
    
    public HashMap <String, String> asMap() {
        
        HashMap <String, String> map = new HashMap <> ();
        map.put("dataset_name", this.getDataset());
        map.put("phenotype", this.getPhenotype());
        map.put("testtype", this.getTestType());
        map.put("significance_level", this.getSignificanceLevel() + "");
        map.put("apply_fdr", this.getApplyFDR() + "");
        map.put("fdr_threshold", this.getFDRThreshold() + "");
        return map;
        
    }

    
}
