/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Soumita
 */
public class ClinicalInformation {
    
    private transient final int min = 0;
    private transient final int max = 255;
    
    public static final int PHENOTYPE_DATATYPE_UNARY       = 1;
    public static final int PHENOTYPE_DATATYPE_BINARY      = 2;
    public static final int PHENOTYPE_DATATYPE_CATEGORICAL = 3;
    public static final int PHENOTYPE_DATATYPE_CONTINUOUS  = 0;
    
    double[][] standard_categorical_colors = new double[12][3];
    
    HashMap <String, Integer> phenotypeKeyPositionMap;
    HashMap <String, String[]> samplePhenotypeMap;
    public HashMap <String, HashMap <String, double[]>> phenotypeColorMap;
    HashMap <String, Integer> phenotypeDatatypeMap;
    
    public ClinicalInformation() {
        phenotypeKeyPositionMap = new HashMap <String, Integer> ();
        samplePhenotypeMap = new HashMap <String, String[]> ();
        
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
    }
    
    public String getPhenotypeValue (String patient_id, String phenotype) {
        String[] p = samplePhenotypeMap.get(patient_id);
        int index = phenotypeKeyPositionMap.get(phenotype.toLowerCase());
        return p[index];
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
    
    public double[] getPhenotypeColor(String phenotype, String phenotype_value) {
        return phenotypeColorMap.get(phenotype.toLowerCase()).get(phenotype_value);
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
        
        phenotypeColorMap = new HashMap <String, HashMap <String, double[]>> ();
        double[] blank_color = new double[]{200,200,200};
        
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
            
            int nValues = t.size();
            
            HashMap <String, double[]> colormap = new HashMap <String, double[]> ();
            
            if (nValues == 1) {
                dtypes[j] = ClinicalInformation.PHENOTYPE_DATATYPE_UNARY;
                double[] color = new double[]{177,89,40};
                colormap.put((String)t.entrySet().iterator().next().getKey(), color);
            } else if (nValues == 2) {
                dtypes[j] = ClinicalInformation.PHENOTYPE_DATATYPE_BINARY;
                double[][] colors = new double[2][3];
                colors[0] = new double[]{255,126,62};
                colors[1] = new double[]{38,156,211};
                int c = 0;
                for (Map.Entry p : t.entrySet()) {
                    colormap.put((String)p.getKey(), colors[c++]);
                }
            } else if (nValues > 2) {
                
                boolean isContinuous = true;
                for (Map.Entry p : t.entrySet()) {
                    String value = (String)p.getKey();
                    try {
                        double d = Double.parseDouble(value);
                    } catch (NumberFormatException nfe) {
                        isContinuous = false;
                        break;
                    }
                }
                
                if (isContinuous) {
                    dtypes[j] = ClinicalInformation.PHENOTYPE_DATATYPE_CATEGORICAL;
                    double[][] colors = getCategoricalColors(nValues);
                    int c = 0;
                    for (Map.Entry p : t.entrySet()) {
                        colormap.put((String)p.getKey(), colors[c++]);
                    }
                } else {
                    dtypes[j] = ClinicalInformation.PHENOTYPE_DATATYPE_CONTINUOUS;
                    double[][] colors = getContinuousColors(nValues);
                    int c = 0;
                    for (Map.Entry p : t.entrySet()) {
                        colormap.put((String)p.getKey(), colors[c++]);
                    }
                }
            }
            
            if (hasBlanks) {
                colormap.put("", blank_color);
            }
            
            phenotypeColorMap.put(phen_name, colormap);
        }
        
        phenotypeDatatypeMap = new HashMap <String, Integer> ();
        for (Map.Entry pair : phenotypeKeyPositionMap.entrySet()) {
            String key = (String)pair.getKey();
            int index = (int)pair.getValue();
            phenotypeDatatypeMap.put(key, dtypes[index]);
        }
    }
    
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
    
    public double[][] getContinuousColors(int n) {
        Random r = new Random();
        double[][] colors = new double[n][3];
        for (int i=0; i<n; i++) {
            if (n<13) {
                colors[i] = this.standard_categorical_colors[i];
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
    
    public double[][] getCategoricalColors(int n) {
        Random r = new Random();
        double[][] colors = new double[n][3];
        for (int i=0; i<n; i++) {
            if (n<13) {
                colors[i] = this.standard_categorical_colors[i];
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
