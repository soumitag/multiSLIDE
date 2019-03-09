/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.searcher.GeneObject;

/**
 *
 * @author soumitag
 */
public class Data_1 {
    
    int nOmicsTypes;
    int nGenes;
    int nPatients;
    
    static MongoClient mongoClient;
    static boolean isConnected;
    
    static DBCollection entrezMap;
    static DBCollection aliasEntrezMap;
    static DBCollection entrezAliasMap;
    static DBCollection refseqMap;
    static DBCollection ensemblGeneMap;
    static DBCollection ensemblTranscriptMap;
    static DBCollection ensemblProteinMap;
    static DBCollection uniprotMap;
    static DBCollection mirAccMap;
    
    
    static HashMap <String, Integer> omicstype_keys;
    
    float[][][] expressions;
    boolean[][][] missing_expressions_ind;
    HashMap <String, Integer> entrezPosMap;
    
    Phenotypes[] phenotypes;
    GeneIdentifier[] gene_identifiers;
    
    public Data_1(String[] omics_types,
                String[] dataFilenames,
                String[] identifier_types,
                String clinicalInformationFilename,
                String delimiter)
    throws DataParsingException {
        
        //phenotype_keys = new HashMap <String, Integer> ();
        omicstype_keys = new HashMap <String, Integer> ();
        
        String[][] genes_data = null;               // temporary place holder for holding raw data loaded from file
        
        ArrayList <String[][]> data_files = new ArrayList <String[][]> ();
        
        for (int i=0; i<omics_types.length; i++) {
            String[][] data = loadData(dataFilenames[i], delimiter);
            data_files.add(data);
            Data_1.omicstype_keys.put(omics_types[i], i);
            
            
            /*
            for (int row=0; row<nGenes; row++) {
                genes_data[i][row] = data[row+1][0];
                for (int col=0; col<nPatients; col++) {
                    expressions[i][row][col] = Integer.parseInt(data[row+1][col+1]);
                }
            }
            */
        }
        
        consolidateDataFiles(data_files, identifier_types, omics_types);
        
        //gene_identifiers = GeneIdentifier.create(genes_data);
        //Phenotypes.createClinicalColumnHeaders(clinicalInformationFilename);
        //phenotypes = Phenotypes.createClinicalDataFromFile(clinicalInformationFilename);
    }
    
    public String[][] loadData(String filename, String delimiter) {
        
        String[][] input_data = null;
        
        try {
            input_data = FileHandler.loadDelimData(filename, delimiter, false);
            
        } catch(Exception e){
            System.out.println("Error reading input data:");
            System.out.println(e);
            //throw new DataParsingException("Unknown error while reading data file. If the problem persists please report an issue.");
        }
        
        return input_data;
    }
    
    public void consolidateDataFiles(ArrayList <String[][]> data_files, String[] identifier_types, String[] omics_type) {
        
        int omics_type_size = data_files.size();
        
        ArrayList <String[]> mapped_entrez = new ArrayList <String[]> ();
        ArrayList <String[]> mapped_sample = new ArrayList <String[]> ();
        HashMap <String, Boolean> entrez_master = new HashMap <String, Boolean> ();
        HashMap <String, Boolean> sample_master = new HashMap <String, Boolean> ();
        
        connectToMongoDB();
        for (int i = 0; i < omics_type_size; i++ ){
            //System.out.println("Omics type:" + i);
            String[][] data = data_files.get(i);
            String[] samples_in_file_i = new String[data[0].length];
            for (int row = 0; row < 1; row++) {
                for (int col = 1; col < data[0].length; col++) {
                    sample_master.put(data[row][col], Boolean.TRUE);
                    samples_in_file_i[col] = data[row][col];
                }
            }
            mapped_sample.add(samples_in_file_i);
            
            String[] mapped_entrez_i = new String[data.length];
            for (int row = 1; row < data.length; row++) {
                String identifier_value = data[row][0].trim().toLowerCase();
                String identifier_type = identifier_types[i];
                mapped_entrez_i[row] = getEntrez(identifier_type, identifier_value);
                /*
                if(mapped_entrez_i[row-1] == null){
                    System.out.println("Gene:" + "\t" + identifier_value + "\t" + "row:" + "\t" + row);
                }
                */
                if(mapped_entrez_i[row] != null){
                    entrez_master.put(mapped_entrez_i[row], Boolean.TRUE);
                }
            }
            mapped_entrez.add(mapped_entrez_i);
        }
        
        HashMap <String, ArrayList<ArrayList<Integer>>> identifierPosMap = 
                new HashMap <String, ArrayList<ArrayList<Integer>>> ();
        
        Iterator iter = entrez_master.keySet().iterator();
        while(iter.hasNext()) {
            String entrez = (String)iter.next();
            ArrayList <ArrayList<Integer>> positions = new ArrayList <ArrayList<Integer>> ();
            for (int file = 0; file < omics_type_size; file++) {
                String[] file_positions = mapped_entrez.get(file);
                ArrayList <Integer> entrez_positions_in_file = new ArrayList <Integer> ();
                for (int row = 0; row < file_positions.length; row++) {
                    if (file_positions[row] != null && file_positions[row].equals(entrez)) {
                        entrez_positions_in_file.add(row);
                    }
                }
                positions.add(entrez_positions_in_file);
            }
            identifierPosMap.put(entrez, positions);
        }
        
        ArrayList <String> sample_masterlist = new ArrayList <String> ();
        iter = sample_master.keySet().iterator();
        while(iter.hasNext()) {
            sample_masterlist.add((String)iter.next());
        }
        
        this.expressions = new float[omics_type_size][entrez_master.size()][sample_master.size()];
        this.missing_expressions_ind = new boolean[omics_type_size][entrez_master.size()][sample_master.size()];
        this.gene_identifiers = new GeneIdentifier[entrez_master.size()];
        
        int count = 0;
        iter = entrez_master.keySet().iterator();
        while(iter.hasNext()) {
            String entrez = (String)iter.next();
            ArrayList <ArrayList<Integer>> positions = identifierPosMap.get(entrez);
            for (int file = 0; file < omics_type_size; file++) {
                ArrayList <Integer> entrez_positions_in_file = positions.get(file);
                for (int j=0; j<entrez_positions_in_file.size(); j++) {
                    for (int k=0; k<sample_master.size(); k++) {
                        String sample_name = sample_masterlist.get(k);
                        String[] sample_position_in_file = mapped_sample.get(file);
                        int sample_position = -1;
                        for (int t=0; t<sample_position_in_file.length; t++) {
                            if (sample_position_in_file[t].equals(sample_name)) {
                                sample_position = t;
                                break;
                            }
                        }
                       expressions[file][j][k] = 
                               Float.parseFloat(data_files.get(file)[entrez_positions_in_file.get(j)][sample_position]);
                       missing_expressions_ind[file][j][k] = true;
                    }
                }
            }
            entrezPosMap.put(entrez, count++);
        }
        
        //HashMap <String, ArrayList<ArrayList<Integer>>> entrezDataMap = new HashMap <String, ArrayList<ArrayList<Integer>>> ();
        closeMongoDBConnection();
    }
    
    
    public static void connectToMongoDB(){
        DB db;
        
        try {
            mongoClient = new MongoClient(Arrays.asList(new ServerAddress("localhost", 27017)));
            db = mongoClient.getDB("geneVocab_HomoSapiens");
            
            entrezMap = db.getCollection("HS_EntrezMap");
            aliasEntrezMap = db.getCollection("HS_aliasEntrezMap");
            entrezAliasMap = db.getCollection("HS_entrezAliasMap");            
            refseqMap = db.getCollection("HS_refseqEntrezMap");
            ensemblGeneMap = db.getCollection("HS_ensemblEntrezMap");
            ensemblTranscriptMap = db.getCollection("HS_ensembltranscriptEntrezMap");
            ensemblProteinMap = db.getCollection("HS_ensemblprotEntrezMap");
            uniprotMap = db.getCollection("HS_uniprotEntrezMap");
            
            isConnected = true;
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
    
    
    public String getEntrez(String identifier_type, String identifier_value) { 
       
    
        ArrayList <GeneObject> genes_info = new ArrayList <> ();
        String eid = null;
        
        try {        
                        
            DBCursor cursor = null;
            BasicDBObject query = new BasicDBObject("_id", identifier_value.trim().toLowerCase());
            
            if(identifier_type.equals("genesymbol")){
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
                        eid = ((String)entrezs.get(0).get("entrez"));
                    }
                }
                
            } else if (identifier_type.equals("refseq")) {
                cursor = refseqMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.REFSEQ_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                        eid = ((String)entrezs.get(0).get("entrez"));
                    }
                }
            } else if (identifier_type.equals("ensembl_gene_id")){
                 cursor = ensemblGeneMap.find(query);
                 while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.ENSEMBL_GENE_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                        eid = ((String)entrezs.get(0).get("entrez"));
                    }
                    
                }
                 
            } else if (identifier_type.equals("ensembl_transcript_id")) {
                cursor = ensemblTranscriptMap.find(query);
                 while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.ENSEMBL_TRANSCRIPT_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                        eid = ((String)entrezs.get(0).get("entrez"));
                    }                    
                }                
            } else if (identifier_type.equals("ensembl_protein_id")){
                cursor = ensemblProteinMap.find(query);
                 while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.ENSEMBL_PROTEIN_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                        eid = ((String)entrezs.get(0).get("entrez"));
                    }
                    
                }                
            } else if (identifier_type.equals("uniprot_id")) {
                cursor = uniprotMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.UNIPROT_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                        eid = ((String)entrezs.get(0).get("entrez"));
                    }                    
                }                
            } else if (identifier_type.equals("mir_acc")){
                
            }
            
            
            
        } catch (Exception e) {
            
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
        
        return eid;
    }
    
    public String[] getSampleNames(boolean hasHeader, 
                                   String filename,
                                   String delim,
                                   int[] data_height_width) {
        if (hasHeader) {
            return FileHandler.getFileHeader(filename, delim);
        } else {
            String[] colheaders = new String[data_height_width[1]];
            for (int i = 0; i < data_height_width[1]; i++) {
                colheaders[i] = "Column_" + i;
            }
            return colheaders;
        }

    }
    
    
    /*
        Representation and manipulation of genomic intervals and variables defined along a genome
    */
    
    public void findOverlapsGenomicSegments (String chr, String start, String end, GeneCoordinate[] ref_arr, int max_gap, int min_overlap, int max_dist) {
        
        int query_start = Integer.parseInt(start);
        int query_end = Integer.parseInt(end);
        int min_dist = Integer.MAX_VALUE;
        int best_non_overlap_match = -1;
        ArrayList <Integer> overlap_list = new ArrayList<Integer>();
        
        for (int i= 0; i < ref_arr.length; i++) {
            
            int ref_start = ref_arr[i].start;
            int ref_end = ref_arr[i].end;

            int overlap_start = Math.max(query_start, ref_start);
            int overlap_end = Math.min(query_end, ref_end);

            int total_overlap = overlap_end - overlap_start;
            int query_size = query_end - query_start;
            int ref_size = ref_end - ref_start;

            int max_size_query_ref = Math.max(query_size, ref_size);

            double overlap_score = total_overlap / max_size_query_ref;

            double percent_overlap = Double.MAX_VALUE;
            int dist = Integer.MAX_VALUE;
            

            if (total_overlap < 0) {
                percent_overlap = 0;
                dist = Math.abs(total_overlap);
                if(dist < min_dist){
                    min_dist = dist;
                    best_non_overlap_match = i;
                }
                    
            } else {
                percent_overlap = overlap_score;
                if (query_start >= ref_start && query_end <= ref_end) {
                    System.out.println("Query coordinates is inside feature");
                } else if (ref_start >= query_start && ref_end <= query_end) {
                    System.out.println("Feature lies within query coordinates");
                } else if (query_start <= ref_start && query_end <= ref_end) {
                    System.out.println("Query coordinates overlap start of feature");
                } else if (ref_start <= query_start && ref_end <= query_end) {
                    System.out.println("Query coordinates overlap end of feature");
                }
                dist = 0;
                
                overlap_list.add(i);
                
            }

        }
        
        if(overlap_list.size() == 0) {
            overlap_list.add(best_non_overlap_match);
        }
        
    }

        
    
    
    
    
}
