/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.algorithms.statistics;

/**
 *
 * @author soumitag
 */

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.cssblab.multislide.structure.ClinicalInformation;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.structure.MultiSlideException;

public class SignificanceTester implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public static final int SIGNIFICANCE_TEST_TYPE_ANOVA = 0;
    public static final int SIGNIFICANCE_TEST_TYPE_LINEAR_REGRESSION = 1;
    public static final int SIGNIFICANCE_TEST_TYPE_T_TEST = 2;
    
    public String PYTHON_HOME;
    public String PYTHON_MODULE_PATH;
    public String DATA_FILES_PATH;
    
    HashMap <String, float[]> cache;
    
    public SignificanceTester ( String data_files_path,
                                String python_module_path,
                                String python_home) {
        
        this.PYTHON_MODULE_PATH = python_module_path;
        this.PYTHON_HOME = python_home;
        this.DATA_FILES_PATH = data_files_path;
        
        cache = new HashMap <String, float[]> ();
    }
    
    private String makeCacheKey( String dataset_name,String phenotype) {
        return dataset_name + "_" + phenotype;
    }
    
    public boolean isCached(String dataset_name, String phenotype, int nGenes) {
        String cache_key = makeCacheKey(dataset_name,phenotype);
        if (this.cache.containsKey(cache_key)) {
            float[] significance_levels = cache.get(cache_key);
            if (significance_levels.length == nGenes) {
                return true;
            }
        }
        return false;
    }
    
    public HashMap <String, Object> computeSignificanceLevel ( 
            float[][] data, 
            int nGenes,
            String dataset_name,
            String phenotype, 
            int phenotype_datatype,
            double desired_significance_level,
            ArrayList <Integer> available_col_indices
    ) throws MultiSlideException {
    
        HashMap <String, Object> retval = new HashMap <String, Object> ();
        
        String cache_key = makeCacheKey(dataset_name,phenotype);
        if (this.cache.containsKey(cache_key)) {
            float[] significance_levels = cache.get(cache_key);
            if (significance_levels.length == nGenes) {
                boolean[] gene_masks = new boolean[nGenes];
                ArrayList <Integer> significant_gene_indices = new ArrayList <Integer> ();
                float sl;
                for (int i=0; i<significance_levels.length; i++) {
                    sl = (float)significance_levels[i];
                    if (sl <= desired_significance_level) {
                        gene_masks[i] = true;
                        significant_gene_indices.add(i);
                    }
                }
                retval.put("significance_levels", significance_levels);
                retval.put("gene_masks", gene_masks);
                retval.put("significant_gene_indices", significant_gene_indices);
                return retval;
            }
        }
        
        float[] significance_levels = new float[nGenes];
        boolean[] gene_masks = new boolean[nGenes];
        
        int test_type;
        switch (phenotype_datatype) {
            case ClinicalInformation.PHENOTYPE_DATATYPE_BINARY:
                test_type = SignificanceTester.SIGNIFICANCE_TEST_TYPE_T_TEST;
                break;
            case ClinicalInformation.PHENOTYPE_DATATYPE_CONTINUOUS:
                test_type = SignificanceTester.SIGNIFICANCE_TEST_TYPE_LINEAR_REGRESSION;
                break;
            case ClinicalInformation.PHENOTYPE_DATATYPE_CATEGORICAL:
                test_type = SignificanceTester.SIGNIFICANCE_TEST_TYPE_ANOVA;
                break;
            default:
                ArrayList <Integer> significant_gene_indices = new ArrayList <Integer> ();
                for (int i=0; i<significance_levels.length; i++) {
                    gene_masks[i] = true;
                    significant_gene_indices.add(i);
                }
                retval.put("significance_levels", significance_levels);
                retval.put("gene_masks", gene_masks);
                retval.put("significant_gene_indices", significant_gene_indices);
                return retval;
        }
        
        String id = System.currentTimeMillis() + "";
        FileHandler.saveDataMatrix(DATA_FILES_PATH + File.separator + dataset_name + "_" + phenotype + "_SigTestData.txt", "\t", data);
        
        String significance_levels_fname = DATA_FILES_PATH + File.separator + "SigTestOutput_" + id + ".txt";
        String error_fname = DATA_FILES_PATH + File.separator + "SigTestError_" + id + ".txt";
        
        try {

            File sig_file = new File(significance_levels_fname);
            Files.deleteIfExists(sig_file.toPath());
            
            File err_file = new File(error_fname);
            Files.deleteIfExists(err_file.toPath());
            
            ProcessBuilder pb = new ProcessBuilder(
                    PYTHON_HOME + File.separator + "python",
                    PYTHON_MODULE_PATH + File.separator + "significance_testing.py",
                    DATA_FILES_PATH,
                    dataset_name + "_" + phenotype + "_SigTestData.txt",
                    test_type + "",
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
            
        } catch (Exception e) {
            System.out.println(e);
            throw new MultiSlideException("Failed to start significance tesing.");
        }
        
        File sig_file = new File(significance_levels_fname);
        File err_file = new File(error_fname);
        
        int waiting = 0;
        while (true) {
            if (sig_file.exists() || err_file.exists()) {
                break;
            }
            if (waiting > 250) {
                throw new MultiSlideException("Significance tesing failed: waiting time exceeded.");
            }
            waiting++;
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(e);
                throw new MultiSlideException("Failed to track significance tesing.");
            }
        }
        System.out.println("Waited for: " + waiting);
        
        if (err_file.exists()) {
            throw new MultiSlideException("Significance tesing failed due to error.");
        } else {
            try {
                double[][] sig_levels = FileHandler.loadDoubleDelimData(significance_levels_fname, " ", false);
                ArrayList <Integer> significant_gene_indices = new ArrayList <Integer> ();
                for (int i=0; i<significance_levels.length; i++) {
                    significance_levels[i] = 1;
                }
                int col_index;
                for (int i=0; i<sig_levels.length; i++) {
                    col_index = available_col_indices.get(i);
                    significance_levels[col_index] = (float)sig_levels[i][0];
                    if (significance_levels[col_index] <= desired_significance_level) {
                        gene_masks[col_index] = true;
                        significant_gene_indices.add(col_index);
                    }
                }
                this.cache.put(cache_key, significance_levels);
                retval.put("significance_levels", significance_levels);
                retval.put("gene_masks", gene_masks);
                retval.put("significant_gene_indices", significant_gene_indices);
                return retval;
            } catch (Exception e) {
                throw new MultiSlideException("Failed to read significance testing results");
            }
        }
    
    }
    
    public void clearCache() {
        this.cache.clear();
    }
    
}
