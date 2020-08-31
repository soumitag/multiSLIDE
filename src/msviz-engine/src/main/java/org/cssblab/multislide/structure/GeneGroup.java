package org.cssblab.multislide.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.cssblab.multislide.graphics.ColorPalette;

/**
 *
 * @author soumitag
 */
public class GeneGroup implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final double[] GROUP_TYPE_GENE_COLOR = new double[]{217.0, 217.0, 217.0};
    
    private final String _id;
    public int tag;        // the index of this GeneGroup among all unique_gene_groups in FilteredSortedData. Used in HeatmapData to get color. 
    public String type;     // gene_group_entrez (since "entrez" alone is too non-descript), pathid, pathname, goid, user_defined
    public String name;     // the value of pathid, pathname, goid, etc
    public String display_tag;         // used to color
    public ArrayList <String> entrez_list;
    public double[] color;
    
    public GeneGroup(String type, String group_name, String display_tag) {
        this.display_tag = display_tag;
        this.type = type;
        this.name = group_name;
        this.entrez_list = new ArrayList <String> ();
        if (this.type.equalsIgnoreCase("gene_group_entrez")) {
            this._id = this.type;
        } else {
            this._id = this.name + "_" + this.type;
        }
        this.color = GeneGroup.GROUP_TYPE_GENE_COLOR;
    }
    
    public GeneGroup(String type, String group_name, String display_tag, ArrayList <String> entrez_list) {
        this.display_tag = display_tag;
        this.type = type;
        this.name = group_name;
        this.entrez_list = entrez_list;
        this._id = this.name + "_" + this.type;
        this.color = GeneGroup.GROUP_TYPE_GENE_COLOR;
    }
    
    public String getID() {
        return this._id;
    }
    
    public void setEntrezList(ArrayList <String> entrez_list) {
        this.entrez_list = entrez_list;
    }
    
    public void addEntrez(String entrez) {
        this.entrez_list.add(entrez);
    }
    
    public void setGeneGroupColor(double[] color) {
        this.color = color;
    }
    
}
