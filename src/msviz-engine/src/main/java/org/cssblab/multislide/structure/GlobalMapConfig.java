package org.cssblab.multislide.structure;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.SignificanceTestingParams;
import org.cssblab.multislide.structure.data.DataFrame;
import org.cssblab.multislide.utils.Utils;

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
    
    public static final int ENTITY_VISUALIZATION_TYPE_SUMMARIZED = 0;
    public static final int ENTITY_VISUALIZATION_TYPE_COARSE = 1;

    public int mapOrientation;
    public String mapResolution;
    public int gridLayout;
    public double colHeaderHeight;
    public double rowLabelWidth;
    //public double numClusterLabels;
    /*
    public int userSpecifiedRowsPerPage;
    public int userSpecifiedColsPerPage;
    public int rowsPerPageDisplayed;
    public int colsPerPageDisplayed;
    public int current_sample_start;
    public int current_feature_start;
    public int available_rows;
    public int available_cols;
    public int scrollBy;
    */
    public int columnOrderingScheme;
    public int sampleOrderingScheme;
    public boolean isGeneFilteringOn;
    public boolean isSampleFilteringOn;
    public ClusteringParams row_clustering_params;
    public ClusteringParams col_clustering_params;
    public SignificanceTestingParams significance_testing_params;
    public PhenotypeSortingParams phenotype_sorting_params;
    public boolean isDatasetLinkingOn;
    private HashMap <String, Boolean> dataset_linkings;
    
    private int visualization_type;
    private String[] row_identifiers;
    
    public boolean isShowClusterLabelsOn;
    
    public GlobalMapConfig() throws MultiSlideException {
        this.mapOrientation = GlobalMapConfig.MAP_ORIENTATION_GENES_ALONG_X;
        this.mapResolution = "M";
        this.gridLayout = -1;
        this.rowLabelWidth = 100;
        this.colHeaderHeight = 100;
        
        
        /*
        this.userSpecifiedRowsPerPage = GlobalMapConfig.DEFAULT_ROWS_PER_PAGE;
        this.userSpecifiedColsPerPage = GlobalMapConfig.DEFAULT_COLS_PER_PAGE;
        this.rowsPerPageDisplayed = GlobalMapConfig.DEFAULT_ROWS_PER_PAGE;
        this.colsPerPageDisplayed = GlobalMapConfig.DEFAULT_COLS_PER_PAGE;
        this.current_sample_start = 0;
        this.current_feature_start = 0;
        this.available_rows = -1;
        this.available_cols = -1;
        this.scrollBy = 1;
        */
        this.isGeneFilteringOn = false;
        this.isSampleFilteringOn = false;
        this.columnOrderingScheme = GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING;
        this.sampleOrderingScheme = GlobalMapConfig.PHENOTYPE_SAMPLE_ORDERING;
        this.significance_testing_params = new SignificanceTestingParams();
        this.col_clustering_params = new ClusteringParams(ClusteringParams.TYPE_FEATURE_CLUSTERING);
        this.row_clustering_params = new ClusteringParams(ClusteringParams.TYPE_SAMPLE_CLUSTERING);
        this.phenotype_sorting_params = new PhenotypeSortingParams();
        
        this.visualization_type = GlobalMapConfig.ENTITY_VISUALIZATION_TYPE_SUMMARIZED;
        this.row_identifiers = new String[0];
        
        this.isDatasetLinkingOn = false;
        this.dataset_linkings = new HashMap<>();
        
        //this.isShowClusterLabelsOn = false;
        //this.numClusterLabels = 5;
    }
    
    public String makeDescString(int nFeatures, int nSamples) {
        String desc = "";
        desc += "Genes: " + nFeatures;
        
        if (isGeneFilteringOn) {
            if (this.significance_testing_params.getApplyFDR()) {
                desc += " (p-value=" + this.significance_testing_params.getSignificanceLevel();
                desc += ", FDR=" + this.significance_testing_params.getFDRThreshold()+ ")";
            } else {
                desc += " (unadjusted p-value=" + this.significance_testing_params.getSignificanceLevel() + ")";
            }
        }
        if (this.columnOrderingScheme == HIERARCHICAL_COLUMN_ORDERING) {
            if (this.isDatasetLinkingOn) {
                desc += ", ordered by hierarchical clustering (" 
                        + "distance=" + this.col_clustering_params.getDistanceFunctionS() 
                        + ", linkage=" + this.col_clustering_params.getLinkageFunctionS() 
                        + ") based on " + this.col_clustering_params.dataset + " data";
            } else {
                desc += ", ordered by hierarchical clustering (" 
                        + "distance=" + this.col_clustering_params.getDistanceFunctionS() 
                        + ", linkage=" + this.col_clustering_params.getLinkageFunctionS() 
                        + ")";
            }
        } else if (this.columnOrderingScheme == SIGNIFICANCE_COLUMN_ORDERING) {
            desc += ", ordered by significance.";
        }
        
        desc += "\n";
        desc += "Samples: " + nSamples;
        
        if (this.sampleOrderingScheme == HIERARCHICAL_SAMPLE_ORDERING) {
            desc += ", ordered by hierarchical clustering ("
                    + "distance=" + this.row_clustering_params.getDistanceFunctionS() 
                    + ", linkage=" + this.row_clustering_params.getLinkageFunctionS() + ")";
        }
        return desc;
    }
    
    public void setDefaultDatasetLinking(ListOrderedMap <String, DataFrame> datasets) {
        int linker_count = 0;
        for(String dataset_name: datasets.keySet()){
            DataFrame d = datasets.get(dataset_name);
            this.dataset_linkings.put(d.name, d.specs.has_linker);
            if (d.specs.has_linker) {
                linker_count++;
            }
        }
        this.isDatasetLinkingOn = linker_count > 1;
    }
    
    public void setDatasetLinkingOn(boolean isDatasetLinkingOn) {
        this.isDatasetLinkingOn = isDatasetLinkingOn;
    }
    
    
    public HashMap <String, Boolean> getDatasetLinkages() {
        
        HashMap <String, Boolean> are_linked;
        
        if (isDatasetLinkingOn) {
            are_linked = this.dataset_linkings;
        } else {
            /*
            if isDatasetLinkingOn is False, all datasets are unlinked 
            under all circumstances
            */
            are_linked = new HashMap <> ();
            for (String name: dataset_linkings.keySet())
                are_linked.put(name, Boolean.FALSE);
        }
        
        Utils.log_info("isDatasetLinkingOn: " + isDatasetLinkingOn);
        for(String s: are_linked.keySet())
            Utils.log_info("dataset: " + s + "\tis_linked=" + are_linked.get(s));
        
        return are_linked;
    }
    
    public void updateDatasetLinkings(String dataset_name, boolean link_state) {
        this.dataset_linkings.put(dataset_name, link_state);
    }
    
    public void setDefaultRowIdentifier(DatasetSpecs specs) {
        this.visualization_type = GlobalMapConfig.ENTITY_VISUALIZATION_TYPE_SUMMARIZED;
        this.row_identifiers = new String[] {specs.getLinkerIdentifierType()};
    }
    
    public String[] getRowIdentifiers() throws MultiSlideException {
        if (this.row_identifiers.length == 0) {
            throw new MultiSlideException("Row identifier not set");
        } else {
            return this.row_identifiers;
        }
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

    /*
    public int getRowsPerPageDisplayed(AnalysisContainer analysis) {
        int available_rows = analysis.data.selected.getNumSamples();
        this.rowsPerPageDisplayed = Math.min(available_rows, this.userSpecifiedRowsPerPage);
        return rowsPerPageDisplayed;
    }
    
    public void setUserSpecifiedRowsPerPage(int rowsPerPage) {
        this.userSpecifiedRowsPerPage = rowsPerPage;
    }
    
    public int getUserSpecifiedRowsPerPage() {
        return userSpecifiedRowsPerPage;
    }

    public int getColsPerPageDisplayed(AnalysisContainer analysis, String dataset_name) {
        int available_cols = analysis.data.selected.getNumFeatures(dataset_name, this);
        this.colsPerPageDisplayed = Math.min(available_cols, this.userSpecifiedColsPerPage);
        return colsPerPageDisplayed;
    }
    
    public void setUserSpecifiedColsPerPage(int colsPerPage) {
        this.userSpecifiedColsPerPage = colsPerPage;
    }
    
    public int getUserSpecifiedColsPerPage() {
        return userSpecifiedColsPerPage;
    }
    */
    
    public void setShowClusterLabelsOn(boolean isShowClusterLabelsOn) {
        this.isShowClusterLabelsOn = isShowClusterLabelsOn;
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

    /*
    public int getCurrentSampleStart() {
        return current_sample_start;
    }

    public void setCurrentSampleStart(int current_sample_start) {
        //this.current_sample_start = current_sample_start;
        current_sample_start = (current_sample_start < 0) ? 0 : current_sample_start;
        //this.current_sample_start = ((current_sample_start + this.getRowsPerPageDisplayed()) > this.available_rows) ? this.current_sample_start : current_sample_start;
        this.current_sample_start = ((current_sample_start + this.getRowsPerPageDisplayed()) > available_rows) ? available_rows - this.getRowsPerPageDisplayed() : current_sample_start;
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
    */
    
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
        this.significance_testing_params = new SignificanceTestingParams();
        this.significance_testing_params.setDataset(dataset_names[0]);
        if (phenotypes.length > 0) {
            this.significance_testing_params.setPhenotype(phenotypes[0]);
        } else {
            this.significance_testing_params.setPhenotype(null);
        }
    }
    
    public void resetPhenotypeSortingParameters(String[] phenotypes) {
        
    }

    public String mapConfigAsJSON () {
        /*
        this.colsPerPageDisplayed = this.getColsPerPageDisplayed();
        this.rowsPerPageDisplayed = this.getRowsPerPageDisplayed();
        */
        return new Gson().toJson(this);
    }
    
    @Override
    public GlobalMapConfig clone() {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(this), GlobalMapConfig.class);
    }
    
}
