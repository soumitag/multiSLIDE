package org.cssblab.multislide.searcher;

/**
 *
 * @author Soumita
 */

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
import org.cssblab.multislide.utils.Utils;


public class Searcher implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final byte TYPE_ENTREZ_CURSOR = 1;
    public static final byte TYPE_ALIAS_CURSOR = 2;
    public static final byte TYPE_REFSEQ_CURSOR = 3; 
    public static final byte TYPE_ENSEMBL_GENE_CURSOR = 4; 
    public static final byte TYPE_ENSEMBL_TRANSCRIPT_CURSOR = 5; 
    public static final byte TYPE_ENSEMBL_PROTEIN_CURSOR = 6; 
    public static final byte TYPE_UNIPROT_CURSOR = 7;
    public static final byte TYPE_GO_ID_CURSOR = 8;
    public static final byte TYPE_GO_TERM_CURSOR = 9;
    public static final byte TYPE_PATH_ID_CURSOR = 10;
    public static final byte TYPE_PATH_NAME_CURSOR = 11;
    
    public static final byte TYPE_MIRNA_GO_ID_CURSOR = 12;
    public static final byte TYPE_MIRNA_GO_TERM_CURSOR = 13;
    public static final byte TYPE_MIRNA_PATH_ID_CURSOR = 14;
    public static final byte TYPE_MIRNA_PATH_NAME_CURSOR = 15;
    
    
    private MongoClient mongoClient;
    private DB db;
    //private DBCollection geneMap;
    private DBCollection entrezMap;
    private DBCollection aliasEntrezMap;
    private DBCollection entrezAliasMap;
    private DBCollection geneMap2;
    private DBCollection goMap2;
    private DBCollection pathwayMap;
    private DBCollection refseqMap;
    private DBCollection ensemblGeneMap;
    private DBCollection ensemblTranscriptMap;
    private DBCollection ensemblProteinMap;
    private DBCollection uniprotMap;
    
    private DBCollection mi_rna_pathwayMap;
    private DBCollection mi_rna_goMap;
    
    private String species;
    public boolean isConnected;
    
    public Searcher(String species){
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
                
                entrezMap = db.getCollection("HS_EntrezMap");
                aliasEntrezMap = db.getCollection("HS_aliasEntrezMap");
                entrezAliasMap = db.getCollection("HS_entrezAliasMap");
                geneMap2 = db.getCollection("HS_geneMap2");
                goMap2 = db.getCollection("HS_goMap2");
                pathwayMap = db.getCollection("HS_pathwayMap");
                refseqMap = db.getCollection("HS_refseqEntrezMap");
                ensemblGeneMap = db.getCollection("HS_ensemblEntrezMap");
                ensemblTranscriptMap = db.getCollection("HS_ensembltranscriptEntrezMap");
                ensemblProteinMap = db.getCollection("HS_ensemblprotEntrezMap");
                uniprotMap = db.getCollection("HS_uniprotEntrezMap");
                mi_rna_pathwayMap = db.getCollection("HS_miRPathwayMap");
                mi_rna_goMap = db.getCollection("HS_miRGOMap");
                                
                isConnected = true;
                
            } else if (species.equals("mouse")){    // mouse 
                db = mongoClient.getDB("geneVocab_MusMusculus");
                
                entrezMap = db.getCollection("MM_EntrezMap");
                aliasEntrezMap = db.getCollection("MM_aliasEntrezMap");
                entrezAliasMap = db.getCollection("MM_entrezAliasMap");
                geneMap2 = db.getCollection("MM_geneMap2");
                goMap2 = db.getCollection("MM_goMap2");
                pathwayMap = db.getCollection("MM_pathwayMap");
                refseqMap = db.getCollection("MM_refseqEntrezMap");
                ensemblGeneMap = db.getCollection("MM_ensemblEntrezMap");
                ensemblTranscriptMap = db.getCollection("MM_ensembltranscriptEntrezMap");
                ensemblProteinMap = db.getCollection("MM_ensemblprotEntrezMap");
                uniprotMap = db.getCollection("MM_uniprotEntrezMap");
                mi_rna_pathwayMap = db.getCollection("MM_miRPathwayMap");
                mi_rna_goMap = db.getCollection("MM_miRGOMap");
                
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
    
    /*public ArrayList <ArrayList<String>> processRequest (String queryString, String queryType, String searchType) {
        StringTokenizer st = new StringTokenizer(queryString, ",");
        ArrayList <ArrayList<String>> fullList = new ArrayList <ArrayList<String>> ();
        int count = 0;
        while(st.hasMoreTokens()) {
            ArrayList <ArrayList<String>> part_lists = processSingleRequest(st.nextToken(), queryType, searchType);
            
            for(int i = 0; i < part_lists.size(); i++){
                if (count == 0){
                    fullList.add(part_lists.get(i));
                } else {
                    fullList.get(i).addAll(part_lists.get(i));
                }
            }
            count++;
        }
        return fullList;
    } */
    
    /*public ArrayList <ArrayList<String>> processSingleRequest(String queryString, String queryType, String searchType){
        switch(queryType){
            case "entrez": 
                return processGeneQuery(queryString, searchType, "entrez");
            case "genesymbol":
                return processGeneQuery(queryString, searchType, "genesymbol");
            case "pathname":
                return processPathQuery(queryString, searchType, "pathname");
            case "pathid":
                return processPathQuery(queryString, searchType, "pathid");
            case "goid":
                return processGOQuery(queryString, searchType, "goid");
            case "goterm":
                return processGOQuery(queryString, searchType, "goterm");
            default:
                return null;
        }
    }*/
    
      
    public ArrayList <SearchResultObject> processAllDB (
            byte cursor_type,
            String queryString,
            String searchType,
            String queryType
    ) {
            
            ArrayList <SearchResultObject> search_results = new ArrayList <SearchResultObject> ();
            
            DBCursor entrez_cursor = null;
            DBCursor alias_cursor = null;
            DBCursor refseq_cursor = null;
            DBCursor ensembl_gene_cursor = null;
            DBCursor ensembl_transcript_cursor = null;
            DBCursor ensembl_protein_cursor = null;
            DBCursor uniprot_cursor = null;
            DBCursor go_id_cursor = null;
            DBCursor go_term_cursor = null;
            DBCursor path_id_cursor = null;
            DBCursor path_name_cursor = null;
            
            String[] DBObjectNames = {"aliases", "entrez_ids", "entrezs"};
            String[] DBElementNames = {"alias", "entrez", "entrez"};
            
            try {
                BasicDBObject query = null; 
                BasicDBObject query_go = null; 
                BasicDBObject query_go_term = null; 
                BasicDBObject query_pathway = null;
                
                String query_string = queryString.trim().toLowerCase();
                
                if (searchType.equals("contains")) {
                    query = new BasicDBObject("_id", java.util.regex.Pattern.compile(query_string));
                    query_go = new BasicDBObject("go", java.util.regex.Pattern.compile(query_string));
                    query_go_term = new BasicDBObject("term", java.util.regex.Pattern.compile(query_string));
                    query_pathway = new BasicDBObject("pathway", java.util.regex.Pattern.compile(query_string));
                    
                    if(cursor_type == Searcher.TYPE_ENTREZ_CURSOR) {
                        
                        entrez_cursor = entrezAliasMap.find(query);
                        search_results.addAll(processGeneCursor(query_string, GeneObject.ENTREZ, entrez_cursor, DBObjectNames[0], DBElementNames[0]));
                        
                    } else if(cursor_type == Searcher.TYPE_ALIAS_CURSOR){
                        
                        alias_cursor = aliasEntrezMap.find(query);
                        search_results.addAll(processGeneCursor(query_string, GeneObject.GENESYMBOL, alias_cursor, "entrez_ids", "entrez"));
                        
                    } else if(cursor_type == Searcher.TYPE_REFSEQ_CURSOR){
                    
                        refseq_cursor = refseqMap.find(query);
                        search_results.addAll(processGeneCursor(query_string, GeneObject.REFSEQ_ID, refseq_cursor, DBObjectNames[2], DBElementNames[2]));
                        
                    } else if(cursor_type == Searcher.TYPE_ENSEMBL_GENE_CURSOR){
                    
                        ensembl_gene_cursor = ensemblGeneMap.find(query);
                        search_results.addAll(processGeneCursor(query_string, GeneObject.ENSEMBL_GENE_ID, ensembl_gene_cursor, DBObjectNames[2], DBElementNames[2]));
                        
                    } else if (cursor_type == Searcher.TYPE_ENSEMBL_TRANSCRIPT_CURSOR) {
                    
                        ensembl_transcript_cursor = ensemblTranscriptMap.find(query);
                        search_results.addAll(processGeneCursor(query_string, GeneObject.ENSEMBL_TRANSCRIPT_ID, ensembl_transcript_cursor, DBObjectNames[2], DBElementNames[2]));
                        
                    } else if (cursor_type == Searcher.TYPE_ENSEMBL_PROTEIN_CURSOR){
                    
                        ensembl_protein_cursor = ensemblProteinMap.find(query);
                        search_results.addAll(processGeneCursor(query_string, GeneObject.ENSEMBL_PROTEIN_ID, ensembl_protein_cursor, DBObjectNames[2], DBElementNames[2]));
                        
                    } else if (cursor_type == Searcher.TYPE_UNIPROT_CURSOR) {
                    
                        uniprot_cursor = uniprotMap.find(query);
                        search_results.addAll(processGeneCursor(query_string, GeneObject.UNIPROT_ID, uniprot_cursor, DBObjectNames[2], DBElementNames[2]));
                        
                    } else if (cursor_type == Searcher.TYPE_GO_ID_CURSOR) {
                    
                        go_id_cursor = goMap2.find(query_go);
                        search_results.addAll(processGoCursor(query_string, GoObject.GO_ID, go_id_cursor));
                    
                    } else if (cursor_type == Searcher.TYPE_GO_TERM_CURSOR){
                    
                        go_term_cursor = goMap2.find(query_go_term);
                        search_results.addAll(processGoCursor(query_string, GoObject.GO_TERM, go_term_cursor));
                        
                    } else if (cursor_type == Searcher.TYPE_PATH_ID_CURSOR){
                    
                        path_id_cursor = pathwayMap.find(query);
                        search_results.addAll(processPathCursor(query_string, PathwayObject.PATH_ID, path_id_cursor));
                        
                    } else if (cursor_type == Searcher.TYPE_PATH_NAME_CURSOR){
                    
                        path_name_cursor = pathwayMap.find(query_pathway);
                        search_results.addAll(processPathCursor(query_string, PathwayObject.PATH_TERM, path_name_cursor));
                    }
                    
                    //processSearchResultsHashMap(entrez_HM, "entrezMap");
                }
            } catch(Exception e){
                Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
            }
            
        return search_results;        
    }
    
    public ArrayList <SearchResultObject> processGeneCursor(String query_string, byte gene_query_subtype, DBCursor cursor, String DBObjectName, String DBElementName){
        ArrayList <SearchResultObject> db_map = new ArrayList <SearchResultObject>();
        while (cursor.hasNext()) {
            DBObject match = (DBObject) cursor.next();
            String key = (String)match.get("_id");  // =geneidentifier=genesymbol
            List<BasicDBObject> aliases = (List<BasicDBObject>) match.get(DBObjectName);
            GeneObject result;
            if (gene_query_subtype == GeneObject.ENTREZ) {
                result = new GeneObject(key, ((String) aliases.get(0).get(DBElementName)), gene_query_subtype);
            } else {
                result = new GeneObject(((String) aliases.get(0).get(DBElementName)), key, gene_query_subtype);
            }
            db_map.add(SearchResultObject.makeSearchResultObject(query_string, result, false));
        }   
        return db_map;
    }
    
    public ArrayList <SearchResultObject> processPathCursor(String query_string, byte pathway_search_subtype, DBCursor cursor){
        ArrayList <SearchResultObject> db_map = new ArrayList <SearchResultObject>();
        while (cursor.hasNext()) {
            DBObject match = (DBObject) cursor.next();
            String pathid = (String)match.get("_id");
            String pathway = (String)match.get("pathway");
            List<BasicDBObject> aliases = (List<BasicDBObject>) match.get("genes");
            PathwayObject result = new PathwayObject(pathway, pathid, "", pathway_search_subtype);
            result.nGenes = aliases.size();
            db_map.add(SearchResultObject.makeSearchResultObject(query_string, result, false));
        }
        return db_map;
    }
    
    public ArrayList <SearchResultObject> processGoCursor(String query_string, byte go_search_subtype, DBCursor cursor){
        ArrayList <SearchResultObject> db_map = new ArrayList <SearchResultObject>();
        while (cursor.hasNext()) {
            DBObject match = (DBObject) cursor.next();
            String id = (String)match.get("_id");
            String go = (String)match.get("go");
            String term = (String)match.get("term");
            List<BasicDBObject> aliases = (List<BasicDBObject>) match.get("genes");
            GoObject result = new GoObject(id, go, "", term, go_search_subtype);
            result.nGenes = aliases.size();
            db_map.add(SearchResultObject.makeSearchResultObject(query_string, result, false));
        }
        return db_map;
    }
    
    /*
    public HashMap <String, String[]> processGeneCursor(DBCursor cursor, String DBObjectName, String DBElementName){
        
        HashMap <String, String[]> db_map = new HashMap <String, String[]>();
        
        while (cursor.hasNext()) {
            DBObject match = (DBObject) cursor.next();
            String key = (String)match.get("_id");
            List<BasicDBObject> aliases = (List<BasicDBObject>) match.get(DBObjectName);
            String[] db_values = new String[aliases.size()];
            for (int i = 0; i < aliases.size(); i++) {
                db_values[i] = ((String) aliases.get(i).get(DBElementName));
            }
            
            if(!db_map.containsKey(key)){
                db_map.put(key, db_values);
            }
        }
        
        return db_map;
    }
    
    public HashMap <String, String[]> processPathCursor(DBCursor cursor){
        
        HashMap <String, String[]> db_map = new HashMap <String, String[]>();
        
        while (cursor.hasNext()) {
            DBObject match = (DBObject) cursor.next();
            String pathid = (String)match.get("_id");
            String pathway = (String)match.get("pathway");
            String key = pathid + "," + pathway;
            List<BasicDBObject> aliases = (List<BasicDBObject>) match.get("genes");
            String[] db_values = new String[aliases.size()];
            for (int i = 0; i < aliases.size(); i++) {
                db_values[i] = ((String) aliases.get(i).get("entrez"));
            }
            
            if(!db_map.containsKey(key)){
                db_map.put(key, db_values);
            }
        }
        
        return db_map;
    }
    */
    
    public HashMap <String, String[]> processGoCursor(DBCursor cursor){
        
        HashMap <String, String[]> db_map = new HashMap <String, String[]>();
        
        while (cursor.hasNext()) {
            DBObject match = (DBObject) cursor.next();
            String id = (String)match.get("_id");
            String go = (String)match.get("go");
            String term = (String)match.get("term");
            String key = id + "," + go + "," + term;
            List<BasicDBObject> aliases = (List<BasicDBObject>) match.get("genes");
            String[] db_values = new String[aliases.size()];
            for (int i = 0; i < aliases.size(); i++) {
                db_values[i] = ((String) aliases.get(i).get("entrez"));
            }
            
            if(!db_map.containsKey(key)){
                db_map.put(key, db_values);
            }
        }
        
        return db_map;
    }
    
    public ArrayList <GeneObject> processGeneQuery (
            String queryString,
            String searchType,
            String queryType
    )   {
        
        ArrayList <GeneObject> genes_info = new ArrayList <> ();
        //ArrayList <String> entrez_ids = new ArrayList <> ();
        //ArrayList <String> gene_symbols = new ArrayList <> ();
        //ArrayList <ArrayList<String>> entrezList = new ArrayList <ArrayList<String>>();
        DBCursor cursor = null;
        
        try {

            BasicDBObject query = null;            
            
            if (searchType.equals("exact")) {
                query = new BasicDBObject("_id", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains")) {
                query = new BasicDBObject("_id", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            }

            if (queryType.equals("entrez")) {
                cursor = entrezAliasMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    GeneObject gene_info = new GeneObject();
                    gene_info.setEntrezID((String)match.get("_id"));
                    gene_info.setAliases(match);
                    List <BasicDBObject> aliases = (List <BasicDBObject>) match.get("aliases");
                    String aliases_str = "";
                    for (int a = 0; a < aliases.size()- 1; a++) {
                        aliases_str += (String) aliases.get(a).get("alias") + " , ";
                    }
                    aliases_str += (String) aliases.get(aliases.size()- 1).get("alias");
                    //gene_symbols.add(aliases_str); 
                    gene_info.setGeneIdentifier(aliases_str, GeneObject.ENTREZ);
                    //entrez_ids.add((String)match.get("_id"));
                    genes_info.add(gene_info);
                }
                
            } else if (queryType.equals("genesymbol")) {
                cursor = aliasEntrezMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrez_ids");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){
                        //gene_symbols.add((String)match.get("_id")); 
                        //entrez_ids.add((String)match.get("entrez")); 
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.GENESYMBOL); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                    }
                }
            } else if (queryType.equals("refseq")) {
                cursor = refseqMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.REFSEQ_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                    }
                    
                }
            } else if (queryType.equals("ensembl_gene_id")) {
                cursor = ensemblGeneMap.find(query);
                 while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.ENSEMBL_GENE_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                    }
                    
                }
            } else if (queryType.equals("ensembl_transcript_id")) {
                cursor = ensemblTranscriptMap.find(query);
                 while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.ENSEMBL_TRANSCRIPT_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                    }
                    
                }
            } else if (queryType.equals("ensembl_protein_id")) {
                cursor = ensemblProteinMap.find(query);
                 while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.ENSEMBL_PROTEIN_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                    }
                    
                }
            } else if (queryType.equals("uniprot_id")) {
                cursor = uniprotMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.UNIPROT_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                    }
                    
                }
            }
            
            
        } catch(Exception e) {
            Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
        }
        
        //entrezList.add(entrez_ids);
        //entrezList.add(gene_symbols);
        return genes_info;
        
    }
    
    public ArrayList <PathwayObject> processPathQuery (
            String queryString,
            String searchType,
            String queryType
    ) {
        ArrayList <PathwayObject> path_infos = new ArrayList <PathwayObject>();
        DBCursor cursor = null;
        
        try {

            BasicDBObject query = null;            
            
            if (searchType.equals("exact") && queryType.equals("pathid")) {
                query = new BasicDBObject("_id", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains") && queryType.equals("pathid")) {
                query = new BasicDBObject("_id", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            } else if (searchType.equals("exact") && queryType.equals("pathname")){
                query = new BasicDBObject("pathway", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains") && queryType.equals("pathname")) {
                query = new BasicDBObject("pathway", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            }

            if (queryType.equals("pathname")) {
                cursor = pathwayMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    PathwayObject pathinfo = new PathwayObject(PathwayObject.PATH_TERM);
                    pathinfo.setPathname((String)match.get("pathway")); 
                    pathinfo.setPathid((String)match.get("_id"));
                    pathinfo.setEntrezIDs(match.get("genes"));
                   
                    path_infos.add(pathinfo);
                }
            } else if (queryType.equals("pathid")) {
                cursor = pathwayMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    PathwayObject pathinfo = new PathwayObject(PathwayObject.PATH_ID);
                    pathinfo.setPathname((String)match.get("pathway")); 
                    pathinfo.setPathid((String)match.get("_id"));
                    pathinfo.setEntrezIDs(match.get("genes"));
                    
                    path_infos.add(pathinfo);
                }
            }
        } catch(Exception e) {
            Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
        }
        
        return path_infos;
    }
    
    
    public ArrayList <PathwayObject> processmiRNAPathQuery (
            String queryString,
            String searchType,
            String queryType
    ) {
        ArrayList <PathwayObject> path_infos = new ArrayList <PathwayObject>();
        DBCursor cursor = null;
        
        try {

            BasicDBObject query = null;            
            
            if (searchType.equals("exact") && queryType.equals("pathid")) {
                query = new BasicDBObject("_id", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains") && queryType.equals("pathid")) {
                query = new BasicDBObject("_id", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            } else if (searchType.equals("exact") && queryType.equals("pathname")){
                query = new BasicDBObject("pathway", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains") && queryType.equals("pathname")) {
                query = new BasicDBObject("pathway", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            }

            if (queryType.equals("pathname")) {
                cursor = mi_rna_pathwayMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    PathwayObject pathinfo = new PathwayObject(PathwayObject.PATH_TERM);
                    pathinfo.setPathname((String)match.get("pathway")); 
                    pathinfo.setPathid((String)match.get("_id"));
                    pathinfo.set_miRNA_IDs(match.get("genes"));
                   
                    path_infos.add(pathinfo);
                }
            } else if (queryType.equals("pathid")) {
                cursor = mi_rna_pathwayMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    PathwayObject pathinfo = new PathwayObject(PathwayObject.PATH_ID);
                    pathinfo.setPathname((String)match.get("pathway")); 
                    pathinfo.setPathid((String)match.get("_id"));
                    pathinfo.set_miRNA_IDs(match.get("genes"));
                    
                    path_infos.add(pathinfo);
                }
            }
        } catch(Exception e) {
            Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
        }
        
        return path_infos;
    }
    
    public ArrayList <GoObject> processGOQuery (
            String queryString,
            String searchType,
            String queryType
    ) {
        ArrayList <GoObject> go_infos = new ArrayList <GoObject>();
        DBCursor cursor = null;
        
        try {

            BasicDBObject query = null;            
            
            if (searchType.equals("exact") && queryType.equals("goid")) {
                query = new BasicDBObject("go", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains") && queryType.equals("goid")) {
                query = new BasicDBObject("go", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            } else if (searchType.equals("exact") && queryType.equals("goterm")){
                query = new BasicDBObject("term", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains") && queryType.equals("goterm")) {
                query = new BasicDBObject("term", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            }

            if (queryType.equals("goterm")) {
                cursor = goMap2.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    GoObject goinfo = new GoObject(GoObject.GO_TERM);
                    goinfo.setGOIDIndex((String)match.get("_id"));
                    goinfo.setGOTerm((String)match.get("term"));
                    goinfo.setGOID((String)match.get("go"));
                    goinfo.setEntrezIDs(match.get("genes"));
                    //goinfo.setEvidences(match.get("evidences"));
                    //goinfo.setDefinition((String)match.get("definition"));
                    //goinfo.setOntology((String)match.get("ontology"));
                    //goinfo.setSynonyms(match.get("synonym"));
                    
                    go_infos.add(goinfo);
                }
            } else if (queryType.equals("goid")) {
                cursor = goMap2.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    GoObject goinfo = new GoObject(GoObject.GO_ID);
                    goinfo.setGOTerm((String)match.get("term"));
                    goinfo.setGOIDIndex((String)match.get("_id"));
                    goinfo.setGOID((String)match.get("go"));
                    goinfo.setEntrezIDs(match.get("genes"));
                    //goinfo.setEvidences(match.get("evidences"));
                    //goinfo.setDefinition((String)match.get("definition"));
                    //goinfo.setOntology((String)match.get("ontology"));
                    //goinfo.setSynonyms(match.get("synonym"));
                    
                    go_infos.add(goinfo);
                }
            }
        } catch(Exception e) {
            Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
        }
        
        return go_infos;
    }
    
    public ArrayList <GoObject> processmiRNAGOQuery (
            String queryString,
            String searchType,
            String queryType
    ) {
        ArrayList <GoObject> go_infos = new ArrayList <GoObject>();
        DBCursor cursor = null;
        
        try {

            BasicDBObject query = null;            
            
            if (searchType.equals("exact") && queryType.equals("goid")) {
                query = new BasicDBObject("go", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains") && queryType.equals("goid")) {
                query = new BasicDBObject("go", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            } else if (searchType.equals("exact") && queryType.equals("goterm")){
                query = new BasicDBObject("term", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains") && queryType.equals("goterm")) {
                query = new BasicDBObject("term", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            }

            if (queryType.equals("goterm")) {
                cursor = mi_rna_goMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    GoObject goinfo = new GoObject(GoObject.GO_TERM);
                    goinfo.setGOIDIndex((String)match.get("_id"));
                    goinfo.setGOTerm((String)match.get("term"));
                    goinfo.setGOID((String)match.get("go"));
                    goinfo.set_miRNA_IDs(match.get("genes"));
                    go_infos.add(goinfo);
                }
            } else if (queryType.equals("goid")) {
                cursor = mi_rna_goMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    GoObject goinfo = new GoObject(GoObject.GO_ID);
                    goinfo.setGOTerm((String)match.get("term"));
                    goinfo.setGOIDIndex((String)match.get("_id"));
                    goinfo.setGOID((String)match.get("go"));
                    goinfo.set_miRNA_IDs(match.get("genes"));
                    go_infos.add(goinfo);
                }
            }
        } catch(Exception e) {
            Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
        }
        
        return go_infos;
    }
    
    public ArrayList <GoObject> processGOid (String id) {
        ArrayList <GoObject> go_s = new ArrayList<GoObject> ();
        DBCursor cursor = null;
        
        try{
            BasicDBObject query = new BasicDBObject("go", id.trim().toLowerCase());
            cursor = goMap2.find(query);
            
            while(cursor.hasNext()){
                DBObject match = (DBObject)cursor.next();
                GoObject go = new GoObject(match);
                go_s.add(go);
            }
            
        } catch(Exception e) {
            Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
        }
        
        return go_s;
    }
    
    public ArrayList <PathwayObject> processPathid (String id){
        
        ArrayList <PathwayObject> paths = new ArrayList<PathwayObject> ();
        DBCursor cursor = null;
                
        try{
            BasicDBObject query = new BasicDBObject("_id", id.trim().toLowerCase());
            cursor = pathwayMap.find(query);
            
            while(cursor.hasNext()){
                DBObject match = (DBObject)cursor.next();
                PathwayObject path = new PathwayObject(match);
                paths.add(path);
            }
        } catch(Exception e) {
            Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
        }
        
        return paths;
    }
    
    public ArrayList <GeneObject> processEntrezidGeneSymbol(String entrezid){
               
        ArrayList <GeneObject> genes = new ArrayList<GeneObject> ();
        DBCursor cursor = null;
        DBCursor cursor1 = null;
        
        
        try {
            BasicDBObject query = new BasicDBObject("_id", entrezid.trim().toLowerCase());
            cursor = geneMap2.find(query);
            
            while(cursor.hasNext()){
                DBObject match = (DBObject)cursor.next();
                GeneObject gene = new GeneObject(match);
                
                BasicDBObject q1 = new BasicDBObject("_id", gene.entrez_id.trim().toLowerCase());
                cursor1 = entrezAliasMap.find(q1);
                if(cursor1.hasNext()){
                    DBObject match1 = (DBObject)cursor1.next();
                    gene.setAliases(match1); 
                }
                genes.add(gene);
            }
            
            
        } catch(Exception e) {
            Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
        }
        return genes;
    }
    
    public ArrayList <String> processEntrezidGOInfo(String entrezid){
        
        ArrayList <String> goinfo = new ArrayList<String> ();
        DBCursor cursor = null;
        
        try{
            BasicDBObject query = null;  
            query = new BasicDBObject("_id", entrezid.trim().toLowerCase());
            cursor = geneMap2.find(query);
            while(cursor.hasNext()){
                DBObject match = (DBObject)cursor.next();
                List <BasicDBObject> godata = (List <BasicDBObject>) match.get("goids");
                
                for(int i = 0; i < godata.size(); i++){
                    goinfo.add((String)godata.get(i).get("go"));
                    goinfo.add((String)godata.get(i).get("ontology"));
                    goinfo.add((String)godata.get(i).get("evidence"));
                    goinfo.add((String)godata.get(i).get("term"));                    
                }
            }      
            
        } catch(Exception e) {
            Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
        }
        return goinfo;
    }
    
    public ArrayList <String> processEntrezidPathInfo(String entrezid){
        
        ArrayList <String> pathinfo = new ArrayList<String> ();
        DBCursor cursor = null;
        
        try{
            BasicDBObject query = null;  
            query = new BasicDBObject("_id", entrezid.trim().toLowerCase());
            cursor = geneMap2.find(query);
            while(cursor.hasNext()){
                DBObject match = (DBObject)cursor.next();
                List <BasicDBObject> godata = (List <BasicDBObject>) match.get("pathways");
                
                for(int i = 0; i < godata.size(); i++){
                    pathinfo.add((String)godata.get(i).get("pathway"));
                    pathinfo.add((String)godata.get(i).get("external_id"));
                    pathinfo.add((String)godata.get(i).get("source"));
                                     
                }
            }      
            
        } catch(Exception e) {
            Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
        }
        return pathinfo;
    }
    
    public ArrayList <ArrayList <String>> getAllIdentifiersFromDB (String entrezid) {
        
        ArrayList <ArrayList <String>> db_values = new ArrayList <ArrayList <String>> ();
        DBCursor cursor = null;
        
        try {
            BasicDBObject query = null; 
            query = new BasicDBObject("_id", entrezid.trim().toLowerCase());
            cursor = entrezMap.find(query);
            
            while(cursor.hasNext()){
                
                String[] DBObjectNames = {"aliases", "refseqs", "ensembl_genes", "ensembl_transcripts", "ensembl_proteins", "uniprots"};
                String[] DBElementNames = {"alias", "refseq", "ensembl_gene", "ensembl_transcript", "ensembl_protein", "uniprot"};
                
                if(cursor.hasNext()){
                    
                    DBObject match = (DBObject)cursor.next();
                    
                    for (int t=0; t<DBObjectNames.length; t++) {
                        List <BasicDBObject> aliases = (List <BasicDBObject>) match.get(DBObjectNames[t]);
                        ArrayList <String> temp = new ArrayList <> ();
                        for(int j = 0; j < aliases.size(); j++){
                            temp.add((String)aliases.get(j).get(DBElementNames[t]));
                        }
                        db_values.add(temp);
                    }
                    
                }
                
            }
        } catch(Exception e) {
            Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
        }
        
        return db_values;        
    }
    
    public ArrayList <String> getIdentifiersFromDB (String entrezid, int identifier_type) {
        
        ArrayList <String> db_values = new ArrayList <> ();
        DBCursor cursor = null;
        
        try {
            BasicDBObject query = null; 
            query = new BasicDBObject("_id", entrezid.trim().toLowerCase());
            cursor = entrezMap.find(query);

            String[] DBObjectNames = {"aliases", "refseqs", "ensembl_genes", "ensembl_transcripts", "ensembl_proteins", "uniprots"};
            String[] DBElementNames = {"alias", "refseq", "ensembl_gene", "ensembl_transcript", "ensembl_protein", "uniprot"};

            if (cursor.hasNext()) {

                DBObject match = (DBObject) cursor.next();

                List<BasicDBObject> aliases = (List<BasicDBObject>) match.get(DBObjectNames[identifier_type-1]);
                for (int j = 0; j < aliases.size(); j++) {
                    db_values.add((String) aliases.get(j).get(DBElementNames[identifier_type-1]));
                }
            }

        } catch(Exception e) {
            Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
        }
        
        return db_values;        
    }
    
    public ArrayList <String> getEntrezFromDB (String identifier, int identifier_type) {
        
        ArrayList <String> db_entrezs = new ArrayList <> ();
        DBCursor cursor = null;
        
        String fieldname = "entrezs";

        try {
            
            BasicDBObject query = null; 
            query = new BasicDBObject("_id", identifier.trim().toLowerCase());
            
            switch (identifier_type) {
                case GeneObject.ENTREZ:
                    db_entrezs.add(identifier);
                    return db_entrezs;
                case GeneObject.GENESYMBOL:
                    //cursor = geneMap2.find(query);
                    cursor = aliasEntrezMap.find(query);
                    fieldname = "entrez_ids";
                    break;
                case GeneObject.REFSEQ_ID:
                    cursor = refseqMap.find(query);
                    break;
                case GeneObject.ENSEMBL_GENE_ID:
                    cursor = ensemblGeneMap.find(query);
                    break;
                case GeneObject.ENSEMBL_PROTEIN_ID:
                    cursor = ensemblProteinMap.find(query);
                    break;
                case GeneObject.ENSEMBL_TRANSCRIPT_ID:
                    cursor = ensemblTranscriptMap.find(query);
                    break;
                case GeneObject.UNIPROT_ID:
                    cursor = uniprotMap.find(query);
                    break;
                default:
                    break;
            }
            
            if (!(cursor == null) && cursor.hasNext()) {

                DBObject match = (DBObject) cursor.next();

                List<BasicDBObject> aliases = (List<BasicDBObject>) match.get(fieldname);
                for (int j = 0; j < aliases.size(); j++) {
                    db_entrezs.add((String) aliases.get(j).get("entrez"));
                }
            }
            
        } catch(Exception e) {
            Utils.log_exception(e, e.getClass().getName() + ": " + e.getMessage());
        }
        
        return db_entrezs;
    }
    
}