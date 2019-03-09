/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.util.ArrayList;

/**
 *
 * @author soumitag
 */
public class GeneGroup {
    
    public int tag;         // used to color
    public String type;     // pathid, pathname, goid, etc
    public String name;     // the value of pathid, pathname, goid, etc
    public ArrayList <String> entrez_list;
    
    public GeneGroup(int tag, String type, String group_name) {
        this.tag = tag;
        this.type = type;
        this.name = group_name;
        this.entrez_list = new ArrayList <String> ();
    }
    
    public GeneGroup(int tag, String type, String group_name, ArrayList <String> entrez_list) {
        this.tag = tag;
        this.type = type;
        this.name = group_name;
        this.entrez_list = entrez_list;
    }
    
    public void setEntrezList(ArrayList <String> entrez_list) {
        this.entrez_list = entrez_list;
    }
    
    public void addEntrez(String entrez) {
        this.entrez_list.add(entrez);
    }
    
    public String getGroupKey() {
        return this.type + "_" + this.name;
    }
}
