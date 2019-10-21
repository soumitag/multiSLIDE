/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import com.google.gson.Gson;
import java.io.Serializable;

/**
 *
 * @author abhikdatta
 */
public class MapConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String[] custom_gene_identifiers;
    public double data_min;
    public double data_max;

    public String columnLabel;
    public String binningRange;
    public int nColors;
    public String colorScheme;
    public double binningRangeStart;
    public double binningRangeEnd;
    
    public MapConfig() {
        this.custom_gene_identifiers = new String[0];
        
    }
    
    public String[] getCustomGeneIdentifiers() {
        return custom_gene_identifiers;
    }

    public void setCustomGeneIdentifiers(String[] custom_gene_identifiers) {
        this.custom_gene_identifiers = custom_gene_identifiers;
    }

    public double getDataMin() {
        return data_min;
    }

    public void setDataMin(double data_min) {
        this.data_min = Math.round(data_min * 100.0)/100.0;
    }

    public double getDataMax() {
        return data_max;
    }

    public void setDataMax(double data_max) {
        this.data_max = Math.round(data_max * 100.0)/100.0;
    }
    
    public String getColumnLabel() {
        return columnLabel;
    }

    public void setColumnLabel(String columnLabel) {
        this.columnLabel = columnLabel;
    }

    public String getBinningRange() {
        return binningRange;
    }

    public void setBinningRange(String binningRange) {
        this.binningRange = binningRange;
    }
    
    public int getnColors() {
        return nColors;
    }

    public void setnColors(int nColors) {
        this.nColors = nColors;
    }
    
    public String getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(String colorScheme) {
        this.colorScheme = colorScheme;
    }
    
    public double getBinningRangeStart() {
        return binningRangeStart;
    }

    public void setBinningRangeStart(double binningRangeStart) {
        this.binningRangeStart = binningRangeStart;
    }

    public double getBinningRangeEnd() {
        return binningRangeEnd;
    }

    public void setBinningRangeEnd(double binningRangeEnd) {
        this.binningRangeEnd = binningRangeEnd;
    }
    
    public String mapConfigAsJSON () {
        return new Gson().toJson(this);
    }
}
