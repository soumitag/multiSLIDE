package org.cssblab.multislide.utils;

import java.io.Serializable;

/**
 *
 * @author Soumita
 */
public class IndexedEntrez implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public int index;
    public String entrez;
    
    public IndexedEntrez(int index, String entrez) {
        this.index = index;
        this.entrez = entrez;
    }
    
    public String getEntrez(int i) {
        return this.entrez;
    }
    
}
