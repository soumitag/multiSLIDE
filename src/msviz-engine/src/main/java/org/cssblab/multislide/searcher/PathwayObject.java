package org.cssblab.multislide.searcher;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 *
 * @author soumitag
 */
public class PathwayObject implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final byte PATH_ID = 0;
    public static final byte PATH_TERM = 1;
    
    public byte search_type;
    public String _id;
    public String pathway;
    public String source;
    public ArrayList <String> entrez_ids;
    public ArrayList <String> genesymbols;
    public int nGenes;
    
    public PathwayObject(String pathway, String _id, String source, byte search_type) {
        this.search_type = search_type;
        this._id = _id;
        this.pathway = pathway;
        this.source = source;
        this.entrez_ids = new ArrayList <> ();
        this.genesymbols = new ArrayList <> ();
        this.nGenes = -1;
    }
    
    public PathwayObject(DBObject pathwayMap_Doc) {
        this._id = (String)pathwayMap_Doc.get("_id");
        this.pathway = (String)pathwayMap_Doc.get("pathway");
        this.source = (String)pathwayMap_Doc.get("source");
        List <BasicDBObject> gene_dbobjs = 
                (List <BasicDBObject>)pathwayMap_Doc.get("genes");
        entrez_ids = new ArrayList <> ();
        genesymbols = new ArrayList <> ();
        for (int i=0; i<gene_dbobjs.size(); i++) {
            BasicDBObject gene_dbobj = gene_dbobjs.get(i);
            //entrez_ids.add((String)gene_dbobj.getString("entrez_id"));
            entrez_ids.add((String)gene_dbobj.getString("entrez"));
            genesymbols.add((String)gene_dbobj.getString("genesymbol"));
        }
        this.nGenes = entrez_ids.size();
    }
    
    public PathwayObject(byte search_type){
        this.search_type = search_type;
        this._id = null;
        this.pathway = null;
        this.source = null;
        this.entrez_ids = new ArrayList <> ();
        this.genesymbols = new ArrayList <> ();
        this.nGenes = -1;
    }
    
    public void setPathid (String id) {
        _id = id;
    }
    
    public void setPathname (String pathname){
        pathway = pathname;
    }
    
    public void setSource (String src){
        source = src;
    }
    
    public void setEntrezIDs (Object genes){
        List <BasicDBObject> gene_list = (List <BasicDBObject>) genes;
        for (int i = 0; i < gene_list.size(); i++){
            entrez_ids.add((String)gene_list.get(i).get("entrez"));
            genesymbols.add((String)gene_list.get(i).get("genesymbol"));
        }
        this.nGenes = entrez_ids.size();
    }
    
    public void set_miRNA_IDs (Object genes){
        List <BasicDBObject> gene_list = (List <BasicDBObject>) genes;
        for (int i = 0; i < gene_list.size(); i++){
            entrez_ids.add(((String)gene_list.get(i).get("gene")).toUpperCase());
            genesymbols.add((String)gene_list.get(i).get("gene"));            
        }
        this.nGenes = entrez_ids.size();
    }
    
    public double getOverlapScore(String query) {
        if (this.search_type == PathwayObject.PATH_ID) {
            return (query.length()*1.0)/(this._id.length()*1.0);
        } else if (this.search_type == PathwayObject.PATH_TERM) {
            return (query.length()*1.0)/(this.pathway.length()*1.0);
        } else {
            return -1.0;
        }
    }
    
    public String getSearchTypeAsString() {
        if (this.search_type == PathwayObject.PATH_ID) {
            return "pathid";
        } else if (this.search_type == PathwayObject.PATH_TERM) {
            return "pathway";
        } else {
            return "error";
        }
    }

}

