/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.graphics;

/**
 *
 * @author Soumita
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.spark.sql.SparkSession;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.structure.AnalysisContainer;
//import org.cssblab.multislide.structure.FilteredSortedData;
import org.cssblab.multislide.structure.MapConfig;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.data.Data;
import org.cssblab.multislide.structure.data.Selection;

/**
 *
 * @author Soumita
 */
public class Heatmap implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String dataset_name;
    public Histogram hist;
    private MapConfig map_config;
    
    //public FilteredSortedData data;
    //public ArrayList <Integer> leaf_ordering;
    //public String range_type;
    //public String colormap;
    //public String[] column_identifiers;
    //public double binningRangeStart;
    //public double binningRangeEnd;
    
    public HashMap <String, HeatmapContainer> current_rasters;
    
    public Heatmap() {}
    
    public Heatmap( AnalysisContainer analysis,
                    String dataset_name, 
                    DatasetSpecs spec,
                    int nBins, 
                    String range_type, 
                    String colormap,
                    double range_start, 
                    double range_end
    ){
        //this.data = data;
        this.dataset_name = dataset_name;
        this.map_config = new MapConfig(dataset_name, analysis);
        
        double[] min_max = analysis.data.selected.range(this.dataset_name);
        this.map_config.setDataMin(min_max[0]);
        this.map_config.setDataMax(min_max[1]);
        
        this.map_config.setBinningRange(range_type);
        if(range_type.equals("data_bins")) {
            this.hist = new Histogram(nBins, min_max);
        } else if(range_type.equals("symmetric_bins")) {
            this.hist = new Histogram(nBins, Math.max(Math.abs(min_max[0]), Math.abs(min_max[1])));
        } else if(range_type.equals("user_specified")) {
            this.hist = new Histogram(nBins, range_start, range_end);
        }
        
        this.map_config.setColorScheme(colormap);
        this.map_config.setnColors(this.hist.nBins+1);
        this.map_config.setBinningRangeStart(range_start);
        this.map_config.setBinningRangeEnd(range_end);
        
        //this.leaf_ordering = leaf_ordering;
        current_rasters = new HashMap <> ();
    }
    
    public Heatmap( Selection selection, 
                    String dataset_name,
                    Heatmap reference_heatmap
    ){
        //this.data = data;
        this.dataset_name = dataset_name;
        this.map_config = reference_heatmap.map_config;
        
        String range_type = this.map_config.getBinningRange();
        double[] min_max = selection.range(dataset_name);
        this.map_config.setDataMin(min_max[0]);
        this.map_config.setDataMax(min_max[1]);
        
        if(range_type.equals("data_bins")) {
            this.hist = new Histogram(reference_heatmap.hist.nBins, min_max);
        } else if(range_type.equals("symmetric_bins")) {
            this.hist = new Histogram(reference_heatmap.hist.nBins, Math.max(Math.abs(min_max[0]), Math.abs(min_max[1])));
        } else if(range_type.equals("user_specified")) {
            this.hist = new Histogram(reference_heatmap.hist.nBins, this.map_config.getBinningRangeStart(), this.map_config.getBinningRangeEnd());
        }
        //this.leaf_ordering = reference_heatmap.leaf_ordering;
        this.current_rasters = new HashMap <> ();
    }
    
    public void genColorData(PyColorMaps colormaps) throws MultiSlideException {
        short[][] rgb = colormaps.getColors(this.map_config.getColorScheme(), (short)(this.hist.nBins));
        hist.rgb = new double[rgb.length+1][rgb[0].length];
        for (int i=0; i<rgb.length; i++) {
            for (int j=0; j<rgb[0].length; j++) {
                hist.rgb[i][j] = (double)rgb[i][j];
            }
        }
        hist.rgb[rgb.length] = new double[]{210.0, 210.0, 210.0};
    }
    
    public void assignBinsToRows(SparkSession spark_session, Selection selection) {
        selection.views.get(this.dataset_name).setBinNumbers(spark_session, this.hist);
    }
    
    /*
    public void assignBinsToRows(FilteredSortedData_1 data, GlobalMapConfig global_map_config) {
        Float[][] expressions = data.getUnsortedExpressions(dataset_name);
        int[][] expression_bin_nos = new int[data.nSamples][data.nGenes];
        for(int i = 0; i < data.nSamples; i++) {
            for(int j = 0; j < data.nGenes; j++) {
                expression_bin_nos[i][j] = hist.getBinNum_Int(expressions[i][j]);
                if (!Float.isNaN(expressions[i][j])) {
                    hist.addToBin_Int(expression_bin_nos[i][j]);
                }
            }
        }
        data.addExpressionBinNos(expression_bin_nos, dataset_name);
    }
    */
    
    public MapConfig getMapConfig() {
        return this.map_config;
        /*
        MapConfig config = new MapConfig();
        config.setDataMin(this.data.getMinMax(this.dataset_name)[0]);
        config.setDataMax(this.data.getMinMax(this.dataset_name)[1]);
        config.setCustomGeneIdentifiers(analysis.data.metadata.getMetaDataColumnNames(this.dataset_name));
        config.setBinningRange(this.range_type);
        config.setColorScheme(this.colormap);
        config.setColumnLabel(this.column_label);
        config.setnColors(this.hist.nBins+1);
        config.setBinningRangeStart(this.binningRangeStart);
        config.setBinningRangeEnd(this.binningRangeEnd);
        return config;
        */
    }
    
}
