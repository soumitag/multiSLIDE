/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.datahandling;

/**
 *
 * @author abhikdatta
 */
public class RequestParam {
    
    public static final int DATA_TYPE_STRING    = 0;
    public static final int DATA_TYPE_INT       = 1;
    public static final int DATA_TYPE_FLOAT     = 2;
    public static final int DATA_TYPE_DOUBLE    = 3;
    public static final int DATA_TYPE_BOOLEAN   = 4;
    public static final int DATA_TYPE_BYTE      = 5;
    
    public static final int PARAM_TYPE_REQUIRED = 0;
    public static final int PARAM_TYPE_OPTIONAL = 1;
    
    public static final int VALUE_TYPE_SINGLE   = 0;
    public static final int VALUE_TYPE_LIST     = 1;
    
    protected String name;
    protected String value;
    protected int data_type;
    protected int param_type;
    protected boolean hasValueConstraints;
    protected String[] valid_values;
    private Object parsed_value;
    protected boolean isProcessed;
    protected boolean isValueTypeList;
    protected String delimiter;
    protected String default_value;
    
    public RequestParam(String name, String value, int data_type, int param_type) {
        this.name = name;
        this.value = value;
        this.data_type = data_type;
        this.param_type = param_type;
        this.hasValueConstraints = false;
        this.parsed_value = null;
        this.isProcessed = false;
        this.isValueTypeList = false;
        this.delimiter = null;
        this.setDefaultDefaultValue();
    }
    
    public RequestParam(String name, String value, int data_type, int param_type, String[] valid_values) {
        this.name = name;
        this.value = value;
        this.data_type = data_type;
        this.param_type = param_type;
        this.valid_values = valid_values;
        this.hasValueConstraints = true;
        this.parsed_value = null;
        this.isProcessed = false;
        this.isValueTypeList = false;
        this.delimiter = null;
        this.setDefaultDefaultValue();
    }
    
    public RequestParam(String name, String value, int data_type, int param_type, String delimiter) {
        this.name = name;
        this.value = value;
        this.data_type = data_type;
        this.param_type = param_type;
        this.hasValueConstraints = false;
        this.parsed_value = null;
        this.isProcessed = false;
        this.isValueTypeList = true;
        this.delimiter = delimiter;
        this.setDefaultDefaultValue();
    }
    
    public RequestParam(String name, String value, int data_type, int param_type, String delimiter, String[] valid_values) {
        this.name = name;
        this.value = value;
        this.data_type = data_type;
        this.param_type = param_type;
        this.valid_values = valid_values;
        this.hasValueConstraints = true;
        this.parsed_value = null;
        this.isProcessed = false;
        this.isValueTypeList = true;
        this.delimiter = delimiter;
        this.setDefaultDefaultValue();
    }
    
    private void setDefaultDefaultValue() {
        switch (data_type) {
            case RequestParam.DATA_TYPE_STRING:
                this.default_value = "";
                break;
            case RequestParam.DATA_TYPE_INT:
                this.default_value = "0";
                break;
            case RequestParam.DATA_TYPE_FLOAT:
                this.default_value = "0.0";
                break;
            case RequestParam.DATA_TYPE_DOUBLE:
                this.default_value = "0.0";
                break;
            case RequestParam.DATA_TYPE_BOOLEAN:
                this.default_value = "false";
                break;
            case RequestParam.DATA_TYPE_BYTE:
                this.default_value = "0";
                break;
            default:
                break;
        }
    }
    
    public void setParsedValue(Object value) {
        this.parsed_value = value;
        this.isProcessed = true;
    }
    
    public Object get() {
        return this.parsed_value;
    }
    
    public boolean hasFailed() {
        return this.isProcessed && this.parsed_value == null;
    }
    
    public void setDefaultValue(String default_value) {
        this.default_value = default_value;
    }
}
