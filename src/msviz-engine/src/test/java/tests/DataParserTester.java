/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

/**
 *
 * @author abhikdatta
 */

import org.cssblab.multislide.datahandling.DataParser;
import org.cssblab.multislide.datahandling.RequestParam;
import org.cssblab.multislide.utils.Utils;

public class DataParserTester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        DataParser parser = new DataParser();
        parser.addParam("dataset_name",     "CNA",     RequestParam.DATA_TYPE_STRING,  RequestParam.PARAM_TYPE_REQUIRED);
        parser.addParam("nSamples",         "50",      RequestParam.DATA_TYPE_INT,     RequestParam.PARAM_TYPE_REQUIRED);
        parser.addParam("mapResolution",    "M",       RequestParam.DATA_TYPE_STRING,  RequestParam.PARAM_TYPE_REQUIRED, new String[]{"XS", "S", "M", "L", "XL"});
        parser.addParam("rowLabelWidth",    "86.6",    RequestParam.DATA_TYPE_DOUBLE,  RequestParam.PARAM_TYPE_REQUIRED);
        parser.addParam("optional_one",     null,      RequestParam.DATA_TYPE_DOUBLE,  RequestParam.PARAM_TYPE_OPTIONAL);
        
        parser.addListParam("list_test1",    "a1,b2,c3",    RequestParam.DATA_TYPE_STRING,  RequestParam.PARAM_TYPE_REQUIRED, ",");
        parser.addListParam("list_test2",    "11,22,33",    RequestParam.DATA_TYPE_DOUBLE,  RequestParam.PARAM_TYPE_REQUIRED, ",");
        parser.addListParam("empty_list_test",    "",    RequestParam.DATA_TYPE_DOUBLE,  RequestParam.PARAM_TYPE_OPTIONAL, ",");
        
        if (!parser.parse()) {
            Utils.log_info(parser.error_msg);
        } else {
            try {
                String dataset_name = parser.getString("dataset_name");
                int nSamples = parser.getInt("nSamples");
                String mapResolution = parser.getString("mapResolution");
                double rowLabelWidth = parser.getDouble("rowLabelWidth");

                double optional_one = parser.getDouble("optional_one");
                String[] list_test1 = parser.getStringArray("list_test1");
                double[] list_test2 = parser.getDoubleArray("list_test2");
                double[] empty_list = parser.getDoubleArray("empty_list_test");
                Utils.log_info(empty_list.toString());
                Utils.log_info("Positive Tests Passed.");
            } catch (Exception e) {
                Utils.log_exception(e, "");
            }
        }
        
        parser.addListParam("list_test3",    "11,22,dd",    RequestParam.DATA_TYPE_DOUBLE,  RequestParam.PARAM_TYPE_REQUIRED, ",");
        if (!parser.parse()) {
            Utils.log_info(parser.error_msg);
            Utils.log_info("Negative Test Passed.");
        } else {
            Utils.log_info("Negative Test Failed.");
        }
    }
    
}
