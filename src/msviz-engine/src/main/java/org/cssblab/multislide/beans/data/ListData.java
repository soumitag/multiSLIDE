package org.cssblab.multislide.beans.data;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.structure.AnalysisContainer;

/**
 *
 * @author soumitag
 */

/*
class ListEntrez {
    public String entrez;
    public int group_id;
    
    public ListEntrez(String entrez, int group_id) {
        this.entrez = entrez;
        this.group_id = group_id;
    }
}
*/

public class ListData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String name;
    public String type;
    //public String[] group_names;
    //public String[] group_types;
    public String[] entrez;
    public String[] features;
    //public int[] entrez_group_ids;
    public String[] display_tags;
    //public String identifier_type;
    public String[] dataset_names;
    
    public ListData(
            Searcher searcher,
            String name, 
            String type, ArrayList <String[]> list_data, 
            AnalysisContainer analysis
    ) {
        
        this.name = name;
        this.type = type;
        
        /*
        HashMap <String, Integer> group_names_map = new HashMap <String, Integer> ();
        int group_count = 0;
        for (int i=0; i<list_data.size(); i++) {
            String group_name = list_data.get(i)[3];
            if (!group_names_map.containsKey(group_name)) {
                group_names_map.put(group_name,group_count++);
            }
        }
        
        this.group_names = new String[group_names_map.size()];
        this.group_types = new String[group_names_map.size()];
        for (String key : group_names_map.keySet()) {
            this.group_names[group_names_map.get(key)] = key;
            this.group_types[group_names_map.get(key)] = key;
        }
        */
        
        this.entrez = new String[list_data.size()];
        this.features = new String[list_data.size()];
        //this.entrez_group_ids = new int[list_data.size()];
        this.display_tags = new String[list_data.size()];
        this.dataset_names = new String[list_data.size()];
        for (int i=0; i<list_data.size(); i++) {
            String[] list_entrez = list_data.get(i);
            entrez[i] = list_entrez[0];
            features[i] = list_entrez[1];
            //entrez_group_ids[i] = group_names_map.get(list_entrez[3]);
            dataset_names[i] = list_entrez[2];
            String dataset_name = analysis.data.datasets.get(list_entrez[2]).specs.display_name;
            display_tags[i] = list_entrez[1] + " (" + dataset_name + ")";
            /*
            ArrayList <String> identifier_values = searcher.getIdentifiersFromDB(entrez[i], identifier_index);
            display_tags[i] = "Entrez: " + entrez[i] + ", Aliases: ";
            if (identifier_values.size() > 1) {
                for (int j=0; j<identifier_values.size()-1; j++) {
                    display_tags[i] += identifier_values.get(j).toUpperCase() + ", ";
                }
                display_tags[i] += identifier_values.get(identifier_values.size()-1).toUpperCase();
            }
            */
        }
    }
    
    public static String getAsJSON (
            Searcher searcher, 
            HashMap <String, ArrayList<String[]>> list_map, 
            String type,
            AnalysisContainer analysis
    ) {
        
        ListData[] lists;
        lists = new ListData[list_map.size()];
        int i = 0;
        for (String key : list_map.keySet()) {
            lists[i++] = new ListData(searcher, key, type, list_map.get(key), analysis);
        }
        
        String json = new Gson().toJson(lists);
        return json;
    }
    
}

/*
public class ListData {
    
    List[] lists;
    
    public ListData(HashMap <String, ArrayList<String[]>> list_map, String type) {
        lists = new List[list_map.size()];
        int i = 0;
        for (String key : list_map.keySet()) {
            lists[i++] = new List(key, type, list_map.get(key));
        }
    }
    
    public String getAsJSON () {
        String json = new Gson().toJson(this);
        return json;
    }
}
*/

