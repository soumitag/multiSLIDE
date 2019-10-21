/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import com.google.gson.Gson;
import java.io.Serializable;
import org.cssblab.multislide.algorithms.clustering.BinaryTree;
import org.cssblab.multislide.algorithms.clustering.HierarchicalClusterer;

/**
 *
 * @author Soumita
 */
public class ClusteringParams implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    int type;
    boolean use_defaults;
    int linkage_function;
    int distance_function;
    int leaf_ordering;
    String dataset;

    public ClusteringParams(int type) throws MultiSlideException {
        if (type != HierarchicalClusterer.TYPE_ROW_CLUSTERING && type != HierarchicalClusterer.TYPE_COL_CLUSTERING) {
            throw new MultiSlideException("Illegal argument for type: " + type);
        }
        this.type = type;
        this.use_defaults = true;
    }
    
    public ClusteringParams(int type, String dataset) throws MultiSlideException {
        if (type != HierarchicalClusterer.TYPE_ROW_CLUSTERING && type != HierarchicalClusterer.TYPE_COL_CLUSTERING) {
            throw new MultiSlideException("Illegal argument for type: " + type);
        }
        this.type = type;
        this.use_defaults = true;
        this.linkage_function = HierarchicalClusterer.COMPLETE_LINKAGE;
        this.distance_function = HierarchicalClusterer.EUCLIDEAN_DISTANCE;
        this.leaf_ordering = BinaryTree.SMALLEST_CHILD_FIRST_LEAF_ORDER;
        this.dataset = dataset;
    }
    
    public ClusteringParams(int type, int linkage_function, int distance_function, int leaf_ordering, String dataset) throws MultiSlideException {
        if (type != HierarchicalClusterer.TYPE_ROW_CLUSTERING && type != HierarchicalClusterer.TYPE_COL_CLUSTERING) {
            throw new MultiSlideException("Illegal argument for type: " + type);
        }
        this.type = type;
        this.use_defaults = false;
        this.linkage_function = linkage_function;
        this.distance_function = distance_function;
        this.leaf_ordering = leaf_ordering;
        this.dataset = dataset;
    }
    
    public int getType() {
        return type;
    }

    public boolean usesDefaultParams() {
        return use_defaults;
    }

    public int getLinkageFunction() {
        if (this.use_defaults) {
            return HierarchicalClusterer.COMPLETE_LINKAGE;
        } else {
            return linkage_function;
        }
    }

    public int getDistanceFunction() {
        if (this.use_defaults) {
            return HierarchicalClusterer.EUCLIDEAN_DISTANCE;
        } else {
            return distance_function;
        }
    }
    
    public String getDistanceFunctionS () {
        int distance_function_code = this.getDistanceFunction();
        switch (distance_function_code) {
            case HierarchicalClusterer.EUCLIDEAN_DISTANCE:
                return "euclidean";
            case HierarchicalClusterer.CITYBLOCK_DISTANCE:
                return "cityblock";
            case HierarchicalClusterer.COSINE_DISTANCE:
                return "cosine";
            case HierarchicalClusterer.CORRELATION_DISTANCE:
                return "correlation";
            case HierarchicalClusterer.CHEBYSHEV_DISTANCE:
                return "chebyshev";
            default:
                return "";
        }
    }
    
    public String getLinkageFunctionS () {
        int linkage_function_code = this.getLinkageFunction();
        switch (linkage_function_code) {
            case HierarchicalClusterer.SINGLE_LINKAGE:
                return "single";
            case HierarchicalClusterer.COMPLETE_LINKAGE:
                return "complete";
            case HierarchicalClusterer.AVERAGE_LINKAGE:
                return "average";
            case HierarchicalClusterer.MEDIAN_LINKAGE:
                return "median";
            case HierarchicalClusterer.CENTROID_LINKAGE:
                return "centroid";
            case HierarchicalClusterer.WARD_LINKAGE:
                return "ward";
            case HierarchicalClusterer.WEIGHTED_LINKAGE:
                return "weighted";
            default:
                return "";
        }
    }
    
    public int getLeafOrdering() {
        if (this.use_defaults) {
            return BinaryTree.SMALLEST_CHILD_FIRST_LEAF_ORDER;
        }
        return leaf_ordering;
    }
    
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
    
    public final int getDistanceFunctionCode(String distance_function) throws MultiSlideException {
        if (distance_function.equalsIgnoreCase("euclidean")) {
            return HierarchicalClusterer.EUCLIDEAN_DISTANCE;
        } else if (distance_function.equalsIgnoreCase("cityblock")) {
            return HierarchicalClusterer.CITYBLOCK_DISTANCE;
        } else if (distance_function.equalsIgnoreCase("cosine")) {
            return HierarchicalClusterer.COSINE_DISTANCE;
        } else if (distance_function.equalsIgnoreCase("correlation")) {
            return HierarchicalClusterer.CORRELATION_DISTANCE;
        } else if (distance_function.equalsIgnoreCase("chebyshev")) {
            return HierarchicalClusterer.CHEBYSHEV_DISTANCE;
        } else {
            throw new MultiSlideException("Illegal argument for distance function: " + distance_function);
        }
    }
    
    public final int getLinkgeFunctionCode(String linkge_function) throws MultiSlideException {
        if (linkge_function.equalsIgnoreCase("single")) {
            return HierarchicalClusterer.SINGLE_LINKAGE;
        } else if (linkge_function.equalsIgnoreCase("complete")) {
            return HierarchicalClusterer.COMPLETE_LINKAGE;
        } else if (linkge_function.equalsIgnoreCase("average")) {
            return HierarchicalClusterer.AVERAGE_LINKAGE;
        } else if (linkge_function.equalsIgnoreCase("median")) {
            return HierarchicalClusterer.MEDIAN_LINKAGE;
        } else if (linkge_function.equalsIgnoreCase("centroid")) {
            return HierarchicalClusterer.CENTROID_LINKAGE;
        } else if (linkge_function.equalsIgnoreCase("ward")) {
            return HierarchicalClusterer.WARD_LINKAGE;
        } else if (linkge_function.equalsIgnoreCase("weighted")) {
            return HierarchicalClusterer.WEIGHTED_LINKAGE;
        } else {
            throw new MultiSlideException("Illegal argument for linkage function: " + linkge_function);
        }
    }
    
    public String getDatasetName() {
        return dataset;
    }
    
    public String getHashString() {
        return linkage_function + "_" + distance_function;
    }
    
    public String asJSON () {
        return new Gson().toJson(this);
    }
}
