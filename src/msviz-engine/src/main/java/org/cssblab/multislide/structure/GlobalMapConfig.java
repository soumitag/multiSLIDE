package org.cssblab.multislide.structure;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.beans.data.SignificanceTestingParams;

/**
 *
 * @author soumitag
 */
public class GlobalMapConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final int MAP_ORIENTATION_GENES_ALONG_X   = 0;
    public static final int MAP_ORIENTATION_SAMPLES_ALONG_X = 1;
    
    public static final int DEFAULT_COLS_PER_PAGE = 30;
    public static final int DEFAULT_ROWS_PER_PAGE = 40;
    
    public static final int GENE_GROUP_COLUMN_ORDERING = 0;
    public static final int HIERARCHICAL_COLUMN_ORDERING = 1;
    public static final int SIGNIFICANCE_COLUMN_ORDERING = 2;
    
    public static final int PHENOTYPE_SAMPLE_ORDERING = 0;
    public static final int HIERARCHICAL_SAMPLE_ORDERING = 1;

    public int mapOrientation;
    public String mapResolution;
    public int gridLayout;
    public int rowsPerPageDisplayed;
    public int colsPerPageDisplayed;
    public int userSpecifiedRowsPerPage;
    public int userSpecifiedColsPerPage;
    public double colHeaderHeight;
    public double rowLabelWidth;
    public int current_sample_start;
    public int current_feature_start;
    public int available_rows;
    public int available_cols;
    public int scrollBy;
    public int columnOrderingScheme;
    public int sampleOrderingScheme;
    public boolean isGeneFilteringOn;
    public boolean isSampleFilteringOn;
    public ClusteringParams row_clustering_params;
    public ClusteringParams col_clustering_params;
    public SignificanceTestingParams significance_testing_params;
    public PhenotypeSortingParams phenotype_sorting_params;
    
    public GlobalMapConfig() throws MultiSlideException {
        this.mapOrientation = GlobalMapConfig.MAP_ORIENTATION_GENES_ALONG_X;
        this.mapResolution = "M";
        this.rowsPerPageDisplayed = GlobalMapConfig.DEFAULT_ROWS_PER_PAGE;
        this.colsPerPageDisplayed = GlobalMapConfig.DEFAULT_COLS_PER_PAGE;
        this.userSpecifiedRowsPerPage = GlobalMapConfig.DEFAULT_ROWS_PER_PAGE;
        this.userSpecifiedColsPerPage = GlobalMapConfig.DEFAULT_COLS_PER_PAGE;
        this.gridLayout = -1;
        this.rowLabelWidth = 100;
        this.colHeaderHeight = 100;
        this.current_sample_start = 0;
        this.current_feature_start = 0;
        this.available_rows = -1;
        this.available_cols = -1;
        this.scrollBy = 1;
        this.isGeneFilteringOn = false;
        this.isSampleFilteringOn = false;
        this.columnOrderingScheme = GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING;
        this.sampleOrderingScheme = GlobalMapConfig.PHENOTYPE_SAMPLE_ORDERING;
        this.significance_testing_params = new SignificanceTestingParams();
        this.col_clustering_params = new ClusteringParams(HierarchicalClusterer.TYPE_COL_CLUSTERING);
        this.row_clustering_params = new ClusteringParams(HierarchicalClusterer.TYPE_ROW_CLUSTERING);
        this.phenotype_sorting_params = new PhenotypeSortingParams();
    }
    
    public int getMapOrientation() {
        return mapOrientation;
    }

    public void setMapOrientation(int mapOrientation) {
        this.mapOrientation = mapOrientation;
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

    public int getRowsPerPageDisplayed() {
        this.rowsPerPageDisplayed = Math.min(this.available_rows, this.userSpecifiedRowsPerPage);
        return rowsPerPageDisplayed;
    }
    
    public void setUserSpecifiedRowsPerPage(int rowsPerPage) {
        this.userSpecifiedRowsPerPage = rowsPerPage;
    }
    
    public int getUserSpecifiedRowsPerPage() {
        return userSpecifiedRowsPerPage;
    }

    public int getColsPerPageDisplayed() {
        this.colsPerPageDisplayed = Math.min(this.available_cols, this.userSpecifiedColsPerPage);
        return colsPerPageDisplayed;
    }
    
    public void setUserSpecifiedColsPerPage(int colsPerPage) {
        this.userSpecifiedColsPerPage = colsPerPage;
    }
    
    public int getUserSpecifiedColsPerPage() {
        return userSpecifiedColsPerPage;
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
    
    public int getSampleOrderingScheme() {
        return sampleOrderingScheme;
    }

    public void setSampleOrderingScheme(int sampleOrderingScheme) {
        this.sampleOrderingScheme = sampleOrderingScheme;
    }

    public int getCurrentSampleStart() {
        return current_sample_start;
    }

    public void setCurrentSampleStart(int current_sample_start) {
        //this.current_sample_start = current_sample_start;
        current_sample_start = (current_sample_start < 0) ? 0 : current_sample_start;
        //this.current_sample_start = ((current_sample_start + this.getRowsPerPageDisplayed()) > this.available_rows) ? this.current_sample_start : current_sample_start;
        this.current_sample_start = ((current_sample_start + this.getRowsPerPageDisplayed()) > this.available_rows) ? this.available_rows - this.getRowsPerPageDisplayed() : current_sample_start;
    }

    public int getCurrentFeatureStart() {
        return current_feature_start;
    }

    public void setCurrentFeatureStart(int current_feature_start) {
        //this.current_feature_start = current_feature_start;
        current_feature_start = (current_feature_start < 0) ? 0 : current_feature_start;
        //this.current_feature_start = ((current_feature_start + this.getColsPerPageDisplayed()) > this.available_cols) ? this.current_feature_start : current_feature_start;
        this.current_feature_start = ((current_feature_start + this.getColsPerPageDisplayed()) > this.available_cols) ? this.available_cols - this.getColsPerPageDisplayed() : current_feature_start;
    }
    
    public void setAvailableRows(int available_rows) {
        this.available_rows = available_rows;
    }
    
    public void setAvailableCols(int available_cols) {
        this.available_cols = available_cols;
    }
    
    public void setScrollBy(int scrollBy) {
        this.scrollBy = scrollBy;
    }
    
    public PhenotypeSortingParams getPhenotypeSortingParams() {
        return this.phenotype_sorting_params;
    }

    public void setPhenotypeSortingParams(PhenotypeSortingParams phenotype_sorting_params) {
        this.phenotype_sorting_params = phenotype_sorting_params;
    }

    public int getColumnOrderingScheme() {
        return this.columnOrderingScheme;
    }
    
    public void setColumnOrderingScheme(int columnOrderingScheme) {
        this.columnOrderingScheme = columnOrderingScheme;
    }
    
    public boolean isGeneFilteringOn() {
        return isGeneFilteringOn;
    }

    public void setGeneFilteringOn(boolean isGeneFilteringOn) {
        this.isGeneFilteringOn = isGeneFilteringOn;
    }
    
    public boolean isSampleFilteringOn() {
        return isSampleFilteringOn;
    }

    public void setSampleFilteringOn(boolean isSampleFilteringOn) {
        this.isSampleFilteringOn = isSampleFilteringOn;
    }
    
    public void setSignificanceTestingParameters(SignificanceTestingParams params) {
        this.significance_testing_params = params;
    }
    
    public void resetSignificanceTestingParameters(String[] dataset_names, String[] phenotypes) {
        
        this.significance_testing_params.setDataset(dataset_names[0]);
        if (phenotypes.length > 0) {
            this.significance_testing_params.setPhenotype(phenotypes[0]);
        } else {
            this.significance_testing_params.setPhenotype(null);
        }
        this.significance_testing_params.setSignificanceLevel(0.05);
        
        /*
        boolean hasDataset = false;
        String dataset = this.significance_testing_params.getDataset();
        if (dataset == null) {
            this.significance_testing_params.setDataset(dataset_names[0]);
        } else {
            for (String dataset_name : dataset_names) {
                if (dataset.equals(dataset_name)) {
                    hasDataset = true;
                    break;
                }
            }
            if (!hasDataset) {
                this.significance_testing_params.setDataset(dataset_names[0]);
            }
        }
        boolean hasPhenotype = false;
        String p = this.significance_testing_params.getPhenotype();
        if (p == null) {
            if (phenotypes.length > 0) {
                this.significance_testing_params.setPhenotype(phenotypes[0]);
            } else {
                this.significance_testing_params.setPhenotype("");
            }
        } else {
            for (String phenotype : phenotypes) {
                if (p.equals(phenotype)) {
                    hasPhenotype = true;
                    break;
                }
            }
            if (!hasPhenotype) {
                if (phenotypes.length > 0) {
                    this.significance_testing_params.setPhenotype(phenotypes[0]);
                } else {
                    this.significance_testing_params.setPhenotype("");
                }
            }
        }
        */
    }

    public String mapConfigAsJSON () {
        //this.colsPerPageDisplayed = Math.min(this.available_cols, this.userSpecifiedColsPerPage);
        //this.rowsPerPageDisplayed = Math.min(this.available_rows, this.userSpecifiedRowsPerPage);
        this.colsPerPageDisplayed = this.getColsPerPageDisplayed();
        this.rowsPerPageDisplayed = this.getRowsPerPageDisplayed();
        return new Gson().toJson(this);
    }
    
}
