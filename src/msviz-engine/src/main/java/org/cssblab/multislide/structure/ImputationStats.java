/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author soumitag
 */
public class ImputationStats implements Serializable {
    
    private static final long serialVersionUID = 1L;

    protected String dataset_name;
    protected boolean hasRowData;
    protected boolean hasColData;
    protected ArrayList <Integer> missing_row_indices;
    protected ArrayList <Integer> available_row_indices;
    protected ArrayList <Integer> missing_col_indices;
    protected ArrayList <Integer> available_col_indices;
    protected ArrayList <Float> row_averages;
    protected ArrayList <Float> col_averages;
    protected ArrayList <int[]> missing_cell_indices;
    
    public ImputationStats(String dataset_name) {
        this.dataset_name = dataset_name;
        this.hasColData = false;
        this.hasRowData = false;
    }
    
    public void computeRowImputationdStats (Float[][] expressions_d) {
        
        missing_row_indices = new ArrayList <Integer> ();
        available_row_indices = new ArrayList <Integer> ();
        row_averages = new ArrayList <Float> ();
        missing_cell_indices = new ArrayList <int[]> ();

        for (int i = 0; i < expressions_d.length; i++) {
            
            ArrayList<Integer> missing_cols_in_row_i = new ArrayList<Integer>();
            float row_average = 0;
            int j = 0;
            for (Float item : expressions_d[i]) {
                if (Float.isNaN(item)) {
                    missing_cols_in_row_i.add(j);
                    missing_cell_indices.add(new int[]{i,j});
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
            }
        }
        
        this.hasRowData = true;
    }
    
    public void computeColImputationdStats (Float[][] expressions_d) {
        
        missing_col_indices = new ArrayList <Integer> ();
        available_col_indices = new ArrayList <Integer> ();
        col_averages = new ArrayList <Float> ();
        missing_cell_indices = new ArrayList <int[]> ();

        Float[][] data_T = new Float[expressions_d[0].length][expressions_d.length];
        for (int i = 0; i < expressions_d.length; i++) {
            for (int j = 0; j < expressions_d[i].length; j++) {
                data_T[j][i] = expressions_d[i][j];
            }
        }

        for (int i = 0; i < data_T.length; i++) {
            ArrayList<Integer> missing_rows_in_col_i = new ArrayList<Integer>();
            float col_average = 0;
            int j = 0;
            for (Float item : data_T[i]) {
                if (Float.isNaN(item)) {
                    missing_rows_in_col_i.add(j);
                    missing_cell_indices.add(new int[]{j,i});
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
                col_averages.add(col_average);
            }
        }
        
        this.hasColData = true;
    }
    
    public Float[][] imputeByRow(Float[][] expressions_d) {
        if (!this.hasRowData) {
            this.computeRowImputationdStats(expressions_d);
        }
        
        Float[][] data = new Float[available_row_indices.size()][expressions_d[0].length];
        for (int i=0; i<available_row_indices.size(); i++) {
            data[i] = expressions_d[available_row_indices.get(i)];
        }
        
        for (int i=0; i<missing_cell_indices.size(); i++) {
            int[] rc = missing_cell_indices.get(i);
            data[rc[0]][rc[1]] = row_averages.get(rc[0]);
        }
        
        return data;
    }
    
    public Float[][] imputeByCol(Float[][] expressions_d) {
        if (!this.hasColData) {
            this.computeColImputationdStats(expressions_d);
        }
        
        Float[][] data = new Float[expressions_d.length][available_col_indices.size()];
        for (int i = 0; i < expressions_d.length; i++) {
            for (int j = 0; j<available_col_indices.size(); j++) {
                data[i][j] = expressions_d[i][available_col_indices.get(j)];
            }
        }
        
        for (int i=0; i<missing_cell_indices.size(); i++) {
            int[] rc = missing_cell_indices.get(i);
            if (!missing_col_indices.contains(rc[1])) {
                data[rc[0]][rc[1]] = col_averages.get(rc[1]);
            }
        }
        
        return data;
    }
    
    public ArrayList <Integer> getAvailableColIndices() {
        return this.available_col_indices;
    }
}
