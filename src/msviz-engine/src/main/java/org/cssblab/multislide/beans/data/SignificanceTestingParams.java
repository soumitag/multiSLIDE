package org.cssblab.multislide.beans.data;

import com.google.gson.Gson;
import java.io.Serializable;

/**
 *
 * @author soumitag
 */
public class SignificanceTestingParams implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String dataset;
    private String phenotype;
    private double significance_level;
    
    public SignificanceTestingParams() {
        this.significance_level = 0.05;
    }
    
    public SignificanceTestingParams(String dataset, String phenotype) {
        this.dataset = dataset;
        this.phenotype = phenotype;
        this.significance_level = 0.05;
    }
    
    public SignificanceTestingParams(String dataset, String phenotype, double significance_level) {
        this.dataset = dataset;
        this.phenotype = phenotype;
        this.significance_level = significance_level;
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
    
    public String asJSON () {
        return new Gson().toJson(this);
    }
    
}
