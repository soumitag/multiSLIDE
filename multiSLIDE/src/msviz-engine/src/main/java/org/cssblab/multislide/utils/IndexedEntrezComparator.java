package org.cssblab.multislide.utils;

import java.util.Comparator;

/**
 *
 * @author Soumita
 */
public class IndexedEntrezComparator  implements Comparator <IndexedEntrez>{
    
    @Override
    public int compare(IndexedEntrez e1, IndexedEntrez e2) {
        return e1.index - e2.index;
    }
    
}
