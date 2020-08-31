/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author soumitaghosh
 */
public class FunctionalGroupContainer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String _id;
    public String functional_grp_name;
    public int size;
    /*
        a map from [dataset name, column name] -> [list of members]
    */
    public HashMap <List<String>, List<String>> dataset_column_member_map;

    public FunctionalGroupContainer () {
        this.functional_grp_name = "";
        //this.functional_grp_members = new ArrayList <String> ();
        this.dataset_column_member_map = new HashMap <> ();
    }
    
    /*
    public FunctionalGroupContainer (
            String identifier, String functional_grp_name, 
            String dataset_name, String column_name, ArrayList <String> functional_grp_members) {
        this._id = identifier;
        this.functional_grp_name = functional_grp_name;
        //this.functional_grp_members = functional_grp_members;
        this.dataset_name = dataset_name;
        this.column_name = column_name;
    }
    */
    
    public FunctionalGroupContainer (
            String identifier, 
            String functional_grp_name, 
            HashMap <List<String>, List<String>> dataset_column_member_map
    ) {
        this._id = identifier;
        this.functional_grp_name = functional_grp_name;
        this.dataset_column_member_map = dataset_column_member_map;
        this.size = 0;
        for (List<String> k: this.dataset_column_member_map.keySet()) {
            this.size += this.dataset_column_member_map.get(k).size();
        }
    }
    
    public static FunctionalGroupContainer[] populateList() {

        FunctionalGroupContainer[] container = new FunctionalGroupContainer[3];
        ArrayList <String> t1 = new ArrayList <> ();
        t1.add("Gene_1");
        t1.add("Gene_2");
        t1.add("Gene_3");
        List <String> dataset_column_name_key = Arrays.asList("dataset_1", "column_1");
        HashMap <List <String>, List <String>> map1 = new HashMap <> ();
        map1.put(dataset_column_name_key, t1);
        container[0] = new FunctionalGroupContainer("0", "Pathway_1", map1);
        
        ArrayList <String> t2 = new ArrayList <> ();
        t2.add("Gene_3");
        t2.add("Gene_4");
        t2.add("Gene_5");
        HashMap <List <String>, List <String>> map2 = new HashMap <> ();
        map2.put(dataset_column_name_key, t2);
        container[1] = new FunctionalGroupContainer("0", "Pathway_2", map2);
        
        
        ArrayList <String> t3 = new ArrayList <> ();
        t3.add("Gene_5");
        t3.add("Gene_6");
        t3.add("Gene_1");
        HashMap <List <String>, List <String>> map3 = new HashMap <> ();
        map3.put(dataset_column_name_key, t3);
        container[2] = new FunctionalGroupContainer("2", "Pathway_3", map3);
        
        return container;
        
    }
    
    public FunctionalGroupContainer getSummary() {
        FunctionalGroupContainer fgc = new FunctionalGroupContainer(this._id, this.functional_grp_name, new HashMap <> ());
        fgc.size = this.size;
        return fgc;
    }
}
