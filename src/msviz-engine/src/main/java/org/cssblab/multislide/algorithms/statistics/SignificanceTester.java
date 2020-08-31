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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.rdd.EmptyRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.cssblab.multislide.beans.data.ServerResponse;
import org.cssblab.multislide.beans.data.SignificanceTestingParams;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.data.table.Table;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.utils.HttpClientManager;
import org.cssblab.multislide.utils.Utils;

public class SignificanceTester implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public String CACHE_PATH;
    public String message;
    public int status;
    HttpClientManager analytics_engine_comm;
    HashMap <String, float[][]> cache;
    
    public SignificanceTester (String cache_path, HttpClientManager analytics_engine_comm) {
        this.CACHE_PATH = cache_path;
        this.status = 0;
        this.analytics_engine_comm = analytics_engine_comm;
        cache = new HashMap <String, float[][]> ();
    }
    
    /*
    private String makeCacheKey(String dataset_name, String phenotype, String test_type_parametric_nonparametric) {
        return dataset_name + "_" + phenotype + "_" + test_type_parametric_nonparametric;
    }
    */
    /*
    public boolean isCached(
            String dataset_name, String phenotype, 
            String test_type_parametric_nonparametric, int nGenes) {
        String cache_key = makeCacheKey(dataset_name, phenotype, test_type_parametric_nonparametric);
        if (this.cache.containsKey(cache_key)) {
            float[] significance_levels = cache.get(cache_key)[0];
            if (significance_levels.length == nGenes) {
                return true;
            }
        }
        return false;
    }
    */
    
    public Table computeSignificanceLevel (
            AnalysisContainer analysis, String dataset_name, Dataset <Row> expression_data, Dataset <Row> phenotype_data
    ) throws MultiSlideException, DataParsingException {
        
        /*
        Create a table to hold the return data
        */
        List<String[]> fields = new ArrayList<>();
        fields.add(new String[]{"_significance", Table.DTYPE_DOUBLE});
        fields.add(new String[]{"_fdr", Table.DTYPE_DOUBLE});
        fields.add(new String[]{"_index", Table.DTYPE_LONG});
        Table significance = new Table(fields, "_id");
        
        /*
        In case of empty input, return empty
        */
        if (expression_data.count() == 0 || phenotype_data.count() == 0) {
            return significance;
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
        
        SignificanceTestingParams params = analysis.global_map_config.significance_testing_params;
        
        try {
            
            expression_data.repartition(1)
                           .write()
                           .mode("overwrite")
                           .format("csv")
                           .option("delimiter", "\t")
                           .option("header", "true")
                           .save(request_path + File.separator + "expressions");
            
            phenotype_data.repartition(1)
                           .write()
                           .mode("overwrite")
                           .format("csv")
                           .option("delimiter", "\t")
                           .option("header", "true")
                           .save(request_path + File.separator + "phenotypes");
            
            HashMap <String, String> props = params.asMap();
            props.put("operation", "significance-testing");
            props.put("dataset_name", dataset_name);
            FileHandler.savePropertiesFile(request_path + File.separator + "_params", props);

        } catch (Exception e) {
            Utils.log_exception(e, "");
            throw new MultiSlideException("Failed to start significance tesing.");
        }
        
        String phenotype_datatype_string = 
                analysis.data.clinical_info.getPhenotypeDatatypeString(params.getPhenotype());
        
        HashMap <String, String> p = new HashMap <String, String> ();
        p.put("request_id", request_id);
        p.put("phenotype_datatype", phenotype_datatype_string);
        p.put("dtype", "float");
        if (params.getTestType().equalsIgnoreCase("Parametric")) {
            p.put("use_parametric", "True");
        } else {
            p.put("use_parametric", "False");
        }
        p.put("significance_level", params.getSignificanceLevel() + "");
        //p.put("test_type", params.getTestType());
        //p.put("apply_fdr", params.getApplyFDR()+"");
        p.put("fdr_rate", params.getFDRThreshold()+"");
        p.put("n_rows", expression_data.count()+"");
        p.put("n_cols", expression_data.columns().length+"");
        
        ServerResponse resp = analytics_engine_comm.doGet("do_significance_testing", p);
        if (resp.status == 0) {
            throw new MultiSlideException(resp.message + ". " + resp.detailed_reason);
        } else {
            try {
                
                String out_fname = request_path + File.separator + "result.txt";
                
                /*
                _id is original index, which will be used to join back with
                data, which will already have "_significance" and "_fdr", 
                hence the new columns are called "_significance2" and "_fdr2"
                */
                
                List<StructField> f = new ArrayList<>();
                f.add(DataTypes.createStructField("_id", DataTypes.LongType, false));
                f.add(DataTypes.createStructField("_significance", DataTypes.DoubleType, false));
                f.add(DataTypes.createStructField("_fdr", DataTypes.DoubleType, false));
                f.add(DataTypes.createStructField("_index", DataTypes.LongType, false));
                StructType schema = DataTypes.createStructType(f);
                
                Dataset <Row> res = analysis.spark_session.read()
                                                          .format("csv")
                                                          .option("sep", "\t")
                                                          .option("inferSchema", "true")
                                                          .option("header", "true")
                                                          .schema(schema)
                                                          .load(out_fname);
                
                significance.fromSparkDataframe(res.collectAsList(), true);
                return significance;
                
            } catch (Exception e) {
                throw new MultiSlideException("Failed to read significance analysis results");
            }
        }
        
    }
    
    /*
    
    public HashMap <String, Object> computeSignificanceLevel ( 
            float[][] data, 
            int nGenes,
            String dataset_name,
            String phenotype, 
            String phenotype_datatype_string,
            double significance_level,
            String test_type_parametric_nonparametric,
            double false_discovery_rate,
            ArrayList <Integer> available_col_indices
    ) throws MultiSlideException {
    
        HashMap <String, Object> retval = new HashMap <String, Object> ();
        
        String cache_key = makeCacheKey(dataset_name, phenotype, test_type_parametric_nonparametric);
        if (this.cache.containsKey(cache_key)) {
            float[] significance_levels = cache.get(cache_key)[0];
            float[] false_discovery_rates = cache.get(cache_key)[1];
            if (significance_levels.length == nGenes) {
                boolean[] gene_masks = new boolean[nGenes];
                ArrayList <Integer> significant_gene_indices = new ArrayList <Integer> ();
                float sl, fdr;
                for (int i=0; i<significance_levels.length; i++) {
                    sl = (float)significance_levels[i];
                    fdr = (float)false_discovery_rates[i];
                    if (sl <= significance_level && fdr <= false_discovery_rate) {
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
        
        String id = System.currentTimeMillis() + "";
        String in_file_id = dataset_name + "_" + phenotype + "_SigTestData_" + id + ".txt";
        String out_file_id = dataset_name + "_" + phenotype + "SigTestOutput_" + id + ".txt";
        FileHandler.saveDataMatrix(
                CACHE_PATH + File.separator + in_file_id, "\t", data
        );
        String significance_levels_fname = CACHE_PATH + File.separator + "SigTestOutput_" + id + ".txt";
        
        try {
            File sig_file = new File(significance_levels_fname);
            Files.deleteIfExists(sig_file.toPath());
            
        } catch (Exception e) {
            Utils.log_exception(e, "");
            throw new MultiSlideException("Failed to start significance tesing.");
        }
        
        HashMap <String, String> params = new HashMap <String, String> ();
        params.put("in_file_id", in_file_id);
        params.put("phenotype_datatype", phenotype_datatype_string);
        params.put("use_parametric", test_type_parametric_nonparametric);
        params.put("n_rows", data.length + "");
        params.put("n_cols", data[0].length + "");
        params.put("out_file_id", out_file_id);
        ServerResponse resp = analytics_engine_comm.doGet("do_significance_testing", params);

        if (resp.status == 0) {
            throw new MultiSlideException(resp.message + ". " + resp.detailed_reason);
        } else {
            try {
                double[][] sig_analysis_result = FileHandler.loadDoubleDelimData(significance_levels_fname, " ", false);
                float[] fdr = new float[nGenes];
                ArrayList <Integer> significant_gene_indices = new ArrayList <Integer> ();
                for (int i=0; i<significance_levels.length; i++) {
                    significance_levels[i] = 1;
                }
                int col_index;
                for (int i=0; i<sig_analysis_result.length; i++) {
                    col_index = available_col_indices.get(i);
                    significance_levels[col_index] = (float)sig_analysis_result[i][0];
                    fdr[col_index] = (float)sig_analysis_result[i][0];
                    if (significance_levels[col_index] <= significance_level && fdr[col_index] <= false_discovery_rate) {
                        gene_masks[col_index] = true;
                        significant_gene_indices.add(col_index);
                    }
                }
                retval.put("significance_levels", significance_levels);
                retval.put("gene_masks", gene_masks);
                retval.put("significant_gene_indices", significant_gene_indices);
                
                float[][] significance_and_fdr = new float[2][nGenes];
                significance_and_fdr[0] = significance_levels;
                significance_and_fdr[1] = fdr;
                this.cache.put(cache_key, significance_and_fdr);
                
                return retval;
            } catch (Exception e) {
                throw new MultiSlideException("Failed to read significance testing results");
            }
        }
    
    }

    */
    
    public void clearCache() {
        this.cache.clear();
    }

}
