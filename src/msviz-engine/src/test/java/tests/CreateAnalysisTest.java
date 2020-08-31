/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.spark.sql.SparkSession;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.algorithms.statistics.EnrichmentAnalysis;
import org.cssblab.multislide.algorithms.statistics.SignificanceTester;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.EnrichmentAnalysisResult;
import org.cssblab.multislide.beans.data.SearchResultSummary;
import org.cssblab.multislide.beans.layouts.MapLinkLayout;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.graphics.Heatmap;
import org.cssblab.multislide.graphics.PyColorMaps;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.ClusteringParams;
import org.cssblab.multislide.structure.DataSelectionState;
import org.cssblab.multislide.structure.GlobalMapConfig;
import org.cssblab.multislide.structure.MapConfig;
import org.cssblab.multislide.structure.NetworkNeighbor;
import org.cssblab.multislide.structure.PhenotypeSortingParams;
import org.cssblab.multislide.structure.data.Data;
import org.cssblab.multislide.utils.MultiSlideConfig;
import org.cssblab.multislide.utils.SessionManager;
import org.cssblab.multislide.utils.Utils;
import org.cssblab.multislide.beans.data.NeighborhoodSearchResults;
import org.cssblab.multislide.beans.data.SignificanceTestingParams;
import org.cssblab.multislide.searcher.network.NetworkSearchHandler;

/**
 *
 * @author abhikdatta
 */
public class CreateAnalysisTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        int N_SPARK_PARTITIONS = 1;
        
        String demo_id = "6404.15707627881";
        String analysis_name = "demo_2021158607524066_" + demo_id;
        String installPath = "/Users/soumitaghosh/Documents/GitHub/multi-slide";
        String session_id = "fc36f39dd6ff9dd390fec99bbf81";
        
        SparkSession spark = SparkSession.builder()
                                         .appName("MultiSLIDE")
                                         .config("key", "value")
                                         .master("local[*]")
                                         .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                                         .config("spark.executor.memory", "4g")
                                         .config("spark.kryo.unsafe", "true")
                                         .config("spark.broadcast.compress", "false")
                                         .config("spark.default.parallelism", N_SPARK_PARTITIONS + "")
                                         .config("spark.sql.shuffle.partitions", N_SPARK_PARTITIONS + "")
                                         .config("spark.sql.inMemoryColumnarStorage.compressed","false")
                                         .getOrCreate();
        //this.spark_session.conf().set("spark.executor.memory", "10g");
        spark.sparkContext().setLogLevel("OFF");
        
        String fullPath = installPath + File.separator + "config" + File.separator + "log4j_config.xml";
        try {
            ConfigurationSource source = new ConfigurationSource (new FileInputStream (fullPath));
            Configurator.initialize(null, source);
        } catch (Exception e) {
            Utils.log_exception(e,"Failed to initialize logging service.");
        }
        
        try {
            
            /*
            Create analysis
            */
            
            long startTime = System.nanoTime();
        
            // create session directory
            SessionManager.createSessionDir(installPath, session_id);
            String uploadFolder = SessionManager.getSessionDir(installPath, session_id);
            
            // log
            Logger logger = LogManager.getRootLogger();
            logger.info("Load Demo called");
            
            // create a new analysis
            AnalysisContainer analysis = new AnalysisContainer("demo_2021158607524066_" + demo_id, "human");
            
            // set base path for analysis
            analysis.setBasePath(SessionManager.getBasePath(installPath, session_id, analysis.analysis_name));
            
            // create analysis directory
            SessionManager.createAnalysisDirs(analysis);
            
            // set analytics_server for analysis
            analysis.setAnalyticsServer("http://127.0.0.1:5000");
            
            // add spark session
            analysis.setSparkSession(spark);
            
            List <DatasetSpecs> specs = new ArrayList <> ();
            
            // Add clinical info
            DatasetSpecs spec_0 = new DatasetSpecs(
                    "demo_2021158607524066_" + demo_id,
                    "Sample_info_SAMs.txt",
                    "tab", "clinical-info", uploadFolder);
            specs.add(spec_0);
            
            // Add dataset 1
            DatasetSpecs spec_2 = new DatasetSpecs(
                    "demo_2021158607524066_" + demo_id,
                    "noNorm_unique_logRatio_prot_median_centered.tsv",
                    "tab", "Protein", uploadFolder
            );
            spec_2.update(
                    new String[]{"Accession", "ProteinName", "GeneName"}, true, "Accession", "uniprot_id_2021158607524066", false, new String[]{}
            );
            specs.add(spec_2);
            
            // Add dataset 2
            DatasetSpecs spec_3 = new DatasetSpecs(
                    "demo" + demo_id,
                    "noNorm_unique_logRatio_phospho_median_centered.tsv",
                    "tab", "mRNA", uploadFolder
            );
            spec_3.update(new String[]{"accession", "res", "phospho_pos", "resPos"}, true, "accession", "uniprot_id_2021158607524066", true, new String[]{"res", "phospho_pos", "resPos"});
            specs.add(spec_3);
            
            /*
            // Add clinical info
            DatasetSpecs spec_0 = new DatasetSpecs(
                    "demo_2021158607524066_" + demo_id,
                    "BRCA_groupinfo_example_new.txt",
                    "tab", "clinical-info", uploadFolder);
            specs.add(spec_0);

            // Add dataset 1
            DatasetSpecs spec_2 = new DatasetSpecs(
                    "demo_2021158607524066_" + demo_id,
                    "PROT_imputedDat_rounded_73_filtered.txt",
                    "tab", "Protein", uploadFolder
            );
            spec_2.update(
                    new String[]{"genesym"}, true, "genesym", "genesymbol_2021158607524066", false, new String[]{}
            );
            specs.add(spec_2);

            // Add dataset 2
            DatasetSpecs spec_3 = new DatasetSpecs(
                    "demo" + demo_id,
                    "mRNA_imputedDat_centered_rounded_73_filtered.txt",
                    "tab", "mRNA", uploadFolder
            );
            spec_3.update(new String[]{"Genesym"}, true, "Genesym", "genesymbol_2021158607524066", false, new String[]{});
            specs.add(spec_3);
            
            DatasetSpecs spec_4 = new DatasetSpecs(
                    "demo" + demo_id,
                    "CPTAC_Phosphoproteome_centered_selected_metadata.txt",
                    "tab", "Phosphoproteome", uploadFolder
            );
            spec_4.update(new String[]{"unique_idenitifier", "variableSites", "sequence", "accession_number", "geneName"},
                    true, "geneName", "genesymbol_2021158607524066", true, new String[]{"unique_idenitifier"});
            specs.add(spec_4);
            */
            
            // create
            // move input file into analysis directory
            SessionManager.moveInputFilesToAnalysisDir_ForDemo(installPath + File.separator + "demo_data", analysis.base_path, specs);
            
            // add searcher (must be done before setting database)
            Searcher searcher = new Searcher(analysis.species);
            analysis.setSearcher(searcher);
            
            // create data
            ColorPalette categorical_palette = new ColorPalette(installPath + File.separator + "db" + File.separator + "categorical_color_palette.txt");
            ColorPalette continuous_palette = new ColorPalette(installPath + File.separator + "db" + File.separator + "continuous_color_palette.txt");
            
            Data database = new Data(
                    analysis.spark_session, analysis.base_path, specs, 
                    categorical_palette, continuous_palette, analysis.searcher);
            analysis.setDatabase(database);
            
            //initialize data_selection_state
            analysis.data_selection_state.setDefaultDatasetAndPhenotypes(database.dataset_names, database.clinical_info.getPhenotypeNames());
            analysis.global_map_config.setDefaultDatasetLinking(database.datasets);
            
            // load system configuration details
            HashMap <String, String> multislide_config = MultiSlideConfig.getMultiSlideConfig(installPath);
            
            // create clusterer and significance tester
            String py_module_path = multislide_config.get("py-module-path");
            String py_home = multislide_config.get("python-dir");
            String cache_path = installPath + File.separator + "temp" + File.separator + "cache";
            HierarchicalClusterer clusterer = new HierarchicalClusterer(cache_path, analysis.analytics_engine_comm);
            analysis.setClusterer(clusterer);
            
            // create significance tester
            SignificanceTester significance_tester = new SignificanceTester(cache_path, analysis.analytics_engine_comm);
            analysis.setSignificanceTester(significance_tester);
            
            // create enrichment analyzer
            EnrichmentAnalysis enrichment_analyzer = new EnrichmentAnalysis(cache_path, analysis.analytics_engine_comm);
            analysis.setEnrichmentAnalyzer(enrichment_analyzer);
            
            // Finally add analysis to session
            Utils.log_info("Analysis: " + analysis_name + " created in " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
            
            /*
            Analysis Reinitializer
            */
            
            startTime = System.nanoTime();
            /*
            Simulate Selection from Enrichment Analysis
            */
            analysis.data_selection_state.add_genes_source_type = DataSelectionState.ADD_GENES_SOURCE_TYPE_ENRICHMENT;
            EnrichmentAnalysisResult[] results = new EnrichmentAnalysisResult[1];
            //results[0] = new EnrichmentAnalysisResult("pathid", "hsa04915", "", 0, 0, 0, 0, 0.01);
            results[0] = new EnrichmentAnalysisResult("pathid", "hsa00562", "", 0, 0, 0, 0, 0.01);
            analysis.data_selection_state.selected_enriched_groups = results;
            
            
            /*
            Simulate Selection from Search
            
            analysis.data_selection_state.add_genes_source_type = DataSelectionState.ADD_GENES_SOURCE_TYPE_SEARCH;
            SearchResultSummary[] searches = new SearchResultSummary[1];
            searches[0] = new SearchResultSummary();
            searches[0].createSearchResultSummary(SearchResultSummary.TYPE_GENE_SUMMARY, "", "2099", 1, new int[]{1});
            analysis.data_selection_state.selected_searches = searches;
            */
            
            //analysis.data_selection_state.selected_phenotypes = new String[]{"BRCA_subtype"};
            analysis.data_selection_state.selected_phenotypes = new String[]{"race"};
                    
            // set defaults
            analysis.data_selection_state.clearNetworkNeighbors();
            /*
            Add Network Neighbors
            
            NetworkSearchHandler searchP = new NetworkSearchHandler();
            HashMap <String, ArrayList<String>> search_results = searchP.processQuery("tf_entrez=2099");
            
            NeighborhoodSearchResults nsr = new NeighborhoodSearchResults();
            nsr.makeNeighborhoodSearchResultObject(analysis.searcher, "genesym", 1, search_results, analysis.data.entrez_master_as_map);
            
            NetworkNeighbor nn = new NetworkNeighbor("Protein", "2099", NetworkNeighbor.NETWORK_TYPE_TF_ENTREZ, nsr.neighbor_entrez);
            analysis.data_selection_state.addNetworkNeighbor(nn);
            */
            /*
            NetworkNeighbor nn = new NetworkNeighbor("Protein", "15", NetworkNeighbor.NETWORK_TYPE_PPI_ENTREZ, new String[]{"805", "810", "51", "54"});
            analysis.data_selection_state.addNetworkNeighbor(nn);
            */
            
            analysis.setRowClusteringParams(
                    new ClusteringParams(
                            ClusteringParams.TYPE_SAMPLE_CLUSTERING,
                            analysis.data_selection_state.selected_datasets[0]
                    )
            );
            analysis.setColClusteringParams(
                    new ClusteringParams(
                            ClusteringParams.TYPE_FEATURE_CLUSTERING,
                            analysis.data_selection_state.selected_datasets[0]
                    )
            );
            analysis.global_map_config.resetSignificanceTestingParameters(
                    analysis.data_selection_state.selected_datasets,
                    analysis.data_selection_state.selected_phenotypes
            );
            analysis.global_map_config.setPhenotypeSortingParams(
                    new PhenotypeSortingParams(analysis.data_selection_state.selected_phenotypes)
            );
            analysis.global_map_config.setGeneFilteringOn(false);
            analysis.global_map_config.setColumnOrderingScheme(GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING);
            analysis.global_map_config.setSampleOrderingScheme(GlobalMapConfig.PHENOTYPE_SAMPLE_ORDERING);
            
            analysis.global_map_config.setDefaultRowIdentifier(analysis.data.datasets.get(analysis.data.dataset_names[0]).specs);
            analysis.clearCaches();
            
            // create selection
            ColorPalette gene_group_color_palette = new ColorPalette(installPath + File.separator + "db" + File.separator + "gene_group_color_palette.txt");
            analysis.data.createSelection(
                analysis, 
                gene_group_color_palette
            );
            
            Utils.log_info("Selection created in " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
            
            //Utils.log_dataset(analysis.data.selected.views.get("Protein").vu, 0, "info");
            //analysis.data.selected.views.get("mRNA").vu.show();
            
            startTime = System.nanoTime();
            
            // create heatmaps
            PyColorMaps colormaps = new PyColorMaps(installPath + File.separator + "db" + File.separator + "py_colormaps");
            
            Map<String, Heatmap> heatmaps = new ListOrderedMap<>();
            for (String dataset_name : analysis.data_selection_state.selected_datasets) {

                Heatmap heatmap;
                if (analysis.heatmaps.containsKey(dataset_name)) {
                    Heatmap reference_heatmap = analysis.heatmaps.get(dataset_name);
                    heatmap = new Heatmap(analysis.data.selected, dataset_name, reference_heatmap);
                } else {
                    heatmap = new Heatmap(
                            analysis, dataset_name, analysis.data.datasets.get(dataset_name).specs,
                            20, "data_bins", "SEISMIC", -1, 1);
                }
                heatmap.genColorData(colormaps);
                heatmap.assignBinsToRows(analysis.spark_session, analysis.data.selected);
                heatmaps.put(dataset_name, heatmap);

            }
            analysis.heatmaps = heatmaps;
            //analysis.data.selected.setBinNumbers(analysis);
            
            // Finally add analysis to session
            Utils.log_info("Heatmaps created in " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
            Utils.log_info("All set, ready to visulize. Woohoo");
            
            MapLinkLayout link_layout = new MapLinkLayout(
                    "Protein",
                    "mRNA",
                    analysis.data_selection_state.selected_phenotypes.length,
                    analysis.global_map_config.getMapResolution(),
                    analysis.global_map_config.getMapOrientation(),
                    analysis.data.selected.views.get("Protein").getFeatureManifest(analysis.global_map_config),
                    analysis.data.selected.views.get("mRNA").getFeatureManifest(analysis.global_map_config),
                    analysis
            );
            
            /*
            String json = link_layout.asJSON();
            Utils.log_info(json);
            */
            
            //analysis.data.selected.views.get("Protein").vu.show();
            //analysis.data.selected.views.get("mRNA").vu.show();
            
            /*
            Test Gets
            */
            startTime = System.nanoTime();
            String dataset_name = analysis.data.selected.dataset_names[0];
            analysis.data.selected.getDatasetNames();
            //analysis.data.selected.getDataForEnrichmentAnalysis(dataset_name, "BRCA_subtype");
            Utils.log_info("Num Datasets: " + analysis.data.selected.getNumDatasets());
            Utils.log_info("Num Samples: " + analysis.data.selected.getNumSamples());
            Utils.log_info("Num Features: " + analysis.data.selected.getNumFeatures(dataset_name, analysis.global_map_config));
            analysis.data.selected.getSampleIDs(dataset_name, analysis.getGlobalMapConfig());
            
            List <List<String>> a = analysis.data.selected.getFeatureIDs(dataset_name, analysis.getGlobalMapConfig(), new String[]{"genesym"});
            /*
            for (List <String> list: a)
                Utils.log_info(list.get(0));
            */
            Utils.log_info("getFeatureIDs() Shape: " + a.size() + "," + a.get(0).size());
            List <String> b = analysis.data.selected.getEntrez(dataset_name, analysis.getGlobalMapConfig());
            Utils.log_info("getEntrez() Length: " + b.size());
            analysis.data.selected.getGeneTags(dataset_name, analysis.getGlobalMapConfig());
            analysis.data.selected.getSearchTags(dataset_name, analysis.getGlobalMapConfig());
            analysis.data.selected.getSearchTags(dataset_name, analysis.getGlobalMapConfig());
            /*
            String[] p = analysis.data.selected.getPhenotypeValues("BRCA_subtype", analysis.getGlobalMapConfig());
            for (String phen: p)
                Utils.log_info(phen);
            */
            int[][] c = analysis.data.selected.getExpressionBins(dataset_name, analysis.getGlobalMapConfig(), heatmaps.get(dataset_name).hist.nBins);
            Utils.log_info("getExpressionBins() Shape: " + c.length + "," + c[0].length);
            analysis.data.selected.getGeneGroups();
            //analysis.data.selected.getGeneGroup("hsa04915_pathid");
            
            Utils.log_info("Get() time: " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
            Utils.log_info("All Get() Tests Successful!");
            
            /*
                Update selected phenotypes
            */
            analysis.data_selection_state.setSelectedPhenotypes(new String[]{"timepoints", "race", "bmi_scr"});
            analysis.data.selected.updateSelectedPhenotypes(analysis, analysis.data.clinical_info);
            
            /*
                Test reset heatmap with aggregation
            */
                /*
                Get the old values of show_aggregated and aggregate_function
                if these have changed, the view will have to be recreated
             */
            for (String dname: new String[]{"Protein","mRNA", "Phosphoproteome"}) {
                
                MapConfig mc = analysis.heatmaps.get(dname).getMapConfig();
                boolean prev_show_aggregated = mc.show_aggregated;
                String prev_agg_func = mc.aggregate_function;

                /*
                    Create a new heatmap and set it to analysis
                 */
                Heatmap heatmap = new Heatmap(
                        analysis,
                        dname,
                        analysis.data.datasets.get(dname).specs,
                        20, "data_bins", "SEISMIC", -1, 1
                );

                heatmap.getMapConfig().setSelectedFeatureIdentifiers(new String[]{"geneName"});
                heatmap.getMapConfig().setShowAggregated(true);
                heatmap.getMapConfig().setAggregateFunction("Mean");

                heatmap.genColorData(colormaps);
                analysis.heatmaps.put(dname, heatmap);

                /*
                        Now check if show_aggregated and aggregate_function have changed
                        if so, updated view
                 */
                if (heatmap.getMapConfig().show_aggregated != prev_show_aggregated
                        || !prev_agg_func.equalsIgnoreCase(heatmap.getMapConfig().aggregate_function)) {

                    analysis.data.selected.updateDatasetAggregation(dname, analysis);
                }

                /*
                        Finallym reset bin numbers
                 */
                heatmap.assignBinsToRows(analysis.spark_session, analysis.data.selected);
            }
            
            analysis.global_map_config.columnOrderingScheme = GlobalMapConfig.HIERARCHICAL_COLUMN_ORDERING;
            
            analysis.setColClusteringParams(new ClusteringParams(
                            ClusteringParams.TYPE_FEATURE_CLUSTERING, 
                            ClusteringParams.AVERAGE_LINKAGE, 
                            ClusteringParams.EUCLIDEAN_DISTANCE, 
                            ClusteringParams.OPTIMAL_LEAF_ORDER,
                            "mRNA"
                        ));
            
            analysis.global_map_config.setSignificanceTestingParameters(new SignificanceTestingParams(
                            "Protein", "BRCA_subtype", 0.001,
                            "Parametric", true, 0.2
                    ));
            
            analysis.global_map_config.isGeneFilteringOn = true;
            
            analysis.data.selected.recomputeFeatureOrdering(analysis);
            
            for (String dname: new String[]{"Protein","mRNA", "Phosphoproteome"}) {
                
                MapConfig mc = analysis.heatmaps.get(dname).getMapConfig();
                boolean prev_show_aggregated = mc.show_aggregated;
                String prev_agg_func = mc.aggregate_function;

                /*
                    Create a new heatmap and set it to analysis
                 */
                Heatmap heatmap = new Heatmap(
                        analysis,
                        dname,
                        analysis.data.datasets.get(dname).specs,
                        20, "data_bins", "SEISMIC", -1, 1
                );

                heatmap.getMapConfig().setSelectedFeatureIdentifiers(new String[]{"geneName"});
                heatmap.getMapConfig().setShowAggregated(true);
                heatmap.getMapConfig().setAggregateFunction("Mean");

                heatmap.genColorData(colormaps);
                analysis.heatmaps.put(dname, heatmap);

                /*
                        Now check if show_aggregated and aggregate_function have changed
                        if so, updated view
                 */
                if (heatmap.getMapConfig().show_aggregated != prev_show_aggregated
                        || !prev_agg_func.equalsIgnoreCase(heatmap.getMapConfig().aggregate_function)) {

                    analysis.data.selected.updateDatasetAggregation(dname, analysis);
                }

                /*
                        Finallym reset bin numbers
                 */
                heatmap.assignBinsToRows(analysis.spark_session, analysis.data.selected);
            }
            
        } catch (Exception e) {
            Utils.log_exception(e, "Test Failed");
            
        }
        
    }
    
}
