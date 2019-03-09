/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Soumita
 */
public class FilteredSortedData {
    
    public int nGenes;
    public int nSamples;
    public int nDatasets;
    
    public ArrayList <String> entrez;
    public ArrayList <Integer> gene_tags;                           // pre-computed tags based on entrez_group_map and gene_groups
    public HashMap <String, String[]> phenotypes;
    public HashMap <String, Float[][]> expressions;
    public HashMap <String, int[][]> bin_nos;
    public HashMap <String, String[]> column_headers;
    public HashMap <String, String[]> row_names;
    public HashMap <String, double[]> min_max;
    public HashMap <String, String> entrez_group_map;               // entrez -> gene_group.name + "_" + gene_group.type
    public HashMap <String, GeneGroup> gene_groups;                 // Key = gene_group.name + "_" + gene_group.type; value = gene_group object
    //public HashMap <String,HashMap <String, String[]>> metadata;
    
    public FilteredSortedData() {
        entrez = new ArrayList <String> ();
        gene_tags = new ArrayList <Integer> ();
        phenotypes = new HashMap <String, String[]> ();
        expressions = new HashMap <String, Float[][]> ();
        bin_nos = new HashMap <String, int[][]> ();
        column_headers = new HashMap <String, String[]> ();
        row_names = new HashMap <String, String[]> ();
        min_max = new HashMap <String, double[]> ();
        entrez_group_map = new HashMap <String, String> ();
        gene_groups = new HashMap <String, GeneGroup> ();
        //metadata = new HashMap <String,HashMap <String, String[]>> ();
        nDatasets = 0;
    }
    
    /*
    public String[] getMetaDataColumnNames(String dataset_name) {
        HashMap <String, String[]> dataset_metadata = this.metadata.get(dataset_name);
        String[] a = new String[dataset_metadata.keySet().size()];
        dataset_metadata.keySet().toArray(a);
        return a;
    }
    */

    public String[] getColumnHeaders(String dataset_name, String column_label) {
        return column_headers.get(dataset_name);
    }

    public void addColumnHeaders(String[] column_headers, String dataset_name) {
        this.column_headers.put(dataset_name, column_headers);
        this.nGenes = column_headers.length;
    }

    public String[] getRowNames(String dataset_name) {
        return row_names.get(dataset_name);
    }

    public void addRowNames(String[] row_names, String dataset_name) {
        this.row_names.put(dataset_name, row_names);
        this.nSamples = row_names.length;
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

    public ArrayList <Integer> getGeneTags_1(String tag_type) {
        return this.gene_tags;
    }

    public void addGeneTags(ArrayList <Integer> gene_tags) {
        this.gene_tags = gene_tags;
    }

    public String[] getPhenotypes(String phenotype) {
        return phenotypes.get(phenotype);
    }

    public void addPhenotypes(String[] phenotype_values, String phenotype) {
        this.phenotypes.put(phenotype, phenotype_values);
    }

    public Float[][] getExpressions(String dataset_name) {
        return expressions.get(dataset_name);
    }

    public void addExpressions(Float[][] expressions, String dataset_name) {
        this.expressions.put(dataset_name, expressions);
    }
    
    public int getExpressionBinNo(String dataset_name, int row, int col) {
        return bin_nos.get(dataset_name)[row][col];
    }
    
    public int[][] getExpressionBinNos(String dataset_name) {
        return bin_nos.get(dataset_name);
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
    
    public void addEntrezList(ArrayList <String> entrez) {
        this.entrez = entrez;
    }
    
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
    
    public void setGeneGroups(HashMap<String, GeneGroup> entrez_gene_group_map) {
        for (int i=0; i<entrez.size(); i++) {
            GeneGroup grp = entrez_gene_group_map.get(entrez.get(i));
            String key = grp.getGroupKey();
            this.gene_groups.put(key, grp);
            this.entrez_group_map.put(entrez.get(i), key);
        }
        for (int i=0; i<this.entrez.size(); i++) {
            String key = this.entrez_group_map.get(this.entrez.get(i));
            this.gene_tags.add(this.gene_groups.get(key).tag);
        }
    }
    
    public ArrayList <String> getGeneGroup(String grp_key) {
        return this.gene_groups.get(grp_key).entrez_list;
    }
    
    public HashMap <String, String> getEntrezGroupMap () {
        return this.entrez_group_map;
    }
    
    public int[] sortByPhenoptype(String phenotype) {
        return null;
    }
    
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
}
