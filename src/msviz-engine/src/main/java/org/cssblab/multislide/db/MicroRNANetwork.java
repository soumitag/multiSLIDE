/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.CommandResult;
import com.mongodb.MongoClientOptions;

import com.mongodb.ServerAddress;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author soumitag
 */
public class MicroRNANetwork {
    
    public static void createGene_miRFamilyMap(){
        
        HashMap <String, ArrayList<String>> gene_miRNA_map = new HashMap <String, ArrayList<String>> ();
        
        try {
            MongoClient mongoClient = new MongoClient (new ServerAddress("localhost" , 27017), (MongoClientOptions) Arrays.asList());
            DB db = mongoClient.getDB("");
            DBCollection C = db.getCollection("");
            
            BufferedReader predicted_targets_file  = new BufferedReader(new FileReader(".txt"));
            String predicted_targets_line = predicted_targets_file.readLine();
            
            while((predicted_targets_line = predicted_targets_file.readLine()) != null){
                String parts[] = predicted_targets_line.split("\t", -1);
                String mir_family = parts[0].trim().toLowerCase();
                
                String gene_id_elements[] = parts[1].trim().toLowerCase().split("\\.");
                String ensembl_id = gene_id_elements[0]; // target
                
                if(gene_miRNA_map.containsKey(ensembl_id)) {
                    ArrayList<String> mir_families = gene_miRNA_map.get(mir_family);
                    mir_families.add(mir_family);
                    gene_miRNA_map.put(ensembl_id, mir_families);                    
                } else {
                    ArrayList<String> mir_families = new ArrayList<String>();
                    mir_families.add(mir_family);
                    gene_miRNA_map.put(ensembl_id, mir_families);                     
                }     
            }
            
            int count = 0;
            Iterator iter = gene_miRNA_map.entrySet().iterator();
            while(iter.hasNext()){
                String gene_id = (String)iter.next();
                ArrayList <String> mir_families_map = gene_miRNA_map.get(gene_id);
                BasicDBObject doc = new BasicDBObject();
                doc.put("ensembl_gene_id", gene_id);
                ArrayList <BasicDBObject> mir_family_map_dbo = new ArrayList<BasicDBObject>();
                
                for(int i = 0; i < mir_families_map.size(); i++) {
                    BasicDBObject mir_families = new BasicDBObject();
                    mir_families.put("mir_family", mir_families_map.get(i));
                    mir_family_map_dbo.add(mir_families);
                }
                doc.put("mir_families", mir_family_map_dbo);
                
                System.out.println(count + ": " + doc);
                C.insert(doc);
                count++;    
            }
            
            C.createIndex(new BasicDBObject("ensembl_gene_id", 1));
            mongoClient.close();
            
            
        }catch(Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
    }
    
    public static void createmiRFamily_miRMap_All(){
        
        // this is one consolidated file from TargetScanDB for all species
        
        try {
            
            MongoClient mongoClient = new MongoClient (new ServerAddress("localhost" , 27017), (MongoClientOptions) Arrays.asList());
            DB db = mongoClient.getDB("");
            DBCollection C = db.getCollection("");
            
            BufferedReader miRFamily_file  = new BufferedReader(new FileReader(".txt"));
            String miRFamily_line = miRFamily_file.readLine();
            
            int count = 0;
            while((miRFamily_line = miRFamily_file.readLine()) != null){
                String parts[] = miRFamily_line.split("\t", -1);
                
                String mir_family = parts[0].trim().toLowerCase();
                String species_id = parts[2].trim().toLowerCase();
                String mirbase_id = parts[3].trim().toLowerCase();
                String mirbase_acc = parts[6].trim().toLowerCase();
                
                BasicDBObject doc = new BasicDBObject();
                doc.put("mir_family", mir_family);
                doc.put("species_id", species_id);
                doc.put("mirbase_id", mirbase_id);
                doc.put("mirbase_acc", mirbase_acc);  
                
                System.out.println(count + ": " + doc);
                C.insert(doc);
                count++;                  
            }     
        }catch(Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }     
    }
    
    
    public static void collectionsDB_drop() {
        
        try {
            
            
            //DB db = mongoClient.getDB("geneVocab_MusMusculus");
            //DBCollection C = db.getCollection("goMap");
            
            MongoClient mongoClient = new MongoClient(new ServerAddress("localhost" , 27017), (MongoClientOptions) Arrays.asList());
            
            //DB db = mongoClient.getDB("goVocab");
            //DBCollection C = db.getCollection("goidTermMap");
            
            //DB db = mongoClient.getDB("goVocab");
            //DB db = mongoClient.getDB("geneVocab_HomoSapiens");
            DB db = mongoClient.getDB("geneVocab_MusMusculus");
            if (db.collectionExists("MM_geneMap2")) {
                    DBCollection genemap = db.getCollection("MM_geneMap2");
                    genemap.drop();
                    System.out.println("Collection dropped");
            }
            
        }catch(Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
            
    }
    
}
