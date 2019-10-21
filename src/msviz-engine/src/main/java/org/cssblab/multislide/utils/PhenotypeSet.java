package org.cssblab.multislide.utils;

import java.io.Serializable;

/**
 *
 * @author Soumita
 */
public class PhenotypeSet implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public int index;
    public String[] values;
    public boolean[] sort_order;
    
    public PhenotypeSet(int index, String[] values, boolean[] sort_order) {
        this.index = index;
        this.values = values;
        this.sort_order = sort_order;
    }
    
    public String getValue(int i) {
        return this.values[i];
    }

    @Override
    public String toString() {
        String str = index + ": ";
        for (int i=0; i<values.length-1; i++) {
            str += values[i] + "\t";
        }
        str += values[values.length-1];
        return str;
    }
}
