package org.cssblab.multislide.beans.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.cssblab.multislide.searcher.GeneObject;
import org.cssblab.multislide.searcher.GoObject;
import org.cssblab.multislide.searcher.PathwayObject;
import org.cssblab.multislide.searcher.SearchResultObject;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author Soumita
 */
public class SearchResultSummary implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final byte TYPE_GENE_SUMMARY = 4;
    public static final byte TYPE_PATH_SUMMARY = 5;
    public static final byte TYPE_GO_SUMMARY = 6;
    
    public String search_id;
    public byte type;
    public String display_tag;
    public String html_display_tag_pre;
    public String html_display_tag_mid;
    public String html_display_tag_post;
    public String _id;
    public int background_count;
    public int intersection_count;
    public int[] intersection_counts_per_dataset;
    
    public SearchResultSummary () { }
    
    public void createSearchResultSummary (
            byte summary_type, 
            String display_tag, 
            String _id, 
            int background_count, 
            int[] intersection_counts_per_dataset
    ) {
        this.type = summary_type;
        this.display_tag = display_tag;
        this._id = _id;
        this.search_id = this.type + "_" + this._id;
        this.background_count = background_count;
        this.intersection_counts_per_dataset = intersection_counts_per_dataset;
        this.intersection_count = Utils.arrayMax(intersection_counts_per_dataset);
    }
    
    public void createHTMLDisplayTag(String query) {
        String search_term;
        if(query.contains(":")) { // wildcard   
            String[] parts = query.split(":", -1); 
            search_term = parts[1].trim().toLowerCase();
        } else {
            search_term = query;
        }
        int startPosition = this.display_tag.indexOf(search_term);
        int endPosition = startPosition + search_term.length();
        if (startPosition != -1) {
            this.html_display_tag_pre = this.display_tag.substring(0, startPosition);
            this.html_display_tag_mid = this.display_tag.substring(startPosition, endPosition);
            this.html_display_tag_post = this.display_tag.substring(endPosition, this.display_tag.length());
            this.html_display_tag_post += " [" + this.background_count + "," + this.intersection_count + "]";
        } else {
            this.html_display_tag_pre = this.display_tag;
            this.html_display_tag_pre += " [" + this.background_count + "," + this.intersection_count + "]";
            this.html_display_tag_mid = "";
            this.html_display_tag_post = "";
        }
    }
    
    private String insertString(String orig, String insertion, int index) {
        if (index == orig.length()) {
            return orig + insertion;
        }
        String out = "";
        for (int i = 0; i < orig.length(); i++) { 
            if (i == index) { 
                out += insertion;
            }
            out += orig.charAt(i); 
        }
        return out;
    }
    
    public static ArrayList <SearchResultSummary> summarize(ArrayList <SearchResultObject> search_result_objects) {
        ArrayList <SearchResultSummary> container_summaries = new ArrayList <SearchResultSummary> ();
        for (int i=0; i<search_result_objects.size(); i++) {
            container_summaries.add(search_result_objects.get(i).getSummary());
        }
        return container_summaries;
    }
    
    public String mapGeneGroupCodeToType()
            throws MultiSlideException {

        return SearchResultSummary.mapGeneGroupCodeToType(this.type);
    }
    
    public static String mapGeneGroupCodeToType(byte type)
            throws MultiSlideException {
        
        switch (type) {
            case SearchResultSummary.TYPE_GENE_SUMMARY:
                return "gene_group_entrez";
            case SearchResultSummary.TYPE_PATH_SUMMARY:
                return "pathid";
            case SearchResultSummary.TYPE_GO_SUMMARY:
                return "goid";
            default:
                throw new MultiSlideException("Invalid Gene Group Type Code: " + type + " valid values are: 4, 5, and 6");
        }
    }
}
