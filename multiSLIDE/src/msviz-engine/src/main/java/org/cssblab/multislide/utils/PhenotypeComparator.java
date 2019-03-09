package org.cssblab.multislide.utils;

import java.util.Comparator;

/**
 *
 * @author Soumita
 */
public class PhenotypeComparator implements Comparator <PhenotypeSet> {
    
    @Override
    public int compare(PhenotypeSet p1, PhenotypeSet p2) {
 
        int[] comparisons = new int[p1.values.length];
        for (int i=0; i<comparisons.length; i++) {
            comparisons[i] = p1.getValue(i).compareTo(p2.getValue(i));
        }
        
        for (int i=0; i<comparisons.length; i++) {
            if (comparisons[i] != 0) {
                return comparisons[i];
            }
        }
        
        return comparisons[comparisons.length-1];

    }
    
}

