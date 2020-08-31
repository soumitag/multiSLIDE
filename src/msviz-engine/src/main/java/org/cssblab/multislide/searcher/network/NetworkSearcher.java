/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.searcher.network;

import java.util.Arrays;
import java.util.List;
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
import com.mongodb.ParallelScanOptions;
import com.mongodb.ServerAddress;
import java.io.Serializable;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import structure.SearchResultContainer;
import org.cssblab.multislide.searcher.GeneObject;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class NetworkSearcher implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final byte TYPE_TF_TARGETS_CURSOR = 1;
    public static final byte TYPE_MIRNA_MIRFAMILY_CURSOR = 2;
    public static final byte TYPE_MIRFAMILY_TARGETS_CURSOR = 3; 
    public static final byte TYPE_PPI_CURSOR = 4; 
    
    public static final byte TYPE_ENTREZ_CURSOR = 1;
   
     
    private MongoClient mongoClient;
    private DB db;
        
    private DBCollection tfTargetMap;
    private DBCollection mirnaTomirFamily;
    private DBCollection mirFamilyToTargets;
    private DBCollection ppiMap;
    
    private String species;
    public boolean isConnected;
    
    public NetworkSearcher(String species){
        this.species = species;
        connectToMongoDB();
    }
    
    public final void connectToMongoDB () {        
        try {
            
            //mongoClient = new MongoClient (new ServerAddress("localhost" , 27017), Arrays.asList());
            mongoClient = new MongoClient ("localhost", 27017);
            
            if(species.equals("human")){            // human
            // Connect to databases
                db = mongoClient.getDB("geneVocab_HomoSapiens"); 
                                
                tfTargetMap = db.getCollection("HS_TFTargetMap");
                mirnaTomirFamily = db.getCollection("HS_miRNATomirFamily");
                mirFamilyToTargets = db.getCollection("HS_mirFamilyToTargets");
                ppiMap = db.getCollection("HS_PPI");               
                
                isConnected = true;
                
            } else if (species.equals("mouse")){    // mouse 
                db = mongoClient.getDB("geneVocab_MusMusculus");
                
                tfTargetMap = db.getCollection("MM_TFTargetMap");
                mirnaTomirFamily = db.getCollection("MM_miRNATomirFamily");
                mirFamilyToTargets = db.getCollection("MM_mirFamilyToTargets");
                ppiMap = db.getCollection("MM_PPI");
                               
                isConnected = true;
            }
            
        } catch(Exception e){
            
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            isConnected = false;
            
        }
    }
    
    public void closeMongoDBConnection () {
        
        try {            
            mongoClient.close();
            isConnected = false;            
        } catch(Exception e){            
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            isConnected = false;
            
        }
        
    }
    
    
    public int convertIdentifierToNum(String query_type){
        int query_type_num = 0;
        
        if (query_type.equals("tf_entrez") || query_type.equals("ppi_entrez")) {
            query_type_num = GeneObject.ENTREZ;
        }         
        
        return query_type_num;
    }    
    
    public ArrayList <String> convertDBObjectListToStringList(List <String> inBasicDbList) {
        ArrayList <String> outStringList = new ArrayList <String>(inBasicDbList.size());
        for (int i=0; i<inBasicDbList.size(); i++) {
            outStringList.add(inBasicDbList.get(i).toString());           
        }
        return outStringList;
    }  
    
    public ArrayList <String> convertDBObjectListToStringList_1(List <BasicDBObject> inBasicDbList) {
        ArrayList <String> outStringList = new ArrayList <String>(inBasicDbList.size());
        for (int i=0; i<inBasicDbList.size(); i++) {
            outStringList.add(inBasicDbList.get(i).toString());           
        }
        return outStringList;
    }
    
    public ArrayList<String> searchTFDB(String query_type, String search_type, String qString) {
        int query_type_num = convertIdentifierToNum(query_type);
        ArrayList<String> all_targets = new ArrayList<String>();

        DBCursor cursor = null;

        try {
            BasicDBObject query = null;

            if (search_type.equals("exact")) {
                query = new BasicDBObject("_id", qString.trim().toLowerCase());
            } else if (search_type.equals("contains")) {
                query = new BasicDBObject("_id", java.util.regex.Pattern.compile(qString.trim().toLowerCase()));
            }

            cursor = tfTargetMap.find(query);
            while (cursor.hasNext()) {
                DBObject match = (DBObject) cursor.next();
                String tf_entrez = (String) match.get("_id");
                List<String> targets_ENCODE = (List<String>) match.get("ENCODE");
                List<String> targets_ITFP = (List<String>) match.get("ITFP");
                List<String> targets_Marbach2016 = (List<String>) match.get("Marbach2016");
                List<String> targets_TRED = (List<String>) match.get("TRED");
                List<String> targets_TRRUST = (List<String>) match.get("TRRUST");

                if (targets_ENCODE != null && !targets_ENCODE.isEmpty()) {
                    ArrayList<String> ENCODE_arr = convertDBObjectListToStringList(targets_ENCODE);
                    all_targets.addAll(ENCODE_arr);
                } 
                    
                if (targets_ITFP != null && !targets_ITFP.isEmpty()) {
                    ArrayList<String> ITFP_arr = convertDBObjectListToStringList(targets_ITFP);
                    all_targets.addAll(ITFP_arr);
                } 
                
                if (targets_Marbach2016 != null && !targets_Marbach2016.isEmpty()) {
                    ArrayList<String> Marbach2016_arr = convertDBObjectListToStringList(targets_Marbach2016);
                    all_targets.addAll(Marbach2016_arr);
                }
                
                if (targets_TRED != null && !targets_TRED.isEmpty()) {
                    ArrayList<String> TRED_arr = convertDBObjectListToStringList(targets_TRED);
                    all_targets.addAll(TRED_arr);
                }
                
                if (targets_TRRUST != null && !targets_TRRUST.isEmpty()) {
                    ArrayList<String> TRRUST_arr = convertDBObjectListToStringList(targets_TRRUST);
                    all_targets.addAll(TRRUST_arr);
                }

            }
        } catch (Exception e) {
            Utils.log_exception(e, "");
        }

        return all_targets;
    }

    public ArrayList<String> searchPPI(String query_type, String search_type, String qString) {
        int query_type_num = convertIdentifierToNum(query_type);
        ArrayList<String> targets_arr = new ArrayList<String>();

        DBCursor cursor = null;

        try {
            BasicDBObject query = null;

            if (search_type.equals("exact")) {
                query = new BasicDBObject("_id", qString.trim().toLowerCase());
            } else if (search_type.equals("contains")) {
                query = new BasicDBObject("_id", java.util.regex.Pattern.compile(qString.trim().toLowerCase()));
            }

            cursor = ppiMap.find(query);
            while (cursor.hasNext()) {
                DBObject match = (DBObject) cursor.next();
                String ppi_entrez = (String) match.get("_id");
                //List<BasicDBObject> targets_ppi = (List<BasicDBObject>) match.get("entrezs");
                String targets_ppi = (String) match.get("entrezs");
                if(targets_ppi != null){
                    String[] targets_ppi_arr = targets_ppi.split(",", -1);                    
                    for(int i = 0 ; i < targets_ppi_arr.length; i++){
                        targets_arr.add(targets_ppi_arr[i]);
                    }
                }
                
                /*
                if (targets_ppi != null && !targets_ppi.isEmpty()) {
                    //targets_arr = convertDBObjectListToStringList_1(targets_ppi);
                }
                */
                
            }

        } catch (Exception e) {
            Utils.log_exception(e, "");
        }

        return targets_arr;
    }

    // so miRNA there are few matches to fix .. not all can be mapped to entrez.. so can we leave it as is ?
    
    
    public ArrayList<String> searchmiRNAToFamilyToTargets(String query_type, String search_type, String qString) {
        String mir_family = searchmirFamilyFromDB(query_type, search_type, qString);
        DBCursor cursor = null;
        ArrayList<String> targets_arr = new ArrayList<String>();

        try {
            BasicDBObject query = null;
            query = new BasicDBObject("_id", mir_family.trim().toLowerCase());
            cursor = mirFamilyToTargets.find(query);
            while (cursor.hasNext()) {
                DBObject match = (DBObject) cursor.next();
                String mir_target_entrezs = (String) match.get("target_entrezs");
                if (mir_target_entrezs != null) {
                    String[] mir_target_entrezs_arr = mir_target_entrezs.split(",", -1);                    
                    for(int i = 0 ; i < mir_target_entrezs_arr.length; i++){
                        targets_arr.add(mir_target_entrezs_arr[i]);
                    }                   
                }
            }

        } catch (Exception e) {
            Utils.log_exception(e, "");
        }

        return targets_arr;
    }
    
    public String searchmirFamilyFromDB (String query_type, String search_type, String qString){
        
        ArrayList <String> mir_families_arr = new ArrayList<String>();
        DBCursor cursor = null;
        String mir_family = null;
        
        try {

            BasicDBObject query = null;            
            
            if (search_type.equals("exact")) {
                query = new BasicDBObject("_id", qString.trim().toLowerCase());
            } else if (search_type.equals("contains")) {
                query = new BasicDBObject("_id", java.util.regex.Pattern.compile(qString.trim().toLowerCase()));
            }
            
            cursor = mirnaTomirFamily.find(query);
            while (cursor.hasNext()) {
                DBObject match = (DBObject)cursor.next();                    
                mir_family = (String) match.get("mir_family");                    
            }
        } catch (Exception e) {
            Utils.log_exception(e, "");
        }
        
        return mir_family;
                
    }
}
