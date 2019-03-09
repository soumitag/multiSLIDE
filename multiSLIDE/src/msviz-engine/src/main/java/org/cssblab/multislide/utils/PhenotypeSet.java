package org.cssblab.multislide.utils;

/**
 *
 * @author Soumita
 */
public class PhenotypeSet {
    
    public int index;
    public String[] values;
    
    public PhenotypeSet(int index, String[] values) {
        this.index = index;
        this.values = values;
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
