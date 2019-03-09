/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

import com.google.gson.Gson;
import org.cssblab.multislide.structure.AnalysisContainer;

/**
 *
 * @author Soumita
 */
public class SelectionPanelData {
    
    public String[] phenotypes;
    public String[] dataset_names;
    
    public SelectionPanelData(AnalysisContainer analysis) {
        this.phenotypes = analysis.data.clinical_info.getPhenotypes();
        this.dataset_names = analysis.data.dataset_names;
    }
    
    public String asJSON () {
        return new Gson().toJson(this);
    }
    
}
