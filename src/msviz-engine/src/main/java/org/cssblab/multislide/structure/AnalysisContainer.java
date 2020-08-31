/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.spark.sql.SparkSession;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.algorithms.statistics.EnrichmentAnalysis;
import org.cssblab.multislide.algorithms.statistics.SignificanceTester;
import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.resources.MultiSlideContextListener;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.structure.data.Data;
import org.cssblab.multislide.utils.HttpClientManager;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class AnalysisContainer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /*
        state variables (created at init/load from context)
    */
    public String base_path;
    public String analytics_server_address;
    
    /*
        user specified state variables (loaded from state in load analysis)
    */
    public String analysis_name;
    public String species;
    
    /*
        state classes (loaded from state in load analysis)
    */
    public DataSelectionState data_selection_state;
    public GlobalMapConfig global_map_config;
    
    public Lists lists;
    
    /*
        transient classes (created at init/load based on state)
    */
    public Data data;
    public Map <String, Heatmap> heatmaps;
    
    /*
        stateless objects (created at init/load)
    */
    public HttpClientManager analytics_engine_comm;
    public Searcher searcher;
    public HierarchicalClusterer clusterer;
    public SignificanceTester significance_tester;
    public EnrichmentAnalysis enrichment_analyzer;
    
    public SparkSession spark_session;
    
    
    public AnalysisContainer (String analysis_name, String species) throws MultiSlideException { 
        this.analysis_name = analysis_name;
        this.species = species;
        this.data_selection_state = new DataSelectionState();
        this.global_map_config = new GlobalMapConfig();
        this.lists = new Lists();
        this.heatmaps = new HashMap <>();
        /*
        this.spark_session = SparkSession.builder()
                                         .appName("MultiSLIDE")
                                         .config("key", "value")
                                         .master("local[*]")
                                         .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                                         .config("spark.executor.memory", "4g")
                                         .config("spark.kryo.unsafe", "true")
                                         .config("spark.broadcast.compress", "false")
                                         .config("spark.default.parallelism", MultiSlideContextListener.N_SPARK_PARTITIONS + "")
                                         .config("spark.sql.shuffle.partitions", MultiSlideContextListener.N_SPARK_PARTITIONS + "")
                                         .config("spark.sql.inMemoryColumnarStorage.compressed","false")
                                         .getOrCreate();
        //this.spark_session.conf().set("spark.executor.memory", "10g");
        this.spark_session.sparkContext().setLogLevel("OFF");
        */
    }
    
    public GlobalMapConfig getGlobalMapConfig() {
        return this.global_map_config;
    }
    
    public HashMap <String,MapConfig> getMapConfigs() {
        HashMap <String,MapConfig> map_configs = new HashMap <> ();
        for (String name:this.heatmaps.keySet())
            map_configs.put(name, this.heatmaps.get(name).getMapConfig());
        return map_configs;
    }
    
    public void setBasePath (String base_path) {
        this.base_path = base_path;
    }
    
    public void setAnalyticsServer (String analytics_server_address) {
        this.analytics_server_address = analytics_server_address;
        this.analytics_engine_comm = new HttpClientManager(this.analytics_server_address);
    }
    
    public void setSparkSession(SparkSession spark_session) {
        this.spark_session = spark_session;
    }
    
    public void setDatabase(Data data) {
        this.data = data;
    }
    
    public void setSearcher(Searcher searcher) {
        this.searcher = searcher;
    }
    
    public boolean selectedGroupsChanged (ArrayList <String> new_group_ids, ArrayList <String> new_group_types) {
        return false;
    }
    
    public void setRowClusteringParams(ClusteringParams row_clustering_params) {
        this.global_map_config.row_clustering_params = row_clustering_params;
    }

    public void setColClusteringParams(ClusteringParams col_clustering_params) {
        this.global_map_config.col_clustering_params = col_clustering_params;
    }
    
    public ClusteringParams getClusteringParams(int type) throws MultiSlideException {
        switch (type) {
            case ClusteringParams.TYPE_SAMPLE_CLUSTERING:
                return this.global_map_config.row_clustering_params;
            case ClusteringParams.TYPE_FEATURE_CLUSTERING:
                return this.global_map_config.col_clustering_params;
            default:
                throw new MultiSlideException("Illegal argument for clustering type");
        }
    }

    public HierarchicalClusterer getClusterer() {
        return clusterer;
    }

    public void setClusterer(HierarchicalClusterer clusterer) {
        this.clusterer = clusterer;
    }
    
    public SignificanceTester getSignificanceTester() {
        return this.significance_tester;
    }

    public void setSignificanceTester(SignificanceTester significance_tester) {
        this.significance_tester = significance_tester;
    }
    
    public EnrichmentAnalysis getEnrichmentAnalyzer() {
        return this.enrichment_analyzer;
    }

    public void setEnrichmentAnalyzer(EnrichmentAnalysis enrichment_analyzer) {
        this.enrichment_analyzer = enrichment_analyzer;
    }
    
    public ArrayList<ArrayList<Integer>> getSearchTags() {
        ArrayList<ArrayList<Integer>> search_tags = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> search_tags_0 = new ArrayList<Integer>();
        search_tags_0.add(4);
        search_tags_0.add(5);
        search_tags_0.add(6);
        search_tags_0.add(7);
        ArrayList<Integer> search_tags_1 = new ArrayList<Integer>();
        search_tags_1.add(6);
        search_tags_1.add(7);
        search_tags_1.add(8);
        search_tags_1.add(9);
        search_tags_1.add(10);
        ArrayList<Integer> search_tags_2 = new ArrayList<Integer>();
        search_tags_2.add(18);
        search_tags_2.add(19);
        search_tags_2.add(20);
        search_tags_2.add(21);
        search_tags_2.add(22);
        search_tags.add(search_tags_0);
        search_tags.add(search_tags_1);
        search_tags.add(search_tags_2);
        return search_tags;
    }
    
    public void clearCaches() {
        this.clusterer.clearCache();
        this.significance_tester.clearCache();
    }
    
    public static HashMap <Integer, String> createIdentifierNameMap() {
        HashMap <Integer, String> identifier_name_map = new HashMap <> ();
        identifier_name_map.put(0, "entrez_2021158607524066");
        identifier_name_map.put(1, "genesymbol_2021158607524066");
        identifier_name_map.put(2, "refseq_2021158607524066");
        identifier_name_map.put(3, "ensembl_gene_id_2021158607524066");
        identifier_name_map.put(4, "ensembl_transcript_id_2021158607524066");
        identifier_name_map.put(5, "ensembl_protein_id_2021158607524066");
        identifier_name_map.put(6, "uniprot_id_2021158607524066");
        identifier_name_map.put(7, "mirna_id_2021158607524066");
        return identifier_name_map;
    }
    
    public static HashMap <String, Integer> createIdentifierIndexMap() {
        HashMap <String, Integer> identifier_index_map  = new HashMap <> ();
        identifier_index_map.put("entrez_2021158607524066", 0);
        identifier_index_map.put("genesymbol_2021158607524066", 1);
        identifier_index_map.put("refseq_2021158607524066", 2);
        identifier_index_map.put("ensembl_gene_id_2021158607524066", 3);
        identifier_index_map.put("ensembl_transcript_id_2021158607524066", 4);
        identifier_index_map.put("ensembl_protein_id_2021158607524066", 5);
        identifier_index_map.put("uniprot_id_2021158607524066", 6);
        identifier_index_map.put("mirna_id_2021158607524066", 7);
        return identifier_index_map;
    }
    
    /*
    public HashMap <String, Boolean> getDatasetLinkages() {
        
        HashMap <String, Boolean> are_linked = new HashMap <> ();
        
        if (global_map_config.isDatasetLinkingOn) {
            /*
            Try to find linked datasets only is isDatasetLinkingOn is True
            
            HashMap<String, MapConfig> map_configs = this.getMapConfigs();
            for (String name:data.dataset_names) {
                MapConfig mc = map_configs.get(name);
                if (mc != null) {
                    /*
                    If map_configs have already been created, this is a createSelection 
                    call post analysis creation, likely when user changed linkings
                    or aggregation, so try to get user provided settings
                    
                    Utils.log_info((new Gson().toJson(mc)));
                    are_linked.put(name, map_configs.get(name).isJoint());
                } else {
                    /*
                    If map_configs don't exist, this is a createSelection call
                    during analysis initialization, so fall back on defaults
                    
                    if (data.datasets.get(name).specs.has_linker) {
                        are_linked.put(name, Boolean.TRUE);
                    } else {
                        are_linked.put(name, Boolean.FALSE);
                    }
                }
            }
        } else {
            /*
            if isDatasetLinkingOn is False, all datasets are unlinked 
            under all circumstances
            
            for (String name:data.dataset_names) {
                are_linked.put(name, Boolean.FALSE);
            }
        }
        
        Utils.log_info("isDatasetLinkingOn: " + global_map_config.isDatasetLinkingOn);
        for (String s: are_linked.keySet())
            Utils.log_info(s + " link state = " + are_linked.get(s));
        
        return are_linked;
    }
    */
    
    /*
    public void addDataset (DatasetProperties dataset) {
        datasets.add(dataset);
        
    }
    */
    
}
