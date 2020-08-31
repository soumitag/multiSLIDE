/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.structure.data.Data;
import org.cssblab.multislide.utils.CollectionUtils;

/**
 *
 * @author Soumita Ghosh and Abhik Datta
 */
public class MapConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final transient String AGGREGATE_FUNC_MIN = "Minimum";
    public static final transient String AGGREGATE_FUNC_MEAN = "Mean";
    public static final transient String AGGREGATE_FUNC_MAX = "Maximum";
    //public static final transient String AGGREGATE_FUNC_MEDIAN = "Median";
    
    private double data_min;
    private double data_max;

    //public String columnLabel;
    private String binningRange;
    private int nColors;
    private String colorScheme;
    private double binningRangeStart;
    private double binningRangeEnd;
    
    /*
    New parameters
    */
    private String[] available_feature_identifiers;
    
    /*
    has_linker is true if this dataset has a linler column
    */
    public boolean has_linker;
    
    /*
    User selected column identifier(s) from the list of available column identifiers
    */
    public String[] selected_feature_identifiers;
    
    /*
    if true, data will be aggregated by linker column
    can only be true if has_linker is true
    */
    public boolean show_aggregated;
    
    /*
    only used when show_aggregated is true
    */
    public String aggregate_function;
    
    
    public MapConfig(String dataset_name, AnalysisContainer analysis) {
        
        DatasetSpecs specs = analysis.data.datasets.get(dataset_name).specs;
        
        /*
        Set linker: used to choose display options
        */
        this.has_linker = specs.has_linker;
        
        /*
        linker and other identifier columns (if any)
        */
        this.available_feature_identifiers = CollectionUtils.asArray(specs.getLinkerAndIdentifierColumnNames());
        
        /*
        set default feature label
        */
        if (specs.has_linker) {
            if (specs.has_additional_identifiers) {
                this.selected_feature_identifiers = new String[]{specs.getLinkerColname(), specs.identifier_metadata_columns[0]};
            } else {
                this.selected_feature_identifiers = new String[]{specs.getLinkerColname()};
            }
        } else {
            this.selected_feature_identifiers = new String[]{specs.identifier_metadata_columns[0]};
        }
        
        /*
        by default coarse view is shown
        */
        show_aggregated = false;
        aggregate_function = MapConfig.AGGREGATE_FUNC_MEAN;
    }
    
    public String[] getAvailableFeatureIdentifiers() {
        return available_feature_identifiers;
    }

    public void setAvailableFeatureIdentifiers(String[] custom_gene_identifiers) {
        this.available_feature_identifiers = custom_gene_identifiers;
    }

    public double getDataMin() {
        return data_min;
    }

    public void setDataMin(double data_min) {
        this.data_min = Math.round(data_min * 100.0)/100.0;
    }

    public double getDataMax() {
        return data_max;
    }

    public void setDataMax(double data_max) {
        this.data_max = Math.round(data_max * 100.0)/100.0;
    }
    
    public String[] getSelectedFeatureIdentifiers() {
        return selected_feature_identifiers;
    }

    public void setSelectedFeatureIdentifiers(String[] selected_feature_identifiers) {
        this.selected_feature_identifiers = selected_feature_identifiers;
    }

    public String getBinningRange() {
        return binningRange;
    }

    public void setBinningRange(String binningRange) {
        this.binningRange = binningRange;
    }
    
    public int getnColors() {
        return nColors;
    }

    public void setnColors(int nColors) {
        this.nColors = nColors;
    }
    
    public String getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(String colorScheme) {
        this.colorScheme = colorScheme;
    }
    
    public double getBinningRangeStart() {
        return binningRangeStart;
    }

    public void setBinningRangeStart(double binningRangeStart) {
        this.binningRangeStart = binningRangeStart;
    }

    public double getBinningRangeEnd() {
        return binningRangeEnd;
    }

    public void setBinningRangeEnd(double binningRangeEnd) {
        this.binningRangeEnd = binningRangeEnd;
    }

    public void setShowAggregated(boolean show_aggregated) {
        this.show_aggregated = show_aggregated;
    }
    
    public boolean showAggregated() {
        return this.show_aggregated;
    }
    
    public void setAggregateFunction(String aggregate_function) {
        this.aggregate_function = aggregate_function;
    }
    
    public String getAggregateFunction() {
        return this.aggregate_function;
    }

    public String mapConfigAsJSON () {
        return new Gson().toJson(this);
    }
    
    
}
