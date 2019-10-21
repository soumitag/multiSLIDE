/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.io.BufferedReader;
import java.io.FileReader;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.structure.GeneCoordinate;
import org.cssblab.multislide.utils.MongoDBConnect;

/**
 *
 * @author Soumita
 */
public class GTFTestStub {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws DataParsingException {
        // TODO code application logic here
        /*
        String gtf_file = "E:\\code_multislide\\GTF\\genes_gencode.vM17.chr_patch_hapl_scaff.annotation.gtf";
        GeneCoordinate.createGeneCoordinatesFromFile(gtf_file);       
        
        String phenotype_file = "E:\\code_multislide\\GTF\\";
        Phenotypes.createClinicalDataFromFile(phenotype_file);

        */
        
        //Colorectal cancer       
        
        
        String data_filenames[] = new String[3];
        data_filenames[0] = "E:\\From_hiromi\\CNV_imputed_edited.txt";
        data_filenames[1] = "E:\\From_hiromi\\mRNA_imputed_edited.txt";
        data_filenames[2] = "E:\\From_hiromi\\PROT_imputed_edited.txt";
        
        String omics_type[] = new String[3];
        omics_type[0] = "cnv";
        omics_type[1] = "mrna";
        omics_type[2] = "prot";
        
        String identifier_type[] = new String[3];
        identifier_type[0] = "genesymbol";
        identifier_type[1] = "genesymbol";
        identifier_type[2] = "genesymbol";
        
        String clinical_info = "E:\\From_hiromi\\PatientsInfo_CRC.txt";
        
        //Data_1 data = new Data_1 (omics_type, data_filenames, identifier_type, clinical_info, "\t");
        
        /*
        String path = "E:\\code_multislide\\miRNA_network\\miRNA_Entrez\\HS_RNA_micro.txt";
        MongoDBConnect.cleanMicroRNAHS(path);
        */
        
        /*
        String path = "E:\\code_multislide\\miRNA_network\\miRNA_Entrez\\MM_ENTREZ_RNA_MICRO_BIG.txt";
        MongoDBConnect.cleanMicroRNAMM(path);
        */
        
    }
    
}
