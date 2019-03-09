package org.cssblab.multislide.beans.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.cssblab.multislide.searcher.GeneObject;
import org.cssblab.multislide.searcher.GoObject;
import org.cssblab.multislide.searcher.PathwayObject;
import org.cssblab.multislide.searcher.SearchResultObject;

/**
 *
 * @author Soumita
 */
public class SearchResultSummary {
    
    public static final byte TYPE_GENE_SUMMARY = 4;
    public static final byte TYPE_PATH_SUMMARY = 5;
    public static final byte TYPE_GO_SUMMARY = 6;
    
    public byte type;
    public String display_tag;
    public String html_display_tag_pre;
    public String html_display_tag_mid;
    public String html_display_tag_post;
    public String _id;
    public int background_count;
    public int intersection_count;
    
    public SearchResultSummary () { }
    
    public void createSearchResultSummary (byte summary_type, String display_tag, String _id, int background_count, int intersection_count) {
        this.type = summary_type;
        this.display_tag = display_tag;
        this._id = _id;
        this.background_count = background_count;
        this.intersection_count = intersection_count;
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
            this.html_display_tag_post = this.display_tag.substring(endPosition, this.display_tag.length()) 
                     + " [" + this.intersection_count + ", " + this.background_count + "]";
        } else {
            this.html_display_tag_pre = this.display_tag + " [" + this.intersection_count + ", " + this.background_count + "]";
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
}
