/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.algorithms.clustering;

/**
 *
 * @author Soumita
 */
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;   
import static org.apache.spark.sql.functions.monotonically_increasing_id;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.ClusteringParams;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.utils.HttpClientManager;
import org.cssblab.multislide.utils.Utils;
import org.cssblab.multislide.algorithms.clustering.BinaryTree;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.structure.data.Data;
import org.cssblab.multislide.structure.data.table.Table;
import org.cssblab.multislide.utils.CollectionUtils;

/**
 *
 * @author Soumita
 */
public class HierarchicalClusterer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    
    public String CACHE_PATH;
    public String message;
    public int status;
    private final HttpClientManager analytics_engine_comm;
    
    public HierarchicalClusterer (String cache_path, HttpClientManager analytics_engine_comm) {
        this.CACHE_PATH = cache_path;
        this.status = 0;
        this.analytics_engine_comm = analytics_engine_comm;
    }
    
    /*
    public HierarchicalClusterer () { }
    */
    
    public Table doClustering(
            SparkSession spark, String dataset_name,
            ClusteringParams params, Dataset <Row> data, 
            Dataset <Row> phenotype_data, String[] group_by
    ) throws MultiSlideException, DataParsingException {
        
        Table result = null;
        
        List<String[]> fields = new ArrayList<>();
        switch (params.getType()) {
            case ClusteringParams.TYPE_FEATURE_CLUSTERING: {
                if (group_by != null && group_by.length == 1 && group_by[0].equals("_linker")) {
                    fields.add(new String[]{"_linker", Table.DTYPE_STRING});
                    result = new Table(fields, "_id");
                    break;
                } else {
                    fields.add(new String[]{"_index", Table.DTYPE_LONG});
                    result = new Table(fields, "_id");
                    break;
                }
            }
            case ClusteringParams.TYPE_SAMPLE_CLUSTERING: {
                fields.add(new String[]{"_index", Table.DTYPE_LONG});
                result = new Table(fields, "_Sample_IDs");
                break;
            }
        }
        
        /*
        In case of empty input, return empty
        */
        if (data.count() == 0) {
            return result;
        }
        
        /*
        Check that there are colums to cluster
        */
        List <String> cols = CollectionUtils.asList(data.columns());
        if (cols.contains("_id"))
            cols.remove("_id");
        if (group_by != null) {
            for (String s: group_by)
                cols.remove(s);
        }
        if (cols.isEmpty()) {
            return result;
        }
    
        /*
        Create a unique request id
        */
        String id = System.currentTimeMillis() + "";
        Random rand = new Random();
        String request_id = "req_" + id + "." + rand.nextInt();
        
        /*
        Get folder and file paths
        */
        String request_path = CACHE_PATH + File.separator + request_id;

        try {
            
            data.repartition(1)
                .write()
                .mode("overwrite")
                .format("csv")
                .option("delimiter", "\t")
                .option("header", "true")
                .save(request_path);
            
            if (phenotype_data != null) {
                phenotype_data.repartition(1)
                               .write()
                               .mode("overwrite")
                               .format("csv")
                               .option("delimiter", "\t")
                               .option("header", "true")
                               .save(request_path + File.separator + "phenotypes");
            }
            //Utils.log_dataset(data, 0, "info");
            
            HashMap <String, String> props = params.asMap();
            if (group_by != null && group_by.length > 0) {
                props.put("operation", "clustering_by_groups");
                props.put("group_by", String.join(",", group_by));
            } else {
                props.put("operation", "clustering");
            }
            props.put("dataset_name", dataset_name);
            FileHandler.savePropertiesFile(request_path + File.separator + "_params", props);
            
        } catch (Exception e) {
            Utils.log_exception(e, "");
            throw new MultiSlideException("Failed to start hierarchical clustering.");
        }
        
        long n_rows = data.count();
        int n_cols = data.columns().length;
        long sz = 0;
        
        HashMap <String, String> p = new HashMap <> ();
        if (params.getType() == ClusteringParams.TYPE_SAMPLE_CLUSTERING) {
            p.put("transpose", "True");
            sz = n_cols;
        } else if (params.getType() == ClusteringParams.TYPE_FEATURE_CLUSTERING) {
            p.put("transpose", "False");
            sz = n_rows;
        }
        
        p.put("request_id", request_id);
        p.put("linkage_strategy", params.getLinkageFunctionS());
        p.put("distance_metric", params.getDistanceFunctionS());
        if (sz > 500 && params.getLeafOrdering() == ClusteringParams.OPTIMAL_LEAF_ORDER) {
            p.put("leaf_ordering", "count_sort_descending");
        } else {
            p.put("leaf_ordering", params.getLeafOrderingS());
        }
        
        if (group_by != null && group_by.length > 0) {
            p.put("group_by", String.join(",", group_by));
        } else {
            p.put("group_by", "");
        }
        p.put("n_rows", n_rows+"");
        p.put("n_cols", n_cols+"");
        
        ServerResponse resp = analytics_engine_comm.doGet("do_clustering", p);

        if (resp.status == 0) {
            throw new MultiSlideException(resp.message + ". " + resp.detailed_reason);
        } else {
            try {
                String out_fname = request_path + File.separator + "result.txt";
                
                //result.load(out_fname, "\t", true);
                //return result;
                
                List <StructField> f = new ArrayList <> ();
                
                switch (params.getType()) {
                    case ClusteringParams.TYPE_FEATURE_CLUSTERING:
                    {
                        if (group_by != null && group_by.length == 1 && group_by[0].equals("_linker")) {
                            f.add(DataTypes.createStructField("_id", DataTypes.LongType, false));
                            f.add(DataTypes.createStructField("_linker", DataTypes.StringType, false));
                        } else {
                            f.add(DataTypes.createStructField("_id", DataTypes.LongType, false));
                            f.add(DataTypes.createStructField("_index", DataTypes.LongType, false));
                        }
                        break;
                    }
                    case ClusteringParams.TYPE_SAMPLE_CLUSTERING:
                    {
                        f.add(DataTypes.createStructField("_Sample_IDs", DataTypes.StringType, false));
                        f.add(DataTypes.createStructField("_index", DataTypes.LongType, false));
                        break;
                    }
                }
                
                Dataset <Row> res = spark.read()
                                         .format("csv")
                                         .option("sep", "\t")
                                         .option("header", "true")
                                         .schema(DataTypes.createStructType(f))
                                         .load(out_fname);
                
                /*
                Utils.log_info("Hierarchical Clustering result");
                Utils.log_dataset(res, 0, "info");
                */
                
                result.fromSparkDataframe(res.collectAsList(), true);
                return result;
                
            } catch (Exception e) {
                throw new MultiSlideException("Failed to read hierarchical clustering results");
            }
        }

    }
    
    public void clearCache() {
        
    }
    
    
    /*
    public double[][] extractTopKNodes(double[][] linkage_tree, int start_node, int K) {
        ArrayList <Integer> sub_tree = new ArrayList <Integer> ();
        sub_tree.add((int)linkage_tree[start_node][0]);
        sub_tree.add((int)linkage_tree[start_node][1]);
        getChild(sub_tree, linkage_tree, (int)linkage_tree[start_node][0], 0, K);
        getChild(sub_tree, linkage_tree, (int)linkage_tree[start_node][1], 0, K);
        return null;
    }
    
    public void getChild(ArrayList <Integer> sub_tree, double[][] linkage_tree, int curr_node, int count, int K) {
        if (count < K) {
            sub_tree.add((int)linkage_tree[curr_node][0]);
            sub_tree.add((int)linkage_tree[curr_node][1]);
            getChild(sub_tree, linkage_tree, (int)linkage_tree[curr_node][0], ++count, K);
            getChild(sub_tree, linkage_tree, (int)linkage_tree[curr_node][1], ++count, K);
        }
    }
    */
}