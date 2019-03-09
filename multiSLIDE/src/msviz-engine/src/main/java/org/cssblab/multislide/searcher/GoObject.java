package org.cssblab.multislide.searcher;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author soumitag
 */
public class GoObject implements Serializable {
    
    public static final byte GO_ID = 0;
    public static final byte GO_TERM = 1;
     
    public String goIDIndex;
    public String goID;
    public String goTerm;
    public String ontology;
    public String evidence;
    
    public boolean isFilled;
    
    public byte search_type;
    public ArrayList <String> evidences;
    public ArrayList <String> entrez_ids;
    public ArrayList <String> genesymbols;
    public String definition;
    public ArrayList <String> synonyms;
    public String secondary;
    public int nGenes;
    
    //public GoObject (String goID, String goTerm, String ontology) {
    public GoObject (String goID, String ontology, String evidence, String goTerm, byte search_type) {    
        /*
            GoObject go = new GoObject (go_dbobj.getString("go"),  // should be term
                                        go_dbobj.getString("ontology"),
                                        go_dbobj.getString("evidence"),
                                        go_dbobj.getString("term"));
        
        
        */
        
        
        this.goID = goID;
        this.goTerm = goTerm;
        this.ontology = ontology;
        //this.evidence = evidence;
        this.search_type = search_type;
        
        this.isFilled = false;
        
        evidences = new ArrayList <> ();
        entrez_ids = new ArrayList <> ();
        genesymbols = new ArrayList <> ();
        definition = "";
        synonyms = new ArrayList <> ();
        secondary = "";
        this.nGenes = -1;
    }
    
    public GoObject (DBObject goMap2_Doc) {
        this.goIDIndex = (String)goMap2_Doc.get("_id");
        this.goID = (String)goMap2_Doc.get("go");
        this.ontology = (String)goMap2_Doc.get("ontology");
        this.goTerm = (String)goMap2_Doc.get("term");
        //this.evidence = (String)goMap2_Doc.get("evidences");
        
        
        List <BasicDBObject> evidence_dbobjs = 
                (List <BasicDBObject>)goMap2_Doc.get("evidences");
        evidences = new ArrayList <> ();
        for (int i=0; i<evidence_dbobjs.size(); i++) {
            evidences.add(evidence_dbobjs.get(i).getString("evidence"));
        }
        
        List <BasicDBObject> entrez_id_dbobjs = 
                (List <BasicDBObject>) goMap2_Doc.get("genes");
        entrez_ids = new ArrayList <> ();
        genesymbols = new ArrayList <> ();
        for (int i=0; i<entrez_id_dbobjs.size(); i++) {
            entrez_ids.add(entrez_id_dbobjs.get(i).getString("entrez"));
            genesymbols.add(entrez_id_dbobjs.get(i).getString("genesymbol"));
        }
        
        this.definition = (String)goMap2_Doc.get("definition");
        
        List <BasicDBObject> synonym_dbobjs = 
                (List <BasicDBObject>) goMap2_Doc.get("synonyms");
        synonyms = new ArrayList <> ();
        for (int i=0; i<synonym_dbobjs.size(); i++) {
            synonyms.add(synonym_dbobjs.get(i).getString("synonym"));
        }
        
        this.secondary = (String)goMap2_Doc.get("secondary");
        this.isFilled = true;
        
        this.nGenes = entrez_ids.size();
    }
    
    public GoObject(byte search_type) {
        
        this.search_type = search_type;
        this.goIDIndex = null;
        this.goID = null;
        this.goTerm = null;
        this.ontology = null;
        this.evidence = null;
        this.isFilled = false;
        
        evidences = new ArrayList <> ();
        entrez_ids = new ArrayList <> ();
        genesymbols = new ArrayList <> ();
        definition = "";
        synonyms = new ArrayList <> ();
        secondary = "";
        
        this.nGenes = -1;
    }
    
    public void setGOIDIndex(String goidIndex){
        goIDIndex = goidIndex;
    }
    
    public void setGOID (String goid){
        goID = goid;
    }
    
    
    public void setGOTerm (String goterm){
        goTerm = goterm;
    }
    
    public void setEntrezIDs (Object genes){
        List <BasicDBObject> gene_list = (List <BasicDBObject>) genes;
        for (int i = 0; i < gene_list.size(); i++){
            entrez_ids.add((String)gene_list.get(i).get("entrez"));
            genesymbols.add((String)gene_list.get(i).get("genesymbol"));            
        }
        this.nGenes = entrez_ids.size();
    }
    
    public void setOntology (String onto){
        ontology = onto;
    }
    
    public void setEvidences (Object evids){
        List <BasicDBObject> evid_list = (List <BasicDBObject>) evids;
        for(int i = 0; i < evid_list.size(); i++){
            evidences.add((String)evid_list.get(i).get("evidence"));
        }
    }
    
    public void setDefinition (String def){
        definition = def;
    }
    
    public void setSynonyms (Object syns){
        List <BasicDBObject> synonym_list = (List <BasicDBObject>) syns;
        for(int i = 0; i < synonym_list.size(); i++){
            synonyms.add((String)synonym_list.get(i).get("synonym"));
        }
    }
    
    public double getOverlapScore(String query) {
        if (this.search_type == GoObject.GO_ID) {
            return (query.length()*1.0)/(this.goID.length()*1.0);
        } else if (this.search_type == GoObject.GO_TERM) {
            return (query.length()*1.0)/(this.goTerm.length()*1.0);
        } else {
            return -1.0;
        }
    }
    
    public String getSearchTypeAsString() {
        if (this.search_type == GoObject.GO_ID) {
            return "goid";
        } else if (this.search_type == GoObject.GO_TERM) {
            return "goterm";
        } else {
            return "error";
        }
    }
    
}
