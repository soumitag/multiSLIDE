package org.cssblab.multislide.searcher;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author SOUMITA GHOSH
 */
public class GeneObject implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public static final byte ENTREZ = 0;
    public static final byte GENESYMBOL = 1;
    public static final byte REFSEQ_ID = 2;
    public static final byte ENSEMBL_GENE_ID = 3;
    public static final byte ENSEMBL_TRANSCRIPT_ID = 4;
    public static final byte ENSEMBL_PROTEIN_ID = 5;
    public static final byte UNIPROT_ID = 6;
    public static final byte OTHER_USER_DEFINED_GENE_ID = 7;         // used for user-defined custom columns
    
    public String entrez_id;
    public String gene_identifier;
    public byte gene_identifier_type;
    public ArrayList <GoObject> goTerms;
    public ArrayList <PathwayObject> pathways;
    public ArrayList <String> aliases;
    protected boolean isFilled;
    public String user_defined_identifier_name;
    
    public GeneObject (String entrez_id, String gene_identifier, byte gene_identifier_type) {
        this.entrez_id = entrez_id;
        this.gene_identifier = gene_identifier;
        this.gene_identifier_type = gene_identifier_type;
        goTerms = new ArrayList <> ();
        pathways = new ArrayList <> ();
        aliases = new ArrayList <> ();
        isFilled = false;
    }
    
    public static GeneObject createMetacolGeneObject (String entrez_id, String identifier_value, String metacol_name) {
        GeneObject GO = new GeneObject(entrez_id, identifier_value, GeneObject.OTHER_USER_DEFINED_GENE_ID);
        GO.user_defined_identifier_name = metacol_name;
        return GO;
    }
    
    public GeneObject (DBObject geneMap2_Doc) {
        
        this.gene_identifier_type = GeneObject.GENESYMBOL;
        
        this.entrez_id = (String)geneMap2_Doc.get("_id");
        this.gene_identifier = (String)geneMap2_Doc.get("genesymbol");
        
        List <BasicDBObject> go_dbobjs = 
                (List <BasicDBObject>)geneMap2_Doc.get("goids");
        
        goTerms = new ArrayList <> ();
        for (int i=0; i<go_dbobjs.size(); i++) {
            BasicDBObject go_dbobj = go_dbobjs.get(i);
            GoObject go = new GoObject (go_dbobj.getString("go"),  // should be term
                                        go_dbobj.getString("ontology"),
                                        go_dbobj.getString("evidence"),
                                        go_dbobj.getString("term"), GoObject.GO_ID);
            goTerms.add(go);
        }
        
        List <BasicDBObject> path_dbobjs = 
                (List <BasicDBObject>)geneMap2_Doc.get("pathways");
        
        pathways = new ArrayList <> ();
        for (int i=0; i<path_dbobjs.size(); i++) {
            BasicDBObject path_dbobj = path_dbobjs.get(i);
            PathwayObject path = new PathwayObject (
                                        path_dbobj.getString("pathway"),
                                        path_dbobj.getString("external_id"),
                                        path_dbobj.getString("source"),
                                        PathwayObject.PATH_ID
                                 );
            pathways.add(path);
        }
        isFilled = true;
        
    }
    
    public void setAliases (DBObject aliases_match) {
        aliases = new ArrayList <> ();
        List <BasicDBObject> aliases_list = (List <BasicDBObject>) aliases_match.get("aliases"); 
        for (int i=0; i<aliases_list.size(); i++) {
            //Utils.log_info(aliases.get(i).get("alias"));
            aliases.add((String)aliases_list.get(i).get("alias"));
        }
    }
    
    public GeneObject(){
        this.entrez_id = null;
        this.gene_identifier_type = -1;
        goTerms = new ArrayList <> ();
        pathways = new ArrayList <> ();
        aliases = new ArrayList <> ();
        isFilled = false;
    }
    
    public void setEntrezID(String eid){
        entrez_id = eid;
    }
    
    public void setGeneIdentifier (String id, byte identifier_type){
        gene_identifier = id;
        this.gene_identifier_type = identifier_type;
    }
    
    public double getOverlapScore(String query) {
        return (query.length()*1.0)/(this.gene_identifier.length()*1.0);
    }

    public String getSearchTypeAsString() {
        if (this.gene_identifier_type == GeneObject.ENTREZ) {
            return "entrez";
        } else if (this.gene_identifier_type == GeneObject.GENESYMBOL) {
            return "genesymbol";
        }  else if (this.gene_identifier_type == GeneObject.REFSEQ_ID) {
            return "refseq";
        }  else if (this.gene_identifier_type == GeneObject.ENSEMBL_GENE_ID) {
            return "ensembl gene";
        }  else if (this.gene_identifier_type == GeneObject.ENSEMBL_TRANSCRIPT_ID) {
            return "ensembl transcript";
        }  else if (this.gene_identifier_type == GeneObject.ENSEMBL_PROTEIN_ID) {
            return "ensembl protein";
        }  else if (this.gene_identifier_type == GeneObject.UNIPROT_ID) {
            return "uniprot";
        }  else if (this.gene_identifier_type == GeneObject.OTHER_USER_DEFINED_GENE_ID) {
            return this.user_defined_identifier_name;
        } else {
            return "error";
        }
    }
}

