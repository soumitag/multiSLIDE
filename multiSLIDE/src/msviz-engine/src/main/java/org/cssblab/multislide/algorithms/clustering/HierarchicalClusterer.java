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
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.Data;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.structure.MultiSlideException;


/**
 *
 * @author Soumita
 */
public class HierarchicalClusterer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final int SINGLE_LINKAGE      = 0;
    public static final int COMPLETE_LINKAGE    = 1;
    public static final int AVERAGE_LINKAGE     = 2;
    public static final int MEDIAN_LINKAGE      = 3;
    public static final int CENTROID_LINKAGE    = 4;
    public static final int WARD_LINKAGE        = 5;
    public static final int WEIGHTED_LINKAGE    = 6;
    
    public static final int EUCLIDEAN_DISTANCE      = 0;
    public static final int CITYBLOCK_DISTANCE      = 1;
    public static final int COSINE_DISTANCE         = 2;
    public static final int CORRELATION_DISTANCE    = 3;
    public static final int CHEBYSHEV_DISTANCE      = 4;
    
    public String PYTHON_HOME;
    public String PYTHON_MODULE_PATH;
    public String DATA_FILES_PATH;

    HashMap <String, String> cache;
    
    public HierarchicalClusterer (String data_files_path,
                                  String python_module_path,
                                  String python_home) {
        
        this.PYTHON_MODULE_PATH = python_module_path;
        this.PYTHON_HOME = python_home;
        this.DATA_FILES_PATH = data_files_path;
        
        this.cache = new HashMap <String, String> ();
    }
    
    public HierarchicalClusterer () { }

    public BinaryTree doClustering(Float[][] data,
                                   AnalysisContainer analysis,
                                   String dataset_name,
                                   int type,
                                   String linkage, 
                                   String distance_function, 
                                   int leaf_ordering) throws MultiSlideException {
        
        String key = getCacheKey(analysis, type);
        if (this.cache.containsKey(key)) {
            String filetag = cache.get(key);
            String linkage_tree_fname = DATA_FILES_PATH + File.separator + "ClusteringOutput_" + filetag + ".txt";
            double[][] linkage_tree = null;
            try {
                linkage_tree = FileHandler.loadDoubleDelimData(linkage_tree_fname, " ", false);
            } catch (Exception e) {
                throw new MultiSlideException("Cannot read linkage file " + linkage_tree_fname);
            }
            return new BinaryTree(linkage_tree, leaf_ordering);
        } else {
            try {
                return doClustering(data, analysis, dataset_name, type, linkage, distance_function, leaf_ordering, key);
            } catch (Exception e) {
                throw new MultiSlideException("Exception in HierarchicalClustering.java");
            }
        }
    }
    
    private String getCacheKey (AnalysisContainer analysis, int type) throws MultiSlideException { 
        String cluster_params_hash_string = analysis.getClusteringParams(type).getHashString();
        String key = cluster_params_hash_string;
        return key;
    }

    public BinaryTree doClustering (Float[][] data, AnalysisContainer analysis, String dataset_name, int type, String linkage, String distance_function, int leaf_ordering, String key)
    throws MultiSlideException {
        
        String id = System.currentTimeMillis() + "";
        FileHandler.saveDataMatrix(DATA_FILES_PATH + File.separator + type + "_InData.txt", 
                "\t", data
        );
        String linkage_tree_fname = DATA_FILES_PATH + File.separator + type + "_ClusteringOutput_" + id + ".txt";
        String error_fname = DATA_FILES_PATH + File.separator + type + "_ClusteringError_" + id + ".txt";
        
        try {

            File link_file = new File(linkage_tree_fname);
            
            Files.deleteIfExists(link_file.toPath());

            ProcessBuilder pb = new ProcessBuilder(
                    PYTHON_HOME + File.separator + "python",
                    PYTHON_MODULE_PATH + File.separator + "fast_hierarchical_clustering.py",
                    DATA_FILES_PATH,
                    type + "_InData.txt",
                    linkage,
                    distance_function,
                    id
            );
            System.out.println(pb.toString());
            
            pb.directory(new File(PYTHON_MODULE_PATH));
            File log = new File(PYTHON_MODULE_PATH + File.separator + "log.txt");
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
            Process p = pb.start();
            assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
            assert pb.redirectOutput().file() == log;
            assert p.getInputStream().read() == -1;

            //p.destroyForcibly();
        } catch (Exception e) {
            System.out.println(e);
            throw new MultiSlideException("Failed to start clustering.");
        }

        File link_file = new File(linkage_tree_fname);
        File error_file = new File(error_fname);
        
        boolean isClusteringSuccessful = true;
        int waiting = 0;
        while (!link_file.exists() && !error_file.exists()) {
            waiting++;
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(e);
            }
            if (waiting > 6000) {
                isClusteringSuccessful = false;
                break;
            }
        }
        System.out.println("Waited for: " + waiting);
        
        if (error_file.exists()) {
            isClusteringSuccessful = false;
        }

        if (isClusteringSuccessful) {
            // wait another 2 secs for filewrite to finish
            try {
                TimeUnit.MILLISECONDS.sleep(2000);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(e);
            }
        }

        if (isClusteringSuccessful) {
            double[][] linkage_tree = null;
            try {
                linkage_tree = FileHandler.loadDoubleDelimData(linkage_tree_fname, " ", false);
            } catch (Exception e) {
                throw new MultiSlideException("Cannot read linkage file " + linkage_tree_fname);
            }
            cache.put(key, id);
            return new BinaryTree(linkage_tree, leaf_ordering);
        } else {
            throw new MultiSlideException("Failed to perform clustering.");
        }

    }
    
    public void clearCache() {
        this.cache.clear();
    }
    
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
    
}