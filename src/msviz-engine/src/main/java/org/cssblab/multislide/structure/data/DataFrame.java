/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.trim;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.data.table.Table;
import org.cssblab.multislide.utils.CollectionUtils;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.utils.FormElementMapper;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author Soumita Ghosh and Abhik Datta
 */
public class DataFrame implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public DatasetSpecs specs;
    public String name;
    public int linker_count;
    Dataset<Row> df;        // columns -> samples, rows -> features
    HashMap <String, List<Row>> ds;     // linker -> List of Rows
    
    /*
    entrez -> count map
    Entrez master for individual dataframes.
    Used to speed up multiple count() calls during search. 
    Directly making such fractured queries to spark is too expensive.
    Will be null if no linker column is present
    */
    public HashMap <String, Integer> entrez_master;
    public HashMap <String, List<String>> linker_entrez_map;
    public HashMap <String, List<String>> entrez_linker_map;
    
    /*
    public String[] sample_ids;
    public HashMap <String, Boolean> missing_rows_indicator;    // entrez => boolean
    public HashMap <String, ArrayList<Integer>> entrezPosMaps;
    public HashMap <Integer, String> positionEntrezMaps;
    */
    
    /*
        numeric expression columns, used for enrichment analysis based add genes
    */
    private List <String> expression_columns;
    
    public DataFrame (
            SparkSession spark,
            String analysis_basepath, 
            DatasetSpecs spec,
            ClinicalInformation clinical_info,
            Searcher searcher,
            long seed
    ) throws MultiSlideException, DataParsingException, IOException {
        
        this.specs = spec;
        this.name = specs.getUniqueName();
        
        /*
            0. check if column names have "."
        */
        String filepath = analysis_basepath + File.separator + specs.getFilenameWithinAnalysisFolder();
        String message = FileHandler.checkFileHeader(filepath, specs.getDelimiter());
        if (!message.equals("")) {
            throw new MultiSlideException(message);
        }
        
        /*
            1. Load both data amd metadata columns into ds
        */
        Dataset<Row> data = spark.read()
                                 .format("csv")
                                 .option("sep", FormElementMapper.parseDelimiter(specs.getDelimiter()))
                                 .option("inferSchema", "true")
                                 .option("header", "true")
                                 .option("nullValue", "NA")
                                 .load(filepath);
        
        /* 
            2. Drop metadata columns that are either not a linker column or an identifier column
        */
        HashMap <String,Boolean> metadata_column_names = CollectionUtils.asMap(specs.metadata_columns);
        HashMap <String,Boolean> usable_metadata_columns = CollectionUtils.asMap(specs.getLinkerAndIdentifierColumnNames());
        expression_columns = new ArrayList <> ();
        
        for (String col:data.columns()) {
            if (metadata_column_names.containsKey(col)) {
                if (!usable_metadata_columns.containsKey(col)) {
                    data = data.drop(col);
                }
            } else {
                expression_columns.add(col);
            }
        }

        /* 
            3. Create a schema
        */
        List<StructField> fields = new ArrayList<>();
        for (String cname: data.columns()) {
            if (usable_metadata_columns.containsKey(cname)) {
                data = data.withColumn(cname, col(cname).cast(DataTypes.StringType));
            } else {
                data = data.withColumn(cname, col(cname).cast(DataTypes.DoubleType));
            }
        }
        StructType schema = DataTypes.createStructType(fields);
        
        Utils.log_info("data schema:");
        Utils.log_info(data.schema().treeString());
        Utils.log_info("target schema:");
        Utils.log_info(schema.treeString());
        
        /*
            4. Parse dataset using schema
        */
        try {
            df = data.na()
                     .fill(Double.NaN);
        } catch (Exception e) {
            throw new MultiSlideException(
                    "Error when parsing dataset: " + specs.getUniqueName() 
                            + ". This could be because: 1. the linker or molecular-specific "
                                    + "identifier column have empty or missing values. "
                                    + "2: One or more expression values are non-numeric.");
        }
        for (String c : usable_metadata_columns.keySet()) {
            df = df.withColumn(c, trim(col(c)));
        }
        
        /*
            add pseudo-linker if no linker is present
        */
        if (!specs.has_linker) {
            List <Row> rows = df.collectAsList();
            long linker_value = seed;
            List <Row> a = new ArrayList <> ();
            List <String> _l = new ArrayList <> ();
            for (Row row: rows) {
                String _pseudo_linker = "" + linker_value++;
                _l.add(_pseudo_linker);
            }
            
            this.df = CollectionUtils.horizontalStack(spark, this.df, _l, 
                            "__pseudo__linker__", DataTypes.StringType, false);
        }
        
        /*
            5. create a HashMap: linker -> list of Rows
        */
        this.ds = new HashMap <> ();
        List <Row> rows = df.collectAsList();
        
        int linker_index = df.first().fieldIndex(specs.getLinkerColname());
        for (Row row : rows) {
            String linker_value = row.getString(linker_index);
            if (ds.containsKey(linker_value)) {
                ds.get(linker_value).add(row);
            } else {
                List<Row> a = new ArrayList<>();
                a.add(row);
                ds.put(linker_value, a);
            }
        }

        this.linker_count = ds.size();
        
        /*
            6. create a Map from linker column to entrez: linker -> list of entrez
        */
        if (specs.hasLinker() && !specs.linker_identifier_type.equals("mirna_id_2021158607524066")) {
            
            HashMap <String, Integer> identifier_index_map = AnalysisContainer.createIdentifierIndexMap();
            
            List <String> linker_values = new ArrayList <> ();
            for (String s: ds.keySet())
                linker_values.add(s);
            
            int identifier_type = identifier_index_map.get(specs.linker_identifier_type);
            mapToEntrez(searcher, linker_values, identifier_type);
            
            data.unpersist(false);
            
        } else {
            
            this.entrez_master = new HashMap <> ();
            this.linker_entrez_map = new HashMap <> ();
            this.entrez_linker_map = new HashMap <> ();
        
            long unknown_count = seed;
            for (String linker: ds.keySet()) {
                String entrez = "-" + ++unknown_count;        // the correct version
                //String entrez = "" + --unknown_count;       // wrong one
                
                this.entrez_master.put(entrez, 1);
                
                if (this.linker_entrez_map.containsKey(linker)) {
                    this.linker_entrez_map.get(linker).add(entrez);
                } else {
                    List <String> _e = new ArrayList <> ();
                    _e.add(entrez);
                    this.linker_entrez_map.put(linker, _e);
                }
                
                if (this.entrez_linker_map.containsKey(entrez)) {
                    this.entrez_linker_map.get(entrez).add(linker);
                } else {
                    List <String> _l = new ArrayList <> ();
                    _l.add(linker);
                    this.entrez_linker_map.put(entrez, _l);
                }
                
                Utils.log_info("e1: " + entrez);
            }
            
        }
        
        // check that all sample_ids in data file have a corresponding entry in clinical_info
    }
    
    public HashMap <String, Integer> getEntrezs() {
        return this.entrez_master;
    }
    
    public int getEntrezCounts(String entrez) {
        if (entrez_master.containsKey(entrez)) {
            return entrez_master.get(entrez);
        } else {
            return 0;
        }
    }
    
    /*
    public HashMap <String, Integer> getEntrezs() throws MultiSlideException {
        if (specs.hasLinker()) {
            return this.entrez_master;
            
        } else {
            throw new MultiSlideException("Requested entrez but no linker column was provided during create()");
        }
    }
    
    public int getEntrezCounts(String entrez) throws MultiSlideException {
        if (specs.hasLinker()) {
            if (entrez_master.containsKey(entrez)) {
                return entrez_master.get(entrez);
            } else {
                return 0;
            }
        } else {
            throw new MultiSlideException("Requested entrez but no linker column was provided during create()");
        }
    }
    */
    
    public final void mapToEntrez(Searcher searcher, List <String> values, int identifier_type) {
        
        this.entrez_master = new HashMap <> ();
        this.linker_entrez_map = new HashMap <> ();
        this.entrez_linker_map = new HashMap <> ();
        
        int unknown_count = 0;
        for (String value : values) {
            
            ArrayList <String> t = searcher.getEntrezFromDB(value, identifier_type);

            if (t.size() > 0) {
                this.linker_entrez_map.put(value, t);
            } else {
                List <String> t1 = new ArrayList <> ();
                t1.add("" + --unknown_count);
                this.linker_entrez_map.put(value, t1);
            }
            
            for (String s: t) {
                if (this.entrez_master.containsKey(s)) {
                    this.entrez_master.put(s, this.entrez_master.get(s)+1);
                } else {
                    this.entrez_master.put(s, 1);
                }
                
                if (this.entrez_linker_map.containsKey(s)) {
                    this.entrez_linker_map.get(s).add(value);
                } else {
                    List <String> t1 = new ArrayList <> ();
                    t1.add(value);
                    this.entrez_linker_map.put(s, t1);
                }
            }
                
        }
    }
    
    public String[] getMetaDataColumnNames() {
        return specs.metadata_columns;
    }
    
    public String getDefaultIdentifier() {
        if (this.specs.has_linker) {
            return this.specs.getLinkerColname();
        } else {
            return specs.getLinkerAndIdentifierColumnNames().get(0);
        }
    }
    
    public List <String> search_metadata (String column_name, ArrayList <String> search_terms) {
        
        /*
            a map from column_value -> linker
            for quick search
        */
        HashMap <String, List<String>> search_map = new HashMap <> ();
        for (String linker: this.ds.keySet()) {
            List <Row> rows = this.ds.get(linker);
            for (Row row: rows) {
                int column_index = row.fieldIndex(column_name);
                /*
                    create a reverse search map for quick matching
                */
                String column_value = row.get(column_index).toString();
                if (search_map.containsKey(column_value))
                    search_map.get(column_value).add(linker);
                else {
                    List <String> a = new ArrayList <> ();
                    a.add(linker);
                    search_map.put(column_value, a);
                }
            }
        }
        
        List <String> linkers = new ArrayList <> ();
        for (String s: search_terms) {
            if (search_map.containsKey(s)) {
                linkers.addAll(search_map.get(s));
            }
        }
        
        List <String> entrez = new ArrayList <> ();
        for (String l: linkers) {
            if (this.linker_entrez_map.containsKey(l)) {
                entrez.addAll(this.linker_entrez_map.get(l));
            }
        }
        
        return entrez;
        
    }
    
    public Table getDataForEnrichmentAnalysis() throws MultiSlideException, DataParsingException {

        /*
            Get expression data, which need not be ordered by _index. As the _id 
            is sent to analytics engine. When returning the result, analytics engine 
            must also return the original _id column as it is used to join the results to vu
         */
        
        if (!specs.has_linker) {
            throw new MultiSlideException("Enrichment analysis cannot be performed because no linker column was provided.");
        }
        
        List <String[]> column_names_dtypes = new ArrayList <> ();
        for (String s: expression_columns)
            column_names_dtypes.add(new String[]{s, Table.DTYPE_DOUBLE});
        Table table = new Table(column_names_dtypes, "_entrez");
        
        HashMap <String, Row> entrez_data = new HashMap <> ();
        for (String _linker: this.ds.keySet()) {
            Row row = this.ds.get(_linker).get(0);
            for (String entrez: this.linker_entrez_map.get(_linker))
                entrez_data.put(entrez, row);
        }
        table.fromSparkDataframe(entrez_data);
        
        return table;
    }
    
}
