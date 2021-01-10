/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.GeneGroup;
import org.cssblab.multislide.structure.GlobalMapConfig;
import org.cssblab.multislide.structure.MapConfig;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.utils.Utils;
import static org.apache.spark.sql.functions.col;
import org.cssblab.multislide.beans.data.SignificanceTestingParams;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.resources.MultiSlideContextListener;
import org.cssblab.multislide.structure.data.table.Table;
import org.cssblab.multislide.utils.CollectionUtils;

/**
 *
 * @author Soumita Ghosh and Abhik Datta
 */
public class Selection implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public final String[] dataset_names;
    public final int nDatasets;
    public HashMap <String, View> views;
    /*
    Sample and feature indices
    */
    private SampleIndex sample_ids;        //df->columns
    //private FeatureIndex feature_manifest;
    /*
    Feature related information
    */
    //private Dataset <Row> consolidated_linker;
    private HashMap <String, GeneGroup> gene_groups;            // keys -> gene_group_ids, value -> gene group objects
    private ListOrderedMap <String, GeneGroup> nn_gene_groups;         // keys -> gene_group_ids, value -> gene group objects
    /*
    Sample related information
    */
    private Dataset <Row> phenotypes;                       // columns -> phenotype, rows -> samples
    
    public Selection(
            AnalysisContainer analysis,
            String[] dataset_names,
            Table feature_selection,
            HashMap <String, GeneGroup> gene_groups,
            ListOrderedMap <String, GeneGroup> nn_gene_groups,
            ListOrderedMap <String, DataFrame> datasets,
            ClinicalInformation clinical_info,
            HashMap <String, Boolean> is_linked
    ) throws MultiSlideException {
        
        this.dataset_names = dataset_names;
        this.nDatasets = this.dataset_names.length;
        
        this.gene_groups = gene_groups;
        this.nn_gene_groups = nn_gene_groups;
        
        try {
        
            /*
            1. Create views
            */
            views = new HashMap <> ();
            for (String name : dataset_names) {
                DataFrame dataset = (DataFrame)datasets.get(name);
                View v = new View(
                        name, feature_selection, dataset, analysis.data_selection_state.add_genes_source_type, 
                        is_linked.get(name), dataset.specs,
                        gene_groups, nn_gene_groups, analysis.spark_session, analysis
                );
                this.views.put(name, v);
            }
            
            /*
            Align linked views
            */
            this.alignViews(analysis, is_linked);
            
            /*
            2. Get phenotypes: extract user selected phenotype column and
            */
            List <String> c = new ArrayList <> ();
            c.add("_Sample_IDs");
            c.addAll(Arrays.asList(analysis.data_selection_state.selected_phenotypes));
            this.phenotypes = clinical_info.phenotype_data
                                           .select(CollectionUtils.asColumnArray(c));
            
            /* 
            3. re-order samples based on global map config and set filtered_sorted_sample_ids into filtered_sorted_sample_ids
            Re-order phenotypes as per filtered_sorted_sample_ids
            */
            this.recomputeSampleOrdering(analysis);

            /*
            4. order features based on global map config and filtered_sorted_feature_ids
               create a global (aligned) ordering based on global map config
               depending on whether each dataset is linked or independent apply either the global ordering or recompute ordering
            */
            this.initializeFeatureOrdering(analysis, is_linked);

        } catch (Exception e) {
            
            Utils.log_exception(e, "");
            throw new MultiSlideException("Exception in Selection.init(): \n" + e.getMessage());
            
        }
    }
    
    public final void recomputeSampleOrdering(AnalysisContainer analysis) 
            throws MultiSlideException, DataParsingException {
        /* 
        Function re-orders samples based on global map config and creates the 
        sample_ids index
        */
        
        /*
        Determine dataset to be used
        */
        String dname;
        switch (analysis.global_map_config.sampleOrderingScheme) {
            case GlobalMapConfig.PHENOTYPE_SAMPLE_ORDERING:
                dname = dataset_names[0];
                break;
            case GlobalMapConfig.HIERARCHICAL_SAMPLE_ORDERING:
                dname = analysis.global_map_config.row_clustering_params.getDatasetName();
                break;
            default:
                dname = dataset_names[0];
                break;
        }
        
        /*
        Get sample ordering from recomputeSampleOrdering which returns ordered 
        samples as a Dataset with a "_Sample_IDs" and an "_index" column
        */
        long startTime = System.nanoTime();
        
        sample_ids = new SampleIndex (analysis.spark_session, 
                this.views.get(dname).recomputeSampleOrdering(analysis, this.phenotypes));
        
        /*
            sample count can't be 0 (happens when no rows are selected for a dataset)
            therefore use a different dataset to try and determine order
        */
        if (sample_ids.count() == 0) {
            for (String dataset_name : dataset_names) {
                if (!dataset_name.equals(dname)) {
                    sample_ids = new SampleIndex (analysis.spark_session, 
                            this.views.get(dataset_name).recomputeSampleOrdering(analysis, this.phenotypes));
                }
                if (sample_ids.count() != 0) {
                    break;
                }
            }
        }
        /*
            all attemps to get sample orderings failed, just use the default ordering
            in this case no data will be displayed anyway, therefore this is 
            simply to avoid the call from erroring
        */
        if (sample_ids.count() == 0) {
            sample_ids = new SampleIndex (this.getSampleIDs(dname, analysis.global_map_config));
        }
        
        Utils.log_info("sample ordering view (time) " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
        
        /*
        Apply sample ordering to all datasets
        Does not affect the dataset, only re-orders the sample_ids Index in Views, 
        used during select
        */
        for (String name : dataset_names) {
            this.views.get(name).reorderSamples(sample_ids);
        }
        
        /*
        Add _index to phenotypes by joining with sample_ids for easy
        _index based retrieval
        */
        startTime = System.nanoTime();
        
        if (CollectionUtils.arrayContains(phenotypes.columns(), "_index"))
            phenotypes = phenotypes.drop(col("_index")).repartition(MultiSlideContextListener.N_SPARK_PARTITIONS).cache();
        
        this.phenotypes = this.phenotypes.join(sample_ids.asDataset(), CollectionUtils.asSeq("_Sample_IDs"))
                                         .repartition(MultiSlideContextListener.N_SPARK_PARTITIONS).cache();
        
        Utils.log_info("phenotypes (time) " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
        
        Utils.log_info("Showing phenotype in recomputeSampleOrdering()");
        
    }
    
    public void updateSelectedPhenotypes(
            AnalysisContainer analysis,
            ClinicalInformation clinical_info
    ) throws MultiSlideException, DataParsingException {

        /*
            2. Get phenotypes: extract user selected phenotype column and
         */
        List<String> c = new ArrayList<>();
        c.add("_Sample_IDs");
        c.addAll(Arrays.asList(analysis.data_selection_state.selected_phenotypes));
        this.phenotypes = clinical_info.phenotype_data
                .select(CollectionUtils.asColumnArray(c));

        /* 
            3. re-order samples based on global map config and set filtered_sorted_sample_ids into filtered_sorted_sample_ids
            Re-order phenotypes as per filtered_sorted_sample_ids
         */
        this.recomputeSampleOrdering(analysis);
        
    }
    
    public final void recomputeFeatureOrdering(AnalysisContainer analysis) 
            throws MultiSlideException, DataParsingException {
        this.initializeFeatureOrdering(analysis, analysis.global_map_config.getDatasetLinkages());
    }
    
    public final void alignViews(AnalysisContainer analysis, HashMap <String, Boolean> is_linked) throws MultiSlideException {
        
        /*
            All views need an _id column and feature_index. 
            Which is used for ordering, sorting, etc
        */
        for (String name : dataset_names) {
            this.views.get(name).addId();
            this.views.get(name).createDefaultFeatureManifest();
        }
        
    }
    
    public final void initializeFeatureOrdering(AnalysisContainer analysis, HashMap <String, Boolean> is_linked)
            throws MultiSlideException, DataParsingException {
        /*
        1. Check if there are at least two columns to be linked, 
        */
        int linked_count = 0;
        for (String name : dataset_names) {
            if (is_linked.get(name))
                linked_count++;
        }
        
        HashMap <String, Boolean> processed_datasets = new HashMap <> ();
        
        if (linked_count > 1) {
            
            Table significance_linker = null;
            if (analysis.global_map_config.isGeneFilteringOn) {
                String dname = analysis.global_map_config.significance_testing_params.getDataset();
                Table significance = this.views.get(dname).doSignificanceAnalysis(analysis);
                
                /*
                    get _id, and _linker columns
                */
                Table _id_linker_table = this.views.get(dname).getIdAndLinkerAsTable();
            
                /*
                    Join to get
                    significance_linker: _id, _significance, _fdr, _index, _linker
                */
                significance_linker = _id_linker_table.joinByKey(significance, "inner");
            }
            
            /*
            2.1 Identify the dataset to be used for linking based on global map config
            */
            String dname;
            switch (analysis.global_map_config.columnOrderingScheme) {
                case GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING:
                    dname = dataset_names[0];
                    Utils.log_info("In GENE_GROUP_COLUMN_ORDERING, dataset name: " + dname);
                    break;
                case GlobalMapConfig.SIGNIFICANCE_COLUMN_ORDERING:
                    dname = analysis.global_map_config.significance_testing_params.getDataset();
                    Utils.log_info("In SIGNIFICANCE_COLUMN_ORDERING, dataset name: " + dname);
                    break;
                case GlobalMapConfig.HIERARCHICAL_COLUMN_ORDERING:
                    dname = analysis.global_map_config.col_clustering_params.getDatasetName();
                    Utils.log_info("In HIERARCHICAL_COLUMN_ORDERING, dataset name: " + dname);
                    break;
                default:
                    dname = dataset_names[0];
                    Utils.log_info("In default, dataset name: " + dname);
                    break;
            }
            
            /*
            2.2 Compute feature ordering using dname and get the ordering.
                The returned ordering has "_linker" and "_index" columns
                additionally it can have "_significance" and "_fdr" columns for
                significance based ordering. This function does not apply the 
                index on the dataset(dname. So reorderFeatures must be called 
                on all datasets including dname
                
                Additionally, the returned feature_order may not contain all 
                linker values (in case of hierarchical clustering and significance), 
                therefore when setOrdering on feature index, set_missing_indices
                should be set to True
            */
            Table feature_order = this.views.get(dname).recomputeFeatureOrdering(analysis, significance_linker);
            //Utils.log_info("Selection: feature_order");
            //feature_order.show();
            
            /*
            2.3 Apply same ordering to the all linked datasets
                by propagating _index to all views
            */
            for (String name : dataset_names) {
                if (is_linked.get(name)) {
                    this.views.get(name).reorderFeatures(analysis, feature_order, false);
                    processed_datasets.put(name, Boolean.TRUE);
                }
            }
            
        }
        
        /*
        3. Independently compute ordering for unlinked datasets
        */
        for (String name : dataset_names) {
            if (!processed_datasets.containsKey(name)) {
                Utils.log_info("Calling recomputeFeatureOrdering() for " + name);
                Table feature_order = this.views.get(name).recomputeFeatureOrdering(analysis, null);
                //Utils.log_info("Selection: feature_order");
                //feature_order.show();
                this.views.get(name).reorderFeatures(analysis, feature_order, true);
            }
        }

        /*
        4.  Finalizes manifest: constructs a sorted list of "_index" column 
            values for pagination
        
        for (String name : dataset_names) {
            this.views.get(name).finalizeManifest();
        }
        */
    }
    
    public void updateDatasetLinkages(AnalysisContainer analysis) 
            throws MultiSlideException, DataParsingException {
        /*
        Get the new linking states
        */
        HashMap <String, Boolean> is_linked = analysis.global_map_config.getDatasetLinkages();
        
        /*
        Check if there are at least two linked datasets
        */
        int linked_count = 0;
        for (String name : dataset_names) {
            if (is_linked.get(name))
                linked_count++;
        }
        
        if (linked_count > 1) {
            /*
            changing linked datasets changes consolidated entrez list
            so re-creates consolidated_linker and re-align remaining linked views
            */
            this.alignViews(analysis, is_linked);
        }
        
        /*
        the newly unlined datasets needs new feature ordering and since the linked views have changed
        re-compute feature ordering
        */
        this.initializeFeatureOrdering(analysis, analysis.global_map_config.getDatasetLinkages());
        
    }
    
    public void updateDatasetAggregation(String dataset_name, AnalysisContainer analysis) 
            throws MultiSlideException, DataParsingException {
        
        Utils.log_info("updateDatasetAggregation called");
        
        /*
            Get the new linking states
        */
        HashMap <String, Boolean> is_linked = analysis.global_map_config.getDatasetLinkages();
        
        /*
            aggregation is possible only if the dataset has a linker
        */
        if (analysis.data.datasets.get(dataset_name).specs.has_linker) {
            
            /*
                Change the view for the corresponding dataset
            */
            MapConfig mc = analysis.heatmaps.get(dataset_name).getMapConfig();
            if (mc.show_aggregated) {
                this.views.get(dataset_name).aggregate(mc.aggregate_function);
            } else {
                this.views.get(dataset_name).disaggregate();
            }
            
            /*
                the newly aggregated/disaggregated dataset needs new feature ordering 
                while this might affect only one dataset (in case of independent views) 
                or multiple datasets (in case of linked views), for the sake of 
                implementation simplicity we re-compute feature ordering for all datasets
            */
            this.alignViews(analysis, is_linked);
            this.initializeFeatureOrdering(analysis, analysis.global_map_config.getDatasetLinkages());

            /*
                bins are assigned in GetHeatmap
            */
        }
        
    }
    
    public void generateGeneGroupColors(ColorPalette gene_group_color_palette) {
        
        int min = 0;
        int max = 255;
        Random r = new Random();
        
        ArrayList <short[]> gene_group_colors = gene_group_color_palette.palette;
        
        int i = 0;
        for (GeneGroup gene_group : this.gene_groups.values()) {
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

    }
    
    /*
        Gets
    */
    
    public double[] range(String dataset_name) {
        return this.views.get(dataset_name).range();
    }
    
    /*
    public long getEntrezCounts(String dataset_name, String entrez) throws MultiSlideException {
        return this.views.get(dataset_name).getEntrezCounts(entrez);
    }
    */
    
    public HashMap <String, Dataset<Row>> getDataForEnrichmentAnalysis(AnalysisContainer analysis, String dataset_name, String phenotype_name) {
        return this
                .views
                .get(dataset_name)
                .getDataForEnrichmentAnalysis(
                        analysis,
                        phenotype_name, 
                        this.phenotypes.select("_Sample_IDs", phenotype_name));
    }
    
    public String[] getDatasetNames() {
        return dataset_names;
    }
    
    public int getNumDatasets() {
        return nDatasets;
    }
    
    public int getNumSamples() {
        return (int)this.sample_ids.count();
    }
    
    /*
    Only used when creating heatmaps
    */
    public int getNumFeatures(String dataset_name, GlobalMapConfig global_map_config) {
        // returns number of features after gene_filtering
        return Math.toIntExact(this.views.get(dataset_name).getNumFeatures(global_map_config));
    }
    
    public List <List<String>> getFeatureIDs(String dataset_name, GlobalMapConfig global_map_config, String[] identifiers) {
        return this.views.get(dataset_name).getFeatureIDs(global_map_config, identifiers);
    }
    
    public List<Integer> getClusterLabels(String dataset_name, GlobalMapConfig global_map_config) {
        return this.views.get(dataset_name).getClusterLabels(global_map_config);
    }
    
    public boolean hasClusterLabels(String dataset_name) {
        return this.views.get(dataset_name).cluster_labels != null;
    }
    
    public boolean hasWithinLinkerOrdering(String dataset_name) {
        return this.views.get(dataset_name).hasWithinLinkerOrdering();
    }
    
    public List <String> getEntrez(String dataset_name, GlobalMapConfig global_map_config) throws MultiSlideException {
        return this.views.get(dataset_name).getEntrez(global_map_config);
    }
    
    public List <List <Integer>> getGeneTags(String dataset_name, GlobalMapConfig global_map_config) {
        
        /*
        HashMap <String, Boolean> dataset_linkings = global_map_config.getDatasetLinkages();
        
        if (dataset_linkings.get(dataset_name)) {
            return View.getGeneTags(this.consolidated_linker, this.feature_manifest, global_map_config, this.gene_groups);
        } else {
            return this.views.get(dataset_name).getGeneTags(global_map_config, this.gene_groups);
        }
        */
        return this.views.get(dataset_name).getGeneTags(global_map_config, this.gene_groups);
    }
    
    public List <List <Integer>> getSearchTags(String dataset_name, GlobalMapConfig global_map_config) {
        
        /*
        HashMap <String, Boolean> dataset_linkings = global_map_config.getDatasetLinkages();
        
        if (dataset_linkings.get(dataset_name)) {
            return View.getSearchTags(this.consolidated_linker, feature_manifest, global_map_config, this.gene_groups);
        } else {
            return this.views.get(dataset_name).getSearchTags(global_map_config, this.nn_gene_groups);
        }
        */
        return this.views.get(dataset_name).getSearchTags(global_map_config, this.nn_gene_groups);
    }
    
    public int[][] getExpressionBins(String dataset_name, GlobalMapConfig global_map_config, int nBins) throws MultiSlideException {
        return this.views.get(dataset_name).getExpressionBins(global_map_config, nBins);
    }
    
    public String[] getSampleIDs(String dataset_name, GlobalMapConfig global_map_config) {
        return sample_ids.asStringArray();
    }
    
    public String[] getPhenotypeValues(String phenotype, GlobalMapConfig global_map_config) {
        
        List <String> selected_samples = sample_ids.asList();
        
        /*
        Utils.log_info("getPhenotypeValues() called for: " + phenotype);
        for (String sample_id: selected_samples)
            Utils.log_info(sample_id);
        Utils.log_dataset(phenotypes, 0, "info");
        */
        
        List <Row> p = this.phenotypes
                           .select(col(phenotype))
                           .filter(col("_Sample_IDs").isInCollection(selected_samples))
                           .orderBy("_index")
                           .collectAsList();
        
        return CollectionUtils.columnAsArray(p);
    }
    
    /*
    public float[][] getExpressions(String dataset_name, GlobalMapConfig global_map_config) {
        return this.views.get(dataset_name).getExpressions(global_map_config);
    }
    */
    
    public HashMap <String, GeneGroup> getGeneGroups () {
        return this.gene_groups;
    }
    
    public ArrayList <String> getGeneGroup(String grp_key) throws MultiSlideException {
        // return the GeneGroup where grp_key = gene_group.name + "_" + gene_group.type
        if (this.gene_groups.containsKey(grp_key)) {
            return this.gene_groups.get(grp_key).entrez_list;
        } else {
            throw new MultiSlideException("Exception in getGeneGroup() of FilteredSortedData.java: unknown gene group " + grp_key);
        }
    }
    
    /*
    public void setBinNumbers(AnalysisContainer analysis) {
        for (String dataset_name : dataset_names) {
            this.views.get(dataset_name).setBinNumbers(
                    analysis.spark_session, analysis.heatmaps.get(dataset_name).hist);
        }
    }
    */
    
}
