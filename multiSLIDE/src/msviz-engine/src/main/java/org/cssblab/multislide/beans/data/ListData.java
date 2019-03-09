/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;

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

public class ListData {

    public String name;
    public String type;
    public String[] group_names;
    public String[] group_types;
    public String[] entrez;
    public int[] entrez_group_ids;
    
    public ListData(String name, String type, ArrayList <String[]> list_data) {
        
        this.name = name;
        this.type = type;
        
        HashMap <String, Integer> group_names_map = new HashMap <String, Integer> ();
        int group_count = 0;
        for (int i=0; i<list_data.size(); i++) {
            String group_name = list_data.get(i)[1];
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
        
        this.entrez = new String[list_data.size()];
        this.entrez_group_ids = new int[list_data.size()];
        for (int i=0; i<list_data.size(); i++) {
            String[] list_entrez = list_data.get(i);
            entrez[i] = list_entrez[0];
            entrez_group_ids[i] = group_names_map.get(list_entrez[1]);
        }
    }
    
    public static String getAsJSON (HashMap <String, ArrayList<String[]>> list_map, String type) {
        
        ListData[] lists;
        lists = new ListData[list_map.size()];
        int i = 0;
        for (String key : list_map.keySet()) {
            lists[i++] = new ListData(key, type, list_map.get(key));
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

