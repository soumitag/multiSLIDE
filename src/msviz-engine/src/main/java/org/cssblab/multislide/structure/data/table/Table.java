/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure.data.table;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.data.Data;
import org.cssblab.multislide.utils.CollectionUtils;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitaghosh
 */

class Row implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public int[] _ints;
    public String[] _strings;
    public long[] _longs;
    public double[] _doubles;
    
    public Row(int nInts, int nStrings, int nLongs, int nDoubles) {
        this._ints = new int[nInts];
        this._strings = new String[nStrings];
        this._longs = new long[nLongs];
        this._doubles = new double[nDoubles];
    }
    /*
    Setters
    */
    public void set(int pos, int value) {
        this._ints[pos] = value;
    }
    
    public void set(int pos, String value) {
        this._strings[pos] = value;
    }
    
    public void set(int pos, long value) {
        this._longs[pos] = value;
    }
    
    public void set(int pos, double value) {
        this._doubles[pos] = value;
    }
    
    /*
    Getters
    */
    public String getString(int pos) {
        return this._strings[pos];
    }
    
    public int getInt(int pos) {
        return this._ints[pos];
    }
    
    public long getLong(int pos) {
        return this._longs[pos];
    }
    
    public double getDouble(int pos) {
        return this._doubles[pos];
    }
    
}

public class Table implements Serializable, Iterable<String> {
    
    private static final long serialVersionUID = 1L;
    
    public static final String DTYPE_INT = "int";
    public static final String DTYPE_STRING = "String";
    public static final String DTYPE_LONG = "long";
    public static final String DTYPE_DOUBLE = "double";
    
    public static final String AGG_MODE_ALL = "all";
    public static final String AGG_MODE_MAX = "max";
    public static final String AGG_MODE_MIN = "min";
    public static final String AGG_MODE_SUM = "sum";
    public static final String AGG_MODE_FIRST = "first";
    public static final String AGG_MODE_LAST = "last";
    public static final String AGG_MODE_AVG = "mean";
    public static final String AGG_MODE_COUNT = "count";
    
    private final HashMap <String, String> column_name_dtype_map;
    private final HashMap <String, Integer> column_name_position_map;
    private Map <String, Row> rows;
    private final String row_index_name;
    private final int nCols;
    
    private String numeric_aggregation_mode;
    private String non_numeric_aggregation_mode;
    
    public Table() {
        this.column_name_dtype_map = new HashMap <> ();
        this.column_name_position_map = new HashMap <> ();
        this.rows = new ListOrderedMap <> ();
        this.row_index_name = "";
        this.nCols = 0;
        this.numeric_aggregation_mode = AGG_MODE_LAST;
        this.non_numeric_aggregation_mode = AGG_MODE_LAST;
    }
    
    public Table(List <String[]> column_names_dtypes, String row_index_name)
            throws DataParsingException {
        
        this.column_name_dtype_map = new HashMap <> ();
        this.column_name_position_map = new HashMap <> ();
        this.rows = new ListOrderedMap <> ();
        int i = 0;
        for (String[] name_dtype: column_names_dtypes) {
            /*
                validate column names (including row_key) have no duplicates
            */
            if (this.column_name_dtype_map.containsKey(name_dtype[0]))
                throw new DataParsingException("Duplicate column names: " + name_dtype[0]);
            this.column_name_dtype_map.put(name_dtype[0], name_dtype[1]);
            this.column_name_position_map.put(name_dtype[0], i++);
        }
        /*
            validate row_key and column names don't overlap
        */
        if (this.column_name_dtype_map.containsKey(row_index_name))
            throw new DataParsingException("Row index name matches a column names: " + row_index_name);
        
        this.nCols = column_name_dtype_map.size();
        this.row_index_name = row_index_name;
        this.numeric_aggregation_mode = AGG_MODE_LAST;
        this.non_numeric_aggregation_mode = AGG_MODE_LAST;
    }
    
    private Row newRow() {
        return new Row(nCols,nCols,nCols,nCols);
    }
    
    public void setNumericAggregationMode(String agg_func) {
        this.numeric_aggregation_mode = agg_func;
    }
    
    public void setNonNumericAggregationMode(String agg_func) {
        this.non_numeric_aggregation_mode = agg_func;
    }
    
    private int _aggregate(int v1, int v2) throws MultiSlideException {
        
         if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_FIRST)) {
             return v1;
         } else if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_LAST)) {
             return v2;
         } if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_MIN)) {
             return Math.min(v1, v2);
         } if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_MAX)) {
             return Math.max(v1, v2);
         } if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_SUM)) {
             return (v1+v2);
         } else {
             throw new MultiSlideException("Bad running aggregation function for numeric data");
         }
    }
    
    private long _aggregate(long v1, long v2) throws MultiSlideException {
        
         if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_FIRST)) {
             return v1;
         } else if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_LAST)) {
             return v2;
         } if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_MIN)) {
             return Math.min(v1, v2);
         } if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_MAX)) {
             return Math.max(v1, v2);
         } if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_SUM)) {
             return (v1+v2);
         } else {
             throw new MultiSlideException("Bad running aggregation function for numeric data");
         }
    }
    
    private double _aggregate(double v1, double v2) throws MultiSlideException {
        
         if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_FIRST)) {
             return v1;
         } else if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_LAST)) {
             return v2;
         } if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_MIN)) {
             return Math.min(v1, v2);
         } if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_MAX)) {
             return Math.max(v1, v2);
         } if (this.numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_SUM)) {
             return (v1+v2);
         } else {
             throw new MultiSlideException("Bad running aggregation function for numeric data");
         }
    }
    
    private String _aggregate(String s1, String s2) throws MultiSlideException {
        
        if (this.non_numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_FIRST)) {
             return s1;
         } else if (this.non_numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_LAST)) {
             return s2;
         } if (this.non_numeric_aggregation_mode.equalsIgnoreCase(Table.AGG_MODE_ALL)) {
             return s1 + "," + s2;
         } else {
             throw new MultiSlideException("Bad running aggregation function for String data");
         }
        
    }
    
    public void set(String column_name, String row_key, int value) throws MultiSlideException {
        int col_pos = this.column_name_position_map.get(column_name);
        if (rows.containsKey(row_key)) {
            rows.get(row_key).set(col_pos, _aggregate(rows.get(row_key).getInt(col_pos), value));
        } else {
            Row row = newRow();
            row.set(col_pos, value);
            rows.put(row_key, row);
        }
    }
    
    public <T> void set(String column_name, String row_key, long value) throws MultiSlideException {
        int col_pos = this.column_name_position_map.get(column_name);
        if (rows.containsKey(row_key)) {
            rows.get(row_key).set(col_pos, _aggregate(rows.get(row_key).getLong(col_pos), value));
        } else {
            Row row = newRow();
            row.set(col_pos, value);
            rows.put(row_key, row);
        }
    }
    
    public <T> void set(String column_name, String row_key, double value) throws MultiSlideException {
        int col_pos = this.column_name_position_map.get(column_name);
        if (rows.containsKey(row_key)) {
            rows.get(row_key).set(col_pos, _aggregate(rows.get(row_key).getDouble(col_pos), value));
        } else {
            Row row = newRow();
            row.set(col_pos, value);
            rows.put(row_key, row);
        }
    }
    
    public void set(String column_name, String row_key, String value) throws MultiSlideException {
        int col_pos = this.column_name_position_map.get(column_name);
        if (rows.containsKey(row_key)) {
            rows.get(row_key).set(col_pos, _aggregate(rows.get(row_key).getString(col_pos), value));
        } else {
            Row row = newRow();
            row.set(col_pos, value);
            rows.put(row_key, row);
        }
    }

    @Override
    public Iterator <String> iterator() {
        return rows.keySet().iterator();
    }
    
    public boolean hasRowIndex(String row_index) {
        return this.rows.containsKey(row_index);
    }
    
    public int count() {
        return this.rows.size();
    }
    
    public List <String> columns() {
        List <String> column_names = new ArrayList <> ();
        for (String s: this.column_name_position_map.keySet())
            column_names.add(s);
        return column_names;
    }
    
    public List <String> dtypes() {
        List <String> dtypes = new ArrayList <> ();
        for (String s: this.column_name_dtype_map.keySet())
            dtypes.add(this.column_name_dtype_map.get(s));
        return dtypes;
    }
    
    public boolean containsColumn(String column_name) {
        return this.column_name_position_map.containsKey(column_name);
    }
    
    public String getRowIndexName() {
        return this.row_index_name;
    }
    
    public void drop(String column_name) {
        this.column_name_position_map.remove(column_name);
        this.column_name_dtype_map.remove(column_name);
    }
    
    public String getString(String row_key, String column_name) throws MultiSlideException {
        if (column_name.equalsIgnoreCase(this.row_index_name)) {
            return row_key;
        }
        if (!(column_name_dtype_map.get(column_name).equals(Table.DTYPE_STRING))) {
            throw new MultiSlideException("No column of type 'String' with name '" + column_name + "'");
        }
        int col_pos = this.column_name_position_map.get(column_name);
        return rows.get(row_key).getString(col_pos);
    }
    
    public int getInt(String row_key, String column_name) throws MultiSlideException {
        if (column_name.equalsIgnoreCase(this.row_index_name)) {
            return Integer.parseInt(row_key);
        }
        if (!(column_name_dtype_map.get(column_name).equals(Table.DTYPE_INT))) {
            throw new MultiSlideException("No column of type 'Integer' with name '" + column_name + "'");
        }
        int col_pos = this.column_name_position_map.get(column_name);
        return rows.get(row_key).getInt(col_pos);
    }
    
    public long getLong(String row_key, String column_name) throws MultiSlideException {
        if (column_name.equalsIgnoreCase(this.row_index_name)) {
            return Long.parseLong(row_key);
        }
        if (!(column_name_dtype_map.get(column_name).equals(Table.DTYPE_LONG))) {
            throw new MultiSlideException("No column of type 'Long' with name '" + column_name + "'");
        }
        int col_pos = this.column_name_position_map.get(column_name);
        return rows.get(row_key).getLong(col_pos);
    }
    
    public double getDouble(String row_key, String column_name) throws MultiSlideException {
        if (column_name.equalsIgnoreCase(this.row_index_name)) {
            return Double.parseDouble(row_key);
        }
        if (!(column_name_dtype_map.get(column_name).equals(Table.DTYPE_DOUBLE))) {
            throw new MultiSlideException("No column of type 'Integer' with name '" + column_name + "'");
        }
        int col_pos = this.column_name_position_map.get(column_name);
        return rows.get(row_key).getDouble(col_pos);
    }
    
    public Object get(String row_key, String column_name) throws MultiSlideException {
        
        if (column_name.equalsIgnoreCase(this.row_index_name)) {
            return row_key;
        }
        
        switch (this.column_name_dtype_map.get(column_name)) {
            case Table.DTYPE_INT:
                return this.getInt(row_key, column_name);
                
            case Table.DTYPE_STRING:
                return this.getString(row_key, column_name);
                
            case Table.DTYPE_LONG:
                return this.getLong(row_key, column_name);
                
            case Table.DTYPE_DOUBLE:
                return this.getDouble(row_key, column_name);
                
            default:
                throw new MultiSlideException("Unknown dtype for column name: " + column_name);
        }
        
    }
    
    public Object[] getRow(String row_key, boolean include_key) throws MultiSlideException {
        int n = nCols;
        if (include_key)
            n += 1;
                    
        Object[] d = new Object[n];
        int i=0;
        
        if (include_key)
            d[i++] = row_key;
        
        for (String s: this.column_name_position_map.keySet())
            d[i++] = this.get(row_key, s);
            
        return d;
    }
    
    public Table filter(List <String> row_ids) 
            throws MultiSlideException, DataParsingException {
        
        List <String> column_names = this.columns();
        List <String> dtypes = this.dtypes();
        
        List <String[]> names_dtypes = CollectionUtils.zip(column_names, dtypes);
        for (String[] s: names_dtypes)
            Utils.log_info("Column: " + s[0] + ", " + s[1]);
        
        Table table = new Table(names_dtypes, this.row_index_name);

        for (String row_id : row_ids) {
            if (this.rows.containsKey(row_id)) {
                for (String s : this.column_name_position_map.keySet())
                    _copy_value(this, table, s, row_id);
            }
        }

        return table;
    }
    
    public Table joinByKey(Table that, String join_type)
            throws MultiSlideException, DataParsingException {
            
        if (join_type.equals("outer")) {
            return outerJoinByKey(that);
            
        } else if (join_type.equals("inner")) {
            return innerJoinByKey(that);
            
        } else {
            throw new MultiSlideException("Join type '" + join_type + "' not implemented");
        }
    }
    
    private Table outerJoinByKey(Table that) 
            throws MultiSlideException, DataParsingException {
        
        List <String> column_names = this.columns();
        List <String> dtypes = this.dtypes();
        column_names.addAll(that.columns());
        dtypes.addAll(that.dtypes());
        
        List <String[]> names_dtypes = CollectionUtils.zip(column_names, dtypes);
        
        Table table = new Table(names_dtypes, this.row_index_name);

        for (String row_id : this.rows.keySet()) {
            for (String s : this.column_name_position_map.keySet())
                _copy_value(this, table, s, row_id);
        }

        for (String row_id : that.rows.keySet()) {
            for (String s : that.column_name_position_map.keySet())
                _copy_value(that, table, s, row_id);
        }

        return table;

    }
    
    private Table innerJoinByKey(Table that) 
            throws MultiSlideException, DataParsingException {
        
        List <String> column_names = this.columns();
        List <String> dtypes = this.dtypes();
        column_names.addAll(that.columns());
        dtypes.addAll(that.dtypes());
        
        List <String[]> names_dtypes = CollectionUtils.zip(column_names, dtypes);
        for (String[] s: names_dtypes)
            Utils.log_info("Column: " + s[0] + ", " + s[1]);
        
        Table table = new Table(names_dtypes, this.row_index_name);

        for (String row_id : this.rows.keySet()) {
            if (that.rows.containsKey(row_id)) {
                for (String s : this.column_name_position_map.keySet())
                    _copy_value(this, table, s, row_id);
                
                for (String s : that.column_name_position_map.keySet())
                    _copy_value(that, table, s, row_id);
            }
        }

        return table;

    }
    
    /*
        a utility function for joins
    */
    private static void _copy_value(Table from, Table to, String column_name, String row_key) 
            throws MultiSlideException, DataParsingException{
        
        switch (from.column_name_dtype_map.get(column_name)) {
            case Table.DTYPE_INT:
                to.set(column_name, row_key, from.getInt(row_key, column_name));
                break;
            case Table.DTYPE_STRING:
                to.set(column_name, row_key, from.getString(row_key, column_name));
                break;
            case Table.DTYPE_LONG:
                to.set(column_name, row_key, from.getLong(row_key, column_name));
                break;
            case Table.DTYPE_DOUBLE:
                to.set(column_name, row_key, from.getDouble(row_key, column_name));
                break;
            default:
                break;
        }
        
    }
    
    /*
    column_name: column that will be set as new row key
    keep_current_key: if true, the current row keys will be kept as a column 
                      in the new Table
    numeric_aggregation_function: specifies how to merge numeric column values of rows that have
                          same value in column_name (the new key). Can be one of:
                          max, min, mean, sum, first, last
    string_aggregation_function: specifies how to merge non-numeric column values of rows that have
                          same value in column_name (the new key). Can be one of:
                          all (values will be concatenated into a comma separated list), first, last
    */
    public Table resetRowIndex(
            String column_name, boolean keep_current_key, 
            String numeric_aggregation_function, String non_numeric_aggregation_function
    ) throws MultiSlideException, DataParsingException {
        
        List <String> column_names = this.columns();
        int i = column_names.indexOf(column_name);
        column_names.remove(column_name);
        if (keep_current_key)
            column_names.add(this.row_index_name);
        
        List <String> dtypes = this.dtypes();
        dtypes.remove(i);
        if (keep_current_key)
            dtypes.add(DTYPE_STRING);
        
        List <String[]> names_dtypes = CollectionUtils.zip(column_names, dtypes);
        
        Table table = new Table(names_dtypes, column_name);
        table.setNumericAggregationMode(numeric_aggregation_function);
        table.setNonNumericAggregationMode(non_numeric_aggregation_function);
        
        for (String row_id : this.rows.keySet()) {
            
            String new_key = this.getString(row_id, column_name);

            for (String s : column_names) {

                if (this.column_name_dtype_map.get(s).equals(Table.DTYPE_INT)) {
                    table.set(s, new_key, this.getInt(row_id, s));

                } else if (this.column_name_dtype_map.get(s).equals(Table.DTYPE_STRING)) {
                    table.set(s, new_key, this.getString(row_id, s));

                } else if (this.column_name_dtype_map.get(s).equals(Table.DTYPE_DOUBLE)) {
                    table.set(s, new_key, this.getDouble(row_id, s));

                } else if (this.column_name_dtype_map.get(s).equals(Table.DTYPE_LONG)) {
                    table.set(s, new_key, this.getLong(row_id, s));

                }
            }
            if (keep_current_key)
                table.set(this.row_index_name, new_key, row_id);
        }
        
        return table;

    }
    
    public void load(String filename, String separator, boolean file_has_row_key) 
            throws MultiSlideException, DataParsingException, IOException {
        
        String[][] raw = FileHandler.loadDelimData(filename, separator, false, -1);
        List <String> headers = Arrays.asList(raw[0]);
        
        HashMap <String, Integer> col_indices = new HashMap <> ();
        for(String c: this.column_name_dtype_map.keySet()) {
            int index = headers.indexOf(c);
            if (index == -1)
                throw new DataParsingException("Column '" + c + "' not found in file.");
            col_indices.put(c, index);
        }
        
        if (file_has_row_key) {
            int index = headers.indexOf(this.row_index_name);
            if (index == -1)
                throw new DataParsingException("Row index '" + this.row_index_name + "' not found in file.");
            col_indices.put(this.row_index_name, index);
        }
        
        int i = 0;
        List <String> columns = this.columns();
        for (String[] row_data: raw) {
            
            /*
            parse row_data
            */
            
            String row_key;
            if (file_has_row_key)
                row_key = row_data[col_indices.get(this.row_index_name)];
            else {
                row_key = "" + i++;
            }
            
            for (String c : columns.subList(1, columns.size())) {
                switch (this.column_name_dtype_map.get(c)) {
                    case Table.DTYPE_STRING:
                        this.set(c, row_key, row_data[col_indices.get(c)]);
                        break;
                    case Table.DTYPE_INT:
                        this.set(c, row_key, Integer.parseInt(row_data[col_indices.get(c)]));
                        break;
                    case Table.DTYPE_LONG:
                        this.set(c, row_key, Long.parseLong(row_data[col_indices.get(c)]));
                        break;
                    case Table.DTYPE_DOUBLE: 
                        this.set(c, row_key, Double.parseDouble(row_data[col_indices.get(c)]));
                        break;
                    default:
                        break;
                }
                    
            }
        }
        
    }
    
    public void fromSparkDataframe(HashMap <String, org.apache.spark.sql.Row> df) 
            throws MultiSlideException, DataParsingException {
        
        if (df.isEmpty())
            return;
        
        HashMap <String, Integer> col_indices = new HashMap <> ();
        for(String c: this.column_name_dtype_map.keySet()) {
            int index = df.get(df.keySet().iterator().next()).fieldIndex(c);
            col_indices.put(c, index);
        }
        
        List <String> columns = this.columns();
        for (String row_key: df.keySet()) {
            /*
                parse row_data
            */
            org.apache.spark.sql.Row row = df.get(row_key);
            
            for (String c : columns) {
                switch (this.column_name_dtype_map.get(c)) {
                    case Table.DTYPE_STRING:
                        this.set(c, row_key, row.getString(col_indices.get(c)));
                        break;
                    case Table.DTYPE_INT:
                        this.set(c, row_key, row.getInt(col_indices.get(c)));
                        break;
                    case Table.DTYPE_LONG:
                        this.set(c, row_key, row.getLong(col_indices.get(c)));
                        break;
                    case Table.DTYPE_DOUBLE: 
                        this.set(c, row_key, row.getDouble(col_indices.get(c)));
                        break;
                    default:
                        break;
                }
            }
        }
        
    }
    
    public void fromSparkDataframe(List <org.apache.spark.sql.Row> df, boolean df_has_row_key) 
            throws MultiSlideException, DataParsingException {
        
        if (df.isEmpty())
            return;
        
        HashMap <String, Integer> col_indices = new HashMap <> ();
        for(String c: this.column_name_dtype_map.keySet()) {
            int index = df.get(0).fieldIndex(c);
            col_indices.put(c, index);
        }
        
        if (df_has_row_key) {
            int index = df.get(0).fieldIndex(this.row_index_name);
            col_indices.put(this.row_index_name, index);
        }
        
        int i = 0;
        List <String> columns = this.columns();
        for (org.apache.spark.sql.Row row: df) {
            
            /*
            parse row_data
            */
            
            String row_key;
            if (df_has_row_key)
                row_key = row.get(col_indices.get(this.row_index_name)).toString();
            else {
                row_key = "" + i++;
            }
            
            for (String c : columns) {
                switch (this.column_name_dtype_map.get(c)) {
                    case Table.DTYPE_STRING:
                        this.set(c, row_key, row.getString(col_indices.get(c)));
                        break;
                    case Table.DTYPE_INT:
                        this.set(c, row_key, row.getInt(col_indices.get(c)));
                        break;
                    case Table.DTYPE_LONG:
                        this.set(c, row_key, row.getLong(col_indices.get(c)));
                        break;
                    case Table.DTYPE_DOUBLE: 
                        this.set(c, row_key, row.getDouble(col_indices.get(c)));
                        break;
                    default:
                        break;
                }
                    
            }
        }
        
    }
    
    public Dataset <org.apache.spark.sql.Row> asSparkDataframe(SparkSession spark, boolean include_row_key) 
            throws MultiSlideException, DataParsingException {
        
        List <String> columns = this.columns();
        
        List <StructField> fields = new ArrayList <> ();
        
        if (include_row_key)
            fields.add(DataTypes.createStructField(this.row_index_name, DataTypes.StringType, false));
        
        for (String s : columns) {
            
            switch (this.column_name_dtype_map.get(s)) {
                case Table.DTYPE_STRING:
                    fields.add(DataTypes.createStructField(s, DataTypes.StringType, true));
                    break;
                case Table.DTYPE_INT:
                    fields.add(DataTypes.createStructField(s, DataTypes.IntegerType, true));
                    break;
                case Table.DTYPE_LONG:
                    fields.add(DataTypes.createStructField(s, DataTypes.LongType, true));
                    break;
                case Table.DTYPE_DOUBLE:
                    fields.add(DataTypes.createStructField(s, DataTypes.DoubleType, true));
                    break;
                default:
                    break;
            }
        }
        
        List <org.apache.spark.sql.Row> df_rows = new ArrayList <> ();
        
        for (String row_id : this.rows.keySet()) {
            
            Object[] d = new Object[fields.size()];
            int i = 0;
            if (include_row_key)
                d[i++] = row_id;
            
            for (String s : columns) {
                d[i++] = this.get(row_id, s);
            }
            
            df_rows.add(RowFactory.create(d));
        }
        
        StructType schema = DataTypes.createStructType(fields);
        Dataset<org.apache.spark.sql.Row> df = spark.createDataFrame(df_rows, schema);
        return df;
    }
    
    public void show() throws MultiSlideException {
        Utils.log_table(this, 0, "info");
    }
    
    public void save(String folderpath, String filename, String delimiter, boolean with_header) throws MultiSlideException {
        
        List <String> colnames = new ArrayList<> ();
        colnames.add(this.getRowIndexName());
        colnames.addAll(this.columns());
        
        int N = this.count();
        
        int i;
        String[][] values;
        if (with_header) {
            values = new String[N+1][colnames.size()];
            values[0] = CollectionUtils.asArray(colnames);
            i = 1;
        } else {
            values = new String[N][colnames.size()];
            i = 0;
        }
        
        for (String row_id: this) {
            int j = 0;
            values[i][j++] = row_id;
            for (String c: colnames.subList(1, colnames.size())) {
                Object t = this.get(row_id, c);
                if (t != null) {
                    values[i][j] = t.toString();
                } else {
                    values[i][j] = "null";
                }
                j++;
            }
            i++;
        }
        
        if (FileHandler.makeDirectoryPath(folderpath)) {
            FileHandler.saveDataMatrix(folderpath + File.separator + filename, delimiter, values);
        } else {
            throw new MultiSlideException("Enrichment analysis could not create target directory in cache");
        }
    }
    
}
