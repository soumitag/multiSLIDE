/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.utils;

import org.cssblab.multislide.beans.data.DatasetSpecs;

/**
 *
 * @author soumitag
 */
public class FormElementMapper {
    
    public static String parseDelimiter(String value) {
        
        value = value.trim().toLowerCase();
        
        if (value.equalsIgnoreCase("tab")) {
            return "\t";
        } else if (value.equalsIgnoreCase("comma")) {
            return ",";
        } else if (value.equalsIgnoreCase("space")) {
            return " ";
        } else if (value.equalsIgnoreCase("pipe")) {
            return "|";
        } else if (value.equalsIgnoreCase("semi-colon")) {
            return ";";
        } else if (value.equalsIgnoreCase("colon")) {
            return ":";
        } else if (value.equalsIgnoreCase("line")) {
            return "\n";
        } else {
            return value;
        }
    }
    
    public static String parseDataUploadType(String value) {
        
        int i = DatasetSpecs.OMICS_TYPE_DIPSLAY_NAMES.indexOf(value);
        if (i == -1)
            return value;
        else
            return DatasetSpecs.OMICS_TYPES.get(i);
        
        /*
        value = value.trim().toLowerCase();
        if (value.equalsIgnoreCase("copy number variation")) {
            return "cnv";
        } else if (value.equalsIgnoreCase("dna methylation")) {
            return "dna_meth";
        } else if (value.equalsIgnoreCase("gene expression (mrna)")) {
            return "m_rna";
        } else if (value.equalsIgnoreCase("microrna expression (mirna)")) {
            return "mi_rna";
        } else if (value.equalsIgnoreCase("protein")) {
            return "protein";
        } else if (value.equalsIgnoreCase("phosphoproteome")) {
            return "phosphoproteome";
        } else if (value.equalsIgnoreCase("gene isoform expression")) {
            return "gene_isoform_expression";
        } else if (value.equalsIgnoreCase("metabolome")) {
            return "metabolome";
        } else if (value.equalsIgnoreCase("lipidome")) {
            return "lipidome";
        } else {
            return value;
        }
        */
    }
    
    public static String parseRowIdentifierType(String value) {
        
        value = value.trim().toLowerCase();
        
        if (value.equalsIgnoreCase("entrez")) {
            return "entrez";
        } else if (value.equalsIgnoreCase("gene symbol")) {
            return "gene_symbol";
        } else if (value.equalsIgnoreCase("refseq id")) {
            return "refseq_id";
        } else if (value.equalsIgnoreCase("ensembl gene id")) {
            return "ensembl_gene_id";
        } else if (value.equalsIgnoreCase("ensembl transcript id")) {
            return "ensembl_transcript_id";
        } else if (value.equalsIgnoreCase("ensembl protein id")) {
            return "ensembl_protein_id";
        } else if (value.equalsIgnoreCase("uniprot id")) {
            return "uniprot_id";
        } else {
            return value;
        }
    }
    
    public static String parseFormData(String name, String value) {
        
        name = name.trim().toLowerCase();
        
        if (name.equalsIgnoreCase("delimiter")) {

            value = value.trim().toLowerCase();
            return FormElementMapper.parseDelimiter(value);
            
        } else if (value.equalsIgnoreCase("upload_type")) {
            
            value = value.trim().toLowerCase();
            return FormElementMapper.parseDataUploadType(value);
            
        } else if (value.equalsIgnoreCase("identifier_type")) {
            
            value = value.trim().toLowerCase();
            return FormElementMapper.parseRowIdentifierType(value);
            
        } else {
            return value;
        }
        
    }
}
