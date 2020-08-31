/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import static org.apache.spark.sql.functions.col;
import org.apache.spark.sql.types.Metadata;

/**
 *
 * @author Soumita Ghosh and Abhik Datta
 */
public class CollectionUtils {
    
    public static List<String[]> zip(List<String> a, List<String> b) {
        List <String[]> ab = new ArrayList <> ();
        for (int j=0; j<a.size(); j++)
            ab.add(new String[]{a.get(j), b.get(j)});
        return ab;
    }

    public static Dataset<Row> asDataset(SparkSession spark_session, String column_name, int[] values, DataType dtype, boolean nullable) {
        List<StructField> fields = new ArrayList<>();
        fields.add(DataTypes.createStructField(column_name, dtype, nullable));
        List<Row> entrez_list = new ArrayList<>();
        for (int value : values) {
            entrez_list.add(RowFactory.create(new Object[]{value}));
        }
        StructType schema = DataTypes.createStructType(fields);
        Dataset<Row> dataset = spark_session.createDataFrame(entrez_list, schema);
        return dataset;
    }

    public static Seq<String> asSeq(List<String> a) {
        return JavaConverters.asScalaIteratorConverter(a.iterator()).asScala().toSeq();
    }

    public static Seq<String> asSeq(Set<String> a) {
        return JavaConverters.asScalaIteratorConverter(a.iterator()).asScala().toSeq();
    }

    public static Seq<String> asSeq(String[] a) {
        return CollectionUtils.asSeq(Arrays.asList(a));
    }

    public static Seq<String> asSeq(String a) {
        ArrayList<String> t = new ArrayList<>();
        t.add(a);
        return CollectionUtils.asSeq(t);
    }

    public static String[] asArray(ArrayList<String> a) {
        String[] b = new String[a.size()];
        int i = 0;
        for (String v : a) {
            b[i++] = v;
        }
        return b;
    }

    public static String[] asArray(List<String> a) {
        String[] b = new String[a.size()];
        int i = 0;
        for (String v : a) {
            b[i++] = v;
        }
        return b;
    }

    public static String[] asArray(Set<String> a) {
        String[] b = new String[a.size()];
        int i = 0;
        for (String v : a) {
            b[i++] = v;
        }
        return b;
    }
    
    public static boolean[] asBoolArray(List<Boolean> a) {
        boolean[] b = new boolean[a.size()];
        int i = 0;
        for (boolean v : a) {
            b[i++] = v;
        }
        return b;
    }
    
    public static List <String> asList(String[] a) {
        List <String> b = new ArrayList <> ();
        for (String s: a)
            b.add(s);
        return b;
    }

    public static String[] columnAsArray(List<Row> column) {
        String[] values = new String[column.size()];
        String value;
        int i = 0;
        for (Row row : column) {
            value = row.get(0).toString();
            values[i++] = value;
        }
        return values;
    }

    public static boolean arrayContains(String[] arr, String value) {
        for (String s : arr) {
            if (s.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /*
    Static utility methods for data access
     */
    public static String[] slice(String[] d, int start, int n) {
        String[] sub = new String[n];
        for (int i = 0; i < n; i++) {
            sub[i] = d[start + i];
        }
        return sub;
    }

    public static Column[] slice(Column[] d, int start, int n) {
        Column[] sub = new Column[n];
        for (int i = 0; i < n; i++) {
            sub[i] = d[start + i];
        }
        return sub;
    }

    public static Long[] slice(Long[] d, int start, int n) {
        Long[] sub = new Long[n];
        for (int i = 0; i < n; i++) {
            sub[i] = d[start + i];
        }
        return sub;
    }
    
    public static boolean[] slice(boolean[] d, int start, int n) {
        boolean[] sub = new boolean[n];
        for (int i = 0; i < n; i++) {
            sub[i] = d[start + i];
        }
        return sub;
    }

    public static ArrayList<String> columnAsList(List<Row> column) {
        ArrayList<String> values = new ArrayList<>();
        Object value;
        for (Row row : column) {
            value = row.get(0);
            if (value == null) {
                value = "";
            }
            values.add(value.toString());
        }
        return values;
    }

    public static HashMap<String, Boolean> asMap(ArrayList<String> a) {
        HashMap<String, Boolean> b = new HashMap<>();
        for (String item : a) {
            b.put(item, Boolean.TRUE);
        }
        return b;
    }

    public static HashMap<String, Boolean> asMap(List<String> a) {
        HashMap<String, Boolean> map = new HashMap<>();
        for (String s : a) {
            map.put(s, Boolean.TRUE);
        }
        return map;
    }

    public static HashMap<String, Boolean> asMap(String[] a) {
        HashMap<String, Boolean> b = new HashMap<>();
        for (String item : a) {
            b.put(item, Boolean.TRUE);
        }
        return b;
    }

    public static Column[] asColumnArray(ArrayList<String> colnames) {
        Column[] colList = new Column[colnames.size()];
        int i = 0;
        for (String name : colnames) {
            colList[i++] = col(name);
        }
        return colList;
    }

    public static Column[] asColumnArray(String[] colnames) {
        Column[] colList = new Column[colnames.length];
        int i = 0;
        for (String name : colnames) {
            colList[i++] = col(name);
        }
        return colList;
    }

    public static Column[] asColumnArray(List<String> colnames) {
        Column[] colList = new Column[colnames.size()];
        int i = 0;
        for (String name : colnames) {
            colList[i++] = col(name);
        }
        return colList;
    }

    public static Column[] asColumnArray(HashMap<String, Boolean> why) {
        Column[] colList = new Column[why.size()];
        int i = 0;
        for (String name : why.keySet()) {
            colList[i++] = col(name);
        }
        return colList;
    }

    public static List<String> asList(HashMap<String, Boolean> a) {
        List<String> b = new ArrayList<String>();
        for (String s : a.keySet()) {
            b.add(s);
        }
        return b;
    }
    
    /*
        appends column to dataset horizontally (along rows)
    */
    public static Dataset <Row> horizontalStack(
            SparkSession spark, Dataset <Row> df, List <String> column, 
            String column_name, DataType dtype, boolean nullable) {
        
        List <Row> a = df.collectAsList();
        List <Row> b = new ArrayList <> ();
        
        for (int j=0; j<a.size(); j++) {
            Row r = a.get(j);
            Object[] d = new Object[r.size()+1];
            for (int i=0; i<r.size(); i++) {
                d[i] = r.get(i);
            }
            d[r.size()] = column.get(j);
            b.add(RowFactory.create(d));
        }
        
        StructType newSchema = df.schema().add(new StructField(column_name,
                dtype, nullable, Metadata.empty()));
        
        return spark.createDataFrame(b, newSchema);
    }
    
    /*
        appends column to dataset horizontally (along rows)
    */
    public static Row horizontalStack(Row r, String column_value) {

        Object[] d = new Object[r.size() + 1];
        for (int i = 0; i < r.size(); i++) {
            d[i] = r.get(i);
        }
        d[r.size()] = column_value;
        Row new_row = RowFactory.create(d);

        return new_row;
    }
}
