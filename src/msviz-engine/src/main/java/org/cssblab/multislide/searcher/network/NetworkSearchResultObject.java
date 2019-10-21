/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.searcher.network;
import org.cssblab.multislide.searcher.GeneObject;

import java.io.Serializable;
/**
 *
 * @author soumitag
 */
public class NetworkSearchResultObject implements Serializable {
    
    private static final long serialVersionUID = 1L;   
    
    public static final byte TYPE_GENE = 1;
        
    private final byte type;
    private final Object result;
    
    public NetworkSearchResultObject (byte type, Object result) {
        this.type = type;
        this.result = result;
    }
    
    public static NetworkSearchResultObject makeSearchResultObject(GeneObject result) {
        return new NetworkSearchResultObject(NetworkSearchResultObject.TYPE_GENE, result);
    } 
   
    
    public GeneObject getGeneObject() throws Exception {
        if (this.type == NetworkSearchResultObject.TYPE_GENE) {
            return (GeneObject)this.result;
        } else {
            throw new Exception();
        }
    }
    
}
