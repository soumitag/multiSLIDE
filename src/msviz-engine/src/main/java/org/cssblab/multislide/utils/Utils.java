/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.data.table.Table;

/**
 *
 * @author abhikdatta
 */
public class Utils {
 
    public static int arrayMax(int[] a) {
        int max = Integer.MIN_VALUE;
        for (int i=0; i<a.length; i++) {
            if (a[i] > max) {
                max = a[i];
            }
        }
        return max;
    }
    
    public static Object deepCopy(Object object) {
        try {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
          outputStrm.writeObject(object);
          ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
          ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
          return objInputStream.readObject();
        }
        catch (Exception e) {
          e.printStackTrace();
          return null;
        }
    }
    
    public static String createUniqueID() {
        long time = System.currentTimeMillis();
        Random rand = new Random();
        rand.setSeed(time);
        int r = rand.nextInt(100000000);
        return "" + time + r;
    }
    
    public static void print(String str) {
        Utils.log_info(str);
    }
    
    public static void log(String message) {
        Logger logger = LogManager.getRootLogger();
        logger.debug(message);
    }
    
    public static void log_info(String message) {
        Logger logger = LogManager.getRootLogger();
        logger.info(message);
    }
    
    public static void log_debug(String message) {
        Logger logger = LogManager.getRootLogger();
        logger.debug(message);
    }
    
    public static void log_trace(String message) {
        Logger logger = LogManager.getRootLogger();
        logger.trace(message);
    }
    
    public static void log_error(String message) {
        Logger logger = LogManager.getRootLogger();
        logger.error(message);
    }
    
    public static void log_exception(Exception e, String desc) {
        Logger logger = LogManager.getRootLogger();
        logger.error(desc, e);
    }
    
    public static void log_dataset(Dataset <Row> d, int N, String log_level) {
        
        String[] cols = d.columns();
        
        List <Row> rows;
        if (N < 1) {
            rows = d.collectAsList();
        } else {
            rows = d.takeAsList(N);
        }
        
        String[][] values = new String[rows.size()][cols.length];
        for (int i=0; i<rows.size(); i++) {
            Row r = rows.get(i);
            for (int j=0; j<r.size(); j++) {
                Object t = r.get(j);
                if (t != null) {
                    values[i][j] = t.toString();
                } else {
                    values[i][j] = "null";
                }
            }
        }
        
        print(values, cols, rows.size(), log_level);
        
    }
    
    public static void log_table(Table table, int N, String log_level) throws MultiSlideException {
        
        List <String> colnames = new ArrayList<> ();
        colnames.add(table.getRowIndexName() + "(key)");
        colnames.addAll(table.columns());
        
        if (N < 1)
            N = table.count();
        
        String[][] values = new String[N][colnames.size()];
        int i = 0;
        for (String row_id: table) {
            int j = 0;
            values[i][j++] = row_id;
            for (String c: colnames.subList(1, colnames.size())) {
                Object t = table.get(row_id, c);
                if (t != null) {
                    values[i][j] = t.toString();
                } else {
                    values[i][j] = "null";
                }
                j++;
            }
            i++;
        }
        
        print(values, colnames.toArray(new String[0]), N, log_level);
        
    }
    
    public static void print(String[][] values, String[] cols, int n_rows, String log_level) {
        
        /*
        Get columns widths
        */
        
        int[] col_widths = new int[cols.length];
        for (int i=0; i<cols.length; i++) {
            col_widths[i] = cols[i].length();
        }
        
        for (int i=0; i<n_rows; i++) {
            for (int j=0; j<cols.length; j++) {
                if (values[i][j].length() > col_widths[j]) 
                    col_widths[j] = values[i][j].length();
            }
        }
        
        /*
        Compute table witdh
        */
        int table_width = 0;
        for (int j = 0; j < cols.length; j++) {
            table_width += col_widths[j] + 2;
        }
        table_width += 1;
        
        /*
        Print
        */
        String s = "\n";
        for (int i=0; i<table_width; i++) {
            s += "-";
        }
        s += "\n";
        
        /*
        Headers
        */
        s += "|";
        for (int j = 0; j < cols.length; j++) {
            s += Utils.pad_right(cols[j], col_widths[j] + 1) + "|";
        }
        s += "\n";

        
        for (int i=0; i<table_width; i++) {
            s += "-";
        }
        s += "\n";
        
        /*
        Values
        */
        for (int i=0; i<values.length; i++) {
            s += "|";
            for (int j=0; j<values[i].length; j++) {
                s += Utils.pad_right(values[i][j], col_widths[j]+1) + "|";
            }
            s += "\n";
        }
        
        for (int i=0; i<table_width; i++) {
            s += "-";
        }
        
        Utils._log(s, log_level);
    }
    
    public static String pad_right(String value, int width) {
        String s = value;
        for (int i=0; i<width - value.length(); i++) {
            s += " ";
        }
        return s;
    }
    
    public static void log_dataset_as_list(List <Row> d, String log_level) {
        
        if (d.isEmpty())
            Utils.log_info("dataset is empty");
        
        String[] cols = d.get(0).schema().fieldNames();
        
        String[][] values = new String[d.size()][cols.length];
        for (int i=0; i<d.size(); i++) {
            Row r = d.get(i);
            for (int j=0; j<r.size(); j++) {
                Object t = r.get(j);
                if (t != null) {
                    values[i][j] = t.toString();
                } else {
                    values[i][j] = "null";
                }
            }
        }
        
        print(values, cols, d.size(), "info");
    }
    
    public static void log_schema(Dataset <Row> d, String log_level) {
        Utils._log(d.schema().treeString(), log_level);
    }
    
    public static void _log(String msg, String log_level) {
        Logger logger = LogManager.getRootLogger();
        
        if (log_level.equalsIgnoreCase("info"))
            logger.info(msg);
        
        else if (log_level.equalsIgnoreCase("debug"))
            logger.debug(msg);
        
        else if (log_level.equalsIgnoreCase("trace"))
            logger.trace(msg);
        
        else if (log_level.equalsIgnoreCase("error"))
            logger.error(msg);
        
        else
            logger.info(msg);
    }
}
