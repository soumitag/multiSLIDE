package org.cssblab.multislide.graphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class ColorPalette implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String db_path;
    public ArrayList <short[]> palette;
    
    public ColorPalette(String db_path) {
        this.db_path = db_path;
        this.parseColorMap();
    }
    
    public final void parseColorMap(){
        palette = new ArrayList <short[]> ();
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.db_path));
            String line;
            while((line = br.readLine())!= null){
                String[] color_str = line.split(",", -1);
                short[] color = new short[3];
                for (int i=0; i<3; i++) {
                    color[i] = Short.parseShort(color_str[i]);
                }
                palette.add(color);
            }
        } catch (Exception e) {
            Utils.log_exception(e, "");
        }
    }
    
}
