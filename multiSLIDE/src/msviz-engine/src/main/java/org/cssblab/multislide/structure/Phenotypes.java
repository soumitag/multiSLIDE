/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;


/**
 *
 * @author soumitag
 */
public class Phenotypes {
    
    public String field_values[];
    
    public Phenotypes(String field_values[]){
        this.field_values = field_values;
    }
    
    /*
    public static void createClinicalColumnHeaders(String file){
        
        try {
            BufferedReader phenotype_file = new BufferedReader(new FileReader(file));
            String column_headers[] = phenotype_file.readLine().split("\t", -1);
            for(int i = 0; i < column_headers.length; i++){
                if(!Data.phenotype_keys.containsKey(column_headers[i].trim().toLowerCase())){
                    Data.phenotype_keys.put(column_headers[i].trim().toLowerCase(), i);
                } else {
                    System.out.println("Phenotype Column Header: " + column_headers[i].trim() + " appears more than once.");
                }
            }
        } catch (Exception e){
            System.out.println(e);
        }
    }
    
    
    public static Phenotypes[] createClinicalDataFromFile(String file) {       
        
        ArrayList <Phenotypes> phenotypes = new ArrayList <Phenotypes> ();
        
        try {
            BufferedReader phenotype_file = new BufferedReader(new FileReader(file));
            
            String p_line;
            while ((p_line = phenotype_file.readLine())!= null) {
                String parts[] = p_line.split("\t", -1);
                Phenotypes p = new Phenotypes(parts);
                phenotypes.add(p);
            }
            
        } catch (Exception e){
            System.out.println(e);
        }
        
        return phenotypes.toArray(new Phenotypes[phenotypes.size()]);
    }
    
    public static String getPhenotypeValue (String phenotype_key) {
        int index = Data.phenotype_keys.get(phenotype_key);        
        return field_values[index];
    }
    */
}
