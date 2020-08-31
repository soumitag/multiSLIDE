/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.desc;
import static org.apache.spark.sql.functions.max;
import static org.apache.spark.sql.functions.min;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.resources.MultiSlideContextListener;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.utils.CollectionUtils;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author Soumita
 */
public class ClinicalInformation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private transient final int min = 0;
    private transient final int max = 255;
    
    public static final int PHENOTYPE_DATATYPE_UNARY       = 1;
    public static final int PHENOTYPE_DATATYPE_BINARY      = 2;
    public static final int PHENOTYPE_DATATYPE_CATEGORICAL = 3;
    public static final int PHENOTYPE_DATATYPE_CONTINUOUS  = 0;
    public static final int PHENOTYPE_DATATYPE_ALL_BLANKS  = 4;
    
    public static final String PHENOTYPE_BINARY_DESC_STRING      = "binary";
    public static final String PHENOTYPE_CATEGORICAL_DESC_STRING = "categorical";
    public static final String PHENOTYPE_CONTINUOUS_DESC_STRING  = "continuous";
    
    public static final double[] BLANK_COLOR = new double[]{200,200,200};
    
    public ArrayList <short[]> standard_categorical_colors;
    public ArrayList <short[]> standard_continuous_colors;
    
    Dataset <Row> phenotype_data;
    
    public HashMap <String, HashMap <String, double[]>> phenotypeColorMap;
    HashMap <String, Integer> phenotypeDatatypeMap;
    HashMap <String, double[]> continuousPhenotypeMinMaxMap;
    
    public ClinicalInformation(ColorPalette categorical_palette, ColorPalette continuous_palette) {
        this.standard_categorical_colors = categorical_palette.palette;
        this.standard_continuous_colors = continuous_palette.palette;
    }
    
    public long loadClinicalInformation(SparkSession spark_session, String filename, String delimiter) throws MultiSlideException {
        try {
            /*
            Load data
            */
            Dataset<Row> phen_data = spark_session.read()
                                                  .format("csv")
                                                  .option("sep", delimiter)
                                                  .option("inferSchema", "true")
                                                  .option("header", "true")
                                                  .option("nullValue", "NA")
                                                  .load(filename)
                                                  .repartition(MultiSlideContextListener.N_SPARK_PARTITIONS)
                                                  .cache();
            /*
            phen_data.head();
            phen_data.printSchema();
            */
            
            /*
            Rename the first column: "_Sample_IDs"
            */
            String[] data_columns = phen_data.columns();
            phen_data = phen_data.withColumnRenamed(data_columns[0], "_Sample_IDs");
            
            /*
            Parse column values to find data type and create Schema
            */
            List<StructField> fields = new ArrayList<>();
            fields.add(DataTypes.createStructField("_Sample_IDs", DataTypes.StringType, false));
            
            phenotypeDatatypeMap = new HashMap <> ();
            phenotypeColorMap = new HashMap <> ();
            
            for (int i=1; i<data_columns.length; i++) {
                
                ArrayList <String> values = CollectionUtils.columnAsList(
                        phen_data.select(data_columns[i])
                                 .distinct()
                                 .sort(data_columns[i])
                                 .collectAsList());
                
                boolean hasBlanks = this.hasBlanks(values);
                int dtype = parseDatatype(values, hasBlanks);
                phenotypeDatatypeMap.put(data_columns[i], dtype);
                
                if (ClinicalInformation.PHENOTYPE_DATATYPE_CONTINUOUS == dtype) {
                    StructField field = DataTypes.createStructField(data_columns[i], DataTypes.DoubleType, false);
                    fields.add(field);
                } else {
                    //keep current datatype
                    StructField field = DataTypes.createStructField(
                            data_columns[i], 
                            phen_data.schema().apply(data_columns[i]).dataType(),
                            false);
                    fields.add(field);
                }

                /*
                Create colormaps for each phenotype
                */
                if (phenotypeDatatypeMap.get(data_columns[i]) != ClinicalInformation.PHENOTYPE_DATATYPE_CONTINUOUS) {
                    phenotypeColorMap.put(data_columns[i], this.createColorMap(dtype, values, hasBlanks));
                } else {
                    /*
                    for continuous data simply put an empty colormap, as a 
                    standard acolormap is used in Legends.java
                    */
                    HashMap <String, double[]> colormap = new HashMap <> ();
                    phenotypeColorMap.put(data_columns[i], colormap);
                }
            }
            
            /*
            Apply schema to make "phenotype" dataset
            */
            StructType schema = DataTypes.createStructType(fields);
            ExpressionEncoder<Row> encoder = RowEncoder.apply(schema);
            this.phenotype_data = phen_data.as(encoder)
                                           .repartition(MultiSlideContextListener.N_SPARK_PARTITIONS).cache();
            
            /*
            For continuous phenotypes create Min-Max map
            */
            continuousPhenotypeMinMaxMap = new HashMap <> ();        
            for (int i=1; i<data_columns.length; i++) {
                if (phenotypeDatatypeMap.get(data_columns[i]) == ClinicalInformation.PHENOTYPE_DATATYPE_CONTINUOUS) {
                    Row _min_max = this.phenotype_data.agg(min(data_columns[i]), max(data_columns[i])).head();
                    continuousPhenotypeMinMaxMap.put(data_columns[i], 
                            new double[]{Double.parseDouble(_min_max.get(0).toString()), 
                                         Double.parseDouble(_min_max.get(1).toString())});
                }
            }
            
            phen_data.unpersist(false);
            
            return this.phenotype_data.count();
            
        } catch (Exception e) {
            Utils.log_exception(e, "");
            throw new MultiSlideException("Exception in ClinicalInformation.loadClinicalInformation(): \n" + e.getMessage());
        }
    }
    
    private boolean hasBlanks(ArrayList <String> values) {
        boolean hasBlanks = false;
        for (String value : values) {
            if (value == null || value.equals("")) {
                hasBlanks = true;
            }
        }
        return hasBlanks;
    }
    
    private int parseDatatype(ArrayList <String> values, boolean hasBlanks) {
        
        int nValues = values.size();
        
        if (nValues == 0) {
            return ClinicalInformation.PHENOTYPE_DATATYPE_ALL_BLANKS;

        } else if (nValues == 1) {
            return ClinicalInformation.PHENOTYPE_DATATYPE_UNARY;
            
        } else if (nValues == 2) {
            return ClinicalInformation.PHENOTYPE_DATATYPE_BINARY;

        } else if (nValues > 2) {
            
            if (hasBlanks) {
                return ClinicalInformation.PHENOTYPE_DATATYPE_CATEGORICAL;
            }
                
            for (String value : values) {
                try {
                    double d = Double.parseDouble(value);
                } catch (NumberFormatException nfe) {
                    return ClinicalInformation.PHENOTYPE_DATATYPE_CATEGORICAL;
                }
            }
            
            return ClinicalInformation.PHENOTYPE_DATATYPE_CONTINUOUS;

        }
 
        return -1;
    }
    
    private HashMap <String, double[]> createColorMap(int dtype, ArrayList <String> values, boolean hasBlanks) {
        
        HashMap <String, double[]> colormap = new HashMap <String, double[]> ();
        
        switch (dtype) {
            case ClinicalInformation.PHENOTYPE_DATATYPE_UNARY:
                double[] color = new double[]{177, 89, 40};
                colormap.put(values.get(0), color);
                break;
            case ClinicalInformation.PHENOTYPE_DATATYPE_BINARY:
                {
                    double[][] colors = new double[2][3];
                    colors[0] = new double[]{255, 126, 62};
                    colors[1] = new double[]{38, 156, 211};
                    int c = 0;
                    for (String phen_value : values) {
                        colormap.put(phen_value, colors[c++]);
                    }       
                    break;
                }
            case ClinicalInformation.PHENOTYPE_DATATYPE_CATEGORICAL:
                {
                    double[][] colors = getCategoricalColors(values.size());
                    int c = 0;
                    for (String phen_value : values) {
                        colormap.put(phen_value, colors[c++]);
                    }       
                    break;
                }
            default:
                break;
        }
        
        if (hasBlanks) {
            colormap.put("", ClinicalInformation.BLANK_COLOR);
        }
        
        return colormap;
    }
    
    /*
    private void parsePhenotypeDatatypes(int nSamples) {
        
        continuousPhenotypeMinMaxMap = new HashMap <String, double[]> ();        
        phenotypeColorMap = new HashMap <String, HashMap <String, double[]>> ();
        phenotypeValueSortMap = new HashMap <String, HashMap <String, Integer>> ();
        
        int nPhenotypes = phenotypeKeyPositionMap.size();
        String[][] temp = new String[nSamples][nPhenotypes];
        int i = 0;
        for (Map.Entry pair : samplePhenotypeMap.entrySet()) {
            temp[i++] = (String[])pair.getValue();
        }
        
        int[] dtypes = new int[nPhenotypes];
        for (Map.Entry pair : phenotypeKeyPositionMap.entrySet()) {
        
            int j = (int)pair.getValue();
            String phen_name = (String)pair.getKey();
            
            HashMap <String, Boolean> t = new HashMap <String, Boolean> ();
            boolean hasBlanks = false;
            for (i=0; i<nSamples; i++) {
                if (temp[i][j] != null && !temp[i][j].equals("")) {
                    t.put(temp[i][j], Boolean.TRUE);
                } else {
                    hasBlanks = true;
                }
            }
            
            ArrayList <String> t1 = new ArrayList <String> ();
            for (String phen_value : t.keySet()) {
                t1.add(phen_value);
            }
            Collections.sort(t1);
            
            int nValues = t1.size();
            
            HashMap <String, double[]> colormap = new HashMap <String, double[]> ();
            HashMap <String, Integer> phen_value_sort_map = new HashMap <String, Integer> ();
            
            if (nValues == 0) {
                dtypes[j] = ClinicalInformation.PHENOTYPE_DATATYPE_ALL_BLANKS;
            } else if (nValues == 1) {
                dtypes[j] = ClinicalInformation.PHENOTYPE_DATATYPE_UNARY;
                double[] color = new double[]{177,89,40};
                colormap.put(t1.get(0), color);
                phen_value_sort_map.put(t1.get(0), 0);
            } else if (nValues == 2) {
                dtypes[j] = ClinicalInformation.PHENOTYPE_DATATYPE_BINARY;
                double[][] colors = new double[2][3];
                colors[0] = new double[]{255,126,62};
                colors[1] = new double[]{38,156,211};
                int c = 0;
                for (String phen_value : t1) {
                    colormap.put(phen_value, colors[c]);
                    phen_value_sort_map.put(phen_value, c++);
                }
            } else if (nValues > 2) {
                
                boolean isContinuous = true;
                int count = 0;
                double[] parsed_values = new double[t1.size()];
                for (String phen_value : t1) {
                    if (phen_value.equals("")) {
                        isContinuous = false;
                        break;
                    }
                    try {
                        double d = Double.parseDouble(phen_value);
                        parsed_values[count++] = d;
                    } catch (NumberFormatException nfe) {
                        isContinuous = false;
                        break;
                    }
                }
                
                if (!isContinuous) {
                    dtypes[j] = ClinicalInformation.PHENOTYPE_DATATYPE_CATEGORICAL;
                    double[][] colors = getCategoricalColors(nValues);
                    int c = 0;
                    for (String phen_value : t1) {
                        colormap.put(phen_value, colors[c]);
                        phen_value_sort_map.put(phen_value, c++);
                    }
                } else {
                    dtypes[j] = ClinicalInformation.PHENOTYPE_DATATYPE_CONTINUOUS;
                    double[] min_max = new double[2];
                    min_max[0] = Double.POSITIVE_INFINITY;
                    min_max[1] = Double.NEGATIVE_INFINITY;
                    for (int p=0; p<parsed_values.length; p++) {
                        if (parsed_values[p] < min_max[0]) {
                            min_max[0] = parsed_values[p];
                        }
                        if (parsed_values[p] > min_max[1]) {
                            min_max[1] = parsed_values[p];
                        }
                    }
                    continuousPhenotypeMinMaxMap.put(phen_name, min_max);
                    
                }
            }
            
            if (hasBlanks) {
                colormap.put("", ClinicalInformation.BLANK_COLOR);
                phen_value_sort_map.put("", phen_value_sort_map.size());
            }
            
            phenotypeColorMap.put(phen_name, colormap);
            phenotypeValueSortMap.put(phen_name, phen_value_sort_map);
        }
        
        phenotypeDatatypeMap = new HashMap <String, Integer> ();
        for (Map.Entry pair : phenotypeKeyPositionMap.entrySet()) {
            String key = (String)pair.getKey();
            int index = (int)pair.getValue();
            phenotypeDatatypeMap.put(key, dtypes[index]);
        }
    }
    */
    
    public String[] getSampleIDs() {
        return null;
    }
    
    public String getPhenotypeValue (String sample_id, String phenotype_name) {
        Dataset <Row> t = this.phenotype_data.select(phenotype_name).filter(col("_Sample_IDs").equalTo(sample_id));
        return t.head().getString(0);
    }
    
    /*
    public String getPhenotypeValue (String patient_id, String phenotype) {
        String[] p = samplePhenotypeMap.get(patient_id);
        int index = phenotypeKeyPositionMap.get(phenotype.toLowerCase());
        return p[index];
    }
    */
    
    public ArrayList <String> getPhenotypeValuesForAllSamples (String phenotype_name) {
        List <Row> t = this.phenotype_data.select(phenotype_name).collectAsList();
        return CollectionUtils.columnAsList(t);
    }
    
    public ArrayList <String> getPhenotypeValuesForAllSamples (ArrayList <String> phenotype_names) {
        List <Row> t = this.phenotype_data.select(CollectionUtils.asColumnArray(phenotype_names)).collectAsList();
        return CollectionUtils.columnAsList(t);
    }
    
    public ArrayList <String> getPhenotypeValues (String phenotype_name, ArrayList <String> sample_ids) {
        List <Row> t = this.phenotype_data.select(phenotype_name).filter(col("_Sample_IDs").isInCollection(sample_ids)).collectAsList();
        return CollectionUtils.columnAsList(t);
    }
    
    public ArrayList <String> getPhenotypeValues (ArrayList <String> phenotype_names, ArrayList <String> sample_ids) {
        List <Row> t = this.phenotype_data
                .select(CollectionUtils.asColumnArray(phenotype_names))
                .filter(col("_Sample_IDs")
                        .isInCollection(sample_ids))
                .collectAsList();
        return CollectionUtils.columnAsList(t);
    }
    
    /*
    public String[] getPhenotypeValues (String phenotype) {
        int index = phenotypeKeyPositionMap.get(phenotype.toLowerCase());
        String[] phenotype_values = new String[samplePhenotypeMap.size()];
        int i = 0;
        for (String patient_id : samplePhenotypeMap.keySet()) {
            phenotype_values[i++] = samplePhenotypeMap.get(patient_id)[index];
        }
        return phenotype_values;
    }
    */
    
    public String[] getPhenotypeNames() {
        String[] colnames = this.phenotype_data.columns();
        return CollectionUtils.slice(colnames, 1, colnames.length-1);
    }
    
    public HashMap <String, HashMap <String, Integer>> getDistinctPhenotypeValues() {
        HashMap <String, HashMap <String, Integer>> phenotype_name_distinct_values_map = new HashMap <> ();
        String[] pnames = this.getPhenotypeNames();
        for (String pname:pnames) {
            phenotype_name_distinct_values_map.put(pname, this.getDistinctPhenotypeValues(pname));
        }
        return phenotype_name_distinct_values_map;
    }
    
    public HashMap <String, HashMap <String, Integer>> getDistinctPhenotypeValues(String[] phenotype_names) {
        HashMap <String, HashMap <String, Integer>> phenotype_name_distinct_values_map = new HashMap <> ();
        for (String pname:phenotype_names) {
            phenotype_name_distinct_values_map.put(pname, this.getDistinctPhenotypeValues(pname));
        }
        return phenotype_name_distinct_values_map;
    }
    
    public HashMap <String, Integer> getDistinctPhenotypeValues(String phenotype_name) {
        List <Row> rows = this.phenotype_data
                .select(phenotype_name)
                .groupBy(phenotype_name)
                .count()
                .sort(desc("count"))
                .collectAsList();
        HashMap <String, Integer> vc = new HashMap <> ();
        for (Row row:rows) {
            vc.put(row.get(0).toString(), Math.toIntExact(row.getLong(1)));
        }
        return vc;
    }
    
    /*
    public String[] getPhenotypes() {
        String[] phenotypes = new String[phenotypeKeyPositionMap.size()];
        int count = 0;
        for (Map.Entry pair : phenotypeKeyPositionMap.entrySet()) {
            phenotypes[count++] = (String)pair.getKey();
        }
        return phenotypes;
    }
    */
    
    public int getPhenotypeDatatype(String phenotype) throws MultiSlideException {
        if (this.phenotypeDatatypeMap.containsKey(phenotype)) {
            return this.phenotypeDatatypeMap.get(phenotype);
        } else {
            throw new MultiSlideException("Exception in getPhenotypeDatatype() of ClinicalInformation.java: unknown phenotype: " + phenotype);
        }
    }
    
    public String getPhenotypeDatatypeString(String phenotype) throws MultiSlideException {
        if (this.phenotypeDatatypeMap.containsKey(phenotype)) {
            return ClinicalInformation.mapPhenotypeDatatypeCodeToDescriptiveString(this.phenotypeDatatypeMap.get(phenotype));
        } else {
            throw new MultiSlideException("Exception in getPhenotypeDatatype() of ClinicalInformation.java: unknown phenotype: " + phenotype);
        }
    }
    
    public double[] getPhenotypeRange(String phenotype) throws MultiSlideException {
        if (this.phenotypeDatatypeMap.containsKey(phenotype)) {
            return this.continuousPhenotypeMinMaxMap.get(phenotype);
        } else {
            throw new MultiSlideException("Exception in getPhenotypeDatatype() of ClinicalInformation.java: unknown phenotype: " + phenotype);
        }
    }
    
    public double[] getPhenotypeColor(String phenotype, String phenotype_value) throws MultiSlideException {
        if (phenotype_value.equals("")) {
            return ClinicalInformation.BLANK_COLOR;
        }
        if (phenotypeDatatypeMap.get(phenotype) == ClinicalInformation.PHENOTYPE_DATATYPE_CONTINUOUS) {
            return getContinuousColor(continuousPhenotypeMinMaxMap.get(phenotype), Double.parseDouble(phenotype_value));
        } else {
            return phenotypeColorMap.get(phenotype).get(phenotype_value);
        }
    }
    
    /*
    private void loadClinicalColumnHeaders(String file, String delimiter) throws MultiSlideException {
        
        try {
            BufferedReader phenotype_file = new BufferedReader(new FileReader(file));
            String column_headers[] = phenotype_file.readLine().split(delimiter, -1);
            for(int i = 1; i < column_headers.length; i++){
                if(!phenotypeKeyPositionMap.containsKey(column_headers[i].trim().toLowerCase())){
                    phenotypeKeyPositionMap.put(column_headers[i].trim().toLowerCase(), i-1);
                } else {
                    Utils.log_info("Phenotype Column Header: " + column_headers[i].trim() + " appears more than once.");
                }
            }
        } catch (Exception e){
            Utils.log_exception(e, "");
            throw new MultiSlideException("Exception in ClinicalInformation.loadClinicalColumnHeaders(): \n" + e.getMessage());
        }
    }
    
    private int loadClinicalDataFromFile(String file, String delimiter) throws MultiSlideException {
        
        int nSamples = 0;
        try {
            BufferedReader phenotype_file = new BufferedReader(new FileReader(file));
            
            String p_line;
            phenotype_file.readLine();
            while ((p_line = phenotype_file.readLine())!= null) {
                String[] parts = p_line.split(delimiter, -1);
                String[] temp = new String[parts.length-1];
                for (int i=1; i<parts.length; i++) {
                    temp[i-1] = parts[i];
                }
                samplePhenotypeMap.put(parts[0], temp);
                nSamples++;
            }
            
        } catch (Exception e){
            Utils.log_exception(e, "");
            throw new MultiSlideException("Exception in ClinicalInformation.loadClinicalDataFromFile(): \n" + e.getMessage());
        }
        
        return nSamples;
    }
    */
    
    
    
    /*
    private void createClinicalDataMap() {
        phenotypeColorMap = new HashMap <String, HashMap <String, double[]>> ();
        for (Map.Entry pair : phenotypeKeyPositionMap.entrySet()) {
            String key = (String)pair.getKey();
            int index = (int)pair.getValue();
            HashMap <String, double[]> temp = new HashMap <String, double[]> ();
            for (Map.Entry sp_pair : samplePhenotypeMap.entrySet()) {
                String[] clinical_data_row = (String[])sp_pair.getValue();
                temp.put(clinical_data_row[index], new double[3]);
            }
            phenotypeColorMap.put(key, temp);
        }
        
        Random r = new Random();
        for (Map.Entry pair : phenotypeColorMap.entrySet()) {
            HashMap <String, double[]> temp = (HashMap <String, double[]>)pair.getValue();
            for (Map.Entry t_pair : temp.entrySet()) {
                String key = (String)t_pair.getKey();
                double[] color = new double[3];
                color[0] = r.nextInt(this.max-this.min) + this.min;
                color[1] = r.nextInt(this.max-this.min) + this.min;
                color[2] = r.nextInt(this.max-this.min) + this.min;
                temp.put(key, color);
            }
        }
    }
    */
    
    public double[] getContinuousColor (
            double[] min_max, double value
    ) throws MultiSlideException {
        double normed_value = (value - min_max[0])/(min_max[1]-min_max[0]);
        if (normed_value == 1) {
            double[] rgb = new double[3];
            rgb[0] = this.standard_continuous_colors.get(this.standard_continuous_colors.size()-1)[0];
            rgb[1] = this.standard_continuous_colors.get(this.standard_continuous_colors.size()-1)[1];
            rgb[2] = this.standard_continuous_colors.get(this.standard_continuous_colors.size()-1)[2];
            return rgb;
        } else {
            for (int i=0; i<20; i++) {
                if (normed_value >= i*0.05 && normed_value < (i+1)*0.05) {
                    double d1 = (normed_value - i*0.05)/0.05;
                    double d2 = ((i+1)*0.05 - normed_value)/0.05;
                    double[] rgb = new double[3];
                    rgb[0] = this.standard_continuous_colors.get(i)[0]*d1 + this.standard_continuous_colors.get(i+1)[0]*d2;
                    rgb[1] = this.standard_continuous_colors.get(i)[1]*d1 + this.standard_continuous_colors.get(i+1)[1]*d2;
                    rgb[2] = this.standard_continuous_colors.get(i)[2]*d1 + this.standard_continuous_colors.get(i+1)[2]*d2;
                    return rgb;
                }
            }
        }
        throw new MultiSlideException("Exception in ClinicalInformation.getContinuousColor(): normed value cannot be greater than or equal to 1");
    }
    
    public double[][] getCategoricalColors(int n) {
        Random r = new Random();
        double[][] colors = new double[n][3];
        for (int i=0; i<n; i++) {
            if (i < this.standard_categorical_colors.size()) {
                colors[i][0] = (double)this.standard_categorical_colors.get(i)[0];
                colors[i][1] = (double)this.standard_categorical_colors.get(i)[1];
                colors[i][2] = (double)this.standard_categorical_colors.get(i)[2];
            } else {
                double[] color = new double[3];
                color[0] = r.nextInt(this.max-this.min) + this.min;
                color[1] = r.nextInt(this.max-this.min) + this.min;
                color[2] = r.nextInt(this.max-this.min) + this.min;
                colors[i] = color;
            }
        }
        return colors;
    }
    
    public static String mapPhenotypeDatatypeCodeToDescriptiveString (int phenotype_datatype) 
    throws MultiSlideException {
        
        String phenotype_datatype_string;
        switch (phenotype_datatype) {
            case ClinicalInformation.PHENOTYPE_DATATYPE_UNARY:
                phenotype_datatype_string = ClinicalInformation.PHENOTYPE_CATEGORICAL_DESC_STRING;
                break;
            case ClinicalInformation.PHENOTYPE_DATATYPE_BINARY:
                phenotype_datatype_string = ClinicalInformation.PHENOTYPE_BINARY_DESC_STRING;
                break;
            case ClinicalInformation.PHENOTYPE_DATATYPE_CONTINUOUS:
                phenotype_datatype_string = ClinicalInformation.PHENOTYPE_CONTINUOUS_DESC_STRING;
                break;
            case ClinicalInformation.PHENOTYPE_DATATYPE_CATEGORICAL:
                phenotype_datatype_string = ClinicalInformation.PHENOTYPE_CATEGORICAL_DESC_STRING;
                break;
            case ClinicalInformation.PHENOTYPE_DATATYPE_ALL_BLANKS:
                throw new MultiSlideException("Selected phenotype has only blanks");
            default:
                throw new MultiSlideException("Unable to map phenotype datatype");
        }
        return phenotype_datatype_string;
    }
    
    public Dataset<Row> getDataForEnrichmentAnalysis(String phenotype_name) {

        /*
            Get phenotypes for samples
         */
        List<String> c = new ArrayList<>();
        c.add("_Sample_IDs");
        c.add(phenotype_name);
        Dataset<Row> p = this.phenotype_data.select(CollectionUtils.asColumnArray(c));
        return p;
    }
}
