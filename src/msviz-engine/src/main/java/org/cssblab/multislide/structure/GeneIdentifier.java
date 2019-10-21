/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.io.Serializable;

/**
 *
 * @author soumitag
 */
public class GeneIdentifier implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String entrez;
    public String genesymbol;
    public String refseq;
    public String ensembl_gene;
    public String ensembl_transcript;
    public String ensembl_protein;
    public String uniprot;
    
    public static GeneIdentifier[] create(String[][] gene_data) {
        return null;
    }
    
}
