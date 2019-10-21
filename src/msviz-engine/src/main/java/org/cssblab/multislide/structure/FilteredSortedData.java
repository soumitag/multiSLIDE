/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.cssblab.multislide.algorithms.clustering.BinaryTree;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.algorithms.statistics.SignificanceTester;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.structure.SortOrderCache.SorterCacheEntry;
import org.cssblab.multislide.utils.IndexedEntrez;
import org.cssblab.multislide.utils.PhenotypeComparator;
import org.cssblab.multislide.utils.PhenotypeSet;
import org.cssblab.multislide.utils.SignificanceIndexedEntrez;

/**
 *
 * @author Soumita
 */
public class FilteredSortedData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public int nGenes;
    public int nFilteredGenes;
    public final int nSamples;
    public final int nDatasets;
    private final String[] dataset_names;
    
    private final ArrayList <String> entrez;
    
    private final ArrayList <String> selected_phenotypes;
    private final HashMap <String, String[]> phenotype_values;
    private final HashMap <String, Float[][]> expressions;
    private final HashMap <String, int[][]> bin_nos;
    private final HashMap <String, String[]> column_headers;
    private final HashMap <String, String[]> row_names;
    private final HashMap <String, double[]> min_max;
    private HashMap <String, ArrayList<GeneGroup>> entrez_group_map;        // entrez -> gene_group.name + "_" + gene_group.type
    private final HashMap <String, GeneGroup> unique_gene_groups;           // key = gene_group._id = gene_group.name + "_" + gene_group.type; value = gene_group object
    private final HashMap <String, ArrayList<Integer>> entrez_pos_map;      // entrez -> original position in unsorted data

    private int[] row_ordering;
    private int[] col_ordering;        
    private final HashMap <String, Integer> dataset_indices;
    private ArrayList <Integer> significant_gene_indices;
    //private boolean[] gene_significance_mask;
    
    private HashMap <String, ImputationStats> imputation_stats;
    private SortOrderCache cache;
    
    public FilteredSortedData(
            int nGenes, 
            int nSamples, 
            ArrayList <String> entrez, 
            String[] dataset_names, 
            HashMap <String, ArrayList<GeneGroup>> entrez_group_map
    ) {
        
        this.nGenes = nGenes;
        this.nSamples = nSamples;
        this.nDatasets = dataset_names.length;
        this.dataset_names = dataset_names;
        this.entrez = entrez;
        this.nFilteredGenes = this.nGenes;
        entrez_pos_map = new HashMap <String, ArrayList<Integer>> ();
        for (int i=0; i<entrez.size(); i++) {
            if (entrez_pos_map.containsKey(entrez.get(i))) {
                entrez_pos_map.get(entrez.get(i)).add(i);
            } else {
                ArrayList<Integer> t = new ArrayList<Integer>();
                t.add(i);
                entrez_pos_map.put(entrez.get(i), t);
            }
        }
        
        selected_phenotypes = new ArrayList <String> ();
        phenotype_values = new HashMap <String, String[]> ();
        expressions = new HashMap <String, Float[][]> ();
        bin_nos = new HashMap <String, int[][]> ();
        column_headers = new HashMap <String, String[]> ();
        row_names = new HashMap <String, String[]> ();
        min_max = new HashMap <String, double[]> ();
        unique_gene_groups = new HashMap <String, GeneGroup> ();
        //gene_significance_mask = new boolean[this.nGenes];
        this.setEntrezGroupMap(entrez_group_map);
        
        this.row_ordering = new int[this.nSamples];
        for (int i=0; i<this.nSamples; i++) {
            this.row_ordering[i] = i;
        }
        
        this.col_ordering = new int[this.nGenes];
        this.significant_gene_indices = new ArrayList <Integer> ();
        for (int i=0; i<this.nGenes; i++) {
            this.col_ordering[i] = i;
            this.significant_gene_indices.add(i);
        }
        
        dataset_indices = new HashMap <String, Integer> ();
        for (int i=0; i<dataset_names.length; i++) {
            dataset_indices.put(dataset_names[i], i);
        }
        
        imputation_stats = new HashMap <String, ImputationStats> ();
        cache = new SortOrderCache();
    }
    
    /*
    public String[] getMetaDataColumnNames(String dataset_name) {
        HashMap <String, String[]> dataset_metadata = this.metadata.get(dataset_name);
        String[] a = new String[dataset_metadata.keySet().size()];
        dataset_metadata.keySet().toArray(a);
        return a;
    }
    */

    public String[] getDatasetNames() {
        return dataset_names;
    }
    
    public int getNumDatasets() {
        return nDatasets;
    }
    
    public String[] getColumnHeaders(String dataset_name, String column_label, GlobalMapConfig global_map_config) {
        return this.applyColOrdering(
            column_headers.get(dataset_name),
            global_map_config.getCurrentFeatureStart(),
            global_map_config.getColsPerPageDisplayed()
        );
    }
    
    public String getEntrezAt(int position) {
        return this.entrez.get(this.col_ordering[position]);
    }
    
    public void addColumnHeaders(String[] column_headers, String dataset_name) throws MultiSlideException {
        this.column_headers.put(dataset_name, column_headers);
        if(this.nGenes != column_headers.length) {
            throw new MultiSlideException("Exception in FilteredSortedData.addColumnHeaders(): has " + this.nGenes + " genes but received " + column_headers.length + " column headers.");
        }
    }
    
    public void consolidateColumnHeaders() {
        String[] consolidated_column_headers = new String[this.nGenes];
        for (int i=0; i<this.nGenes; i++) {
            for (int j=0; j<this.nDatasets; j++) {
                String header = this.column_headers.get(this.dataset_names[j])[i];
                if (header!=null && !header.equals("")) {
                    consolidated_column_headers[i] = header;
                }
            }
        }
        for (int j=0; j<this.nDatasets; j++) {
            this.column_headers.put(this.dataset_names[j], consolidated_column_headers);
        }
    }

    public String[] getRowNames(String dataset_name, GlobalMapConfig global_map_config) {
        return this.applyRowOrdering(
            row_names.get(dataset_name),
            global_map_config.getCurrentSampleStart(), 
            global_map_config.getRowsPerPageDisplayed()
        );
    }

    public void addRowNames(String[] row_names, String dataset_name) throws MultiSlideException {
        this.row_names.put(dataset_name, row_names);
        if(this.nSamples != row_names.length) {
            throw new MultiSlideException("Exception in FilteredSortedData.addRowNames(): has " + this.nSamples + " samples but received " + row_names.length + " row names.");
        }
    }
    
    /*
    public void addMetaDataColumn(String dataset_name, String metadata_col_name, String[] metadata_col_values) {
        if (this.metadata.containsKey(dataset_name)) {
            this.metadata.get(dataset_name).put(metadata_col_name, metadata_col_values);
        } else {
            HashMap <String, String[]> t = new HashMap <String, String[]> ();
            t.put(metadata_col_name, metadata_col_values);
            this.metadata.put(dataset_name, t);
        }
    }
    */

    public String[] getPhenotypeValues(String phenotype, GlobalMapConfig global_map_config) {
        return this.applyRowOrdering(
            phenotype_values.get(phenotype),
            global_map_config.getCurrentSampleStart(), 
            global_map_config.getRowsPerPageDisplayed()
        );
    }
    
    public boolean hasPhenotype(String phenotype) {
        return this.selected_phenotypes.contains(phenotype);
    }
    
    public int getNumSelectedPhenotype() {
        return this.selected_phenotypes.size();
    }
    
    public String getPhenotype(int pos) {
        return selected_phenotypes.get(pos);
    }
    
    public ArrayList <String> getPhenotypes() {
        return selected_phenotypes;
    }

    public void addPhenotypes(String[] phenotype_values, String phenotype) {
        this.phenotype_values.put(phenotype, phenotype_values);
        this.selected_phenotypes.add(phenotype);
    }
    
    // called in Heatmap.assignBinsToRows() to assign color bins to expression data for "Apply Changes"
    public Float[][] getUnsortedExpressions(String dataset_name) {
        return expressions.get(dataset_name);
    }

    public Float[][] getExpressions(String dataset_name, GlobalMapConfig global_map_config) {
        
        return this.applyRowColOrdering(
            expressions.get(dataset_name), 
            global_map_config.getCurrentSampleStart(), 
            global_map_config.getCurrentFeatureStart(),
            global_map_config.getRowsPerPageDisplayed(),
            global_map_config.getColsPerPageDisplayed()
        );

    }
    
    public void addExpressions(Float[][] expressions, String dataset_name) {
        this.expressions.put(dataset_name, expressions);
    }
    
    /*
    public int getExpressionBinNo_1(String dataset_name, int row, int col) {
        return bin_nos.get(dataset_name)[row][col];
    }
    */
    
    public int[][] getExpressionBinNos(String dataset_name, GlobalMapConfig global_map_config) {
        return this.applyRowColOrdering(
            bin_nos.get(dataset_name), 
            global_map_config.getCurrentSampleStart(), 
            global_map_config.getCurrentFeatureStart(),
            global_map_config.getRowsPerPageDisplayed(),
            global_map_config.getColsPerPageDisplayed()
        );
    }

    public void addExpressionBinNos(int[][] expression_bin_nos, String dataset_name) {
        this.bin_nos.put(dataset_name, expression_bin_nos);
    }
    
    public double[] getMinMax(String dataset_name) {
        return this.min_max.get(dataset_name);
    }
    
    public void addMinMax(double[] min_max, String dataset_name) {
        this.min_max.put(dataset_name, min_max);
    }

    /*
    public void addMissingRows(boolean[] missing_row_ind, String dataset_name) {
        this.missing_rows_indicator.put(dataset_name, missing_row_ind);
    }
    
    public void addMissingCols(boolean[] missing_col_ind, String dataset_name) {
        this.missing_cols_indicator.put(dataset_name, missing_col_ind);
    }
    */
    
    /*
    public void setGeneGroups(ArrayList <GeneGroup> gene_group_list) {
        for (int i=0; i<gene_group_list.size(); i++) {
            GeneGroup grp = gene_group_list.get(i);
            String key = grp.getGroupKey();
            this.gene_groups.put(key, grp);
            for (int j=0; j<grp.entrez_list.size(); j++) {
                this.entrez_group_map.put(grp.entrez_list.get(j), key);
            }
        }
        for (int i=0; i<this.entrez.size(); i++) {
            String key = this.entrez_group_map.get(this.entrez.get(i));
            this.gene_tags.add(this.gene_groups.get(key).tag);
        }
    }
    */
    
    private void setEntrezGroupMap(HashMap <String, ArrayList<GeneGroup>> entrez_group_map) {
        this.entrez_group_map = entrez_group_map;
        for (ArrayList<GeneGroup> gene_groups : this.entrez_group_map.values()) {
            for (GeneGroup gene_group : gene_groups) {
                this.unique_gene_groups.put(gene_group.getID(), gene_group);
            }
        }
        
        int i = 0;
        for (GeneGroup gene_group : this.unique_gene_groups.values()) {
            gene_group.tag = i++;
        }
        
        for (ArrayList<GeneGroup> gene_groups : this.entrez_group_map.values()) {
            for (GeneGroup gene_group : gene_groups) {
                gene_group.tag = this.unique_gene_groups.get(gene_group.getID()).tag;
            }
        }
        
        for (Map.Entry <String, ArrayList<GeneGroup>> entry : this.entrez_group_map.entrySet()) {
            String entrez = entry.getKey();
            for (GeneGroup gene_group : entry.getValue()) {
                 this.unique_gene_groups.get(gene_group.getID()).addEntrez(entrez);
            }
        }
    }
    
    public HashMap <String, GeneGroup> getGeneGroups () {
        return this.unique_gene_groups;
    }
    
    public ArrayList <String> getGeneGroup(String grp_key) throws MultiSlideException {
        // return the GeneGroup where grp_key = gene_group.name + "_" + gene_group.type
        if (this.unique_gene_groups.containsKey(grp_key)) {
            return this.unique_gene_groups.get(grp_key).entrez_list;
        } else {
            throw new MultiSlideException("Exception in getGeneGroup() of FilteredSortedData.java: unknown gene group " + grp_key);
        }
    }
    
    public HashMap <String, ArrayList<GeneGroup>> getEntrezGroupMap () {
        return this.entrez_group_map;
    }
    
    /*
    public boolean[] getGeneSignificanceMask(GlobalMapConfig global_map_config) {
        return this.gene_significance_mask;
    }
    */
    
    public final void computeDataRanges() {
        for (Map.Entry pair : expressions.entrySet()) {
            this.addMinMax(computeRange((Float[][]) pair.getValue()), (String)pair.getKey());
        }
    }
    
    public double[] computeRange(Float[][] expression_values) {
        
        double minval = Double.POSITIVE_INFINITY;
        double maxval = Double.NEGATIVE_INFINITY;
        
        for(int i = 0; i < expression_values.length; i++){
            for(int j = 0; j < expression_values[0].length; j++){
                if(minval > expression_values[i][j]) {
                    minval = expression_values[i][j];
                }
                if(maxval < expression_values[i][j]) {
                    maxval = expression_values[i][j];
                }
            }
        }
        
        double[] min_max = new double[2];
        min_max[0] = minval;
        min_max[1] = maxval;
        return min_max;
    }
    
    public void generateGeneGroupColors(ColorPalette gene_group_color_palette) {
        
        int min = 0;
        int max = 255;
        Random r = new Random();
        
        ArrayList <short[]> gene_group_colors = gene_group_color_palette.palette;
        
        int i = 0;
        for (GeneGroup gene_group : this.unique_gene_groups.values()) {
            if (!gene_group.type.equals("entrez")) {
                double[] color = new double[3];
                if (i < gene_group_colors.size()) {
                    color[0] = (double)gene_group_colors.get(i)[0];
                    color[1] = (double)gene_group_colors.get(i)[1];
                    color[2] = (double)gene_group_colors.get(i)[2];
                } else {
                    color[0] = r.nextInt(max-min) + min;
                    color[1] = r.nextInt(max-min) + min;
                    color[2] = r.nextInt(max-min) + min;
                }
                gene_group.setGeneGroupColor(color);
                i++;
            }
        }
        
        for (ArrayList<GeneGroup> gene_groups : this.entrez_group_map.values()) {
            for (GeneGroup gene_group : gene_groups) {
                gene_group.setGeneGroupColor(this.unique_gene_groups.get(gene_group.getID()).color);
            }
        }
    }
    
    public HashMap <String, Object> doSignificanceTesting(
            GlobalMapConfig global_map_config,
            SignificanceTester tester, 
            String dataset_name, 
            String phenotype, 
            int phenotype_datatype, 
            String[] phenotype_values,
            double significance_level
    ) throws MultiSlideException {
        
        /*
        if (phenotype.equals("")) {
            for (int i = 0; i < this.nGenes; i++) {
                this.gene_significance_mask[i] = true;
            }
        } else {
            for (int i = 0; i < this.nGenes; i++) {
                if (i % 5 == 0) {
                    this.gene_significance_mask[i] = false;
                } else {
                    this.gene_significance_mask[i] = true;
                }
            }
        }
        */
        
        if (tester.isCached(dataset_name, phenotype, this.nGenes)) {
            HashMap <String, Object> st_results = tester.computeSignificanceLevel(null, this.nGenes, dataset_name, phenotype, phenotype_datatype, significance_level, null);
            return st_results;
        } else {
            float[][] d = this.getDataForSignificanceTesting(dataset_name, phenotype, phenotype_datatype, phenotype_values);
            HashMap <String, Object> st_results = tester.computeSignificanceLevel(
                    d, this.nGenes, dataset_name, phenotype, phenotype_datatype, significance_level, this.imputation_stats.get(dataset_name).getAvailableColIndices()
            );
            return st_results;
        }
    }
    
    public int[] doRowClustering (
        AnalysisContainer analysis
    ) throws MultiSlideException {
        
        int[] row_cluster_ordering = new int[this.nSamples];
        if (analysis.global_map_config.sampleOrderingScheme == GlobalMapConfig.HIERARCHICAL_SAMPLE_ORDERING) {
            
            Float[][] expressions_d = this.expressions.get(analysis.global_map_config.row_clustering_params.dataset);
            
            ArrayList <ArrayList <Float>> data = new ArrayList <ArrayList <Float>> ();
            ArrayList <Integer> missing_row_indices = new ArrayList <Integer> ();
            ArrayList <Integer> available_row_indices = new ArrayList <Integer> ();
            ArrayList <Float> row_averages = new ArrayList <Float> ();
            
            for (int i = 0; i < expressions_d.length; i++) {
                ArrayList <Float> row = new ArrayList <Float> ();
                ArrayList <Integer> missing_cols_in_row_i = new ArrayList <Integer> ();
                float row_average = 0;
                int j = 0;
                for (Float item : expressions_d[i]) {
                    row.add(item);
                    if (Float.isNaN(item)) {
                        missing_cols_in_row_i.add(j);
                    } else {
                        row_average += item;
                    }
                    j++;
                }
                if (missing_cols_in_row_i.size() == expressions_d[i].length) {
                    missing_row_indices.add(i);
                } else {
                    available_row_indices.add(i);
                    row_average /= expressions_d[i].length;
                    row_averages.add(row_average);
                    for (int m = 0; m < missing_cols_in_row_i.size(); m++) {
                        row.set(missing_cols_in_row_i.get(m),row_average);
                    }
                    data.add(row);
                }
            }

            try {
                BinaryTree row_binary_tree = analysis.clusterer.doClustering(
                        data,
                        analysis,
                        analysis.global_map_config.row_clustering_params.getDatasetName(),
                        analysis.global_map_config.row_clustering_params
                );
                
                int row_count = 0;
                for (int i=0; i<available_row_indices.size(); i++) {
                    row_cluster_ordering[row_count++] = available_row_indices.get(row_binary_tree.leaf_ordering.get(i));
                }
                for (int i=0; i<missing_row_indices.size(); i++) {
                    row_cluster_ordering[row_count++] = missing_row_indices.get(i);
                }
            } catch (MultiSlideException mse) {
                System.out.println("Hierarchical clustering failed. " + mse.getMessage() + " Falling back on gene groups.");
                for (int i=0; i<this.nSamples; i++) {
                    row_cluster_ordering[i] = i;
                }
            }
            
        } else {
            for (int i=0; i<this.nSamples; i++) {
                row_cluster_ordering[i] = i;
            }
        }
        
        this.row_ordering = row_cluster_ordering;
        return row_cluster_ordering;
    }
    
    public int[] doColumnClustering (AnalysisContainer analysis, boolean[] gene_masks) throws MultiSlideException {
        
        int[] col_cluster_ordering = new int[this.nFilteredGenes];

        Float[][] expressions_d = this.expressions.get(analysis.global_map_config.col_clustering_params.dataset);
        Float[][] data_T = new Float[this.nFilteredGenes][expressions_d.length];
        for (int i = 0; i < expressions_d.length; i++) {
            int col = 0;
            for (int j = 0; j < expressions_d[i].length; j++) {
                if (gene_masks[j]) {
                    data_T[col++][i] = expressions_d[i][j];
                }
            }
        }

        ArrayList<ArrayList<Float>> data = new ArrayList<ArrayList<Float>>();
        ArrayList<Integer> missing_col_indices = new ArrayList<Integer>();
        ArrayList<Integer> available_col_indices = new ArrayList<Integer>();

        for (int i = 0; i < data_T.length; i++) {
            ArrayList<Float> col = new ArrayList<Float>();
            ArrayList<Integer> missing_rows_in_col_i = new ArrayList<Integer>();
            float col_average = 0;
            int j = 0;
            for (Float item : data_T[i]) {
                col.add(item);
                if (Float.isNaN(item)) {
                    missing_rows_in_col_i.add(j);
                } else {
                    col_average += item;
                }
                j++;
            }
            if (missing_rows_in_col_i.size() == data_T[i].length) {
                missing_col_indices.add(i);
            } else {
                available_col_indices.add(i);
                col_average /= data_T[i].length;
                for (int m = 0; m < missing_rows_in_col_i.size(); m++) {
                    col.set(missing_rows_in_col_i.get(m), col_average);
                }
                data.add(col);
            }
        }

        try {
            BinaryTree col_binary_tree = analysis.clusterer.doClustering(
                    data,
                    analysis,
                    analysis.global_map_config.col_clustering_params.getDatasetName(),
                    analysis.global_map_config.col_clustering_params
            );

            int col_count = 0;
            for (int i = 0; i < available_col_indices.size(); i++) {
                col_cluster_ordering[col_count++] = this.significant_gene_indices.get(available_col_indices.get(col_binary_tree.leaf_ordering.get(i)));
            }
            for (int i = 0; i < missing_col_indices.size(); i++) {
                col_cluster_ordering[col_count++] = this.significant_gene_indices.get(missing_col_indices.get(i));
            }
        } catch (MultiSlideException mse) {
            System.out.println("Hierarchical clustering failed. " + mse.getMessage() + " Falling back on gene groups.");
            for (int i = 0; i < this.nFilteredGenes; i++) {
                col_cluster_ordering[i] = i;
            }
        }

        /*
        for (int i = 0; i < this.nFilteredGenes; i++) {
            col_cluster_ordering[i] = i;
        }
        */

        return col_cluster_ordering;

    }
    
    /*
    public void doClustering (
        AnalysisContainer analysis
    ) throws MultiSlideException {
        
        int[] row_cluster_ordering = new int[this.nSamples];
        if (analysis.global_map_config.sampleOrderingScheme == GlobalMapConfig.HIERARCHICAL_SAMPLE_ORDERING) {
            
            Float[][] expressions_d = this.expressions.get(analysis.global_map_config.row_clustering_params.dataset);
            
            ArrayList <ArrayList <Float>> data = new ArrayList <ArrayList <Float>> ();
            ArrayList <Integer> missing_row_indices = new ArrayList <Integer> ();
            ArrayList <Integer> available_row_indices = new ArrayList <Integer> ();
            ArrayList <Float> row_averages = new ArrayList <Float> ();
            
            for (int i = 0; i < expressions_d.length; i++) {
                ArrayList <Float> row = new ArrayList <Float> ();
                ArrayList <Integer> missing_cols_in_row_i = new ArrayList <Integer> ();
                float row_average = 0;
                int j = 0;
                for (Float item : expressions_d[i]) {
                    row.add(item);
                    if (Float.isNaN(item)) {
                        missing_cols_in_row_i.add(j);
                    } else {
                        row_average += item;
                    }
                    j++;
                }
                if (missing_cols_in_row_i.size() == expressions_d[i].length) {
                    missing_row_indices.add(i);
                } else {
                    available_row_indices.add(i);
                    row_average /= expressions_d[i].length;
                    row_averages.add(row_average);
                    for (int m = 0; m < missing_cols_in_row_i.size(); m++) {
                        row.set(missing_cols_in_row_i.get(m),row_average);
                    }
                    data.add(row);
                }
            }

            try {
                BinaryTree row_binary_tree = analysis.clusterer.doClustering(
                        data,
                        analysis,
                        analysis.global_map_config.row_clustering_params.getDatasetName(),
                        analysis.global_map_config.row_clustering_params
                );
                
                int row_count = 0;
                for (int i=0; i<available_row_indices.size(); i++) {
                    row_cluster_ordering[row_count++] = available_row_indices.get(row_binary_tree.leaf_ordering.get(i));
                }
                for (int i=0; i<missing_row_indices.size(); i++) {
                    row_cluster_ordering[row_count++] = missing_row_indices.get(i);
                }
            } catch (MultiSlideException mse) {
                System.out.println("Hierarchical clustering failed. " + mse.getMessage() + " Falling back on gene groups.");
                for (int i=0; i<this.nSamples; i++) {
                    row_cluster_ordering[i] = i;
                }
            }
            
        } else {
            for (int i=0; i<this.nSamples; i++) {
                row_cluster_ordering[i] = i;
            }
        }
        
        this.row_ordering = row_cluster_ordering;
        
        // Now For columns
        int[] col_cluster_ordering = new int[this.nFilteredGenes];
        if (analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.HIERARCHICAL_COLUMN_ORDERING) {

            Float[][] expressions_d = this.expressions.get(analysis.global_map_config.col_clustering_params.dataset);
            Float[][] data_T = new Float[expressions_d[0].length][expressions_d.length];
            for (int i=0; i<expressions_d.length; i++) {
                for (int j = 0; j < expressions_d[i].length; j++) {
                    data_T[j][i] = expressions_d[i][j];
                }
            }
            
            ArrayList <ArrayList <Float>> data = new ArrayList <ArrayList <Float>> ();
            ArrayList <Integer> missing_col_indices = new ArrayList <Integer> ();
            ArrayList <Integer> available_col_indices = new ArrayList <Integer> ();
            
            for (int i = 0; i < data_T.length; i++) {
                ArrayList <Float> col = new ArrayList <Float> ();
                ArrayList <Integer> missing_rows_in_col_i = new ArrayList <Integer> ();
                float col_average = 0;
                int j = 0;
                for (Float item : data_T[i]) {
                    col.add(item);
                    if (Float.isNaN(item)) {
                        missing_rows_in_col_i.add(j);
                    } else {
                        col_average += item;
                    }
                    j++;
                }
                if (missing_rows_in_col_i.size() == data_T[i].length) {
                    missing_col_indices.add(i);
                } else {
                    available_col_indices.add(i);
                    col_average /= data_T[i].length;
                    for (int m = 0; m < missing_rows_in_col_i.size(); m++) {
                        col.set(missing_rows_in_col_i.get(m),col_average);
                    }
                    data.add(col);
                }
            }
            
            try {
                BinaryTree col_binary_tree = analysis.clusterer.doClustering(
                        data,
                        analysis,
                        analysis.global_map_config.col_clustering_params.getDatasetName(),
                        analysis.global_map_config.col_clustering_params
                );

                int col_count = 0;
                for (int i=0; i<available_col_indices.size(); i++) {
                    col_cluster_ordering[col_count++] = available_col_indices.get(col_binary_tree.leaf_ordering.get(i));
                }
                for (int i=0; i<missing_col_indices.size(); i++) {
                    col_cluster_ordering[col_count++] = missing_col_indices.get(i);
                }
            } catch (MultiSlideException mse) {
                System.out.println("Hierarchical clustering failed. " + mse.getMessage() + " Falling back on gene groups.");
                for (int i=0; i<this.nFilteredGenes; i++) {
                    col_cluster_ordering[i] = i;
                }
            }
        } else {
            for (int i=0; i<this.nFilteredGenes; i++) {
                col_cluster_ordering[i] = i;
            }
        }
        
        this.col_ordering = new int[col_cluster_ordering.length];
        for (int i=0; i<this.significant_gene_indices.size(); i++) {
            this.col_ordering[i] = significant_gene_indices.get(col_cluster_ordering[i]);
        }

    }
    */
    
    public float[][] getDataForSignificanceTesting(String dataset_name, String phenotype, int phenotype_datatype, String[] phenotype_values) {
     
        Float[][] expression_d = getUnsortedExpressions(dataset_name);
        
        ImputationStats imputer;
        if (this.imputation_stats.containsKey(dataset_name)) {
            imputer = this.imputation_stats.get(dataset_name);
        } else {
            imputer = new ImputationStats(dataset_name);
            this.imputation_stats.put(dataset_name, imputer);
        }
        
        Float[][] imputed_data = imputer.imputeByCol(expression_d);
        float[][] d = new float[imputed_data.length][imputed_data[0].length+1];
        for (int s=0; s<imputed_data.length; s++) {
            for (int g=1; g<d[0].length; g++) {
                d[s][g] = imputed_data[s][g-1];
            }
        }
        
        HashMap <String, Integer> phenkeys = new HashMap <String, Integer> ();
        int c = 0;
        for (int i=0; i<phenotype_values.length; i++) {
            if (!phenkeys.containsKey(phenotype_values[i])) {
                phenkeys.put(phenotype_values[i], c++);
            }
        }
        
        for (int s=0; s<imputed_data.length; s++) {
            d[s][0] = phenkeys.get(phenotype_values[s]);
        }
        
        for (int i=0; i<d.length; i++) {
            System.out.println(d[i][0] + "," + d[i][1] + "," + d[i][2]);
        }
        
        return d;
    }
    
    
    private static Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>() {
        @Override
        public int compare(String str1, String str2) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            if (res == 0) {
                res = str1.compareTo(str2);
            }
            return res;
        }
    };
    
    public int[] getGeneGroupFeatureSortOrder(boolean[] gene_masks) {
        
        ArrayList <String> genegroup_list = new ArrayList <String> (this.unique_gene_groups.keySet());
        Collections.sort(genegroup_list, ALPHABETICAL_ORDER);
        
        int index = 0;
        int[] col_order = new int[this.nFilteredGenes];
        HashMap <String, Boolean> t = new HashMap <String, Boolean>();
        for (int i=0; i<genegroup_list.size(); i++) {
            ArrayList <String> entrez_list = this.unique_gene_groups.get(genegroup_list.get(i)).entrez_list;
            for(int j=0; j<entrez_list.size(); j++) {
                String e = entrez_list.get(j);
                if (entrez_pos_map.containsKey(e)) {
                    ArrayList<Integer> ps = entrez_pos_map.get(e);
                    if (!t.containsKey(e)) {
                        for (int p : ps) {
                            if (gene_masks[p]) {
                                System.out.println(index + ", " + p + ", " + entrez_list.get(j));
                                col_order[index++] = p;
                            }
                        }
                        t.put(e, Boolean.TRUE);
                    }
                }
            }
        }
        
        /*
        for (int i=0; i<genegroup_list.size(); i++) {
            String g = genegroup_list.get(i);
            ArrayList <String> entrez_list_genegroup_g = this.unique_gene_groups.get(g).entrez_list;
            for (String e : entrez_pos_map.keySet()) {
                if (entrez_list_genegroup_g.contains(e)) {
                    ArrayList <Integer> ps = entrez_pos_map.get(e);
                    for (int p : ps) {
                        if (gene_masks[p]) {
                            col_order[index++] = p;
                        }
                    }
                }
            }
        }
        */
        
        return col_order;
    }
    
    public int[] getSignificanceLevelFeatureSortOrder(float[] significance_levels) {
        SignificanceIndexedEntrez[] sig_e = new SignificanceIndexedEntrez[this.nFilteredGenes];
        int c = 0;
        for (int i=0; i<this.significant_gene_indices.size(); i++) {
            int index = significant_gene_indices.get(i);
            sig_e[c++] = new SignificanceIndexedEntrez(significance_levels[index], this.entrez.get(index), index);
        }
        Arrays.sort(sig_e);
        int[] col_order = new int[this.nFilteredGenes];
        for (int i=0; i<sig_e.length; i++) {
            col_order[i] = sig_e[i].orig_position;
        }
        return col_order;
    }
    
    public int[] getPhenotypeSortOrder(AnalysisContainer analysis) {
        
        int[] row_order = new int[this.nSamples];
        
        List<PhenotypeSet> phensets = new ArrayList<PhenotypeSet>();
        String[] sample_ids = this.row_names.get(dataset_names[0]);
        
        String[] sp = analysis.global_map_config.phenotype_sorting_params.getPhenotypes();
        if (sp == null || sp.length == 0) {
            for (int s = 0; s < sample_ids.length; s++) {
                row_order[s] = s;
            }
            return row_order;
        }
        
        for (int s = 0; s < this.nSamples; s++) {
            String[] phenset = new String[sp.length];
            for (int p = 0; p < sp.length; p++) {
                phenset[p] = analysis.data.clinical_info.getPhenotypeValue(sample_ids[s], sp[p]);
            }
            phensets.add(new PhenotypeSet(s, phenset, analysis.global_map_config.phenotype_sorting_params.getSortOrders()));
        }
        Collections.sort(phensets, new PhenotypeComparator());
        for (int s = 0; s < sample_ids.length; s++) {
            row_order[s] = phensets.get(s).index;
        }
        return row_order;
    }
    
    public void recomputeRowOrdering(AnalysisContainer analysis) throws MultiSlideException {
        
        SorterCacheEntry e = cache.getSampleOrderFromCache(
                analysis.global_map_config.sampleOrderingScheme,
                analysis.global_map_config.row_clustering_params,
                analysis.global_map_config.phenotype_sorting_params
        );
        if (e != null) {
            this.row_ordering = e.getSortOrder();
            return;
        }
        
        int[] row_cluster_ordering;
        if (analysis.global_map_config.sampleOrderingScheme == GlobalMapConfig.PHENOTYPE_SAMPLE_ORDERING) {
            row_cluster_ordering = this.getPhenotypeSortOrder(analysis);
        } else if (analysis.global_map_config.sampleOrderingScheme == GlobalMapConfig.HIERARCHICAL_SAMPLE_ORDERING) {
            row_cluster_ordering = this.doRowClustering(analysis);
        } else {
            throw new MultiSlideException("Unknown column ordering scheme.");
        }
        
        this.row_ordering = row_cluster_ordering;        
        
        cache.cacheSampleSortOrder(
                analysis.global_map_config.sampleOrderingScheme,
                analysis.global_map_config.row_clustering_params, 
                analysis.global_map_config.phenotype_sorting_params, 
                this.row_ordering
        );
    }
    
    public void recomputeColOrdering(AnalysisContainer analysis) throws MultiSlideException {
        
        SorterCacheEntry e = cache.getFeatureOrderFromCache(
                analysis.global_map_config.columnOrderingScheme,
                analysis.global_map_config.isGeneFilteringOn, 
                analysis.global_map_config.significance_testing_params, 
                analysis.global_map_config.col_clustering_params);
        if (e != null) {
            this.col_ordering = e.getSortOrder();
            this.significant_gene_indices = e.getSignificantGeneIndices();
            this.nFilteredGenes = this.significant_gene_indices.size();
            return;
        }
        
        float[] significance_levels = null;
        boolean[] gene_masks = null;
        String phenotype = analysis.global_map_config.significance_testing_params.getPhenotype();
        if (analysis.global_map_config.isGeneFilteringOn && phenotype != null) {
            HashMap <String, Object> st_results = this.doSignificanceTesting(
                analysis.global_map_config,
                analysis.significance_tester,
                analysis.global_map_config.significance_testing_params.getDataset(), 
                phenotype, 
                analysis.data.clinical_info.getPhenotypeDatatype(phenotype),
                this.phenotype_values.get(phenotype),
                analysis.global_map_config.significance_testing_params.getSignificanceLevel()
            );
            this.significant_gene_indices = (ArrayList <Integer>) st_results.get("significant_gene_indices");
            significance_levels = (float[])st_results.get("significance_levels");
            gene_masks = (boolean[])st_results.get("gene_masks");
        } else {
            this.significant_gene_indices = new ArrayList <Integer> ();
            gene_masks = new boolean[this.nGenes];
            for (int i=0; i<this.nGenes; i++) {
                gene_masks[i] = true;
                this.significant_gene_indices.add(i);
            }
            
            if (analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.SIGNIFICANCE_COLUMN_ORDERING) {
                if (phenotype != null) {
                    HashMap <String, Object> st_results = this.doSignificanceTesting(
                        analysis.global_map_config,
                        analysis.significance_tester,
                        analysis.global_map_config.significance_testing_params.getDataset(), 
                        phenotype, 
                        analysis.data.clinical_info.getPhenotypeDatatype(phenotype),
                        this.phenotype_values.get(phenotype),
                        analysis.global_map_config.significance_testing_params.getSignificanceLevel()
                    );
                    significance_levels = (float[])st_results.get("significance_levels");
                } else {
                    significance_levels = new float[this.nGenes];
                }
            }
        }
        
        this.nFilteredGenes = this.significant_gene_indices.size();
        
        if (analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING) {
            this.col_ordering = getGeneGroupFeatureSortOrder(gene_masks);
        } else if (analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.SIGNIFICANCE_COLUMN_ORDERING) {
            this.col_ordering = getSignificanceLevelFeatureSortOrder(significance_levels);
        } else if (analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.HIERARCHICAL_COLUMN_ORDERING) {
            this.col_ordering = this.doColumnClustering(analysis, gene_masks);
        } else {
            throw new MultiSlideException("Unknown column ordering scheme.");
        }
        
        /*
        this.col_ordering = new int[col_cluster_ordering.length];        
        for (int i=0; i<this.significant_gene_indices.size(); i++) {
            this.col_ordering[i] = significant_gene_indices.get(col_cluster_ordering[i]);
        }
        */
        
        cache.cacheFeatureSortOrder(
                analysis.global_map_config.columnOrderingScheme,
                analysis.global_map_config.isGeneFilteringOn, 
                analysis.global_map_config.significance_testing_params, 
                analysis.global_map_config.col_clustering_params,
                this.col_ordering,
                this.significant_gene_indices
        );
    }
    
    public HashMap <String, ArrayList<Integer>> getEntrezSortPositionMap(GlobalMapConfig global_map_config) {
        HashMap <String, ArrayList<Integer>> entrezSortPositionMap = new HashMap <String, ArrayList<Integer>> ();
        for (int i=0; i<this.nFilteredGenes; i++) {
            String e = this.getEntrezAt(i);
            if (entrezSortPositionMap.containsKey(e)) {
                entrezSortPositionMap.get(e).add(i);
            } else {
                ArrayList<Integer> t = new ArrayList<Integer>();
                t.add(i);
                entrezSortPositionMap.put(e,t);
            }
        }
        return entrezSortPositionMap;
    }
    
    
    
    public Float[][] applyRowColOrdering(Float[][] expressions_d, int r_start, int c_start, int n_Samples, int n_Entrez) {
        Float[][] reordered = new Float[n_Samples][n_Entrez];
        int r;
        for (int i = 0; i < n_Samples; i++) {
            r = this.row_ordering[r_start+i];
            for (int j=0; j < n_Entrez; j++) {
                reordered[i][j] = expressions_d[r][this.col_ordering[c_start+j]];
            }
        }
        return reordered;
    }
    
    public Float[][] applyRowOrdering(Float[][] expressions_d, int r_start, int n_Samples) {
        Float[][] reordered = new Float[n_Samples][expressions_d[0].length];
        int r;
        for (int i = 0; i < n_Samples; i++) {
            r = this.row_ordering[r_start+i];
            for (int j = 0; j < expressions_d[i].length; j++) {
                reordered[i][j] = expressions_d[r][j];
            }
        }
        return reordered;
    }
    
    public Float[][] applyColOrdering(Float[][] expressions_d, int c_start, int n_Entrez) {
        Float[][] reordered = new Float[expressions_d.length][n_Entrez];
        for (int i = 0; i < expressions_d.length; i++) {
            for (int j=0; j < n_Entrez; j++) {
                reordered[i][j] = expressions_d[i][this.col_ordering[c_start+j]];
            }
        }
        return reordered;
    }
    
    public int[][] applyRowColOrdering(int[][] bin_nos, int r_start, int c_start, int n_Samples, int n_Entrez) {
        int[][] reordered = new int[n_Samples][n_Entrez];
        int r;
        for (int i = 0; i < n_Samples; i++) {
            r = this.row_ordering[r_start+i];
            for (int j=0; j < n_Entrez; j++) {
                reordered[i][j] = bin_nos[r][this.col_ordering[c_start+j]];
            }
        }
        return reordered;
    }
    
    public int[][] applyRowOrdering(int[][] bin_nos, int r_start, int n_Samples) {
        int[][] reordered = new int[n_Samples][bin_nos[0].length];
        int r;
        for (int i = 0; i < n_Samples; i++) {
            r = this.row_ordering[r_start+i];
            for (int j = 0; j < bin_nos[i].length; j++) {
                reordered[i][j] = bin_nos[r][j];
            }
        }
        return reordered;
    }
    
    public int[][] applyColOrdering(int[][] bin_nos, int c_start, int n_Entrez) {
        int[][] reordered = new int[bin_nos.length][n_Entrez];
        for (int i = 0; i < bin_nos.length; i++) {
            for (int j=0; j < n_Entrez; j++) {
                reordered[i][j] = bin_nos[i][this.col_ordering[c_start+j]];
            }
        }
        return reordered;
    }
    
    public String[] applyRowOrdering(String[] row_names, int r_start, int n_Samples) {
        String[] reordered = new String[n_Samples];
        for (int i = 0; i < n_Samples; i++) {
            reordered[i] = row_names[this.row_ordering[r_start+i]];
        }
        return reordered;
    }
    
    public String[] applyColOrdering(String[] col_headers, int c_start, int n_Entrez) {
        String[] reordered = new String[n_Entrez];
        for (int i=0; i < n_Entrez; i++) {
            reordered[i] = col_headers[this.col_ordering[c_start+i]];
        }
        return reordered;
    }
    
    /*
    public void addGenes(NetworkNeighbor NN) {
        
        ArrayList<String> entrez_list = new ArrayList<String>();

        GeneGroup gene_group = new GeneGroup("entrez", NN.network_type, NN.query_entrez);
        if (!entrez.contains(NN.query_entrez)) {
            entrez.add(NN.query_entrez);
            if (entrez_group_map.containsKey(NN.query_entrez)) {
                entrez_group_map.get(NN.query_entrez).add(gene_group);
            } else {
                ArrayList <GeneGroup> gg  = new ArrayList <GeneGroup> ();
                gg.add(gene_group);
                entrez_group_map.put(NN.query_entrez, gg);
            }
            this.nGenes++;
        }
        
        for (int j = 0; j < NN.neighbor_entrez_list.length; j++) {
            if (!entrez.contains(NN.neighbor_entrez_list[j])) {
                entrez.add(NN.neighbor_entrez_list[j]);
                if (entrez_group_map.containsKey(NN.neighbor_entrez_list[j])) {
                    entrez_group_map.get(NN.neighbor_entrez_list[j]).add(gene_group);
                } else {
                    ArrayList<GeneGroup> gg = new ArrayList<GeneGroup>();
                    gg.add(gene_group);
                    entrez_group_map.put(NN.neighbor_entrez_list[j], gg);
                }
                this.nGenes++;
            }
        }
        
    }
    */
    
}
