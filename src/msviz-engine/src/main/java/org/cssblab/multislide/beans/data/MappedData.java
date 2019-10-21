package org.cssblab.multislide.beans.data;

import com.google.gson.Gson;
import java.util.ArrayList;

/**
 *
 * @author soumitag
 */
public class MappedData {
    
    private int status;
    private String message;
    private String detailed_reason;
    private ArrayList <String> names;
    private ArrayList <String> values;
    
    public MappedData(int status, String message, String detailed_reason) {
        this.status = status;
        this.message = message;
        this.detailed_reason = detailed_reason;
        this.names = new ArrayList <String> ();
        this.values = new ArrayList <String> ();
    }
    
    public void addNameValuePair(String name, String value) {
        this.names.add(name);
        this.values.add(value);
    }
    
    public String asJSON () {
        return new Gson().toJson(this);
    }
}
