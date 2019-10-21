package org.cssblab.multislide.utils;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author Soumita
 */
public class IndexedEntrezComparator implements Serializable, Comparator <IndexedEntrez> {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public int compare(IndexedEntrez e1, IndexedEntrez e2) {
        return e1.index - e2.index;
    }
    
}
