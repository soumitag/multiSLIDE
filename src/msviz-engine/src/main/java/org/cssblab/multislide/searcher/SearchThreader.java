package org.cssblab.multislide.searcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class SearchThreader implements Callable, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public byte cursor_type;
    public String queryString;
    public String search_type;
    public String keyword;
    public Searcher searcher;
    
    public SearchThreader(byte cursor_type, String queryString, String search_type, String keyword, Searcher searcher){
        this.cursor_type = cursor_type;
        this.queryString = queryString;
        this.search_type = search_type;
        this.keyword = keyword;
        this.searcher = searcher;
    }
    
    @Override
    public ArrayList <SearchResultObject> call() {
        Utils.log_info("Inside : " + Thread.currentThread().getName());
        return this.searcher.processAllDB(this.cursor_type, this.queryString, this.search_type, this.keyword);
    }
    
}
