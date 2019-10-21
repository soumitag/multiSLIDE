/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.cssblab.multislide.beans.data.ListData;
import org.cssblab.multislide.searcher.Searcher;

/**
 *
 * @author soumitag
 */


public class Lists implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    HashMap <String, ArrayList<String[]>> featureListMap;
    HashMap <String, ArrayList<String[]>> sampleListMap;
    
    public Lists() {
        featureListMap = new HashMap <String, ArrayList <String[]>> ();
        sampleListMap = new HashMap <String, ArrayList <String[]>> ();
    }
    
    public String generateFeatureListName() throws MultiSlideException {
        
        for(int i = 1; i < 101; i++){
            String list_name = "New_List_" + i;
            if(!featureListMap.containsKey(list_name)){
                return list_name;
            }
        }
        throw new MultiSlideException("Unable to allocate feature list name");
    }
    
    public void createEmptyFeatureList(String name) throws MultiSlideException {
        if (!hasFeatureList(name)) {
            ArrayList <String[]> list =  new ArrayList <String[]> ();
            featureListMap.put(name,list);
        } else {
            throw new MultiSlideException("A feature list with the same name already exists. Please specify a different name.");
        }
    }
    
    public void removeFeatureList(String name) throws MultiSlideException {
        if (hasFeatureList(name)) {
            featureListMap.remove(name);
        } else {
            throw new MultiSlideException("The feature list '" + name + "' does not exist.");
        }
    }
    
    public void addToFeatureList(String name, ArrayList <String> entrez, String group_name) throws MultiSlideException {
        if (hasFeatureList(name)) {
            ArrayList <String[]> feature_list = featureListMap.get(name);
            boolean[] isAlreadyInList = new boolean[entrez.size()];
            for (int i=0; i<entrez.size(); i++) {
                isAlreadyInList[i] = false;
                for (int j=0; j<feature_list.size(); j++) {
                    if (feature_list.get(j)[0].equalsIgnoreCase(entrez.get(i))) {
                        isAlreadyInList[i] = true;
                        break;
                    }
                }
            }
            for (int i=0; i<entrez.size(); i++) {
                if (!isAlreadyInList[i]) {
                    if (group_name.equalsIgnoreCase("single_feature")) {
                        feature_list.add(new String[]{entrez.get(i),""});
                    } else {
                        feature_list.add(new String[]{entrez.get(i),group_name});
                    }
                }
            }            
        } else {
            throw new MultiSlideException("The feature list '" + name + "' does not exist.");
        }
    }
    
    public void removeFeatureFromList(String name, String entrez) throws MultiSlideException {
        if (hasFeatureList(name)) {
            ArrayList <String[]> list = featureListMap.get(name);
            for (int i=0; i<list.size(); i++) {
                if(list.get(i)[0].equalsIgnoreCase(entrez)) {
                    list.remove(i);
                }
            }
        } else {
            throw new MultiSlideException("The feature list '" + name + "' does not exist.");
        }
    }
    
    public void removeGroupFromList(String name, String group_name) throws MultiSlideException {
        if (hasFeatureList(name)) {
            ArrayList <String[]> list = featureListMap.get(name);
            for (int i=0; i<list.size(); i++) {
                if(list.get(i)[1].equalsIgnoreCase(group_name)) {
                    list.remove(i);
                }
            }
        } else {
            throw new MultiSlideException("The feature list '" + name + "' does not exist.");
        }
    }
    
    public boolean hasFeatureList(String name) {
        return featureListMap.containsKey(name);
    }
    
    public String serializeFeatureList(String name, String identifier, String delimval, Searcher searcher) throws MultiSlideException {
        if (hasFeatureList(name)) {
            String str = "";
            ArrayList <String[]> list = featureListMap.get(name);
            for (int i=0; i<list.size(); i++) {
                ArrayList <String> identifier_values = searcher.getIdentifiersFromDB(list.get(i)[0], 1);
                String aliases = "";
                if (identifier_values.size() > 1) {
                    for (int j=0; j<identifier_values.size()-1; j++) {
                        aliases += identifier_values.get(j).toUpperCase() + ", ";
                    }
                    aliases += identifier_values.get(identifier_values.size()-1).toUpperCase();
                }
                if(list.get(i)[1].equalsIgnoreCase("")) {
                    str += list.get(i)[0] + "\t" + aliases + "\n";
                } else {
                    str += list.get(i)[0] + "\t" + aliases + "\t" + list.get(i)[1] + "\n";
                }
            }
            return str;
        } else {
            throw new MultiSlideException("The feature list '" + name + "' does not exist.");
        }
    }
    
    public String getListsMetadata(Searcher searcher) {
        //ListData list_data = new ListData();
        return ListData.getAsJSON (searcher, this.featureListMap, "feature_list", 1);
    }
}
