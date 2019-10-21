/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.utils;


import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import java.util.ArrayList;



import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ParallelScanOptions;
import com.mongodb.ServerAddress;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author soumitag
 */
public class MongoDBConnect {
    
    
    public static void cleanMicroRNAHS(String path) {
        
        HashMap <String, String> mirEntrezMap = new HashMap <String, String> ();
        try {
            
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = br.readLine();
            
            while ((line = br.readLine()) != null) {
                String parts[] = line.split("\t", -1);
                String mirbase_acc_parts[] = parts[8].split("\\|", -1);
                String eid = parts[18].trim().toLowerCase();
                for(int i = 0; i < mirbase_acc_parts.length; i++) {
                    if(!mirEntrezMap.containsKey(mirbase_acc_parts[i].trim().toLowerCase()) && mirbase_acc_parts[i] != null) {
                        mirEntrezMap.put(mirbase_acc_parts[i].trim().toLowerCase(), eid);
                    }
                }    
            }
            
            File file = new File("E:\\code_multislide\\miRNA_network\\miRNA_Entrez\\HS_MICRORNA_ENTREZ_CLEAN.txt");
            FileWriter fw = new FileWriter(file);
            PrintWriter pw = new PrintWriter(fw);
            Iterator iter = (Iterator)mirEntrezMap.keySet().iterator();
            while(iter.hasNext()){
                String key = iter.next().toString();
                pw.print(key + "\t");                
                pw.print(mirEntrezMap.get(key) + "\n");
            }
            
            pw.flush();
            pw.close();
            fw.close();      
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
            
    }
    
    public static void cleanMicroRNAMM(String path){
        HashMap <String, String> mirEntrezMap = new HashMap <String, String> ();
        try {
            
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = br.readLine();
            
            while ((line = br.readLine()) != null) {
                String parts[] = line.split("\t", -1);
                String mirbase_acc_parts[] = parts[9].split("\\|", -1);
                String eid = parts[8].trim().toLowerCase();
                for(int i = 0; i < mirbase_acc_parts.length; i++) {
                    if(!mirEntrezMap.containsKey(mirbase_acc_parts[i].trim().toLowerCase()) && mirbase_acc_parts[i] != null) {
                        mirEntrezMap.put(mirbase_acc_parts[i].trim().toLowerCase(), eid);
                    }
                }    
            }
            
            File file = new File("E:\\code_multislide\\miRNA_network\\miRNA_Entrez\\MM_MICRORNA_ENTREZ_CLEAN.txt");
            FileWriter fw = new FileWriter(file);
            PrintWriter pw = new PrintWriter(fw);
            Iterator iter = (Iterator)mirEntrezMap.keySet().iterator();
            while(iter.hasNext()){
                String key = iter.next().toString();
                pw.print(key + "\t");                
                pw.print(mirEntrezMap.get(key) + "\n");
            }
            
            pw.flush();
            pw.close();
            fw.close();      
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    } 
        
        
    
    
    
    
    
    public static void collectionsDB_drop() {

        try {

            //DB db = mongoClient.getDB("geneVocab_MusMusculus");
            //DBCollection C = db.getCollection("goMap");
            MongoClient mongoClient = new MongoClient(Arrays.asList(new ServerAddress("localhost", 27017)));

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

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
            
    
    
    
    }
    
    
    
}
