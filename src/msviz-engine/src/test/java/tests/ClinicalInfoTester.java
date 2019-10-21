/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.io.File;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.structure.ClinicalInformation;

/**
 *
 * @author abhikdatta
 */
public class ClinicalInfoTester {

    public static void main(String[] args) {
        
        try {
            
            String install_path = "D:\\ad\\sg\\code_multislide_03_Nov_2018\\multiSLIDE";
            ColorPalette categorical_palette = new ColorPalette(install_path + File.separator + "db" + File.separator + "categorical_color_palette.txt");
            ColorPalette continuous_palette = new ColorPalette(install_path + File.separator + "db" + File.separator + "continuous_color_palette.txt");
        
            ClinicalInformation clinical_info = new ClinicalInformation(categorical_palette, continuous_palette);
            int nSamples = clinical_info.loadClinicalInformation(install_path + File.separator + "demo_data" + File.separator + "clinical_info_blca.txt", "\t");
            System.out.println(nSamples);
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
}
