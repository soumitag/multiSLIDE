/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

/**
 *
 * @author Soumita
 */
public class ClusteringParams {
    
    public static final int CLUSTERING_PARAM_TYPE_ROW = 0;
    public static final int CLUSTERING_PARAM_TYPE_COL = 1;
    
    int type;
    boolean isActive;
    boolean defaultParams;
    String linkage_function;
    String distance_function;
    int leaf_ordering;
    String dataset_name;

    public ClusteringParams(int type) throws MultiSlideException {
        if (type != ClusteringParams.CLUSTERING_PARAM_TYPE_ROW && type != ClusteringParams.CLUSTERING_PARAM_TYPE_COL) {
            throw new MultiSlideException("Illegal Argument for type: " + type);
        }
        isActive = false;
        this.type = type;
    }
    
    public ClusteringParams(int type, boolean makeActive, String dataset_name) throws MultiSlideException {
        if (type != ClusteringParams.CLUSTERING_PARAM_TYPE_ROW && type != ClusteringParams.CLUSTERING_PARAM_TYPE_COL) {
            throw new MultiSlideException("Illegal Argument for type: " + type);
        }
        this.type = type;
        if (makeActive) {
            isActive = true;
            defaultParams = true;
        } else {
            isActive = false;
        }
        this.dataset_name = dataset_name;
    }
    
    public ClusteringParams(int type, String linkage_function, String distance_function, int leaf_ordering, String dataset_name) throws MultiSlideException {
        if (type != ClusteringParams.CLUSTERING_PARAM_TYPE_ROW && type != ClusteringParams.CLUSTERING_PARAM_TYPE_COL) {
            throw new MultiSlideException("Illegal Argument for type: " + type);
        }
        this.type = type;
        isActive = true;
        defaultParams = false;
        this.linkage_function = linkage_function;
        this.distance_function = distance_function;
        this.leaf_ordering = leaf_ordering;
        this.dataset_name = dataset_name;
    }
    
    public int getType() {
        return type;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean usesDefaultParams() {
        return defaultParams;
    }

    public String getLinkageFunction() {
        if (this.defaultParams) {
            return "Complete";
        } else {
            return linkage_function;
        }
    }

    public String getDistanceFunction() {
        if (this.defaultParams) {
            return "Euclidean";
        } else {
            return distance_function;
        }
    }

    public int getLeafOrdering() {
        return leaf_ordering;
    }
    
    public String getDatasetName() {
        return dataset_name;
    }
    
    public String getHashString() {
        return linkage_function + "_" + distance_function;
    }
}
