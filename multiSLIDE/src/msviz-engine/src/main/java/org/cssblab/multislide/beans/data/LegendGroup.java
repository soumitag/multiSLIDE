/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.cssblab.multislide.structure.AnalysisContainer;

/**
 *
 * @author Soumita
 */
public class LegendGroup {
    
    public String title;
    public ArrayList <String> legend_names;
    public ArrayList <double[]> colors;
    
    public LegendGroup () {
        
    }
    
    public LegendGroup (String title, ArrayList <String> legend_names, ArrayList <double[]> colors) {
        this.title = title;
        this.legend_names = legend_names;
        this.colors = colors;
    }
    
    public ArrayList <LegendGroup> createPhenotypeLegendGroups(AnalysisContainer analysis) {
        
        HashMap <String, HashMap <String, double[]>> phenotypeColorMap = analysis.data.clinical_info.phenotypeColorMap;
        ArrayList <LegendGroup> L = new ArrayList <LegendGroup> ();
        
        for (Map.Entry pair : phenotypeColorMap.entrySet()) {
            String phenotype = (String)pair.getKey();
            if (analysis.data.fs_data.phenotypes.containsKey(phenotype)) {
                ArrayList <String> names = new ArrayList <String> ();
                ArrayList <double[]> col = new ArrayList <double[]> ();
                for (Map.Entry pair_1 : ((HashMap <String, double[]>)pair.getValue()).entrySet()) {
                    String name = (String)pair_1.getKey();
                    if (name.equals("")){
                        name = "-";
                    }
                    names.add(name);
                    col.add((double[])pair_1.getValue());
                }
                L.add(new LegendGroup(phenotype, names, col));
            }
        }
        
        return L;
    }
    
    public void createGenetagLegendGroup(AnalysisContainer analysis) {
        HashMap <String, double[]> group_color = analysis.group_colors;
        this.legend_names = new ArrayList <String> ();
        this.colors = new ArrayList <double[]> ();
        for (Map.Entry pair : group_color.entrySet()) {
            String name = (String)pair.getKey();
            if (name.equals("")){
                name = "-";
            }
            this.legend_names.add(name);
            this.colors.add((double[])pair.getValue());
        }
        this.title = "Pathways";
    }
    
}
