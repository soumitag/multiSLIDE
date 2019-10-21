package org.cssblab.multislide.utils;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author Soumita
 */
public class PhenotypeComparator implements Serializable, Comparator <PhenotypeSet> {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public int compare(PhenotypeSet p1, PhenotypeSet p2) {
 
        int[] comparisons = new int[p1.values.length];
        for (int i=0; i<comparisons.length; i++) {
            comparisons[i] = p1.getValue(i).compareTo(p2.getValue(i));
            if (!p1.sort_order[i]) {
                comparisons[i] = -comparisons[i];
            }
        }
        
        for (int i=0; i<comparisons.length; i++) {
            if (comparisons[i] != 0) {
                return comparisons[i];
            }
        }
        
        return comparisons[comparisons.length-1];

    }
    
}

