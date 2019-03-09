package org.cssblab.multislide.utils;

/**
 *
 * @author Soumita
 */
public class IndexedEntrez {
    
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
