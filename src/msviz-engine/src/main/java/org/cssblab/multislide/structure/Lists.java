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
        featureListMap = new HashMap <> ();
        sampleListMap = new HashMap <> ();
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
            ArrayList <String[]> list =  new ArrayList <> ();
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
    
    public void addToFeatureList(
            String name, 
            ArrayList <String[]> entrez_identifiers, 
            String group_name, 
            String dataset_name
   ) throws MultiSlideException {
        
        if (hasFeatureList(name)) {
            ArrayList <String[]> feature_list = featureListMap.get(name);
            boolean[] isAlreadyInList = new boolean[entrez_identifiers.size()];
            for (int i=0; i<entrez_identifiers.size(); i++) {
                isAlreadyInList[i] = false;
                for (int j=0; j<feature_list.size(); j++) {
                    // match entrez and dataset
                    if (feature_list.get(j)[0].equalsIgnoreCase(entrez_identifiers.get(i)[0]) &&        // match entrez
                            feature_list.get(j)[2].equalsIgnoreCase(dataset_name) &&                    // match dataset name
                            feature_list.get(j)[4].equalsIgnoreCase(entrez_identifiers.get(i)[1]))      // match display features
                    {
                        isAlreadyInList[i] = true;
                        break;
                    }
                }
            }
            for (int i=0; i<entrez_identifiers.size(); i++) {
                if (!isAlreadyInList[i]) {
                    feature_list.add(new String[]{
                        entrez_identifiers.get(i)[0],       // entrez
                        entrez_identifiers.get(i)[1],       // display feature ids concatenated to a string for display
                        dataset_name, 
                        group_name
                    });
                }
            }            
        } else {
            throw new MultiSlideException("The feature list '" + name + "' does not exist.");
        }
    }
    
    public void removeFeatureFromList(String name, String entrez, String features, String dataset_name) throws MultiSlideException {
        if (hasFeatureList(name)) {
            ArrayList <String[]> list = featureListMap.get(name);
            for (int i=0; i<list.size(); i++) {
                if(list.get(i)[0].equalsIgnoreCase(entrez) && 
                        list.get(i)[1].equalsIgnoreCase(features) && 
                        list.get(i)[2].equalsIgnoreCase(dataset_name)) {
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
    
    /*
    private ArrayList <String[]> list_entrez_identifiers(ArrayList <String[]> feature_list) {
        ArrayList <String[]> feature_list_sans_group_id = new ArrayList <> ();
        for (String[] feature: feature_list) {
            feature_list_sans_group_id.add(new String[]{feature[0], feature[1], feature[2]});
        }
        return feature_list_sans_group_id;
    }
    
    private HashMap <String, ArrayList<String[]>> list_entrez_identifiers() {
        HashMap <String, ArrayList<String[]>> feature_lists_sans_group_id = new HashMap <> ();
        for (String name: this.featureListMap.keySet()) {
            feature_lists_sans_group_id.put(name, this.list_entrez_identifiers(featureListMap.get(name)));
        }
        return feature_lists_sans_group_id;
    }
    */
    
    public String serializeFeatureList(
            String name, 
            String identifier, 
            String delimval, 
            AnalysisContainer analysis
    ) throws MultiSlideException {
        
        if (hasFeatureList(name)) {
            String str = "Identifiers\t" + "Entrez\t" + "Aliases\t" + "Dataset\n";
            ArrayList <String[]> list = featureListMap.get(name);
            for (int i=0; i<list.size(); i++) {
                
                String entrez = list.get(i)[0];
                if (entrez.startsWith("-")) {
                    entrez = "-";
                }
                
                String aliases;
                if (entrez.startsWith("-")) {
                    aliases = "-";
                } else {
                    ArrayList <String> identifier_values = analysis.searcher.getIdentifiersFromDB(list.get(i)[0], 1);
                    aliases = "";
                    if (identifier_values.size() > 1) {
                        for (int j=0; j<identifier_values.size()-1; j++) {
                            aliases += identifier_values.get(j).toUpperCase() + ",";
                        }
                        aliases += identifier_values.get(identifier_values.size()-1).toUpperCase();
                    }
                }
                // disabling saving of gene groups, as a single gene can be associated with multiple gene groups
                // instead specifying: identifers, entrez, aliases, dataset name
                String dataset_name = analysis.data.datasets.get(list.get(i)[2]).specs.display_name;
                str += list.get(i)[1] + "\t" + entrez + "\t" + aliases + "\t" + dataset_name + "\n";
                /*
                if(list.get(i)[1].equalsIgnoreCase("")) {
                    str += list.get(i)[0] + "\t" + aliases + "\n";
                } else {
                    str += list.get(i)[0] + "\t" + aliases + "\t" + list.get(i)[1] + "\n";
                }
                */
            }
            return str;
        } else {
            throw new MultiSlideException("The feature list '" + name + "' does not exist.");
        }
    }
    
    public String getListsMetadata(AnalysisContainer analysis) {
        //ListData list_data = new ListData();
        return ListData.getAsJSON (analysis.searcher, this.featureListMap, "feature_list", analysis);
    }
}
