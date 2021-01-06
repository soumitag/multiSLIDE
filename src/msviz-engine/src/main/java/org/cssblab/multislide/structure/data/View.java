/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.count;
import static org.apache.spark.sql.functions.lit;
import static org.apache.spark.sql.functions.monotonically_increasing_id;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.FunctionalGroupContainer;
import org.cssblab.multislide.beans.data.SignificanceTestingParams;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.graphics.Histogram;
import org.cssblab.multislide.resources.MultiSlideContextListener;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.DataSelectionState;
import org.cssblab.multislide.structure.GeneGroup;
import org.cssblab.multislide.structure.GlobalMapConfig;
import org.cssblab.multislide.structure.MapConfig;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.data.table.Table;
import org.cssblab.multislide.utils.CollectionUtils;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author Soumita Ghosh and Abhik Datta
 */
public class View implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public final String name;
    private final DatasetSpecs specs;
    
    public Dataset<Row> vu;                    // columns -> sample ids, rows -> features, cell -> expression / bin number
    public Dataset<Row> disaggregated_vu;      // columns -> sample ids, rows -> features, cell -> expression / bin number
    public Dataset<Row> aggregated_vu;         // columns -> sample ids, rows -> features, cell -> expression / bin number
    
    // cluster_labels:
    // maps _id -> cluster_label, 
    // ideally this would be part of vu, 
    // however for simplicity of implementation we keep it seperate
    public HashMap <Long, Integer> cluster_labels;
    
    //String linker_colname;
    HashMap<String, Boolean> metadata_columns;         // all metadata columns
    HashMap<String, Boolean> identifier_columns;       // identifier columns (subset of metadata columns), includes linker
    HashMap<String, Boolean> system_columns;           // non-expression columns that are for internal use
    HashMap<String, Boolean> gene_group_columns;       // non-expression columns that are for internal use
    HashMap<String, Boolean> nn_gene_group_columns;    // non-expression columns that are for internal use

    /*
        Expression column names as HashMap for quick search 
        and an array of Columns for quick select
    */
    private SampleIndex expression_columns;
    private FeatureIndex feature_manifest;
    /*
    HashMap <String, Boolean> expression_column_names;
    Column[] expression_columns;
     */

    private String _last_used_agg_func;
    private final double[] min_max;
    private boolean bin_are_set;

    private final String _linker_colname;
    
    public View(
            String name,
            Table feature_selection, // Rows => selected entrez, columns => metadata_column_values
            DataFrame dataset,
            int add_genes_source_type,
            boolean is_joint,
            DatasetSpecs specs,
            HashMap<String, GeneGroup> gene_groups,
            ListOrderedMap<String, GeneGroup> nn_gene_groups,
            SparkSession spark_session,
            AnalysisContainer analysis
    ) throws MultiSlideException, DataParsingException {
        this.bin_are_set = false;

        this.name = name;
        this.specs = specs;
        
        this.system_columns = new HashMap<>();
        
        this._linker_colname = specs.getLinkerColname();
        
        // select rows (_entrez, metdata_columns...,expressions...) based on feature_selection
        
        if (add_genes_source_type == DataSelectionState.ADD_GENES_SOURCE_TYPE_SEARCH
                || add_genes_source_type == DataSelectionState.ADD_GENES_SOURCE_TYPE_ENRICHMENT 
                    || add_genes_source_type == DataSelectionState.ADD_GENES_SOURCE_TYPE_UPLOAD) {

            List <String[]> column_names_dtypes = CollectionUtils.zip(feature_selection.columns(), feature_selection.dtypes());
            column_names_dtypes.add(new String[]{"_entrez", Table.DTYPE_STRING});
            
            Table le_table = new Table(column_names_dtypes, _linker_colname);
            /*
                for each user selected entrez
            */
            for (String _entrez: feature_selection) {
                /*
                    find all possible matching linkers
                */
                if (dataset.entrez_linker_map.containsKey(_entrez)) {
                    List <String> _linkers = dataset.entrez_linker_map.get(_entrez);
                    for (String _l: _linkers) {
                        /*
                            add to table where key=linker to create a unqiue set of linker 
                            values along with the corresponding set of gene group information
                        */
                        le_table.set("_entrez", _l, _entrez);
                        for (String s: feature_selection.columns())
                            le_table.set(s, _l, feature_selection.getInt(_entrez, s));
                    }
                }
            }

            /*
            Convert to dataset
            */
            List <StructField> fields = new ArrayList<>();
            fields.add(DataTypes.createStructField(_linker_colname, DataTypes.StringType, false));
            
            List <String> column_names = le_table.columns();
            List <String> d_types = le_table.dtypes();
            for (int i=0; i<column_names.size(); i++) {
                if (d_types.get(i).equalsIgnoreCase(Table.DTYPE_INT))
                    fields.add(DataTypes.createStructField(column_names.get(i), DataTypes.IntegerType, false));
                else if  (d_types.get(i).equalsIgnoreCase(Table.DTYPE_STRING))
                    fields.add(DataTypes.createStructField(column_names.get(i), DataTypes.StringType, false));
            }
            
            StructType le_schema = DataTypes.createStructType(fields);
            
            List <Row> rows = new ArrayList <> ();
            for (String _linker: le_table) {
                rows.add(RowFactory.create(le_table.getRow(_linker, true)));
            }
            Dataset <Row> le = spark_session.createDataFrame(rows, le_schema).cache();
            
            /*
                for all functional grouos applicable to this dataset
                a map from functional_grp_name -> map(column_value -> true)
                for quick search
            */
            HashMap <String, HashMap <String, Boolean>> search_map = new HashMap <> ();
            if (add_genes_source_type == DataSelectionState.ADD_GENES_SOURCE_TYPE_UPLOAD) {
                for (FunctionalGroupContainer uploaded_group : analysis.data_selection_state.selected_functional_groups) {
                    for (List<String> k: uploaded_group.dataset_column_member_map.keySet()) {
                        if (this.name.equals(k.get(0))) {
                            HashMap <String, Boolean> f = new HashMap <> ();
                            for (String s: uploaded_group.dataset_column_member_map.get(k))
                                f.put(s, true);
                            search_map.put(k.get(1), f);
                        }
                    }
                }
            }
            
            /*
                select all rows from dataset.ds that match the unique set of linkers 
                in le_table
            */
            List <Row> selected_rows = new ArrayList <> ();
            for (String _linker: le_table) {
                List <Row> potentially_selected_rows = dataset.ds.get(_linker);
                /*
                    include an additional check for uploaded pathways to ensure
                    non-linker column conditions are satisfied
                */
                if (add_genes_source_type == DataSelectionState.ADD_GENES_SOURCE_TYPE_UPLOAD) {
                    
                    for (Row row: potentially_selected_rows) {
                        
                        boolean select_row = false;
                        
                        for (FunctionalGroupContainer uploaded_group : analysis.data_selection_state.selected_functional_groups) {
                            
                            for (List<String> k: uploaded_group.dataset_column_member_map.keySet()) {
                            
                                if (this.name.equals(k.get(0))) {

                                    int column_index = row.fieldIndex(k.get(1));
                                    String column_value = row.get(column_index).toString();

                                    if (search_map.containsKey(k.get(1))) {
                                        HashMap <String, Boolean> f = search_map.get(k.get(1));    
                                        if (f.containsKey(column_value))
                                            select_row = true;
                                    }
                                }
                            }
                        }
                        
                        if (select_row)
                            selected_rows.add(row);
                    }
                } else {
                    selected_rows.addAll(potentially_selected_rows);
                }
            }
            /*
            Create view
            */
            this.vu = spark_session.createDataFrame(selected_rows, dataset.df.schema());
            
            /*
            Utils.log_info("le");
            Utils.log_dataset(le, 0, "info");
            Utils.log_info("vu");
            Utils.log_dataset(vu, 0, "info");
            */
            
            /*
            Add _entrez and gene group information to vu
            */
            this.vu = this.vu.join(le, CollectionUtils.asSeq(_linker_colname), "inner");
            this.system_columns.put("_entrez", Boolean.TRUE);
            if (!this.specs.has_linker) {
                this.system_columns.put("__pseudo__linker__", Boolean.TRUE);
            }
            
            /*
            Utils.log_info("vu after join");
            Utils.log_dataset(vu, 0, "info");
            */
            le.unpersist(false);

            /*
            this.vu = dataset.ds.join(feature_selection, Data.asSeq("_entrez"));
            
            */
            
            //Utils.log_dataset(feature_selection, 0, "info");
            //Utils.log_dataset(le, 0, "info");

        }
        
        /*
        Add "_significance" and "_fdr" column
         */
        this.vu = this.vu.withColumn("_significance", lit(0.0)).withColumn("_fdr", lit(0.0));
        this.system_columns.put("_significance", Boolean.TRUE);
        this.system_columns.put("_fdr", Boolean.TRUE);

        /*
        Metadata and identifier columns
         */
        this.metadata_columns = CollectionUtils.asMap(dataset.specs.metadata_columns);
        this.identifier_columns = CollectionUtils.asMap(dataset.specs.getLinkerAndIdentifierColumnNames());

        /*
        Get expression column names: anything that is not a metadata column, not a system column, not gene group column, and not a network neighbor column
         */
        this.gene_group_columns = new HashMap<>();
        gene_groups.keySet().forEach((group_name) -> {
            this.gene_group_columns.put(group_name, Boolean.TRUE);
        });

        this.nn_gene_group_columns = new HashMap<>();
        nn_gene_groups.keySet().forEach((group_name) -> {
            this.nn_gene_group_columns.put(group_name, Boolean.TRUE);
        });

        HashMap<String, Boolean> t = new HashMap<>();
        for (String colname : this.vu.columns()) {
            if (!(this.metadata_columns.containsKey(colname)
                    || this.system_columns.containsKey(colname)
                    || this.gene_group_columns.containsKey(colname)
                    || this.nn_gene_group_columns.containsKey(colname))) {
                t.put(colname, Boolean.TRUE);
            }
        }
        this.expression_columns = new SampleIndex(t);

        /*
        initialize internal variables
        */

        min_max = new double[2];
        if (this.vu.count() > 0) {
            min_max[0] = Double.MAX_VALUE;
            min_max[1] = Double.MIN_VALUE;

            Dataset<Row> summary_data = this.vu
                    .select(expression_columns.asColumnArray())
                    .summary("min", "max");

            Row mins = summary_data.filter(col("summary").equalTo("min"))
                    .collectAsList()
                    .get(0);

            Row maxs = summary_data.filter(col("summary").equalTo("max"))
                    .collectAsList()
                    .get(0);

            /*
            Utils.log_info("vu:");
            Utils.log_dataset(vu, 0, "info");

            Utils.log_info("summary_data:");
            Utils.log_dataset(summary_data, 0, "info");
            Utils.log_info("length mins:" + mins.size());
            for (int i=0; i<mins.size(); i++)
                Utils.log_info(mins.get(i).toString());
            Utils.log_info("length maxs:" + maxs.size());
            for (int i=0; i<maxs.size(); i++)
                Utils.log_info(maxs.get(i).toString());
            */
            
            double min, max;
            for (int i = 1; i < mins.size(); i++) {
                min = Double.parseDouble(mins.getString(i));
                max = Double.parseDouble(maxs.getString(i));
                if (min_max[0] > min) {
                    min_max[0] = min;
                }
                if (min_max[1] < max) {
                    min_max[1] = max;
                }
            }
            
        }
        
        Utils.log_info(name + ": min = " + min_max[0]);
        Utils.log_info(name + ": max = " + min_max[1]);
        
        this._last_used_agg_func = "";
    }
    
    /*
    Creation routines
    */
    public void setBinNumbers(SparkSession spark, Histogram hist) {
        /*
        Create a new dataset with bin numbers in expression columns and include 
        _id column
        */
        
        long startTime = System.nanoTime();
        
        List<String> selected_cols = new ArrayList <> ();
        selected_cols.addAll(expression_columns.asList());
        selected_cols.add("_id");
        List<Row> exps = this.vu.select(CollectionUtils.asColumnArray(selected_cols)).collectAsList();
        List<Row> bins = new ArrayList<>();
        
        /*
        Utils.log_info("expression_columns");
        for(String s: expression_columns.asList())
            Utils.log_info(s);
        */
        
        int num_exp_cols = expression_columns.count();
        for (Row exp_row : exps) {
            Object[] d = new Object[num_exp_cols + 1];
            for (int i = 0; i < num_exp_cols; i++) {
                double val = exp_row.getDouble(i);
                int bin = hist.getBinNum_Int((float) val);
                if (!(new Double(val)).isNaN()) {
                    hist.addToBin_Int(bin);
                }
                d[i] = bin;
            }
            d[num_exp_cols] = exp_row.getLong(num_exp_cols);     // _id
            bins.add(RowFactory.create(d));
        }

        List<StructField> bin_fields = new ArrayList<>();
        for (int i = 0; i < selected_cols.size()-1; i++) {
            StructField field = DataTypes.createStructField("_bin_" + selected_cols.get(i), DataTypes.IntegerType, false);
            bin_fields.add(field);
        }
        StructField field = DataTypes.createStructField("_id", DataTypes.LongType, false);
        bin_fields.add(field);

        Dataset<Row> t = spark.createDataFrame(bins, DataTypes.createStructType(bin_fields))
                              .repartition(MultiSlideContextListener.N_SPARK_PARTITIONS).cache();
        
        //Utils.log_info("Bin count: " + t.count());
        Utils.log_info(name + ": Bin creation time = " + (System.nanoTime() - startTime)/1000000);

        /*
        Join with vu
        */
        startTime = System.nanoTime();
        
        HashMap <String, Boolean> t1 = CollectionUtils.asMap(vu.columns());
        for (String ec: expression_columns.asList()) {
            if (t1.containsKey("_bin_" + ec)) {
                t1.remove("_bin_" + ec);
            }
        }
        Column[] all_non_bin_cols = CollectionUtils.asColumnArray(t1);
        
        this.vu = this.vu.select(all_non_bin_cols)
                         .join(t, CollectionUtils.asSeq("_id"))
                         .repartition(MultiSlideContextListener.N_SPARK_PARTITIONS).cache();
        
        //Utils.log_dataset(this.vu, 0, "info");
        //Utils.log_info("Bin cached count: " + this.vu.count());
        Utils.log_info(name + ": Bin caching time = " + (System.nanoTime() - startTime)/1000000);
        //Utils.log_info("in set_bins");
        //Utils.log_dataset(this.vu, 0, "info");

        //this.vu.show();
        this.bin_are_set = true;
        
        //Utils.log_dataset(vu, 0, "info");
        
        t.unpersist(false);
        
    }
    
    /*
        Aggregate Functions
    */

    /*
        Aggregate Functions
    
    public Dataset<Row> getLinkerAggregateCounts() throws MultiSlideException {
        return View.getLinkerAggregates(this.vu, "count");
    }

    public Dataset<Row> getIdentiferAggregateCounts(
            boolean has_linker, String[] identifier_columns
    ) throws MultiSlideException {
        return View.getIdentifierAggregates(this.vu, has_linker, identifier_columns, "count");
    }

    public static Dataset<Row> getLinkerAggregates(Dataset<Row> d, String aggregate_func) throws MultiSlideException {

        Dataset<Row> id_agg;
        if (aggregate_func.equalsIgnoreCase("count")) {
            id_agg = d.groupBy(col("_entrez")).count();
        } else if (aggregate_func.equalsIgnoreCase("max")) {
            id_agg = d.groupBy(col("_entrez")).max();
        } else if (aggregate_func.equalsIgnoreCase("mean")) {
            id_agg = d.groupBy(col("_entrez")).mean();
        } else {
            throw new MultiSlideException("Aggreagte function not implemented");
        }
        return id_agg;
    }

    public static Dataset<Row> getIdentifierAggregates(
            Dataset<Row> d, boolean has_linker, String[] identifier_columns, String aggregate_func
    ) throws MultiSlideException {

        ArrayList<Column> id_cols = new ArrayList<>();
        if (has_linker) {
            id_cols.add(col("_entrez"));
        }
        for (String idc : identifier_columns) {
            id_cols.add(col(idc));
        }

        Dataset<Row> id_agg;
        if (aggregate_func.equalsIgnoreCase("count")) {
            id_agg = d.groupBy((Column[]) id_cols.toArray()).count();
        } else if (aggregate_func.equalsIgnoreCase("max")) {
            id_agg = d.groupBy((Column[]) id_cols.toArray()).max();
        } else if (aggregate_func.equalsIgnoreCase("mean")) {
            id_agg = d.groupBy((Column[]) id_cols.toArray()).mean();
        } else {
            throw new MultiSlideException("Aggreagte function not implemented");
        }
        return id_agg;
    }
    */

    /*
        Getters
    */
    /*
    protected float[][] getExpressions(GlobalMapConfig global_map_config) {
        Double[][] a = (Double[][]) this.vu
                .select(this.expression_columns)
                .filter(col("_index")
                        .between(
                                global_map_config.getCurrentSampleStart(), 
                                global_map_config.getCurrentSampleStart() + global_map_config.getRowsPerPageDisplayed()))
                .collect();
        float[][] b = new float[a.length][a[0].length];
        for (int i=0; i<a.length; i++) {
            for (int j=0; j<a.length; j++) {
                b[i][j] = a[i][j].floatValue();
            }
        }
        return b;
    }
     */
    
    /*
        Aggregate Functions
    */
    public void aggregate(String aggregation_function) throws MultiSlideException {
        
        Utils.log_info(name + ": aggregate() called on " + name);
        if (!this.specs.has_linker) {
            throw new MultiSlideException("Views can be aggregate only if it has a linker");
        }
        
        /*
            Create aggregated_vu
        */
        
        if (aggregation_function.equals(this._last_used_agg_func)) {
            this.vu = this.aggregated_vu;
            
        } else {
        
            Column agg_by = col(this._linker_colname);
            
            /*
                Aggregate expression vales, as per user given aggregation function
            */
            Dataset <Row> exp;
            if (aggregation_function.equalsIgnoreCase(MapConfig.AGGREGATE_FUNC_MIN)) {
                exp = this.vu.groupBy(agg_by).min(expression_columns.asStringArray());

            } else if (aggregation_function.equalsIgnoreCase(MapConfig.AGGREGATE_FUNC_MAX)) {
                exp = this.vu.groupBy(agg_by).max(expression_columns.asStringArray());

            } else if (aggregation_function.equalsIgnoreCase(MapConfig.AGGREGATE_FUNC_MEAN)) {
                exp = this.vu.groupBy(agg_by).mean(expression_columns.asStringArray());

            } else {
                throw new MultiSlideException("Unknown aggregate function");
            }
            
            /*
                Now again rename the damn aggeregated columns.... 
            */
            if (aggregation_function.equalsIgnoreCase(MapConfig.AGGREGATE_FUNC_MIN)) {
                
                for (String s: expression_columns.asStringArray())
                    exp = exp.withColumnRenamed("min(" + s + ")", s);
                
            } else if (aggregation_function.equalsIgnoreCase(MapConfig.AGGREGATE_FUNC_MAX)) {
                
                for (String s: expression_columns.asStringArray())
                    exp = exp.withColumnRenamed("max(" + s + ")", s);
                
            } else if (aggregation_function.equalsIgnoreCase(MapConfig.AGGREGATE_FUNC_MEAN)) {
                
                for (String s: expression_columns.asStringArray())
                    exp = exp.withColumnRenamed("avg(" + s + ")", s);
            }
            
            /*
            Utils.log_info("exp");
            Utils.log_dataset(exp, 0, "info");
            */
            
            /*
                Aggregate gene_groups, using max (or) to indicate presence
            */
            String[] colList = new String[this.gene_group_columns.size() + this.nn_gene_group_columns.size()];
            int i = 0;
            for (String col_name : this.gene_group_columns.keySet()) {
                colList[i++] = col_name;
            }
            for (String col_name : this.nn_gene_group_columns.keySet()) {
                colList[i++] = col_name;
            }
            Dataset <Row> gene_groups = this.vu.groupBy(agg_by).max(colList);
            
            /*
                Now again rename the damn aggeregated columns.... why oh why oh why?
            */
            for (String s: colList)
                    gene_groups = gene_groups.withColumnRenamed("max(" + s + ")", s);
            
            /*
            Utils.log_info("gene_groups");
            Utils.log_dataset(gene_groups, 0, "info");
            */
            
            /*
                get metadata columns, inlcuding entrez
            */
            Column[] meta_cols = new Column[identifier_columns.size()+2];
            i = 0;
            for (String cname: identifier_columns.keySet()) {
                meta_cols[i++] =  col(cname);
            }
            meta_cols[i++] = col("_entrez");
            meta_cols[i] = col("_id");            
            Dataset <Row> metadata = this.vu.select(meta_cols)
                                            .dropDuplicates(CollectionUtils.asSeq(this._linker_colname));
            
            /*
            Utils.log_info("metadata");
            Utils.log_dataset(metadata, 0, "info");
            */
            
            /*
                stitch all together
            */
            this.aggregated_vu = this.vu.select(this._linker_colname)
                                        .distinct()
                                        .join(metadata, CollectionUtils.asSeq(this._linker_colname), "inner")
                                        .join(exp, CollectionUtils.asSeq(this._linker_colname))
                                        .join(gene_groups, CollectionUtils.asSeq(this._linker_colname));

            this.disaggregated_vu = this.vu.select(this.vu.col("*"));
            this.vu = this.aggregated_vu;

        }
        /*
        Utils.log_info("aggregated view");
        Utils.log_dataset(vu, 0, "info");
        */
        this._last_used_agg_func = aggregation_function;
    }
    
    public void disaggregate() {
        Utils.log_info(name + ": disaggregate() called on " + name);
        this.vu = this.disaggregated_vu;
    }
    
    
    protected Column[] getGeneGroupCols() {
        return CollectionUtils.asColumnArray(this.getGeneGroupCols_S());
    }
    
    protected String[] getGeneGroupCols_S() {
        
        int n = this.gene_group_columns.size() + this.nn_gene_group_columns.size();
        if (this.specs.has_linker) {
            n += 2;
        }

        String[] colList = new String[n];
        int i = 0;
        if (this.specs.has_linker) {
            colList[i++] = "_entrez";
            colList[i++] = this._linker_colname;
        }
        for (String col_name : this.gene_group_columns.keySet()) {
            colList[i++] = col_name;
        }
        for (String col_name : this.nn_gene_group_columns.keySet()) {
            colList[i++] = col_name;
        }

        return colList;
    }
    
    protected int getNumFeatures(GlobalMapConfig global_map_config) {
        SignificanceTestingParams p = global_map_config.significance_testing_params;
        return this.feature_manifest.filter(p.getSignificanceLevel(), p.getFDRThreshold())
                                    .count();
    }

    protected double[] range() {
        return min_max;
    }

    protected Dataset<Row> getLinkerCounts() throws MultiSlideException {
        /*
        if (!this.specs.has_linker) {
            throw new MultiSlideException("Attempted to get Entrez, even though no linker provided");
        }
        */
        Dataset <Row> _agg;
        
        if (this.specs.has_additional_identifiers) {
            _agg = vu.select(this._linker_colname)
                     .agg(col(this._linker_colname))
                     .withColumn("counts", lit(1));
        } else {
            _agg = vu.select(this._linker_colname)
                     .groupBy(this._linker_colname)
                     .agg(count(lit(1)).alias("counts"))
                     .withColumnRenamed(this._linker_colname, "_linker");
        }
        
        /*
        Utils.log_info(name + ": _agg");
        Utils.log_dataset(_agg, 0, "info");
        */
        return _agg;
        
        /*
        return Data.aggregate(
                this.vu.select(this.linker_colname)
                        .withColumnRenamed(this.linker_colname, "_linker"),
                new String[]{"_linker"}, new String[]{"_linker"}, "count");
        */
    }

    protected Dataset<Row> getGeneGroupsAsDataset(boolean standardise_linker_name) {
        if (standardise_linker_name) {
            return this.vu.select(this.getGeneGroupCols())
                          .withColumnRenamed(_linker_colname, "_linker");
        } else {
            return this.vu.select(this.getGeneGroupCols());
        }
    }

    /*
    protected Dataset<Row> getEntrez() throws MultiSlideException {
        if (!this.has_linker) {
            throw new MultiSlideException("Attempted to get Entrez, even though no linker provided");
        }
        return this.vu.select("_entrez");
    }
    
    public Dataset<Row> getEntrezCounts() throws MultiSlideException {
        if (!this.has_linker) {
            throw new MultiSlideException("Attempted to get Entrez, even though no linker provided");
        }
        return Data.aggregate(this.vu, new String[]{"_entrez"}, new String[]{"_entrez"}, "count");
    }

    public long getEntrezCounts(String entrez) throws MultiSlideException {
        if (!this.has_linker) {
            throw new MultiSlideException("Attempted to get Entrez, even though no linker provided");
        }
        if (this.linker_aggregate_counts == null) {
            this.linker_aggregate_counts = this.getLinkerAggregateCounts();
        }
        return this.vu.filter(col("_entrez").equalTo(entrez)).collectAsList().get(0).getInt(0);
    }
    */

    public List<String> getEntrez(GlobalMapConfig global_map_config) throws MultiSlideException {
        /*
        if (!this.specs.has_linker) {
            throw new MultiSlideException("Attempted to get Entrez, even though no linker was provided");
        }
        */
        List<List<String>> a = this.getFeatureIDs(global_map_config, new String[]{"_entrez"});
        List<String> b = new ArrayList<>();
        a.forEach((row) -> {
            b.add(row.get(0));
        });
        return b;
    }
    
    public FeatureIndex getFeatureManifest(GlobalMapConfig global_map_config) {
        SignificanceTestingParams p = global_map_config.significance_testing_params;
        return this.feature_manifest.filter(p.getSignificanceLevel(), p.getFDRThreshold());
    }
    
    /*
    private static Column _get_feature_filter_condition(GlobalMapConfig global_map_config) {
        
        SignificanceTestingParams p = global_map_config.significance_testing_params;
        
        Column filter_cond = lit(true);
        if (global_map_config.isGeneFilteringOn) {
            filter_cond = filter_cond.and(col("_significance").leq(p.getSignificanceLevel()));
            if (p.getApplyFDR()) {
                filter_cond = filter_cond.and(col("_fdr").equalTo(1.0));
            }
        }
        
        return filter_cond;
    }
    */
    
    public List<List<String>> getLinkers(GlobalMapConfig global_map_config) throws MultiSlideException {
        
        if (!this.specs.has_linker) {
            throw new MultiSlideException("cannot getLinkerColumn, dataset does not have a linker column");
        }
        
        Column[] id_cols = new Column[]{col("_id"), col(this._linker_colname)};
        List<Row> a = this.vu.select(id_cols)
                             .collectAsList();
        
        SignificanceTestingParams p = global_map_config.significance_testing_params;
        List <Row> t = this.feature_manifest.filter(p.getSignificanceLevel(), p.getFDRThreshold())
                                            .align(a);
        
        List<List<String>> r = new ArrayList <> ();
        for (Row row : t) {
            if (row != null) {
                List<String> _id_linker = new ArrayList<>();
                _id_linker.add(row.getLong(0) + "");
                _id_linker.add(row.getString(1));
                r.add(_id_linker);
            }
        }
        
        return r;
    }
    
    public List<List<String>> getFeatureIDs(GlobalMapConfig global_map_config, String[] identifiers) {

        /*
        Column filter_cond = this._get_feature_filter_condition(global_map_config);

        List<Row> t = this.vu.select(Data.asColumnArray(identifiers))
                             .filter(filter_cond)
                             .orderBy("_index")
                             .collectAsList();
        */
        
        Column[] id_cols = new Column[identifiers.length + 1];
        id_cols[0] = col("_id");
        int k = 1;
        for (String id_name:identifiers) {
            id_cols[k++] = col(id_name);
        }
        
        List<Row> a = this.vu.select(id_cols)
                             .collectAsList();
        
        SignificanceTestingParams p = global_map_config.significance_testing_params;
        List <Row> t = this.feature_manifest.filter(p.getSignificanceLevel(), p.getFDRThreshold())
                                            .align(a);
        /*
        if (this.name.equals("mi_rna")) {
            Utils.log_info("getFeatureIDs: " + this.name);
            this.feature_manifest.show();
        }
        */
        
        List<List<String>> feature_ids = new ArrayList<>();
        for (Row row : t) {
            List<String> fids = new ArrayList<>();
            if (row == null) {
                for (int i = 1; i < id_cols.length; i++) {
                    fids.add("");
                }
            } else {
                for (int i = 1; i < row.size(); i++) {
                    fids.add(row.getString(i));
                }
            }
            feature_ids.add(fids);
        }
        
        /*
        for (List f: feature_ids) {
            for (Object s: f) {
                Utils.log_info("" + s);
            }
        }
        */
        return feature_ids;
    }
    
    public List<Integer> getClusterLabels(GlobalMapConfig global_map_config) {
        
        Column[] id_cols = new Column[1];
        id_cols[0] = col("_id");
        
        List<Row> a = this.vu.select(id_cols).collectAsList();
        
        SignificanceTestingParams p = global_map_config.significance_testing_params;
        List <Row> t = this.feature_manifest.filter(p.getSignificanceLevel(), p.getFDRThreshold())
                                            .align(a);
        
        List<Integer> clabels = new ArrayList<>();
        for (Row row : t) {
            if (row == null) {
                clabels.add(-1);
            } else {
                clabels.add(this.cluster_labels.get(row.getLong(0)));
            }
        }
        return clabels;
    }

    protected int[][] getExpressionBins(GlobalMapConfig global_map_config, int nBins) throws MultiSlideException {

        if (!bin_are_set) {
            throw new MultiSlideException("Requested bin numbers, before expressions have been converted to bin numbers. Call setBinNumbers() first.");
        }

        long startTime = System.nanoTime();
        
        String[] expression_colnames = expression_columns.asStringArray();
        
        Column[] bin_columns = new Column[expression_colnames.length + 1];
        bin_columns[0] = col("_id");
        for (int i=0; i<expression_colnames.length; i++)
            bin_columns[i+1] = col("_bin_" + expression_colnames[i]);

        /*
        Column filter_cond = this._get_feature_filter_condition(global_map_config);

        List<Row> a = this.vu
                          .select(bin_columns)
                          .filter(filter_cond)
                          .orderBy("_index")
                          .collectAsList();
        */
        
        //Utils.log_dataset(vu, 0, "info");
        
        List<Row> d = this.vu.select(bin_columns)
                             .collectAsList();
        
        SignificanceTestingParams p = global_map_config.significance_testing_params;
        List <Row> a = this.feature_manifest.filter(p.getSignificanceLevel(), p.getFDRThreshold())
                                            .align(d);
        
        /*
        if (this.name.equals("mi_rna")) {
            Utils.log_info("getExpressionBins:");
            List<Row> d1 = this.vu.collectAsList();
            List <Row> a1 = this.feature_manifest.filter(p.getSignificanceLevel(), p.getFDRThreshold()).align(d1);
            Utils.log_dataset_as_list(a1, "info");
            this.feature_manifest.show();
        }
        */
        
        Utils.log_info(name + ": getExpressionBins collection (time) " + (System.nanoTime() - startTime)/1000000 + " milliseconds");

        startTime = System.nanoTime();
        
        if (a.isEmpty()) {
            return new int[0][0];
        }
        
        int[][] b = new int[bin_columns.length-1][a.size()];
        for (int i = 0; i < a.size(); i++) {
            Row r = a.get(i);
            if (r != null) {
                for (int j = 1; j < r.size(); j++) {
                    b[j-1][i] = r.getInt(j);
                }
            } else {
                for (int j = 1; j < bin_columns.length-1; j++) {
                    b[j-1][i] = nBins;
                }
            }
        }
        
        Utils.log_info(name + ": getExpressionBins cast (time) " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
        
        return b;

    }
    
    public static List<List<Integer>> getGeneTags(
            Dataset <Row> dataset, FeatureIndex feature_manifest, 
            GlobalMapConfig global_map_config, HashMap<String, GeneGroup> GGs) {
        
        List<String> colList = new ArrayList<>();
        for (String id : GGs.keySet()) {
            colList.add(id);
        }
        colList.add(0, "_id");

        /*
        Column filter_cond = View._get_feature_filter_condition(global_map_config);

        List<Row> d = dataset.select(Data.asColumnArray(colList))
                             .filter(filter_cond)
                             .orderBy("_index")
                             .collectAsList();
        */
        List <Row> a = dataset.select(CollectionUtils.asColumnArray(colList))
                              .collectAsList();
        
        SignificanceTestingParams p = global_map_config.significance_testing_params;
        List <Row> d = feature_manifest.filter(p.getSignificanceLevel(), p.getFDRThreshold())
                                       .align(a);
        
        List<List<Integer>> gene_tags = new ArrayList<>();

        int indicator;
        for (Row row : d) {
            if (row != null) {
                ArrayList<Integer> t = new ArrayList<>();
                for (int i = 1; i < row.size(); i++) {
                    indicator = row.getInt(i);
                    if (indicator != 0) {
                        t.add((i-1) * indicator);
                    }
                }
                gene_tags.add(t);
            }
        }
        return gene_tags;
        
    }

    public List<List<Integer>> getGeneTags(GlobalMapConfig global_map_config, HashMap<String, GeneGroup> GGs) {
        return View.getGeneTags(vu, feature_manifest, global_map_config, GGs);
    }
    
    public static List<List<Integer>> getSearchTags(
            Dataset <Row> dataset, FeatureIndex feature_manifest, 
            GlobalMapConfig global_map_config, ListOrderedMap<String, GeneGroup> GGs) {
        
        List<String> colList = new ArrayList<>();
        for (String id : GGs.keySet()) {
            colList.add(id);
        }
        colList.add(0, "_id");

        /*
        Column filter_cond = _get_feature_filter_condition(global_map_config);

        List<Row> d = dataset.select(Data.asColumnArray(colList))
                             .filter(filter_cond)
                             .orderBy("_index")
                             .collectAsList();
        */
        
        List <Row> a = dataset.select(CollectionUtils.asColumnArray(colList))
                              .collectAsList();
        
        SignificanceTestingParams p = global_map_config.significance_testing_params;
        List <Row> d = feature_manifest.filter(p.getSignificanceLevel(), p.getFDRThreshold())
                                       .align(a);
        
        List<List<Integer>> gene_tags = new ArrayList<>();

        int indicator;
        for (int i = 1; i < colList.size(); i++) {
            ArrayList<Integer> t = new ArrayList<>();
            int j = 1;
            for (Row row : d) {
                if (row != null) {
                    indicator = row.getInt(i);
                    if (indicator != 0) {
                        t.add(j*indicator);
                    }
                }
                j++;
            }
            gene_tags.add(t);
        }
        return gene_tags;
        
    }
    
    public List<List<Integer>> getSearchTags(GlobalMapConfig global_map_config, ListOrderedMap<String, GeneGroup> GGs) {
        return View.getSearchTags(vu, feature_manifest, global_map_config, GGs);
    }

    public HashMap <String, Dataset<Row>> getDataForEnrichmentAnalysis(
            AnalysisContainer analysis, String phenotype_name, Dataset<Row> phenotype) {

        /*
            Get expression data, which need not be ordered by _index. As the _id 
            is sent to analytics engine. When returning the result, analytics engine 
            must also return the original _id column as it is used to join the results to vu
         */
        List<String> cols = new ArrayList<>();
        cols.add("_id");
        cols.addAll(expression_columns.asList());
        Dataset<Row> d = vu.select(CollectionUtils.asColumnArray(cols));

        /*
            Get phenotypes for samples
         */
        List<String> c = new ArrayList<>();
        c.add("_Sample_IDs");
        c.add(phenotype_name);
        Dataset<Row> p = analysis.data.clinical_info.phenotype_data.select(CollectionUtils.asColumnArray(c));
        
        HashMap <String, Dataset<Row>> retval = new HashMap <> ();
        retval.put("expression_data", d);
        retval.put("phenotype_data", p);

        return retval;
    }
    
    public Table getIdAndLinkerAsTable() throws MultiSlideException, DataParsingException {
        String identifier;
        if (specs.has_linker)
            identifier = _linker_colname;
        else
            identifier = specs.identifier_metadata_columns[0];
        
        /*
            get _id, _linker
        */
        List <Row> _id_link = vu.select("_id", identifier)
                                .withColumnRenamed(identifier, "_linker")
                                .collectAsList();
        
        /*
            Convert _id, _linker dataset to table
        */
        List<String[]> fields = new ArrayList<>();
        fields.add(new String[]{"_linker", Table.DTYPE_STRING});
        Table _id_link_table = new Table(fields, "_id");
        _id_link_table.fromSparkDataframe(_id_link, true);
        return _id_link_table;
    }
    
    public boolean hasWithinLinkerOrdering() {
        Map <String, List<Long>> linker_id_map = this.feature_manifest.getLinkerIdMap();
        for (List<Long> _ids: linker_id_map.values()) {
            if (_ids.size() > 1) {
                return true;
            }
        }
        return false;
    }

    /*
    Ordering: Sample and Feature
    */
    protected Table recomputeFeatureOrdering(AnalysisContainer analysis, Table _significance_linker)
            throws MultiSlideException, DataParsingException {
        
        Utils.log_info(name + ": recomputeFeatureOrdering() called for " + name);
        Utils.log_info("isGeneFilteringOn: " + analysis.global_map_config.isGeneFilteringOn);
        Utils.log_info("columnOrderingScheme: " + analysis.global_map_config.columnOrderingScheme);
        
        this.cluster_labels = null;
        
        /*
            If gene filtering is on and columnOrderingScheme is not 
            SIGNIFICANCE_COLUMN_ORDERING, run significance testing to get 
            _significane and _fdr columns
        */
        if (analysis.global_map_config.isGeneFilteringOn || 
                analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.SIGNIFICANCE_COLUMN_ORDERING) {
            
            /*
                get significance: _id, _significance, _fdr, _index
            */
            if (_significance_linker == null) {
                Table significance = this.doSignificanceAnalysis(analysis);
                /*
                get _id, and _linker columns
                */
                Table _id_linker_table = getIdAndLinkerAsTable();
                /*
                    Join to get
                    significance_linker: _id, _significance, _fdr, _index, _linker
                */
                _significance_linker = _id_linker_table.joinByKey(significance, "inner");
            }
            
            if (analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.SIGNIFICANCE_COLUMN_ORDERING) {
                Utils.log_info("1");
                return _significance_linker;
            }
        }

        /*
            select requisite data
         */
        Dataset<Row> d = null;
        String[] colList = null;
        
        if (analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING) {
            
            Utils.log_info("2");

            int n = this.gene_group_columns.size() + this.nn_gene_group_columns.size();
            /*
            if (this.specs.has_linker) {
                n += 1;
            }
            */
            colList = new String[n];
            int i = 0;
            for (String col_name : this.gene_group_columns.keySet()) {
                colList[i++] = col_name;
            }
            for (String col_name : this.nn_gene_group_columns.keySet()) {
                colList[i++] = col_name;
            }
            /*
            if (this.specs.has_linker) {
                colList[i++] = this._linker_colname;
            }
            */
            String[] sel_cols = new String[1 + colList.length + expression_columns.count()];
            sel_cols[0] = "_id";
            System.arraycopy(colList, 0, sel_cols, 1, colList.length);
            System.arraycopy(expression_columns.asStringArray(), 0, sel_cols, 1 + colList.length, expression_columns.count());

            d = this.vu.select(CollectionUtils.asColumnArray(sel_cols));

        } else if (analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.HIERARCHICAL_COLUMN_ORDERING) {
            
            Utils.log_info("3");

            List<String> cols = new ArrayList<>();
            cols.add("_id");
            cols.addAll(expression_columns.asList());

            d = vu.select(CollectionUtils.asColumnArray(cols));

        }

        /*
            if gene filtering is on, filter data before sorting / clustering
         */
        
        /*
            Filter by significance
         */
        

        Dataset<Row> fd = null;
        Table significance_linker = null;
        if (_significance_linker != null) {
            
            boolean significance_filter, fdr_filter;
            ArrayList <String> row_mask = new ArrayList <> ();

            SignificanceTestingParams params = analysis.global_map_config.significance_testing_params;
            for (String _id : _significance_linker) {
                double sig = _significance_linker.getDouble(_id, "_significance");
                double fdr = _significance_linker.getDouble(_id, "_fdr");

                significance_filter = !(sig > params.getSignificanceLevel());
                fdr_filter = !(params.getApplyFDR() && fdr == 0.0);

                if (significance_filter && fdr_filter) {
                    row_mask.add(_id);
                }
            }
            significance_linker = _significance_linker.filter(row_mask);
            
            /*
            _ids in significance_linker are based on the view (dataset) used for gene filtering
            which may not be this dataset. Therefore, the _ids may belong to the other dataset
            and should not be used here. Join significance_linker with this view's 
            feature manifest to get a relevant list of _ids
            */
            HashMap<String, Boolean> significant_linkers = new HashMap<>();
            for (String _id : significance_linker) {
                if (significance_linker.hasRowIndex(_id)) {
                    significant_linkers.put(significance_linker.getString(_id, "_linker"), true);
                }
            }
            
            fd = d.filter(col(this._linker_colname).isInCollection(CollectionUtils.asList(significant_linkers)));
            Utils.log_info("4");
            
        } else {
            fd = d;
            Utils.log_info("5");
        }

        /*
            sort / cluster
         */
        Table _id_index = null;
        if (analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.GENE_GROUP_COLUMN_ORDERING) {

            /*
            Dataset<Row> t2 = fd.orderBy(CollectionUtils.asColumnArray(colList))
                                .withColumn("_index", monotonically_increasing_id());
            
                create sorted table _id_index: _id, _index
            
            List<String[]> fields = new ArrayList<>();
            fields.add(new String[]{"_index", Table.DTYPE_LONG});
            _id_index = new Table(fields, "_id");
            _id_index.fromSparkDataframe(t2.select("_id", "_index").collectAsList(), true);
            */
            
            _id_index = this.clusterFeaturesByGeneGroups(
                    analysis.spark_session, analysis.clusterer, fd, analysis.global_map_config, colList);
            
            Utils.log_info("6");

        } else if (analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.HIERARCHICAL_COLUMN_ORDERING) {

            /*
                cluster features to get _id_index: _id, _index
             */
            _id_index = this.clusterFeatures(
                    analysis.spark_session, analysis.clusterer, fd, analysis.global_map_config);
            
            /*
                create cluster_labels
            */
            this.cluster_labels = new HashMap <> ();
            for (String _id: _id_index) {
                this.cluster_labels.put(Long.parseLong(_id), _id_index.getInt(_id, "_cluster_label"));
            }
            
            Utils.log_info("7");
        }

        /*
            if gene filtering is on, join with significance_linker, else
            just join linker
         */
        if (significance_linker != null) {
            /*
                create sorted table with linker: _id, _index, _linker, _significance, _fdr
                both tables have index, keep the one from gene group ordering (_id_index)
                by dropping _index from significance_linker
             */
            Utils.log_info("8");
            Table _id_linker_table = getIdAndLinkerAsTable();
            Table _id_index_linker = _id_index.joinByKey(_id_linker_table, "inner");
            
            /*
                _id_index now has: _id, _index, _linker
                use the linker column to lookup significance and fdr from 
                significance linker
             */
            Utils.log_info("significance_linker:");
            significance_linker.show();
            Table linker_significance = significance_linker.resetRowIndex("_linker", false, Table.AGG_MODE_LAST, Table.AGG_MODE_LAST);
            Utils.log_info("linker_significance:");
            linker_significance.show();
            /*
                for each row in _id, _index, lookup the _significance and fdr values from linker_significance
            */
            List <String[]> column_names_dtypes = new ArrayList <> ();
            column_names_dtypes.add(new String[]{"_index", Table.DTYPE_LONG});
            column_names_dtypes.add(new String[]{"_linker", Table.DTYPE_STRING});
            column_names_dtypes.add(new String[]{"_significance", Table.DTYPE_DOUBLE});
            column_names_dtypes.add(new String[]{"_fdr", Table.DTYPE_DOUBLE});
            Table _table = new Table(column_names_dtypes, "_id");
            
            Utils.log_info("_ids:");
            HashMap <String, Boolean> added_linkers = new HashMap <> ();
            long max_id = -1;
            for (String _id: _id_index_linker) {
                Utils.log_info(_id);
                Long index = _id_index_linker.getLong(_id, "_index");
                String linker = _id_index_linker.getString(_id, "_linker");
                double significance, fdr;
                if (linker_significance.hasRowIndex(linker)) {
                    significance = linker_significance.getDouble(linker, "_significance");
                    fdr = linker_significance.getDouble(linker, "_fdr");
                } else {
                    significance = 1.0;
                    fdr = 1.0;
                }
                _table.set("_index", _id, index);
                _table.set("_linker", _id, linker);
                _table.set("_significance", _id, significance);
                _table.set("_fdr", _id, fdr);
                added_linkers.put(linker, true);
                if (Long.parseLong(_id) > max_id)
                    max_id = Long.parseLong(_id);
            }
            /*
                Now add the remaining filtered genes, into _table with increasing 
                index, so that other datasets that have these genes can display them
            */
            double significance, fdr;
            long index = _table.count();
            for (String linker: linker_significance) {
                if (!added_linkers.containsKey(linker)) {
                    significance = linker_significance.getDouble(linker, "_significance");
                    fdr = linker_significance.getDouble(linker, "_fdr");
                    _table.set("_index", max_id + "", index++);
                    _table.set("_linker", max_id + "", linker);
                    _table.set("_significance", max_id + "", significance);
                    _table.set("_fdr", max_id + "", fdr);
                    max_id++;
                }
            }
            
            return _table;

        } else {
            /*
                create sorted table with linker: _id, _index, _linker
             */
            Utils.log_info("9");
            Table _id_linker_table = getIdAndLinkerAsTable();
            return _id_index.joinByKey(_id_linker_table, "inner");
        }

    }

    public final Table recomputeSampleOrdering(
            AnalysisContainer analysis,
            Dataset<Row> phenotypes
    ) throws MultiSlideException, DataParsingException {

        /*
        This function returns ordered samples as a Dataset with a "_Sample_IDs" 
        and an "_index" column
         */
        switch (analysis.global_map_config.sampleOrderingScheme) {

            case GlobalMapConfig.PHENOTYPE_SAMPLE_ORDERING:
                
                String[] sp = analysis.global_map_config.phenotype_sorting_params.getPhenotypes();
                return this.clusterSamplesByPhenotypes(
                        analysis.spark_session, analysis.clusterer, analysis.global_map_config, sp, phenotypes);
                
                /*
                List <Row> a = phenotypes.orderBy(CollectionUtils.asColumnArray(sp))
                                         .select("_Sample_IDs")
                                         .withColumn("_index", monotonically_increasing_id())
                                         .collectAsList();
                
                List<String[]> fields = new ArrayList<>();
                fields.add(new String[]{"_index", Table.DTYPE_LONG});
                Table sample_index = new Table(fields, "_Sample_IDs");
                sample_index.fromSparkDataframe(a, true);
                return sample_index;
                */
                
            case GlobalMapConfig.HIERARCHICAL_SAMPLE_ORDERING:
                
                return this.clusterSamples(
                        analysis.spark_session, analysis.clusterer, analysis.global_map_config);

            default:
                throw new MultiSlideException("Unknown column ordering scheme.");
        }

    }

    /*
    All views must have an "_id" column
    This function is used to set _id when view is independent
    In case of linked view, "_id" is constructed in Selection.alignViews()
    and propagated to linked views
    For linked views "_id" column is therefore shared
     */
    protected void addId() {
        this.vu = this.vu.withColumn("_id", monotonically_increasing_id());
    }

    /*
    All views must have an "_index" column
    This function is used to set _index when view is independent
    In case of linked view, "_index" is either constructed in recomputeFeatureOrdering
    or propagated from other views to this using reorderFeatures
     */
    protected void addIndex() {
        this.vu = this.vu.withColumn("_index", monotonically_increasing_id());
    }

    protected void reorderSamples(SampleIndex ordered_samples) {
        this.expression_columns = ordered_samples;
    }
    
    protected void reorderFeatures(
            AnalysisContainer analysis, Table feature_order, boolean set_from_self
    ) throws MultiSlideException, DataParsingException {
        
        Table within_linker_ordering = null;
        if (!(analysis.global_map_config.columnOrderingScheme == GlobalMapConfig.HIERARCHICAL_COLUMN_ORDERING
                && analysis.global_map_config.col_clustering_params.getDatasetName().equals(name))) {
            
            if (this.specs.has_linker) {
                within_linker_ordering = clusterFeaturesByLinker(
                        analysis.spark_session, analysis.clusterer, analysis.global_map_config);
            }
        }
        this.feature_manifest.setOrdering(feature_order, true, set_from_self, within_linker_ordering);
        
    }
    
    /*
        All views must have a feature_manifest
        This function is used to create feature_manifest when view is independent
        In case of linked view, feature_manifest is constructed in Selection.alignViews()
        and propagated to linked views
        For linked views feature_manifest is therefore shared
    */
    protected void createDefaultFeatureManifest() {
        
        String identifier;
        if (specs.has_linker)
            identifier = _linker_colname;
        else
            identifier = specs.identifier_metadata_columns[0];
        
        feature_manifest = new FeatureIndex(this.vu.select("_id", identifier)
                                                   .withColumnRenamed(identifier, "_linker")
                                                   .collectAsList());
    }

    /*
        sample clustering routines
    */
    public Table clusterSamples(
            SparkSession spark, HierarchicalClusterer clusterer,
            GlobalMapConfig global_map_config
    ) throws MultiSlideException, DataParsingException {

        List<String> cols = new ArrayList<>();
        cols.addAll(expression_columns.asList());

        return clusterer.doClustering(
                spark, this.name,
                global_map_config.row_clustering_params,
                vu.select(CollectionUtils.asColumnArray(cols)), 
                null, new String[]{});
    }
    
    public Table clusterSamplesByPhenotypes(
            SparkSession spark, HierarchicalClusterer clusterer,
            GlobalMapConfig global_map_config, String[] phenotype_names,
            Dataset <Row> phenotype
    ) throws MultiSlideException, DataParsingException {

        List<String> cols = new ArrayList<>();
        cols.add("_id");
        cols.addAll(expression_columns.asList());
        Dataset<Row> d = vu.select(CollectionUtils.asColumnArray(cols));

        /*
            Get phenotypes for samples
         */
        List<String> c = new ArrayList<>();
        c.add("_Sample_IDs");
        c.addAll(CollectionUtils.asList(global_map_config.phenotype_sorting_params.getPhenotypes()));
        Dataset<Row> p = phenotype.select(CollectionUtils.asColumnArray(c));
        
        return clusterer.doClustering(
                spark, this.name,
                global_map_config.row_clustering_params,
                d, p, phenotype_names);
    }
    
    /*
        feature clustering routines
    */
    public Table clusterFeaturesByLinker(
            SparkSession spark, HierarchicalClusterer clusterer,
            GlobalMapConfig global_map_config
    ) throws MultiSlideException, DataParsingException {
        
        List<String> cols = new ArrayList<>();
        if (this.specs.has_linker) {
            cols.add(this._linker_colname);
        } else {
            throw new MultiSlideException("clusterFeaturesByLinker called on '" + name + "', but no linker is specified");
        }
        cols.add("_id");
        cols.addAll(expression_columns.asList());

        Dataset <Row> d = vu.select(CollectionUtils.asColumnArray(cols))
                            .withColumnRenamed(_linker_colname, "_linker");

        return clusterer.doClustering(
                spark, this.name,
                global_map_config.col_clustering_params, 
                d, null, new String[]{"_linker"});
    }
    
    public Table clusterFeaturesByGeneGroups(
            SparkSession spark, HierarchicalClusterer clusterer, 
            Dataset <Row> d,
            GlobalMapConfig global_map_config, String[] gene_groups
    ) throws MultiSlideException, DataParsingException {
        
        /*
        List<String> cols = new ArrayList<>();
        cols.add("_id");
        cols.addAll(gene_groups);
        cols.addAll(expression_columns.asList());
        Dataset <Row> d = vu.select(CollectionUtils.asColumnArray(cols));
        */
        
        return clusterer.doClustering(
                spark, this.name,
                global_map_config.col_clustering_params, 
                d, null, gene_groups);
    }

    public Table clusterFeatures(
            SparkSession spark, HierarchicalClusterer clusterer, Dataset<Row> d,
            GlobalMapConfig global_map_config
    ) throws MultiSlideException, DataParsingException {

        return clusterer.doClustering(
                spark, this.name,
                global_map_config.col_clustering_params,
                d, null, new String[]{});
    }

    /*
        significance analysis
    */
    public Table doSignificanceAnalysis(AnalysisContainer analysis) 
            throws MultiSlideException, DataParsingException {

        /*
            Get expression data, which need not be ordered by _index. As the _id 
            is sent to analytics engine. When returning the result, analytics engine 
            must also return the original _id column as it is used to join the results to vu
         */
        List<String> cols = new ArrayList<>();
        cols.add("_id");
        cols.addAll(expression_columns.asList());
        Dataset<Row> d = vu.select(CollectionUtils.asColumnArray(cols));

        /*
            Get phenotypes for samples
         */
        List<String> c = new ArrayList<>();
        c.add("_Sample_IDs");
        c.add(analysis.global_map_config.significance_testing_params.getPhenotype());
        Dataset<Row> p = analysis.data.clinical_info.phenotype_data.select(CollectionUtils.asColumnArray(c));

        return analysis.significance_tester.computeSignificanceLevel(analysis, name, d, p);

    }

}
