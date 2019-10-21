package org.cssblab.multislide.structure;

import java.io.Serializable;

/**
 *
 * @author soumitag
 */
public class PhenotypeSortingParams implements Serializable{

    private static final long serialVersionUID = 1L;
    
    public static final boolean SORT_ASCENDING = true;
    public static final boolean SORT_DESCENDING = false;
    
    private String[] phenotypes;
    private boolean[] sort_orders;
    
    public PhenotypeSortingParams() {
        this.phenotypes = new String[0];
        this.sort_orders = new boolean[0];
    }
    
    public PhenotypeSortingParams(String[] phenotypes) {
        this.phenotypes = phenotypes;
        this.sort_orders = new boolean[phenotypes.length];
        for (int i=0; i<this.sort_orders.length; i++) {
            this.sort_orders[i] = true;
        }
    }
    
    public PhenotypeSortingParams(String[] phenotypes, boolean[] sort_order) {
        this.phenotypes = phenotypes;
        this.sort_orders = sort_order;
    }
    
    public String[] getPhenotypes() {
        return this.phenotypes;
    }
    
    public boolean[] getSortOrders() {
        return this.sort_orders;
    }
    
}
