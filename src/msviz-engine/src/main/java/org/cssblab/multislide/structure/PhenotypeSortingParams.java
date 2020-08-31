package org.cssblab.multislide.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.cssblab.multislide.utils.CollectionUtils;

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
    
    /*
        update phenotype sorting params when selected phenotypes changes
    */
    public void updateOnPhenSelectionChange(String[] selected_phenotypes) {
        HashMap <String, Boolean> selected_phens = CollectionUtils.asMap(selected_phenotypes);
        List <String> _phens = new ArrayList <> ();
        List <Boolean> _orders = new ArrayList <> ();
        for (int i=0; i<phenotypes.length; i++) {
            if (selected_phens.containsKey(phenotypes[i])) {
                _phens.add(phenotypes[i]);
                _orders.add(sort_orders[i]);
                selected_phens.put(phenotypes[i], Boolean.FALSE);
            }
        }
        if (_phens.size() < 5) {
            for (String s: selected_phens.keySet()) {
                if (selected_phens.get(s)) {
                    _phens.add(s);
                    _orders.add(SORT_ASCENDING);
                }
            }
        }
        this.phenotypes = CollectionUtils.slice(CollectionUtils.asArray(_phens), 0, Math.min(_phens.size(), 5));
        this.sort_orders = CollectionUtils.slice(CollectionUtils.asBoolArray(_orders), 0, Math.min(_phens.size(), 5));
    }
    
    
}
