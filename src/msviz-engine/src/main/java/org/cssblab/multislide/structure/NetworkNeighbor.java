package org.cssblab.multislide.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class NetworkNeighbor implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final String NETWORK_TYPE_TF_ENTREZ = "tf_entrez";
    public static final String NETWORK_TYPE_MIRNA_ID = "mirna_id";
    public static final String NETWORK_TYPE_PPI_ENTREZ = "ppi_entrez";
    
    
    /*
    public static final double[] TF_ENTREZ_NEIGHBOR_COLOR = new double[]{0, 150, 136};
    public static final double[] MIRNA_ID_NEIGHBOR_COLOR = new double[]{255, 193, 7};
    public static final double[] PPI_ENTREZ_NEIGHBOR_COLOR = new double[]{103, 58, 183};
    */
    
    public static final double[] SEARCH_KEY_COLOR = new double[]{0, 0, 0};
    
    public static final double[] TF_ENTREZ_NEIGHBOR_COLOR = new double[]{0, 150, 0};
    public static final double[] MIRNA_ID_NEIGHBOR_COLOR = new double[]{166, 94, 46};
    public static final double[] PPI_ENTREZ_NEIGHBOR_COLOR = new double[]{106, 13, 173};
    
    public static final double[] TF_ENTREZ_NEIGHBOR_STROKE_COLOR = new double[]{0, 141, 0};
    public static final double[] MIRNA_ID_NEIGHBOR_STROKE_COLOR = new double[]{166, 94, 46};
    public static final double[] PPI_ENTREZ_NEIGHBOR_STROKE_COLOR = new double[]{106, 13, 173};

    /*
    public static final double[] TF_ENTREZ_NEIGHBOR_STROKE_COLOR = new double[]{0, 0, 0};
    public static final double[] MIRNA_ID_NEIGHBOR_STROKE_COLOR = new double[]{0, 0, 0};
    public static final double[] PPI_ENTREZ_NEIGHBOR_STROKE_COLOR = new double[]{0, 0, 0};
    */
    
    String id;
    String query_entrez;
    String network_type;
    String dataset_name;
    String[] neighbor_entrez_list;
    
    public NetworkNeighbor(String dataset_name, String query_entrez, String network_type, String[] neighbor_entrez_list) {
        this.setDatasetName(dataset_name);
        this.setQueryEntrez(query_entrez);
        this.setNetworkType(network_type);
        this.setNeighborEntrezList(query_entrez, neighbor_entrez_list);
        this.createNeighborID();
    }
    
    public String getID() {
        return id;
    }

    public String getQueryEntrez() {
        return query_entrez;
    }

    public final void setQueryEntrez(String query_entrez) {
        this.query_entrez = query_entrez;
    }

    public String getNetworkType() {
        return network_type;
    }

    public final void setNetworkType(String network_type) {
        this.network_type = network_type;
    }

    public String getDatasetName() {
        return dataset_name;
    }

    public final void setDatasetName(String dataset_name) {
        this.dataset_name = dataset_name;
    }

    public String[] getNeighborEntrezList() {
        return neighbor_entrez_list;
    }

    public final void setNeighborEntrezList(String query_entrez, String[] neighbor_entrez_list) {
        /* 
        sanitize neighbor list: remove duplicates
        */
        HashMap <String, Boolean> duplicate_remove_map = new HashMap <> ();
        for (String neighbor_entrez : neighbor_entrez_list) {
            if (!neighbor_entrez.equals(query_entrez)) {
                duplicate_remove_map.put(neighbor_entrez, Boolean.FALSE);
            }
        }
        /*
        
        /*
        Convert to array
        */
        this.neighbor_entrez_list = new String[duplicate_remove_map.size()];
        Iterator it = duplicate_remove_map.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            this.neighbor_entrez_list[i++] = (String)pair.getKey();
        }
    }
    
    public final void createNeighborID() {
        this.id = this.dataset_name + "_" + this.network_type + "_" + this.query_entrez;
    }
    
    public ArrayList <Integer> getQueryEntrezPositions(HashMap <String, ArrayList<Integer>> entrezSortPositionMap) {
        if (entrezSortPositionMap.containsKey(this.query_entrez)) {
            return entrezSortPositionMap.get(this.query_entrez);
        } else {
            return new ArrayList <Integer>();
        }
    }
    
    public HashMap <Integer, Boolean> getNeighborEntrezPositions(HashMap <String, ArrayList<Integer>> entrezSortPositionMap) {
        //ArrayList <Integer> positions = new ArrayList <Integer> ();
        HashMap <Integer, Boolean> positions = new HashMap <Integer, Boolean> ();
        for (int i=0; i<this.neighbor_entrez_list.length; i++) {
            if (entrezSortPositionMap.containsKey(this.neighbor_entrez_list[i])) {
                ArrayList<Integer> ps = entrezSortPositionMap.get(this.neighbor_entrez_list[i]);
                for (int p : ps) {
                    positions.put(p, Boolean.TRUE);
                }
            }
        }
        //Arrays.sort(positions);
        return positions;
    }
    
}
