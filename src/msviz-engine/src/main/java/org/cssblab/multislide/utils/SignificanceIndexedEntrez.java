package org.cssblab.multislide.utils;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author soumitag
 */
public class SignificanceIndexedEntrez implements Serializable, Comparable <SignificanceIndexedEntrez> {
    
    private static final long serialVersionUID = 1L;
    
    public float q_value;
    public String entrez;
    public int orig_position;
    
    public SignificanceIndexedEntrez(float q_value, String entrez, int orig_position) {
        this.q_value = q_value;
        this.entrez = entrez;
        this.orig_position = orig_position;
    }
    
    public String getEntrez(int i) {
        return this.entrez;
    }
    
    @Override
    public int compareTo(SignificanceIndexedEntrez that) {
        return Float.compare(this.q_value, that.q_value);
    }
    
}


