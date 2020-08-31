/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.col;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.data.table.Table;
import org.cssblab.multislide.utils.CollectionUtils;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author Soumita Ghosh and Abhik Datta
 */
public class SampleIndex implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String[] values;
    private final List <String> list;
    private final Dataset <Row> df;
    private final Column[] cols;
    private final HashMap <String, Boolean> map;
    private final Table table;
    
    public SampleIndex(SparkSession spark, Table table) throws MultiSlideException, DataParsingException {
        
        this.table = table;
        
        this.list = new ArrayList <> ();
        this.values = new String[table.count()];
        this.cols = new Column[table.count()];
        this.map = new HashMap <> ();
        
        String value;
        int i = 0;
        for (String row_id: table) {
            value = table.getString(row_id, "_Sample_IDs");
            this.values[i] = value;
            this.list.add(value);
            this.cols[i] = col(value);
            this.map.put(value, Boolean.TRUE);
            i++;
        }
        
        this.df = table.asSparkDataframe(spark, true);
    }
    
    /*
    public SampleIndex(Dataset <Row> df) {
        this.df = df;
        List <Row> t = df.orderBy("_index").collectAsList();
        
        this.list = new ArrayList <> ();
        this.values = new String[t.size()];
        this.cols = new Column[t.size()];
        this.map = new HashMap <> ();
        
        String value;
        int i = 0;
        for (Row row:t) {
            value = row.getString(0);
            this.values[i] = value;
            this.list.add(value);
            this.cols[i] = col(value);
            this.map.put(value, Boolean.TRUE);
            i++;
        }
    }
    */
    
    public SampleIndex(HashMap <String, Boolean> map) {
        this.map = map;
        
        this.list = new ArrayList <> ();
        this.values = new String[map.size()];
        this.cols = new Column[map.size()];
        
        int i = 0;
        for (String value: map.keySet()) {
            this.values[i] = value;
            this.list.add(value);
            this.cols[i] = col(value);
            i++;
        }
        
        this.df = null;
        this.table = null;
    }
    
    public String[] asStringArray() {
        return this.values;
    }
    
    public List <String> asList() {
        return this.list;
    }
    
    public Dataset <Row> asDataset() {
        return this.df;
    }
    
    public Column[] asColumnArray() {
        return this.cols;
    }
    
    public boolean contains(String value) {
        return this.map.containsKey(value);
    }
    
    public Column[] sliceColumnArray(int start, int N) {
        return CollectionUtils.slice(cols, start, N);
    }
    
    public String[] sliceStringArray(int start, int N) {
        return CollectionUtils.slice(values, start, N);
    }
    
    public List <String> sliceList(int start, int N) {
        return list.subList(start, start+N);
    }

    public int count() {
        return this.values.length;
    }
}
