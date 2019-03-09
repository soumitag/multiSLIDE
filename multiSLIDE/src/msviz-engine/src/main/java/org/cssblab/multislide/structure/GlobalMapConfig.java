/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import com.google.gson.Gson;

/**
 *
 * @author abhikdatta
 */
public class GlobalMapConfig {
    
    public String mapResolution;
    public int gridLayout;
    public int rowsPerPage;
    public int colsPerPage;
    public double colHeaderHeight;
    public double rowLabelWidth;
    public String sortBy;
    public int current_sample_start;
    public int current_feature_start;
    
    
    public GlobalMapConfig() {
        this.mapResolution = "M";
        this.rowsPerPage = 40;
        this.colsPerPage = 30;
        this.gridLayout = -1;
        this.sortBy = "No_Sort";
        this.rowLabelWidth = 100;
        this.colHeaderHeight = 100;
        this.current_sample_start = 0;
        this.current_feature_start = 0;
    }

    public String getMapResolution() {
        return mapResolution;
    }

    public void setMapResolution(String mapResolution) {
        this.mapResolution = mapResolution;
    }

    public int getGridLayout() {
        return gridLayout;
    }

    public void setGridLayout(int gridLayout) {
        this.gridLayout = gridLayout;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public int getColsPerPage() {
        return colsPerPage;
    }

    public void setColsPerPage(int colsPerPage) {
        this.colsPerPage = colsPerPage;
    }

    public double getColHeaderHeight() {
        return colHeaderHeight;
    }

    public void setColHeaderHeight(double colHeaderHeight) {
        this.colHeaderHeight = colHeaderHeight;
    }

    public double getRowLabelWidth() {
        return rowLabelWidth;
    }

    public void setRowLabelWidth(double rowLabelWidth) {
        this.rowLabelWidth = rowLabelWidth;
    }
    
    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public int getCurrentSampleStart() {
        return current_sample_start;
    }

    public void setCurrentSampleStart(int current_sample_start) {
        this.current_sample_start = current_sample_start;
    }

    public int getCurrentFeatureStart() {
        return current_feature_start;
    }

    public void setCurrentFeatureStart(int current_feature_start) {
        this.current_feature_start = current_feature_start;
    }
    
    public String mapConfigAsJSON () {
        return new Gson().toJson(this);
    }
    
}
