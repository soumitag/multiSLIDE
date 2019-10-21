/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.cssblab.multislide.graphics.ColorPalette;

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
    
    public static final double[] BLANK_COLOR = new double[]{200,200,200};
    
    public ArrayList <short[]> standard_categorical_colors;
    public ArrayList <short[]> standard_continuous_colors;
    
    HashMap <String, Integer> phenotypeKeyPositionMap;
    HashMap <String, String[]> samplePhenotypeMap;
    public HashMap <String, HashMap <String, double[]>> phenotypeColorMap;
    public HashMap <String, HashMap <String, Integer>> phenotypeValueSortMap;
    HashMap <String, Integer> phenotypeDatatypeMap;
    HashMap <String, double[]> continuousPhenotypeMinMaxMap;
    
    public ClinicalInformation(ColorPalette categorical_palette, ColorPalette continuous_palette) {
        phenotypeKeyPositionMap = new HashMap <String, Integer> ();
        samplePhenotypeMap = new HashMap <String, String[]> ();
        /*
        standard_categorical_colors[0] = new double[]{178,223,138};
        standard_categorical_colors[1] = new double[]{51,160,44};
        standard_categorical_colors[2] = new double[]{251,154,153};
        standard_categorical_colors[3] = new double[]{227,26,28};
        standard_categorical_colors[4] = new double[]{253,191,111};
        standard_categorical_colors[5] = new double[]{255,127,0};
        standard_categorical_colors[6] = new double[]{202,178,214};
        standard_categorical_colors[7] = new double[]{106,61,154};
        standard_categorical_colors[8] = new double[]{255,255,153};
        standard_categorical_colors[9] = new double[]{177,89,40};
        standard_categorical_colors[10] = new double[]{166,206,227};
        standard_categorical_colors[11] = new double[]{31,120,180};
        */
        this.standard_categorical_colors = categorical_palette.palette;
        this.standard_continuous_colors = continuous_palette.palette;
    }
    
    public String getPhenotypeValue (String patient_id, String phenotype) {
        String[] p = samplePhenotypeMap.get(patient_id);
        int index = phenotypeKeyPositionMap.get(phenotype.toLowerCase());
        return p[index];
    }
    
    public String[] getPhenotypeValues (String phenotype) {
        int index = phenotypeKeyPositionMap.get(phenotype.toLowerCase());
        String[] phenotype_values = new String[samplePhenotypeMap.size()];
        int i = 0;
        for (String patient_id : samplePhenotypeMap.keySet()) {
            phenotype_values[i++] = samplePhenotypeMap.get(patient_id)[index];
        }
        return phenotype_values;
    }
    
    public int loadClinicalInformation(String filename, String delimiter) throws MultiSlideException {
        
        try {
            loadClinicalColumnHeaders(filename, delimiter);
            int nSamples = loadClinicalDataFromFile(filename, delimiter);
            parsePhenotypeDatatypes(nSamples);
            //createClinicalDataMap();
            return nSamples;
        } catch (Exception e) {
            System.out.println(e);
            throw new MultiSlideException("Exception in ClinicalInformation.loadClinicalInformation(): \n" + e.getMessage());
        }
    }
    
    public String[] getPhenotypes() {
        String[] phenotypes = new String[phenotypeKeyPositionMap.size()];
        int count = 0;
        for (Map.Entry pair : phenotypeKeyPositionMap.entrySet()) {
            phenotypes[count++] = (String)pair.getKey();
        }
        return phenotypes;
    }
    
    public int getPhenotypeDatatype(String phenotype) throws MultiSlideException {
        if (this.phenotypeDatatypeMap.containsKey(phenotype)) {
            return this.phenotypeDatatypeMap.get(phenotype);
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
            return getContinuousColor(continuousPhenotypeMinMaxMap.get(phenotype.toLowerCase()), Double.parseDouble(phenotype_value));
        } else {
            return phenotypeColorMap.get(phenotype.toLowerCase()).get(phenotype_value);
        }
    }
    
    private void loadClinicalColumnHeaders(String file, String delimiter) throws MultiSlideException {
        
        try {
            BufferedReader phenotype_file = new BufferedReader(new FileReader(file));
            String column_headers[] = phenotype_file.readLine().split(delimiter, -1);
            for(int i = 1; i < column_headers.length; i++){
                if(!phenotypeKeyPositionMap.containsKey(column_headers[i].trim().toLowerCase())){
                    phenotypeKeyPositionMap.put(column_headers[i].trim().toLowerCase(), i-1);
                } else {
                    System.out.println("Phenotype Column Header: " + column_headers[i].trim() + " appears more than once.");
                }
            }
        } catch (Exception e){
            System.out.println(e);
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
            System.out.println(e);
            throw new MultiSlideException("Exception in ClinicalInformation.loadClinicalDataFromFile(): \n" + e.getMessage());
        }
        
        return nSamples;
    }
    
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
                    /*
                    double[][] colors = getContinuousColors(nValues);
                    int c = 0;
                    for (Map.Entry p : t.entrySet()) {
                        colormap.put((String)p.getKey(), colors[c++]);
                    }
                    */
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
        double normed_value = (value - min_max[0])/min_max[1];
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
    
}
