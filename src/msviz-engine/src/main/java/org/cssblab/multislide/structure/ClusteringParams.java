/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author Soumita
 */
public class ClusteringParams implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final int OPTIMAL_LEAF_ORDER = 0;
    public static final int COUNT_SORT_ASCENDING_LEAF_ORDER = 1;
    public static final int COUNT_SORT_DESCENDING_LEAF_ORDER = 2;
    public static final int DISTANCE_SORT_ASCENDING_LEAF_ORDER = 3;
    public static final int DISTANCE_SORT_DESCENDING_LEAF_ORDER = 4;
    
    public static final int AVERAGE_LINKAGE = 0;
    public static final int COMPLETE_LINKAGE = 1;
    public static final int MEDIAN_LINKAGE = 2;
    public static final int CENTROID_LINKAGE = 3;
    public static final int WARD_LINKAGE = 4;
    public static final int WEIGHTED_LINKAGE = 5;
    public static final int SINGLE_LINKAGE = 6;
    
    public static final int EUCLIDEAN_DISTANCE = 0;
    public static final int CITYBLOCK_DISTANCE = 1;
    public static final int COSINE_DISTANCE = 2;
    public static final int CORRELATION_DISTANCE = 3;
    public static final int CHEBYSHEV_DISTANCE = 4;
    
    public static final int TYPE_SAMPLE_CLUSTERING = 0;
    public static final int TYPE_FEATURE_CLUSTERING = 1;
    
    public static final int DEFAULT_NUM_SAMPLE_CLUSTERS = 3;
    public static final int DEFAULT_NUM_FEATURE_CLUSTERS = 5;
    
    int type;
    boolean use_defaults;
    int linkage_function;
    int distance_function;
    int leaf_ordering;
    String dataset;
    /*
    boolean is_joint;
    boolean use_aggregate;
    String aggregate_func;
    */
    
    public int numClusterLabels;

    public ClusteringParams(int type) throws MultiSlideException {
        if (type != TYPE_SAMPLE_CLUSTERING && type != TYPE_FEATURE_CLUSTERING) {
            throw new MultiSlideException("Illegal argument for type: " + type);
        }
        this.type = type;
        this.use_defaults = true;
        /*
        this.is_joint = true;
        this.use_aggregate = false;
        */
    }
    
    public ClusteringParams(int type, String dataset) throws MultiSlideException {
        if (type != TYPE_SAMPLE_CLUSTERING && type != TYPE_FEATURE_CLUSTERING) {
            throw new MultiSlideException("Illegal argument for type: " + type);
        }
        this.type = type;
        this.use_defaults = true;
        this.linkage_function = COMPLETE_LINKAGE;
        this.distance_function = EUCLIDEAN_DISTANCE;
        this.leaf_ordering = ClusteringParams.OPTIMAL_LEAF_ORDER;
        this.dataset = dataset;
        if (type == TYPE_SAMPLE_CLUSTERING) {
            this.numClusterLabels = ClusteringParams.DEFAULT_NUM_SAMPLE_CLUSTERS;
        } else if (type == TYPE_FEATURE_CLUSTERING) {
            this.numClusterLabels = ClusteringParams.DEFAULT_NUM_FEATURE_CLUSTERS;
        }
        /*
        this.is_joint = true;
        this.use_aggregate = false;
        */
    }
    
    public ClusteringParams(
            int type, int linkage_function, 
            int distance_function, int leaf_ordering, 
            String dataset, int numClusterLabels
    ) throws MultiSlideException {
        if (type != TYPE_SAMPLE_CLUSTERING && type != TYPE_FEATURE_CLUSTERING) {
            throw new MultiSlideException("Illegal argument for type: " + type);
        }
        this.type = type;
        this.use_defaults = false;
        this.linkage_function = linkage_function;
        this.distance_function = distance_function;
        this.leaf_ordering = leaf_ordering;
        this.dataset = dataset;
        this.numClusterLabels = numClusterLabels;
        /*
        if(type == HierarchicalClusterer.TYPE_ROW_CLUSTERING) {
            this.is_joint = true;
            this.use_aggregate = false;
        } else {
            this.is_joint = is_joint;
            this.use_aggregate = use_aggregate;
            this.aggregate_func = aggregate_func;
        }
        */
    }
    
    public int getType() {
        return type;
    }

    public boolean usesDefaultParams() {
        return use_defaults;
    }

    public int getLinkageFunction() {
        if (this.use_defaults) {
            return COMPLETE_LINKAGE;
        } else {
            return linkage_function;
        }
    }

    public int getDistanceFunction() {
        if (this.use_defaults) {
            return EUCLIDEAN_DISTANCE;
        } else {
            return distance_function;
        }
    }
    
    public String getTypeS() {
        switch (this.type) {
            case TYPE_SAMPLE_CLUSTERING:
                return "sample";
            case TYPE_FEATURE_CLUSTERING:
                return "feature";
            default:
                return "unknown";
        }
    }
    
    public String getDistanceFunctionS () {
        int distance_function_code = this.getDistanceFunction();
        switch (distance_function_code) {
            case EUCLIDEAN_DISTANCE:
                return "euclidean";
            case CITYBLOCK_DISTANCE:
                return "cityblock";
            case COSINE_DISTANCE:
                return "cosine";
            case CORRELATION_DISTANCE:
                return "correlation";
            case CHEBYSHEV_DISTANCE:
                return "chebyshev";
            default:
                return "";
        }
    }
    
    public String getLinkageFunctionS () {
        int linkage_function_code = this.getLinkageFunction();
        switch (linkage_function_code) {
            case SINGLE_LINKAGE:
                return "single";
            case COMPLETE_LINKAGE:
                return "complete";
            case AVERAGE_LINKAGE:
                return "average";
            case MEDIAN_LINKAGE:
                return "median";
            case CENTROID_LINKAGE:
                return "centroid";
            case WARD_LINKAGE:
                return "ward";
            case WEIGHTED_LINKAGE:
                return "weighted";
            default:
                return "";
        }
    }
    
    public String getLeafOrderingS() {
        int leaf_ordering_code = this.getLeafOrdering();
        switch (leaf_ordering_code) {
            case ClusteringParams.OPTIMAL_LEAF_ORDER:
                return "optimal";
            case ClusteringParams.COUNT_SORT_ASCENDING_LEAF_ORDER:
                return "count_sort_ascending";
            case ClusteringParams.COUNT_SORT_DESCENDING_LEAF_ORDER:
                return "count_sort_descending";
            case ClusteringParams.DISTANCE_SORT_ASCENDING_LEAF_ORDER:
                return "distance_sort_ascending";
            case ClusteringParams.DISTANCE_SORT_DESCENDING_LEAF_ORDER:
                return "distance_sort_descending";
            default:
                return null;
        }
    }
    
    public int getLeafOrdering() {
        if (this.use_defaults) {
            return ClusteringParams.OPTIMAL_LEAF_ORDER;
        } else {
            return leaf_ordering;
        }
    }
    
    public void setLeafOrdering(int leaf_ordering) {
        this.leaf_ordering = leaf_ordering;
    }
    
    /*
    public final int getLeafOrderingCode(String leaf_ordering) throws MultiSlideException {
        if (leaf_ordering.equalsIgnoreCase("smallest_child_first")) {
            return BinaryTree.SMALLEST_CHILD_FIRST_LEAF_ORDER;
        } else if (leaf_ordering.equalsIgnoreCase("largest_child_first")) {
            return BinaryTree.LARGEST_CHILD_FIRST_LEAF_ORDER;
        } else if (leaf_ordering.equalsIgnoreCase("most_diverse_child_first")) {
            return BinaryTree.MOST_DIVERSE_CHILD_FIRST_LEAF_ORDER;
        } else if (leaf_ordering.equalsIgnoreCase("least_diverse_child_first")) {
            return BinaryTree.LEAST_DIVERSE_CHILD_FIRST_LEAF_ORDER;
        } else {
            throw new MultiSlideException("Illegal argument for leaf ordering: " + leaf_ordering);
        }
    }
    */
    
    public final int getDistanceFunctionCode(String distance_function) throws MultiSlideException {
        if (distance_function.equalsIgnoreCase("euclidean")) {
            return EUCLIDEAN_DISTANCE;
        } else if (distance_function.equalsIgnoreCase("cityblock")) {
            return CITYBLOCK_DISTANCE;
        } else if (distance_function.equalsIgnoreCase("cosine")) {
            return COSINE_DISTANCE;
        } else if (distance_function.equalsIgnoreCase("correlation")) {
            return CORRELATION_DISTANCE;
        } else if (distance_function.equalsIgnoreCase("chebyshev")) {
            return CHEBYSHEV_DISTANCE;
        } else {
            throw new MultiSlideException("Illegal argument for distance function: " + distance_function);
        }
    }
    
    public final int getLinkgeFunctionCode(String linkge_function) throws MultiSlideException {
        if (linkge_function.equalsIgnoreCase("single")) {
            return SINGLE_LINKAGE;
        } else if (linkge_function.equalsIgnoreCase("complete")) {
            return COMPLETE_LINKAGE;
        } else if (linkge_function.equalsIgnoreCase("average")) {
            return AVERAGE_LINKAGE;
        } else if (linkge_function.equalsIgnoreCase("median")) {
            return MEDIAN_LINKAGE;
        } else if (linkge_function.equalsIgnoreCase("centroid")) {
            return CENTROID_LINKAGE;
        } else if (linkge_function.equalsIgnoreCase("ward")) {
            return WARD_LINKAGE;
        } else if (linkge_function.equalsIgnoreCase("weighted")) {
            return WEIGHTED_LINKAGE;
        } else {
            throw new MultiSlideException("Illegal argument for linkage function: " + linkge_function);
        }
    }
    
    public String getDatasetName() {
        return dataset;
    }
    
    public void setNumClusterLabels(int numClusterLabels) {
        this.numClusterLabels = numClusterLabels;
    }
    
    public int getNumClusters() {
        if (this.use_defaults) {
            if (type == TYPE_SAMPLE_CLUSTERING) {
                return ClusteringParams.DEFAULT_NUM_SAMPLE_CLUSTERS;
            } else {
                return ClusteringParams.DEFAULT_NUM_FEATURE_CLUSTERS;
            }
        } else {
            return this.numClusterLabels;
        }
    }

    public String getHashString() {
        return type + "_" + linkage_function + "_" + distance_function;
    }
    
    public String asJSON () {
        return new Gson().toJson(this);
    }
    
    public HashMap <String, String> asMap() {
        HashMap <String, String> map = new HashMap <> ();
        map.put("clustering_on", this.getTypeS());
        map.put("dataset_name", this.getDatasetName());
        map.put("distance_function", this.getDistanceFunctionS());
        map.put("linkage_function", this.getLinkageFunctionS());
        map.put("leaf_ordering", this.getLeafOrderingS());
        map.put("num_clusters", this.getNumClusters()+"");
        return map;
    }
}
