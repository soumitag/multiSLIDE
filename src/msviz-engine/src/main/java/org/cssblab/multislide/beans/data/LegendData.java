package org.cssblab.multislide.beans.data;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Soumita
 */
public class LegendData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final int LEGEND_GROUP_TYPE_PHENOTYPE = 1;
    public static final int LEGEND_GROUP_TYPE_GENE_GROUP = 2;
    public static final int LEGEND_GROUP_TYPE_CLUSTER_LABEL = 3;
    public static final int LEGEND_GROUP_TYPE_NETWORK_NEIGHBOR = 4;
    public static final int LEGEND_GROUP_TYPE_INTER_OMICS_CONNECTION = 5;
    
    private ArrayList <Integer> legend_group_types;
    private ArrayList <String> legend_group_titles;
    private ArrayList <ArrayList <LegendGroup>> legend_groups;
    
    private transient HashMap <String, ArrayList <LegendGroup>> legend_group_map;
    private transient HashMap <String, Integer> legend_type_map;
    
    public LegendData() {
        legend_group_types = new ArrayList <Integer> ();
        legend_group_titles = new ArrayList <String> ();
        legend_groups = new ArrayList <ArrayList <LegendGroup>> ();
        
        legend_group_map = new HashMap <String, ArrayList <LegendGroup>> ();
        legend_type_map = new HashMap <String, Integer> ();
    }
    
    public void addLegendGroup(String group_title, ArrayList <LegendGroup> legend_group, int type) {
        legend_group_map.put(group_title, legend_group);
        legend_type_map.put(group_title, type);
    }
    
    public void removeLegendGroup(String group_title, ArrayList <LegendGroup> legend_group, int type) {
        if (legend_group_map.containsKey(group_title)) {
            legend_group_map.remove(group_title);
            legend_type_map.remove(group_title);
        }
    }
    
    public String legendDataAsJSON() {
        Iterator iter = this.legend_group_map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry pair = (Map.Entry)iter.next();
            String title = (String)pair.getKey();
            ArrayList <LegendGroup> legend_group = (ArrayList <LegendGroup>) pair.getValue();
            int type = this.legend_type_map.get(title);
            this.legend_group_types.add(type);
            this.legend_group_titles.add(title);
            this.legend_groups.add(legend_group);
        }
        return new Gson().toJson(this);
    }

}
