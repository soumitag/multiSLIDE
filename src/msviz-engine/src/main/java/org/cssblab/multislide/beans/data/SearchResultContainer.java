/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Soumita
 */
public class SearchResultContainer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final byte TYPE_GENE = 1;
    public static final byte TYPE_PATH = 2;
    public static final byte TYPE_GO = 3;
    public static final byte TYPE_GENE_SUMMARY = 4;
    public static final byte TYPE_PATH_SUMMARY = 5;
    public static final byte TYPE_GO_SUMMARY = 6;
    
    public byte type;
    public String entrez_id;
    public String group_id;
    public String group_name;
    public int count;
    
    public SearchResultContainer () { }
    
    public void createGeneSearchResult (String entrez_id, String genesymbol) {
        this.type = 1;
        this.entrez_id = entrez_id;
        this.group_name = genesymbol;
    }
    
    public void createPathwaySearchResult (String entrez_id, String path_id, String pathname) {
        this.type = 2;
        this.entrez_id = entrez_id;
        this.group_id = path_id;
        this.group_name = pathname;
    }
    
    public void createGOSearchResult (String entrez_id, String go_id, String go_term) {
        this.type = 3;
        this.entrez_id = entrez_id;
        this.group_id = go_id;
        this.group_name = go_term;
    }
    
    public void createSearchResultSummary (byte summary_type, String group_id, String group_name, int count) {
        this.type = summary_type;
        this.group_id = group_id;
        this.group_name = group_name;
        this.count = count;
    }
    
    public String getEntrezID() {
        return this.entrez_id;
    }
    
    public String getGeneSymbol() {
        if (type == 1)
            return this.group_name;
        else
            return null;
    }
    
    public String getPathID() {
        if (type == 2)
            return this.group_id;
        else
            return null;
    }
    
    public String getGOID() {
        if (type == 3)
            return this.group_id;
        else
            return null;
    }
    
    public String getPathName() {
        if (type == 2)
            return this.group_name;
        else
            return null;
    }
    
    public String getGOTerm() {
        if (type == 3)
            return this.group_name;
        else
            return null;
    }
    
    public static ArrayList <SearchResultContainer> summarize(ArrayList <SearchResultContainer> container_list) {
        HashMap <String,String> group_id_name_map = new HashMap <String,String>();
        HashMap <String,Integer> group_id_count_map = new HashMap <String,Integer>();
        
        for (int i=0; i<container_list.size(); i++) {
            SearchResultContainer container = container_list.get(i);
            group_id_name_map.put(container.group_id, container.group_name);
            if (group_id_count_map.containsKey(container.group_id)) {
                int val = group_id_count_map.get(container.group_id);
                group_id_count_map.put(container.group_id, ++val);
            } else {
                group_id_count_map.put(container.group_id, 1);
            }
        }
        
        ArrayList <SearchResultContainer> container_summaries = new ArrayList <SearchResultContainer> ();
        for (Map.Entry pair : group_id_name_map.entrySet()) {
            SearchResultContainer summary = new SearchResultContainer ();
            summary.createSearchResultSummary((byte)4, 
                    (String)pair.getKey(), (String)pair.getValue(), group_id_count_map.get((String)pair.getKey()));
            container_summaries.add(summary);
        }
     
        return container_summaries;
    }
}
