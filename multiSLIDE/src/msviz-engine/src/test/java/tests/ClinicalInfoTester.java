/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import org.cssblab.multislide.structure.ClinicalInformation;

/**
 *
 * @author abhikdatta
 */
public class ClinicalInfoTester {

    public static void main(String[] args) {
        
        try {
            ClinicalInformation clinical_info = new ClinicalInformation();
            int nSamples = clinical_info.loadClinicalInformation("/Users/abhikdatta/code/code_multislide_03_Nov_2018/multiSLIDE/demo_data/clinical_info_blca.txt", "\t");
            System.out.println(nSamples);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
}
