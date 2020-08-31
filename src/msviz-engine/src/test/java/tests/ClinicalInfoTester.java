/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.io.File;
import org.apache.spark.sql.SparkSession;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.structure.data.ClinicalInformation;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author abhikdatta
 */
public class ClinicalInfoTester {

    public static void main(String[] args) {
        
        try {
            
            String install_path = "/Users/soumitaghosh/Documents/GitHub/multi-slide";
            //String install_path = "D:\\ad\\sg\\code_multislide_03_Nov_2018\\multiSLIDE";
            ColorPalette categorical_palette = new ColorPalette(install_path + File.separator + "db" + File.separator + "categorical_color_palette.txt");
            ColorPalette continuous_palette = new ColorPalette(install_path + File.separator + "db" + File.separator + "continuous_color_palette.txt");
        
            SparkSession session = SparkSession.builder().appName("MultiSLIDE").config("key", "value").master("local").getOrCreate();
            ClinicalInformation clinical_info = new ClinicalInformation(categorical_palette, continuous_palette);
            long nSamples = clinical_info.loadClinicalInformation(
                    session, 
                    "/Users/soumitaghosh/Documents/GitHub/multi-slide/temp/95712d40f42dbdee3559156ee918/demo_2021158607524066_47882.93738928961/clinical-info", "\t");
            Utils.log_info(nSamples+"");
            
        } catch (Exception e) {
            Utils.log_exception(e, "");
        }
    }
    
}
