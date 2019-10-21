/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

import com.google.gson.Gson;
import java.io.Serializable;

/**
 *
 * @author Soumita
 */
public class PreviewData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String[][] data;
    public ServerResponse response;
    
    public PreviewData(String[][] data, ServerResponse response) {
        this.data = data;
        this.response = response;
    }
    
    public PreviewData(ServerResponse response) {
        this.response = response;
        this.data = new String[0][0];
    }
    
    public String mapConfigAsJSON () {
        return new Gson().toJson(this);
    }
}
