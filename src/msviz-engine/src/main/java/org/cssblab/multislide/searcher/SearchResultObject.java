package org.cssblab.multislide.searcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import org.cssblab.multislide.beans.data.SearchResultSummary;
import org.cssblab.multislide.structure.Data;
import org.cssblab.multislide.structure.FilteredSortedData;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author Soumita
 */
public class SearchResultObject implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final byte TYPE_GENE = 1;
    public static final byte TYPE_PATHWAY = 2;
    public static final byte TYPE_GO = 3;
    
    private final byte type;
    private final String query;
    private final Object result;
    public Double overlap_score;
    //public int num_common_genes;
    public int num_common_genes_in_dataset[];
    
    public SearchResultObject (byte type, String query, Object result) {
        this.type = type;
        this.result = result;
        this.query = query;
    }
    
    public byte getType() {
        return type;
    }

    public Object getResult() {
        return result;
    }
    
    public String getQuery() {
        return query;
    }
    
    public static SearchResultObject makeSearchResultObject(String query, GeneObject result) {
        return new SearchResultObject(SearchResultObject.TYPE_GENE, query, result);
    }
    
    public static SearchResultObject makeSearchResultObject(String query, GoObject result) {
        return new SearchResultObject(SearchResultObject.TYPE_GO, query, result);
    }
    
    public static SearchResultObject makeSearchResultObject(String query, PathwayObject result) {
        return new SearchResultObject(SearchResultObject.TYPE_PATHWAY, query, result);
    }
    
    public GeneObject getGeneObject() throws Exception {
        if (this.type == SearchResultObject.TYPE_GENE) {
            return (GeneObject)this.result;
        } else {
            throw new Exception();
        }
    }
    
    public GoObject getGoObject() throws Exception {
        if (this.type == SearchResultObject.TYPE_GO) {
            return (GoObject)this.result;
        } else {
            throw new Exception();
        }
    }
    
    public PathwayObject getPathwayObject() throws Exception {
        if (this.type == SearchResultObject.TYPE_PATHWAY) {
            return (PathwayObject)this.result;
        } else {
            throw new Exception();
        }
    }
    
    public Double getOverlapScore() {
        if (this.type == SearchResultObject.TYPE_GENE) {
            this.overlap_score = (Double)((GeneObject)this.result).getOverlapScore(this.query);
            return this.overlap_score;
        } else if (this.type == SearchResultObject.TYPE_PATHWAY) {
            this.overlap_score = (Double)((PathwayObject)this.result).getOverlapScore(this.query);
            return this.overlap_score;
        } else if (this.type == SearchResultObject.TYPE_GO) {
            this.overlap_score = (Double)((GoObject)this.result).getOverlapScore(this.query);
            return this.overlap_score;
        } else {
            return -1.0;
        }
    }
    
    public int getNumGenes() {
        if (this.type == SearchResultObject.TYPE_GENE) {
            return ((GeneObject)this.result).aliases.size();
        } else if (this.type == SearchResultObject.TYPE_PATHWAY) {
            return ((PathwayObject)this.result).nGenes;
        } else if (this.type == SearchResultObject.TYPE_GO) {
            return ((GoObject)this.result).nGenes;
        } else {
            return 0;
        }
    }
    
    public int compareTo(SearchResultObject that) {
        int num_common_genes_this = Utils.arrayMax(this.num_common_genes_in_dataset);
        int num_common_genes_that = Utils.arrayMax(that.num_common_genes_in_dataset);
        int c = num_common_genes_this - num_common_genes_that;
        if (c != 0) {
            return c;
        } else {
            c = this.getNumGenes() - that.getNumGenes();
            if (c != 0) {
                return c;
            } else {
                return Double.compare(this.overlap_score, that.overlap_score);
            }
        }
    }
    
    static class SearchResultObjectComparator implements Comparator<SearchResultObject> {
        @Override
        public int compare(SearchResultObject r1, SearchResultObject r2) {
            return r1.compareTo(r2);
        }
    }
    
    public static ArrayList <SearchResultObject> sortSearchResultObjects(
            ArrayList <SearchResultObject> search_results, 
            HashMap <String, Boolean> entrezMaster, 
            Data data
    ) {
        for (int i=0; i<search_results.size(); i++) {
            search_results.get(i).computeNumGeneIntersections(entrezMaster, data);
            search_results.get(i).getOverlapScore();
        }
        Collections.sort(search_results, Collections.reverseOrder(new SearchResultObjectComparator()));
        return search_results;
    }
    
    public void computeNumGeneIntersections(HashMap <String, Boolean> entrezMaster, Data data) {
        
        if (this.type == SearchResultObject.TYPE_GENE) {
            GeneObject gene = (GeneObject)this.result;
            this.num_common_genes_in_dataset = new int[data.dataset_names.length];
            for (int i=0; i<data.dataset_names.length; i++) {
                this.num_common_genes_in_dataset[i] = data.getEntrezCounts(i, gene.entrez_id);
            }
            /*
            this.num_common_genes = fs_data.getEntrezCounts("", gene.entrez_id);
            if (entrezMaster.containsKey(gene.entrez_id)) {
                this.num_common_genes = 1;
            } else {
                this.num_common_genes = 0;
            }
            */
        } else if (this.type == SearchResultObject.TYPE_PATHWAY) {
            PathwayObject path = (PathwayObject)this.result;
            this.num_common_genes_in_dataset = new int[data.dataset_names.length];
            for (int i=0; i<path.entrez_ids.size(); i++) {
                for (int j=0; j<this.num_common_genes_in_dataset.length; j++) {
                    this.num_common_genes_in_dataset[j] += data.getEntrezCounts(j, path.entrez_ids.get(i));
                }
            }
            /*
            this.num_common_genes = 0;
            for (int i=0; i<path.entrez_ids.size(); i++) {
                if (entrezMaster.containsKey(path.entrez_ids.get(i))) {
                    this.num_common_genes++;
                }
            }
            */
        } else if (this.type == SearchResultObject.TYPE_GO) {
            GoObject go = (GoObject)this.result;
            this.num_common_genes_in_dataset = new int[data.dataset_names.length];
            for (int i=0; i<go.entrez_ids.size(); i++) {
                for (int j=0; j<this.num_common_genes_in_dataset.length; j++) {
                    this.num_common_genes_in_dataset[j] += data.getEntrezCounts(j, go.entrez_ids.get(i));
                }
            }
            /*
            this.num_common_genes = 0;
            for (int i=0; i<go.entrez_ids.size(); i++) {
                if (entrezMaster.containsKey(go.entrez_ids.get(i))) {
                    this.num_common_genes++;
                }
            }
            */
        }
        
    }
    
    public String getQueryAsString() {        
        if (this.type == SearchResultObject.TYPE_GENE) {            
            return ((GeneObject)this.result).getSearchTypeAsString() + ":" + this.query;            
        } else if (this.type == SearchResultObject.TYPE_PATHWAY) {
            return ((PathwayObject)this.result).getSearchTypeAsString() + ":" + this.query;
        } else if (this.type == SearchResultObject.TYPE_GO) {
            return ((GoObject)this.result).getSearchTypeAsString() + ":" + this.query;
        } else {
            return "unknown";
        }
    }
    
    public SearchResultSummary getSummary () {
        
        SearchResultSummary summary = new SearchResultSummary();

        if (this.type == SearchResultObject.TYPE_GENE) {
            GeneObject gene = (GeneObject) this.result;
            summary.createSearchResultSummary(
                    SearchResultSummary.TYPE_GENE_SUMMARY, 
                    gene.gene_identifier, 
                    gene.entrez_id, 
                    1, 
                    this.num_common_genes_in_dataset);
            summary.createHTMLDisplayTag(query);
        } else if (this.type == SearchResultObject.TYPE_GO) {
            GoObject ontology = (GoObject) this.result;
            summary.createSearchResultSummary(
                    SearchResultSummary.TYPE_GO_SUMMARY, 
                    ontology.goTerm, 
                    ontology.goID, 
                    ontology.nGenes, 
                    this.num_common_genes_in_dataset
            );
            summary.createHTMLDisplayTag(query);
        } else if (this.type == SearchResultObject.TYPE_PATHWAY) {
            PathwayObject pathway = (PathwayObject) this.result;
            summary.createSearchResultSummary(
                    SearchResultSummary.TYPE_PATH_SUMMARY, 
                    pathway.pathway, 
                    pathway._id, 
                    pathway.nGenes, 
                    this.num_common_genes_in_dataset
            );
            summary.createHTMLDisplayTag(query);
        }

        return summary;
    }

}
