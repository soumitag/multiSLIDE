package org.cssblab.multislide.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.cssblab.multislide.beans.data.SignificanceTestingParams;
import org.cssblab.multislide.utils.PhenotypeComparator;
import org.cssblab.multislide.utils.PhenotypeSet;

/**
 *
 * @author soumitag
 */
public class SortOrderCache implements Serializable {

    private static final long serialVersionUID = 1L;
    
    class SorterCacheEntry implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private int row_col_type; 
        private int ordering_type; 
        private boolean with_filtering;
        private String filtering_params;
        private String sort_params;
        private int[] sort_order;
        private ArrayList <Integer> significant_gene_indices;
        
        SorterCacheEntry() {}
        
        SorterCacheEntry(
                int row_col_type, int ordering_type, boolean with_filtering, String filtering_params, String sort_params, int[] sort_order, ArrayList <Integer> significant_gene_indices
        ) {
            this.row_col_type = row_col_type;
            this.ordering_type = ordering_type;
            this.with_filtering = with_filtering;
            this.filtering_params = filtering_params;
            this.sort_params = sort_params;
            this.sort_order = sort_order;
            this.significant_gene_indices = significant_gene_indices;
        }
        
        // only used for making keys
        private SorterCacheEntry(
                int row_col_type, int ordering_type, boolean with_filtering, String filtering_params, String sort_params
        ) {
            this.row_col_type = row_col_type;
            this.ordering_type = ordering_type;
            this.with_filtering = with_filtering;
            this.filtering_params = filtering_params;
            this.sort_params = sort_params;
            this.sort_order = null;
            this.significant_gene_indices = null;
        }
        
        public int[] getSortOrder() {
            return this.sort_order;
        }
        
        public ArrayList <Integer> getSignificantGeneIndices() {
            return this.significant_gene_indices;
        }
        
        public String makeCacheKey() {
            String key = this.row_col_type + "_" + this.ordering_type + "_" + this.with_filtering;
            if (this.with_filtering) {
                key += "_" + this.filtering_params;
            }
            key += "_" + this.sort_params;
            return key;
        }
        
        public String makeCacheKey(int row_col_type, int ordering_type, boolean with_filtering, String filtering_params, String sort_params) {
            return (new SorterCacheEntry(row_col_type, ordering_type, with_filtering, filtering_params, sort_params).makeCacheKey());
        }
    }
            
    
    private HashMap <String, SorterCacheEntry> cache;
    
    SortOrderCache() {
        cache = new HashMap <String, SorterCacheEntry> ();
    }
    
    public SorterCacheEntry getSampleOrderFromCache(
            int sample_ordering_scheme, 
            ClusteringParams row_clustering_params, 
            PhenotypeSortingParams phenotype_sorting_params
    ) {
        String key = "";
        if (sample_ordering_scheme == GlobalMapConfig.HIERARCHICAL_SAMPLE_ORDERING) {
            key = (new SorterCacheEntry()).makeCacheKey(
                    0, GlobalMapConfig.SIGNIFICANCE_COLUMN_ORDERING, false, "", row_clustering_params.toString()
            );
        } else if (sample_ordering_scheme == GlobalMapConfig.PHENOTYPE_SAMPLE_ORDERING) {
            key = (new SorterCacheEntry()).makeCacheKey(
                    0, GlobalMapConfig.SIGNIFICANCE_COLUMN_ORDERING, false, "", phenotype_sorting_params.toString()
            );
        }
        if(!cache.containsKey(key)) {
            return null;
        } else {
            return this.cache.get(key);
        }
    }
    
    public SorterCacheEntry getFeatureOrderFromCache(
            int feature_ordering_scheme, 
            boolean with_filtering, 
            SignificanceTestingParams significance_testing_params, 
            ClusteringParams col_clustering_params
    ) {
        String key = "";
        if (feature_ordering_scheme == GlobalMapConfig.SIGNIFICANCE_COLUMN_ORDERING) {
            key = (new SorterCacheEntry()).makeCacheKey(
                    1, GlobalMapConfig.SIGNIFICANCE_COLUMN_ORDERING, with_filtering, significance_testing_params.toString(), significance_testing_params.toString()
            );
        } else if (feature_ordering_scheme == GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING) {
            key = (new SorterCacheEntry()).makeCacheKey(
                    1, GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING, with_filtering, significance_testing_params.toString(), ""
            );
        } else if (feature_ordering_scheme == GlobalMapConfig.HIERARCHICAL_COLUMN_ORDERING) {
            key = (new SorterCacheEntry()).makeCacheKey(
                    1, GlobalMapConfig.HIERARCHICAL_COLUMN_ORDERING, with_filtering, significance_testing_params.toString(), col_clustering_params.toString()
            );
        }
        
        if(!cache.containsKey(key)) {
            return null;
        } else {
            return this.cache.get(key);
        }
    }
    
    public void cacheSampleSortOrder(
            int sample_ordering_scheme, 
            ClusteringParams row_clustering_params, 
            PhenotypeSortingParams phenotype_sorting_params,
            int[] sort_order
    ) {
        if (sample_ordering_scheme == GlobalMapConfig.HIERARCHICAL_SAMPLE_ORDERING) {
            SorterCacheEntry entry = new SorterCacheEntry(
                0, GlobalMapConfig.HIERARCHICAL_SAMPLE_ORDERING, false, "", row_clustering_params.toString(), sort_order, null
            );
            cache.put(entry.makeCacheKey(), entry);
        } else if (sample_ordering_scheme == GlobalMapConfig.PHENOTYPE_SAMPLE_ORDERING) {
            SorterCacheEntry entry = new SorterCacheEntry(
                0, GlobalMapConfig.PHENOTYPE_SAMPLE_ORDERING, false, "", phenotype_sorting_params.toString(), sort_order, null
            );
            cache.put(entry.makeCacheKey(), entry);
        }
    }
    
    public void cacheFeatureSortOrder(
            int feature_ordering_scheme, 
            boolean with_filtering, 
            SignificanceTestingParams significance_testing_params, 
            ClusteringParams col_clustering_params, 
            int[] sort_order, 
            ArrayList <Integer> significant_gene_indices
    ) {
        if (feature_ordering_scheme == GlobalMapConfig.HIERARCHICAL_COLUMN_ORDERING) {
            SorterCacheEntry entry = new SorterCacheEntry(
                1, GlobalMapConfig.HIERARCHICAL_COLUMN_ORDERING, with_filtering, significance_testing_params.toString(), col_clustering_params.toString(), sort_order, significant_gene_indices
            );
            cache.put(entry.makeCacheKey(), entry);
        } else if (feature_ordering_scheme == GlobalMapConfig.SIGNIFICANCE_COLUMN_ORDERING) {
            SorterCacheEntry entry = new SorterCacheEntry(
                1, GlobalMapConfig.SIGNIFICANCE_COLUMN_ORDERING, with_filtering, significance_testing_params.toString(), significance_testing_params.toString(), sort_order, significant_gene_indices
            );
            cache.put(entry.makeCacheKey(), entry);
        } else if (feature_ordering_scheme == GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING) {
            SorterCacheEntry entry = new SorterCacheEntry(
                1, GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING, with_filtering, significance_testing_params.toString(), "", sort_order, significant_gene_indices
            );
            cache.put(entry.makeCacheKey(), entry);
        }
    }

    
}
