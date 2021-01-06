/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.count;
import static org.apache.spark.sql.functions.lit;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.EnrichmentAnalysisResult;
import org.cssblab.multislide.beans.data.FunctionalGroupContainer;
import org.cssblab.multislide.beans.data.SearchResultSummary;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.searcher.GeneObject;
import org.cssblab.multislide.searcher.GoObject;
import org.cssblab.multislide.searcher.PathwayObject;
import org.cssblab.multislide.searcher.SearchResultObject;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.DataSelectionState;
import org.cssblab.multislide.structure.GeneGroup;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.NetworkNeighbor;
import org.cssblab.multislide.structure.data.table.Table;
import org.cssblab.multislide.utils.CollectionUtils;
import org.cssblab.multislide.utils.FormElementMapper;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author Soumita Ghosh and Abhik Datta
 */
public class Data implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public int nDatasets;
    public ClinicalInformation clinical_info;
    public String[] dataset_names;
    public ListOrderedMap <String, DataFrame> datasets;
    //public String[] sample_ids;
    public Dataset <Row> entrez_master;
    public HashMap <String, Integer> entrez_master_as_map;
    public Selection selected;
    
    public Data (
            SparkSession spark_session,
            String analysis_basepath, 
            List <DatasetSpecs> dataset_specs_map,
            ColorPalette categorical_palette, 
            ColorPalette continuous_palette,
            Searcher searcher
    ) throws MultiSlideException, DataParsingException, IOException {
        
        this.clinical_info = new ClinicalInformation(categorical_palette, continuous_palette);
        for(DatasetSpecs spec: dataset_specs_map) {
            if (spec.getOmicsType().equals("clinical-info")) {
                String filepath = analysis_basepath + File.separator + spec.getFilenameWithinAnalysisFolder();
                clinical_info.loadClinicalInformation(
                        spark_session, filepath, FormElementMapper.parseDelimiter(spec.getDelimiter())
                );
            }
        }
        //sample_ids = clinical_info.getSampleIDs();
        
        this.datasets = new ListOrderedMap <> ();
        ArrayList <String> dataset_names_list = new ArrayList <> ();
        long seed = 0;
        for(DatasetSpecs spec: dataset_specs_map) {
            if (!spec.getOmicsType().equals("clinical-info")) {
                DataFrame data_frame = new DataFrame(
                        spark_session, analysis_basepath, spec, clinical_info, searcher, seed);
                this.datasets.put(data_frame.name, data_frame);
                dataset_names_list.add(data_frame.name);
                seed += data_frame.linker_count;
            }
        }
        this.dataset_names = CollectionUtils.asArray(dataset_names_list);
        this.nDatasets = this.dataset_names.length;
        
        /*
        If linker is present, create entrez master
        */
        List <StructField> f = new ArrayList<>();
        f.add(DataTypes.createStructField("_entrez", DataTypes.StringType, false));
        StructType entrez_map_schema = DataTypes.createStructType(f);
        List <Row> rows = new ArrayList<>();
        
        
        for (String name : dataset_names) {
            HashMap <String, Integer> entrez_master_i = this.datasets.get(name).getEntrezs();
            for (String e: entrez_master_i.keySet()) {
                Object[] d = new Object[1];
                d[0] = e;
                rows.add(RowFactory.create(d));
            }
        }
        this.entrez_master = spark_session.createDataFrame(rows, entrez_map_schema);
        
        if (this.entrez_master != null) {
            this.entrez_master = this.entrez_master.distinct();
        }
        this.entrez_master_as_map = new HashMap <> ();
        List <Row> e = Data.aggregate(this.entrez_master, new String[]{"_entrez"}, new String[]{"_entrez"}, "count")
                           .collectAsList();
        for (Row r:e) {
            this.entrez_master_as_map.put(r.getString(0), Math.toIntExact(r.getLong(1)));
        }
    }
    
    public void createSelection (
            AnalysisContainer analysis,
            ColorPalette gene_group_color_palette
    ) throws MultiSlideException, DataParsingException {
        
        /*
        Consolidate user selection from "Add Genes"
        */
        HashMap <String, GeneGroup> gene_groups = new HashMap <>();
        Table a = getUserFeatureSelection(analysis, gene_groups);
        //entrez_dataset.show();
        
        /*
        Consolidate user selection from "Add Network Neighbors"
        */
        ListOrderedMap <String, GeneGroup> nn_gene_groups = new ListOrderedMap <>();
        Table b = getUserNetworkNeighborSelection(analysis, nn_gene_groups);
        //nn_entrez_dataset.show();
        
        /*
        Put "Add Genes" and "Add Network Neighbors" selections together
        null is returned if add_genes_source_type is "Upload"
        */
        Table ab;
        if (b != null) {
            ab = a.joinByKey(b, "outer");
        } else {
            ab = a;
        }

        /*
            create selection: join between user selection and datasets
            and order features and samples based on global map config
        */
        this.selected = new Selection(
                analysis,
                analysis.data_selection_state.selected_datasets,
                ab,
                gene_groups,
                nn_gene_groups,
                datasets,
                clinical_info,
                analysis.global_map_config.getDatasetLinkages()
        );
        
        this.selected.generateGeneGroupColors(gene_group_color_palette);    // this really should be somewhere else
        
    }
    
    private Table getUserFeatureSelection(
            AnalysisContainer analysis, 
            HashMap <String, GeneGroup> gene_groups
    ) throws MultiSlideException, DataParsingException {
        
        /*
        Data selected from search: 
        for now, assume search is only db search (no text search), therefore assume a linker and therefore _entrez column is present in the data
        to include text based metadata search (in addition to upload), another category (similar to upload) can be added
        */
        if (analysis.data_selection_state.add_genes_source_type == DataSelectionState.ADD_GENES_SOURCE_TYPE_SEARCH) {
            
            /*
             use hashmap first to ensure there aren't multiple instances of "gene_group_entrez", 
             as all individual genes have group_id "gene_group_entrez"
            */
            HashMap <String, Boolean> cnames = new HashMap <> ();
            for (SearchResultSummary search : analysis.data_selection_state.selected_searches) {
                
                GeneGroup gene_group = new GeneGroup(search.mapGeneGroupCodeToType(), search._id, search.display_tag);
                // column_names_dtypes.add(new String[]{gene_group.getID(), Table.DTYPE_INT});
                cnames.put(gene_group.getID(), Boolean.TRUE);
            }
            
            List <String[]> column_names_dtypes = new ArrayList <> ();
            for (String id: cnames.keySet()) {
                column_names_dtypes.add(new String[]{id, Table.DTYPE_INT});
            }
            
            Table table = new Table(column_names_dtypes, "_entrez");
            
            int i = 0;
            List <String> miRNA_Datasets = get_miRNA_Datasets();
            for (SearchResultSummary search : analysis.data_selection_state.selected_searches) {
                
                GeneGroup gene_group = new GeneGroup(search.mapGeneGroupCodeToType(), search._id, search.display_tag);
                gene_groups.put(gene_group.getID(), gene_group);
                
                List <String> entrezs = getGroupEntrez(analysis.searcher, search._id, search.mapGeneGroupCodeToType());
                for (String entrez : entrezs) {
                    table.set(gene_group.getID(), entrez, 1);
                }
                
                /*
                    Speacial handling of miRNA data
                */
                if (miRNA_Datasets.size() > 0) {
                    List <String> miRNA_ids = get_miRNA_GroupIDs(analysis.searcher, search._id, search.mapGeneGroupCodeToType());
                    for (String mi_RNA_id : miRNA_ids) {
                        String mirnaid = mi_RNA_id.toLowerCase();
                        for (String dataset_name: miRNA_Datasets) {
                            if (this.datasets.get(dataset_name).linker_entrez_map.containsKey(mirnaid)) {
                                List <String> entrez_list = this.datasets.get(dataset_name).linker_entrez_map.get(mirnaid);
                                for (String e : entrez_list)
                                    table.set(gene_group.getID(), e, 1);
                            }
                        }
                    }
                }
                
                i++;
            }

            /*
            List<StructField> fields = new ArrayList<>();
            fields.add(DataTypes.createStructField("_entrez", DataTypes.StringType, false));
            
            int i = 1;
            List <Row> entrez_list = new ArrayList<>();
            for (SearchResultSummary search : analysis.data_selection_state.selected_searches) {
                
                ArrayList <String> temp = getGroupEntrez(analysis.searcher, search._id, search.mapGeneGroupCodeToType());
                
                Utils.log_info("Entrez list for:" + search._id);
                
                for (String entrez : temp) {
                    Object[] d = new Object[analysis.data_selection_state.selected_searches.length+1];
                    d[0] = entrez;
                    d[i] = 1;
                    Row row = RowFactory.create(d);
                    entrez_list.add(row);
                    Utils.log_info(entrez);
                }
                
                GeneGroup gene_group = new GeneGroup(search.mapGeneGroupCodeToType(), search._id, search.display_tag);
                gene_groups.put(gene_group.getID(), gene_group);
                fields.add(DataTypes.createStructField(gene_group.getID(), DataTypes.IntegerType, true));
                Utils.log_info("gene_group.getID():" + gene_group.getID());
                i++;
            }
            
            StructType schema = DataTypes.createStructType(fields);
            Dataset <Row> entrez_dataset = analysis.spark_session.createDataFrame(entrez_list, schema)
                                                   .repartition(MultiSlideContextListener.N_SPARK_PARTITIONS).cache();
            */
            
            return table;
            
        /*
        Data selected using upload:
        do text based search for each uploaded pathway across each linker/identifier column and pick the column with the most hits as the match
        */
        } else if (analysis.data_selection_state.add_genes_source_type == DataSelectionState.ADD_GENES_SOURCE_TYPE_UPLOAD) {
            
            List <String[]> column_names_dtypes = new ArrayList <> ();
            
            for (FunctionalGroupContainer uploaded_group : analysis.data_selection_state.selected_functional_groups) {
                
                GeneGroup gene_group = new GeneGroup("user_defined", uploaded_group._id, uploaded_group.functional_grp_name);
                column_names_dtypes.add(new String[]{gene_group.getID(), Table.DTYPE_INT});
            }
            
            Table table = new Table(column_names_dtypes, "_entrez");
            
            int i = 0;
            for (FunctionalGroupContainer uploaded_group : analysis.data_selection_state.selected_functional_groups) {
                
                List <String> entrezs = new ArrayList <> ();
                
                for (List<String> k: uploaded_group.dataset_column_member_map.keySet()) {
                    List <String> entrezs_k = this.datasets.get(k.get(0))
                            .search_metadata(k.get(1),
                                    (ArrayList <String>)uploaded_group.dataset_column_member_map.get(k));
                    entrezs.addAll(entrezs_k);
                }
                
                GeneGroup gene_group = new GeneGroup("user_defined", uploaded_group._id, uploaded_group.functional_grp_name);
                gene_groups.put(gene_group.getID(), gene_group);
                
                for (String entrez : entrezs) {
                    table.set(gene_group.getID(), entrez, 1);
                }
                i++;
            }
            
            return table;
            
        /*
        For user selection through enrichment analysis
        Similar to search result based init
        */
        } else if (analysis.data_selection_state.add_genes_source_type == DataSelectionState.ADD_GENES_SOURCE_TYPE_ENRICHMENT) {
            
            List <String[]> column_names_dtypes = new ArrayList <> ();
            
            for (EnrichmentAnalysisResult grp : analysis.data_selection_state.selected_enriched_groups) {
                
                GeneGroup gene_group = new GeneGroup(grp.type, grp.pathid, grp.pathname);
                column_names_dtypes.add(new String[]{gene_group.getID(), Table.DTYPE_INT});
            }
            
            Table table = new Table(column_names_dtypes, "_entrez");
            
            int i = 0;
            for (EnrichmentAnalysisResult grp : analysis.data_selection_state.selected_enriched_groups) {
                
                List <String> entrezs = getGroupEntrez(analysis.searcher, grp.pathid, grp.type);
                GeneGroup gene_group = new GeneGroup(grp.type, grp.pathid, grp.pathname);
                gene_groups.put(gene_group.getID(), gene_group);
                
                for (String entrez : entrezs) {
                    table.set(gene_group.getID(), entrez, 1);
                }
                i++;
            }
            
            /*
            List<StructField> fields = new ArrayList<>();
            fields.add(DataTypes.createStructField("_entrez", DataTypes.StringType, false));
            
            int i = 1;
            List <Row> entrez_list = new ArrayList<>();
            for (EnrichmentAnalysisResult grp : analysis.data_selection_state.selected_enriched_groups) {
                
                ArrayList <String> temp = getGroupEntrez(analysis.searcher, grp.pathid, grp.type);
                for (String entrez : temp) {
                    Object[] d = new Object[analysis.data_selection_state.selected_enriched_groups.length+1];
                    d[0] = entrez;
                    d[i] = 1;
                    Row row = RowFactory.create(d);
                    entrez_list.add(row);
                }
                
                GeneGroup gene_group = new GeneGroup(grp.type, grp.pathid, grp.pathname);
                gene_groups.put(gene_group.getID(), gene_group);
                fields.add(DataTypes.createStructField(gene_group.getID(), DataTypes.IntegerType, true));
                i++;
            }
            
            StructType schema = DataTypes.createStructType(fields);
            Dataset <Row> entrez_dataset = analysis.spark_session.createDataFrame(entrez_list, schema);
            return entrez_dataset;
            */
            
            return table;
            
        } else {
            
            throw new MultiSlideException("Unable to collate user selected features. Unknown selection type.");
            
        }
        
    }
    
    private Table getUserNetworkNeighborSelection(
            AnalysisContainer analysis, 
            ListOrderedMap <String, GeneGroup> gene_groups
    ) throws MultiSlideException, DataParsingException {
        
        /*
        Add user selection from "Add Network Neighbors"
        Only applicable if linker column is available and therefore for "Search" and "Enrichment Analysis" types
        */
        if (analysis.data_selection_state.add_genes_source_type == DataSelectionState.ADD_GENES_SOURCE_TYPE_SEARCH
                || analysis.data_selection_state.add_genes_source_type == DataSelectionState.ADD_GENES_SOURCE_TYPE_ENRICHMENT) {
            
            List <String[]> column_names_dtypes = new ArrayList <> ();
            
            int i=0;
            for (String id:analysis.data_selection_state.network_neighbors.keySet()) {
                
                NetworkNeighbor NN = analysis.data_selection_state.network_neighbors.get(id);
                GeneGroup gene_group = new GeneGroup("nn_gene_group_entrez_" + i++, NN.getNetworkType(), NN.getQueryEntrez());
                gene_groups.put(gene_group.getID(), gene_group);
                column_names_dtypes.add(new String[]{gene_group.getID(), Table.DTYPE_INT});
            }
            
            Table table = new Table(column_names_dtypes, "_entrez");
            
            i = 0;
            for (String id:analysis.data_selection_state.network_neighbors.keySet()) {
                
                NetworkNeighbor NN = analysis.data_selection_state.network_neighbors.get(id);
                GeneGroup gene_group = new GeneGroup("nn_gene_group_entrez_" + i, NN.getNetworkType(), NN.getQueryEntrez());
                
                table.set(gene_group.getID(), NN.getQueryEntrez(), -1);
                
                List <String> temp = Arrays.asList(NN.getNeighborEntrezList());
                for (String entrez : temp) {
                    table.set(gene_group.getID(), entrez, 1);
                }

                i++;
            }
            
            
            /*
            List<StructField> fields = new ArrayList<>();
            fields.add(DataTypes.createStructField("_entrez", DataTypes.StringType, false));
            
            int i = 1;
            List <Row> entrez_list = new ArrayList<>();
            
            for (String id:analysis.data_selection_state.network_neighbors.keySet()) {
                
                NetworkNeighbor NN = analysis.data_selection_state.network_neighbors.get(id);
                
                /*
                Add the query gene
                
                Object[] d = new Object[analysis.data_selection_state.network_neighbors.size() + 1];
                d[0] = NN.getQueryEntrez();
                d[i] = 1;
                entrez_list.add(RowFactory.create(d));
                
                /*
                Add neighbor genes
                
                List <String> temp = Arrays.asList(NN.getNeighborEntrezList());
                for (String entrez : temp) {
                    d = new Object[analysis.data_selection_state.network_neighbors.size() + 1];
                    d[0] = entrez;
                    d[i] = 1;
                    entrez_list.add(RowFactory.create(d));
                }
                
                GeneGroup gene_group = new GeneGroup("nn_gene_group_entrez_" + i, NN.getNetworkType(), NN.getQueryEntrez());
                gene_groups.put(gene_group.getID(), gene_group);
                fields.add(DataTypes.createStructField(gene_group.getID(), DataTypes.IntegerType, true));
                
                i++;
            }
            
            StructType schema = DataTypes.createStructType(fields);
            Dataset <Row> entrez_dataset = analysis.spark_session.createDataFrame(entrez_list, schema)
                                                                 .repartition(MultiSlideContextListener.N_SPARK_PARTITIONS).cache();
            
            return entrez_dataset;
            */
            
            return table;
            
        } else {
            return null;
        }
        
    }
    
    /*
        group_id can be entrez, pathid or goid
        returns the group name (display name)
    */
    private List <String> getGroupEntrez(Searcher searcher, String group_id, String queryType) {
        
        List <String> entrez_list = new ArrayList <> ();
        
        if (queryType.equals("entrez") || queryType.equals("gene_group_entrez")) {
            ArrayList<GeneObject> genes = searcher.processGeneQuery(group_id, "exact", "entrez");            
            for (int i = 0; i < genes.size(); i++) {
                GeneObject gene = genes.get(i);
                entrez_list.add(gene.entrez_id);
            }
        } else if (queryType.equals("pathid")) {
            ArrayList<PathwayObject> part_paths = searcher.processPathQuery(group_id, "exact", queryType);
            for (int i = 0; i < part_paths.size(); i++) {
                PathwayObject path = part_paths.get(i);
                entrez_list.addAll(path.entrez_ids);
            }
        } else if (queryType.equals("goid")) {
            ArrayList<GoObject> part_go_terms = searcher.processGOQuery(group_id, "exact", queryType);
            for (int i = 0; i < part_go_terms.size(); i++) {
                GoObject go = part_go_terms.get(i);
                entrez_list.addAll(go.entrez_ids);
            }
        }
        
        return entrez_list;
    }
    
    private List <String> get_miRNA_GroupIDs(Searcher searcher, String group_id, String queryType) {
        
        List <String> entrez_list = new ArrayList <> ();
        
        if (queryType.equals("pathid")) {
            ArrayList<PathwayObject> part_paths = searcher.processmiRNAPathQuery(group_id, "exact", queryType);
            for (int i = 0; i < part_paths.size(); i++) {
                PathwayObject path = part_paths.get(i);
                entrez_list.addAll(path.entrez_ids);
            }
        } else if (queryType.equals("goid")) {
            ArrayList<GoObject> part_go_terms = searcher.processmiRNAGOQuery(group_id, "exact", queryType);
            for (int i = 0; i < part_go_terms.size(); i++) {
                GoObject go = part_go_terms.get(i);
                entrez_list.addAll(go.entrez_ids);
            }
        }
        
        return entrez_list;
    }
    
    private List<String> get_miRNA_Datasets() {
        List <String> d = new ArrayList <> ();
        for (DataFrame df : this.datasets.valueList()) {
            if (df.specs.has_mi_rna) {
                d.add(df.name);
            }
        }
        return d;
    }
    
    /*
    Searches all usable metadata columns (linker/identifier columns) in each dataset and returns the best matching column
    returns hashmap: key -> dataset_name; value -> Dataset <Row>. 
    Here the map values (Dataset <Row>) have a single column (the best matching column) with the matching rows
    
    private Dataset <Row> get_matching_metadata_columns (String dataset_name, String column_name, ArrayList <String> search_terms) {
        
        Dataset <Row> d = spark_session.emptyDataFrame();
        
        boolean isFirst = true;
        for (String dataset_name : dataset_names) {
            List <String> entrez = this.datasets.get(dataset_name).search_metadata(column_name, search_terms);
            List <String> entrez = dataset.search_metadata(search_terms);
            for (String colname:r.columns())
                r.withColumnRenamed(colname, dataset_name + "." + colname);
            if (isFirst) {
                d = r;
            } else {
                d = d.join(r, "outer");
            }
        }
        
        return d;
    }
    */
    
    /*
    Public Gets
    */
    
    public static Dataset <Row> aggregate(
            Dataset <Row> d, 
            String[] select_columns, 
            String[] aggregate_by, 
            String aggregate_func
    ) throws MultiSlideException {
        
        Column[] agg_by = CollectionUtils.asColumnArray(aggregate_by);
        Column[] selected_columns = CollectionUtils.asColumnArray(select_columns);
        
        Dataset <Row> id_agg;
        if (aggregate_func.equalsIgnoreCase("count")) {
            
            id_agg = d.select(selected_columns)
                      .groupBy(agg_by)
                      .agg(count(lit(1))
                            .alias("counts"));
            
        } else if (aggregate_func.equalsIgnoreCase("max")) {
            
            id_agg = d.select(selected_columns)
                      .groupBy(agg_by)
                      .max();
            
        } else if (aggregate_func.equalsIgnoreCase("mean")) {
            id_agg = d.groupBy(col("_entrez")).mean();
            
        } else {
            throw new MultiSlideException("Aggreagte function not implemented");
        }
        
        return id_agg;
        
    }
    
    public int getEntrezCounts(String dataset_name, String entrez) throws MultiSlideException {
        return this.datasets.get(dataset_name).getEntrezCounts(entrez);
    }
    
    public String getDefaultIdentifier(String dataset_name) {
        return this.datasets.get(dataset_name).getDefaultIdentifier();
    }
    
    public Table getDataForEnrichmentAnalysis(String dataset_name) throws MultiSlideException, DataParsingException {
        return this.datasets
                .get(dataset_name)
                .getDataForEnrichmentAnalysis();
    }
    
    /*
    Public metadata search functions
    */
    public ArrayList <SearchResultObject> processMetaColQuery(String query) throws MultiSlideException{
        if(query.contains(":")) { // wildcard
            
            String[] parts = query.split(":", -1); 
            String keyword = parts[0].trim().toUpperCase();
            keyword = keyword.substring(1, keyword.length()-1);
            String search_type = "contains";
            String search_term = parts[1].trim().toUpperCase();
            return doMetadataColSearch(keyword, search_type, search_term);
            
        } else if(query.contains("=")) { //exact
            
            String[] parts = query.split("=", -1); 
            String keyword = parts[0].trim().toUpperCase();
            keyword = keyword.substring(1, keyword.length()-1);
            String search_type = "exact";
            String search_term = parts[1].trim().toUpperCase();
            return doMetadataColSearch(keyword, search_type, search_term);
            
        } else {
            throw new MultiSlideException("Metadata column search query must contain a keyword and either an '=' or a ':'");
        }
    }


    public ArrayList <SearchResultObject> doMetadataColSearch(String metadata_column_name, String search_type, String search_term) {
        
        ArrayList <SearchResultObject> current_search_results = new ArrayList <SearchResultObject>();
        
        /*
        HashMap <String, Boolean> selected_entrezs = new HashMap <String, Boolean> ();
        String entrez = "";
        
        if (search_type.equals("exact")) {
            StringTokenizer st = new StringTokenizer(search_term, ",");
            while (st.hasMoreTokens()) {
                String query = st.nextToken();
                query = query.trim().toUpperCase();
                for (Map.Entry <String, HashMap<String,String[]>> entry : values.entrySet()) {
                    String dataset_name = entry.getKey();
                    HashMap <Integer, String> positionEntrezMap = this.positionEntrezMaps.get(dataset_name);
                    String[] metadata_values = entry.getValue().get(metadata_column_name.toUpperCase());
                    for (int i=0; i<metadata_values.length; i++) {
                        entrez = positionEntrezMap.get(i);
                        if (!selected_entrezs.containsKey(entrez)) {
                            if (metadata_values[i].equals(query)) {
                                GeneObject GO = GeneObject.createMetacolGeneObject(entrez, metadata_values[i], metadata_column_name);
                                current_search_results.add(SearchResultObject.makeSearchResultObject(query, GO));
                                selected_entrezs.put(entrez, Boolean.TRUE);
                            }
                        }
                    }
                }
            }
        } else if (search_type.equals("contains")) {
            StringTokenizer st = new StringTokenizer(search_term, ",");
            while (st.hasMoreTokens()) {
                String query = st.nextToken();
                query = query.trim().toUpperCase();
                for (Map.Entry <String, HashMap<String,String[]>> entry : values.entrySet()) {
                    String dataset_name = entry.getKey();
                    HashMap <Integer, String> positionEntrezMap = this.positionEntrezMaps.get(dataset_name);
                    String[] metadata_values = entry.getValue().get(metadata_column_name.toUpperCase());
                    for (int i=0; i<metadata_values.length; i++) {
                        entrez = positionEntrezMap.get(i);
                        if (!selected_entrezs.containsKey(entrez)) {
                            if (metadata_values[i].contains(query)) {
                                GeneObject GO = GeneObject.createMetacolGeneObject(entrez, metadata_values[i], metadata_column_name);
                                current_search_results.add(SearchResultObject.makeSearchResultObject(query, GO));
                                selected_entrezs.put(entrez, Boolean.TRUE);
                            }
                        }
                    }
                }
            }
        }
        */
        
        return current_search_results;
    }
    
    public static Dataset <Row> drop(Dataset <Row> d, List <String> cols_to_drop) {
        
        HashMap <String, Boolean> map = CollectionUtils.asMap(cols_to_drop);
        
        String[] names = d.columns();
        ArrayList <Column> cols = new ArrayList <> ();
        for (String name : names) {
            if (!map.containsKey(name)) {
                cols.add(col(name));
            }
        }
        
        Column[] cols_a = new Column[cols.size()];
        for (int i=0; i<cols.size(); i++) {
            cols_a[i] = cols.get(i);
        }
        
        return d.select(cols_a);
    }
    
    public static Dataset <Row> addIndex_1(SparkSession spark, Dataset <Row> dataset) {
        return Data.addIndex_1(spark, dataset, 0, "_index");
    }
    
    public static Dataset <Row> addIndex_1(SparkSession spark, Dataset <Row> df, int start, String index_name) {
        
        String _index_name;
        if (index_name == null || index_name.equals("")) {
            _index_name = "_index";
        } else {
            _index_name = index_name;
        }
        
        JavaPairRDD <Row, Long> rddzip = df.toJavaRDD().zipWithIndex();
        JavaRDD <Row> rdd = rddzip.map(s->{
            Row r = s._1;
            Object[] arr = new Object[r.size()+1];
            for (int i = 0; i < arr.length-1; i++) {
                arr[i] = r.get(i);
            }
            arr[arr.length-1] = start + s._2;
            return RowFactory.create(arr);
        });

        StructType newSchema = df.schema().add(new StructField(_index_name,
                DataTypes.LongType, false, Metadata.empty()));

        return spark.createDataFrame(rdd,newSchema);
        
        /*
        String[] col_names = dataset.columns();
        if (col_names.length > 1) {
            Utils.log_info("Warning: called addIndex on multi-column datasets (Num. Cols. = " 
                    + col_names.length + "). This can be slow if Num. Cols. is large");
        }
        
        List <Row> in_rows = dataset.collectAsList();
        List <Row> out_rows = new ArrayList<>();
        
        for (int r=0; r<in_rows.size(); r++) {
            Row row = in_rows.get(r);
            Object[] d = new Object[col_names.length+1];
            int i = 0;
            for (String c: col_names) {
                d[i] = row.get(i);
                i++;
            }
            d[i] = start + r;
            out_rows.add(RowFactory.create(d));
        }
        
        StructField[] in_fields = dataset.schema().fields();
        List<StructField> out_fields = new ArrayList<>();
        for (int i = 0; i < col_names.length; i++) {
            out_fields.add(in_fields[i]);
        }
        out_fields.add(DataTypes.createStructField("_index", DataTypes.IntegerType, false));

        return spark.createDataFrame(out_rows, DataTypes.createStructType(out_fields));
        */
        
    }
    
    /*
    aligns dataset to match the ordering in master
    linker_dataset and linker_master are the names of the linker columns in dataset and master
    master is assumed to already have _index column
    among rows with the same linker value, ordering will be the same as in dataset
    if dataset has fewer instances of a particular linker value than master, null rows are inserted
    a null row has null in all columns except, in the linker_dataset column and the columns specified in columns_to_copy
    values in columns specified in columns_to_copy are copied from linker_master to dataset
    */
    protected static Dataset <Row> align_1(
            SparkSession spark,
            Dataset <Row> dataset, String linker_dataset, 
            Dataset <Row> master, String linker_master,
            String[] columns_to_copy) {
        
        /*
        HashMap for fast lookup
        */
        HashMap <String, Boolean> c2c = CollectionUtils.asMap(columns_to_copy);
        
        /*
        Get missing linker values and create a new dataset
        */
        Dataset <Row> missing = master.select(linker_master)
                                      .exceptAll(dataset.select(linker_dataset))
                                      .withColumnRenamed(linker_master, linker_dataset);
        
        String[] cols = dataset.columns();
        for (String c: cols) {
            if (!(c.equals(linker_dataset) || c2c.containsKey(c)))
                missing = missing.withColumn(c, lit(null));
        }
        //missing.show();
        
        /*
        Rename master linker to dataset linker
        Order master by dataset linker
        and add a second index column _index_2
        */
        Dataset <Row> M = Data.addIndex_1(spark, master
                .withColumnRenamed(linker_master, linker_dataset)
                .orderBy(linker_dataset), 0, "_index2");
        
        /*
        Add missing values to Dataset and orderBy linker, so that both dataset and master have same number of rows and are aligned
        */
        Dataset <Row> D = dataset.unionByName(missing)
                                 .orderBy(linker_dataset);
        //D.show();

        /*
        Add _index_2 to dataset
        */
        Dataset <Row> dataset_with_index = addIndex_1(spark, D, 0, "_index2");
        
        /*
        Join dataset and master using _index_2
        Sort dataset by _index
        */
        List <String> c = new ArrayList <> ();
        c.add(linker_dataset);
        c.add("_index2");
        return dataset_with_index.join(M, CollectionUtils.asSeq(c))
                                 .drop("_index2");
    }
    
    
    
}


