/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author Soumita
 */
public class GeneCoordinate {
    
    public int start;
    public int end;
    public String strand_direction;
    public String ensembl_gene_id;
    public String gene_type;
    public String gene_symbol;
    
    
       
    
    public GeneCoordinate(int start, int end, String strand_direction, String ensembl_gene_id, String gene_type, String gene_symbol){
        this.start = start;
        this.end = end;
        this.strand_direction = strand_direction;
        this.ensembl_gene_id = ensembl_gene_id;
        this.gene_type = gene_type;
        this.gene_symbol = gene_symbol;
    }
    
    public static HashMap <String, ArrayList<GeneCoordinate>> createGeneCoordinatesFromFile(String file){
       
        HashMap<String, ArrayList<GeneCoordinate>> gene_coordinate_map = new HashMap<String, ArrayList<GeneCoordinate>>(); 
        try {
            BufferedReader gtf_file = new BufferedReader(new FileReader(file));
            String gtf_line;
            
            while ((gtf_line = gtf_file.readLine())!= null) {
                String parts[] = gtf_line.split("\t");
                String chr_loc = parts[0].trim().toLowerCase();
                int start = Integer.parseInt(parts[3].trim());
                int end = Integer.parseInt(parts[4].trim());
                String strand_dir = parts[6].trim();
                String desc[] = parts[8].trim().split(";");
                
                String gene_id_elements[] = desc[0].split(" ");
                String gene_id[] = gene_id_elements[1].replace("\"", "").split("\\.");
                String gene_id_final = gene_id[0].trim().toLowerCase();
                
                String gene_type_elements[] = desc[1].split(" ");
                String gene_type = gene_type_elements[2].replace("\"", "").trim().toLowerCase();
                
                String gene_symbol_elements[] = desc[2].split(" ");
                String gene_symbol = gene_symbol_elements[2].replace("\"", "").trim().toLowerCase();
                                
                GeneCoordinate gene = new GeneCoordinate(start, end, strand_dir, gene_id_final, gene_type, gene_symbol);
                if(gene_coordinate_map.containsKey(chr_loc)){
                    ArrayList <GeneCoordinate> gene_details = gene_coordinate_map.get(chr_loc);
                    boolean updateMap = true;
                    
                    if(updateMap){
                        gene_details.add(gene);
                        gene_coordinate_map.put(chr_loc, gene_details);
                    }
                } else {
                    ArrayList <GeneCoordinate> gene_details = new ArrayList <GeneCoordinate>();
                    
                    gene_details.add(gene);
                    gene_coordinate_map.put(chr_loc, gene_details);
                }
                
               
            }
                
            } catch (Exception e) {
            System.out.println(e);
        } 
         
        
        return gene_coordinate_map;
    }
}
