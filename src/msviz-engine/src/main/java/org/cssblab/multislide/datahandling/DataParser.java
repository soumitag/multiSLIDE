/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.datahandling;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author abhikdatta
 */

public class DataParser implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String error_msg;
    public boolean status_is_valid;
    public Object value;
    
    public HashMap <String, RequestParam> params;
    public HttpServletRequest request;
    
    public DataParser() { 
        params =  new HashMap <String, RequestParam> ();
    }
    
    public DataParser(HttpServletRequest request) { 
        params =  new HashMap <String, RequestParam> ();
        this.request = request;
    }
    
    private boolean parseParam(String param_name, String value, int data_type, int param_type, boolean hasValueConstraints, String[] valid_values, String default_value) {
        
        this.error_msg = "";
        this.status_is_valid = true;
        this.value = null;
        
        if (param_type == RequestParam.PARAM_TYPE_REQUIRED) {
            if (this.isNullOrEmpty(value)) {
                this.error_msg = "Missing or empty value for required param: " + param_name + ".";
                this.status_is_valid = false;
                return false;
            } else {
                return this.parseString(param_name, value, data_type, hasValueConstraints, valid_values);
            }
        } else {
            if (this.isNullOrEmpty(value)) {
                switch (data_type) {
                    case RequestParam.DATA_TYPE_STRING:
                        this.value = default_value;
                        break;
                    case RequestParam.DATA_TYPE_INT:
                        this.value = Integer.parseInt(default_value);
                        break;
                    case RequestParam.DATA_TYPE_FLOAT:
                        this.value = Float.parseFloat(default_value);
                        break;
                    case RequestParam.DATA_TYPE_DOUBLE:
                        this.value = Double.parseDouble(default_value);
                        break;
                    case RequestParam.DATA_TYPE_BOOLEAN:
                        this.value = Boolean.parseBoolean(default_value);
                        break;
                    case RequestParam.DATA_TYPE_BYTE:
                        this.value = Byte.parseByte(default_value);
                        break;
                    default:
                        break;
                }
                this.status_is_valid = true;
                return true;
            } else {
                return this.parseString(param_name, value, data_type, hasValueConstraints, valid_values);
            }
        }
    }
    
    private boolean parseString(String param_name, String value, int data_type, boolean hasValueConstraints, String[] valid_values) {
        value = value.trim();
        if (hasValueConstraints && !this.isValueValid(value, valid_values)) {
            this.error_msg = "Invalid value '" + value + "' for " + param_name + ": " + param_name + ".";
            this.status_is_valid = false;
            return false;
        }
        if (data_type == RequestParam.DATA_TYPE_STRING) {
            this.value = value;
            this.status_is_valid = true;
            return true;
        } else {
            try {
                this.value = this.parseNumeric(value, data_type);
                this.status_is_valid = true;
                return true;
            } catch (NumberFormatException e) {
                this.error_msg = "Unable to handle non-numeric value for " + param_name + ": " + value + ".";
                this.status_is_valid = false;
                return false;
            }
        }
    }
    
    private Object parseNumeric(String in, int data_type) {
        switch (data_type) {
            case RequestParam.DATA_TYPE_INT:
                return Integer.parseInt(in);
            case RequestParam.DATA_TYPE_FLOAT:
                return Float.parseFloat(in);
            case RequestParam.DATA_TYPE_DOUBLE:
                return Double.parseDouble(in);
            case RequestParam.DATA_TYPE_BOOLEAN:
                return Boolean.parseBoolean(in);
            case RequestParam.DATA_TYPE_BYTE:
                return Byte.parseByte(in);
            default:
                throw new NumberFormatException(); 
        }
    }
    
    private boolean isNullOrEmpty(String in) {
        return (in == null || in.equals(""));
    }
    
    private boolean isValueValid(String in, String[] valid_values) {
        for (Object valid_value : valid_values) {
            if (in.equals(valid_value)) {
                return true;
            }
        }
        return false;
    }
    
    /*
    The next four addParam methods are for the empty constructor
    */
    public void addParam(String name, String value, int data_type, int param_type) {
        RequestParam param = new RequestParam(name, value, data_type, param_type);
        this.params.put(name, param);
    }
    
    public void addParam(String name, String value, int data_type, int param_type, String[] valid_values) {
        RequestParam param = new RequestParam(name, value, data_type, param_type, valid_values);
        this.params.put(name, param);
    }
    
    public void addListParam(String name, String value, int data_type, int param_type, String delimiter) {
        RequestParam param = new RequestParam(name, value, data_type, param_type, delimiter);
        this.params.put(name, param);
    }
    
    public void addListParam(String name, String value, int data_type, int param_type, String delimiter, String[] valid_values) {
        RequestParam param = new RequestParam(name, value, data_type, param_type, delimiter, valid_values);
        this.params.put(name, param);
    }
    
    /*
    The next four addParam methods are for the parameterised constructor
    */
    public void addParam(String name, int data_type, int param_type) {
        RequestParam param = new RequestParam(name, request.getParameter(name), data_type, param_type);
        this.params.put(name, param);
    }
    
    public void addParam(String name, int data_type, int param_type, String[] valid_values) {
        RequestParam param = new RequestParam(name, request.getParameter(name), data_type, param_type, valid_values);
        this.params.put(name, param);
    }
    
    public void addListParam(String name, int data_type, int param_type, String delimiter) {
        RequestParam param = new RequestParam(name, request.getParameter(name), data_type, param_type, delimiter);
        this.params.put(name, param);
    }
    
    public void addListParam(String name, int data_type, int param_type, String delimiter, String[] valid_values) {
        RequestParam param = new RequestParam(name, request.getParameter(name), data_type, param_type, delimiter, valid_values);
        this.params.put(name, param);
    }
    
    public void setDefaultValueForParam(String name, String value) {
        this.params.get(name).setDefaultValue(value);
    }
    
    public boolean parse() {
        for (Map.Entry pair : this.params.entrySet()) {
            RequestParam param = (RequestParam)pair.getValue();
            if (!param.isProcessed) {
                if (param.isValueTypeList) {
                    if (this.isNullOrEmpty(param.value)) {
                        if (param.param_type == RequestParam.PARAM_TYPE_REQUIRED) {
                            this.error_msg = "Missing or empty value for required param: " + param.name + ".";
                            this.status_is_valid = false;
                            return false;
                        } else {
                            //param.setParsedValue(param.default_value);
                            param.setParsedValue(new Object[]{});
                        }
                    } else {
                        String[] values = param.value.split(param.delimiter,-1);
                        Object[] parsed_values = new Object[values.length];
                        for (int i=0; i<values.length; i++) {
                            this.parseParam(param.name, values[i], param.data_type, param.param_type, param.hasValueConstraints, param.valid_values, param.default_value);
                            if (!this.status_is_valid) {
                                return false;
                            }
                            parsed_values[i] = (Object)this.value;
                        }
                        param.setParsedValue(parsed_values);
                    }
                } else {                
                    this.parseParam(param.name, param.value, param.data_type, param.param_type, param.hasValueConstraints, param.valid_values, param.default_value);
                    if (!this.status_is_valid) {
                        return false;
                    }
                    param.setParsedValue(this.value);
                }
            }
        }
        return true;
    }
    
    public String getString(String name) throws DataParsingException {
        if (this.params.get(name).data_type != RequestParam.DATA_TYPE_STRING) {
            throw new DataParsingException();
        }
        return (String)this.params.get(name).get();
    }
    
    public String[] getStringArray(String name) throws DataParsingException {
        if (this.params.get(name).data_type != RequestParam.DATA_TYPE_STRING || !this.params.get(name).isValueTypeList) {
            throw new DataParsingException();
        }
        Object[] v = (Object[])this.params.get(name).get();
        String[] r = new String[v.length];
        for (int i=0; i<v.length; i++) {
            r[i] = (String)v[i];
        }
        return r;
    }
    
    public int getInt(String name) throws DataParsingException {
        if (this.params.get(name).data_type != RequestParam.DATA_TYPE_INT) {
            throw new DataParsingException();
        }
        return (int)this.params.get(name).get();
    }
    
    public int[] getIntArray(String name) throws DataParsingException {
        if (this.params.get(name).data_type != RequestParam.DATA_TYPE_INT || !this.params.get(name).isValueTypeList) {
            throw new DataParsingException();
        }
        Object[] v = (Object[])this.params.get(name).get();
        int[] r = new int[v.length];
        for (int i=0; i<v.length; i++) {
            r[i] = (int)v[i];
        }
        return r;
    }
    
    public float getFloat(String name) throws DataParsingException {
        if (this.params.get(name).data_type != RequestParam.DATA_TYPE_FLOAT) {
            throw new DataParsingException();
        }
        return (float)this.params.get(name).get();
    }
    
    public float[] getFloatArray(String name) throws DataParsingException {
        if (this.params.get(name).data_type != RequestParam.DATA_TYPE_FLOAT || !this.params.get(name).isValueTypeList) {
            throw new DataParsingException();
        }
        Object[] v = (Object[])this.params.get(name).get();
        float[] r = new float[v.length];
        for (int i=0; i<v.length; i++) {
            r[i] = (float)v[i];
        }
        return r;
    }
    
    public double getDouble(String name) throws DataParsingException {
        if (this.params.get(name).data_type != RequestParam.DATA_TYPE_DOUBLE) {
            throw new DataParsingException();
        }
        return (double)this.params.get(name).get();
    }
    
    public double[] getDoubleArray(String name) throws DataParsingException {
        if (this.params.get(name).data_type != RequestParam.DATA_TYPE_DOUBLE || !this.params.get(name).isValueTypeList) {
            throw new DataParsingException();
        }
        Object[] v = (Object[])this.params.get(name).get();
        double[] r = new double[v.length];
        for (int i=0; i<v.length; i++) {
            r[i] = (double)v[i];
        }
        return r;
    }
    
    public boolean getBool(String name) throws DataParsingException {
        if (this.params.get(name).data_type != RequestParam.DATA_TYPE_BOOLEAN) {
            throw new DataParsingException();
        }
        return (boolean)this.params.get(name).get();
    }
    
    public boolean[] getBoolArray(String name) throws DataParsingException {
        if (this.params.get(name).data_type != RequestParam.DATA_TYPE_BOOLEAN || !this.params.get(name).isValueTypeList) {
            throw new DataParsingException();
        }
        Object[] v = (Object[])this.params.get(name).get();
        boolean[] r = new boolean[v.length];
        for (int i=0; i<v.length; i++) {
            r[i] = (boolean)v[i];
        }
        return r;
    }
    
    public byte getByte(String name) throws DataParsingException {
        if (this.params.get(name).data_type != RequestParam.DATA_TYPE_BYTE) {
            throw new DataParsingException();
        }
        return (byte)this.params.get(name).get();
    }
    
    public byte[] getByteArray(String name) throws DataParsingException {
        if (this.params.get(name).data_type != RequestParam.DATA_TYPE_BYTE || !this.params.get(name).isValueTypeList) {
            throw new DataParsingException();
        }
        Object[] v = (Object[])this.params.get(name).get();
        byte[] r = new byte[v.length];
        for (int i=0; i<v.length; i++) {
            r[i] = (byte)v[i];
        }
        return r;
    }

}
