/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.cssblab.multislide.utils.Utils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.data.table.Table;

/**
 *
 * @author Soumita Ghosh and Abhik Datta
 */

class Index implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public Long _index;
    public double _significance;
    public double _fdr;
    public String _linker;
    
    /*
        constructor for a primary index
    */
    public Index(Long index, double significance, double fdr, String linker) {
        this._index = index;
        this._significance = significance;
        this._fdr = fdr;
        this._linker = linker;
    }

}

public class FeatureIndex implements Serializable, Iterable<Long> {
    
    private static final long serialVersionUID = 1L;
    
    /*
        df must at least have the two columns: _id, _index
        additonally it can have: _significance and _fdr columns
    */
    private final HashMap <Long, Index> manifest;
    private boolean has_significance;
    private boolean has_fdr;
    
    public FeatureIndex(Table table) throws MultiSlideException {
        
        /*
            Creates a feature index with default indices: _index=1..N
            Input df is primarily Selection.consolidated_linker._id, but in general can
            be any dataset with an "_id" column
        */
        
        this.manifest = new HashMap <> ();
        
        long i = 0;
        for (String row_id: table) {
            long _id = table.getLong(row_id, "_id");
            String _linker = table.getString(row_id, "_linker");
            manifest.put(_id, new Index((long)i, 1.0, 1.0, _linker));
        }

        this.has_significance = false;
        this.has_fdr = false;
    }
    
    public FeatureIndex(List <Row> data) {
        /*
            Creates a feature index with default indices: _index=1..N
            Input df is primarily Selection.consolidated_linker._id, but in general can
            be any dataset with an "_id" column
        */
        
        this.manifest = new HashMap <> ();
        
        int _id_col, _linker_col;
        if (data.size() > 0) {
            _id_col = data.get(0).fieldIndex("_id");
            _linker_col = data.get(0).fieldIndex("_linker");
        } else {
            return;
        }

        for (int i=0; i<data.size(); i++) {
            Long _id = data.get(i).getLong(_id_col);
            String _linker = data.get(i).getString(_linker_col);
            manifest.put(_id, new Index((long)i, 1.0, 1.0, _linker));
        }

        this.has_significance = false;
        this.has_fdr = false;

    }
    
    private FeatureIndex() {
        this.manifest = new HashMap <> ();
    }
    
    /*
        resets the _index column
        input df must have at least _id and _index columns
        assumes _ids in input df and this.df are identical
        if an _id is present in input df that is not present in manifest, will cause and exception
        if set_missing_indices= False and if _index is not set for an _id in manifest, 
        it will default to will cause an exception in align()
        no check is performed for this, at this stage, all _index are assumed to be set
    
        assumes indices in _index to be positive, unique and contiguous
        no checks are performed for this, at this stage but
        any deviations from this will cause errors downstream
    */
    protected void setOrdering(
            Table table, boolean set_missing_indices, boolean df_is_self, Table within_linker_ordering) throws MultiSlideException {
        
        /*
            make a hashmap for internal ordering within linkers
            linker -> ordered list of _ids
        */
        HashMap <String, List<Long>> within_linker_ordered_ids = new HashMap <> ();
        if (within_linker_ordering != null) {
            for (String id: within_linker_ordering) {

                String linker = within_linker_ordering.getString(id, "_linker");
                Long _id = Long.parseLong(id);

                if (within_linker_ordered_ids.containsKey(linker)) {
                    within_linker_ordered_ids.get(linker).add(_id);
                } else {
                    List <Long> _ids = new ArrayList <> ();
                    _ids.add(_id);
                    within_linker_ordered_ids.put(linker, _ids);
                }
            }
        }
        /*
            First find if significance and fdr are present
        */
        this.has_significance = table.containsColumn("_significance");
        this.has_fdr = table.containsColumn("_fdr");
        
        /*
            Before updating, set all indices to -1
        */
        for (Long _id: manifest.keySet()) {
            manifest.get(_id)._index = (long)-1;
        }
        
        /*
            input df can have duplicate linkers, so create a list of unique 
            linker values and sequential indices. The first instance of 
            a linker is used to determine the index
        */
        HashMap <String, Long> linker_index_map = new HashMap <> ();
        long _seq_index = 0;
        for (String row_id: table) {
            String _linker = table.getString(row_id, "_linker");
            if(!linker_index_map.containsKey(_linker)) {
                linker_index_map.put(_linker, _seq_index++);
            }
        }
        String[] ordered_linkers = new String[linker_index_map.size()];
        for (String linker: linker_index_map.keySet())
            ordered_linkers[Math.toIntExact(linker_index_map.get(linker))] = linker;
        
        /*
            feature index also can have duplicate linkers, these should appear
            contiguously, although the order within each set of duplicate linkers
            can be arbitrary
        */
        
        /*
            create a HashMap: linker -> list(_ids) for fast lookup
        */
        HashMap <String, List<Long>> linker_id_map = new HashMap <> ();
        for (Long _id: manifest.keySet()) {
            String _linker = manifest.get(_id)._linker;
            if (linker_id_map.containsKey(_linker)) {
                linker_id_map.get(_linker).add(_id);
            } else {
                List<Long> a = new ArrayList<>();
                a.add(_id);
                linker_id_map.put(_linker, a);
            }
        }
        
        /*
            Sort _ids based on ordered_linkers
        */
        _seq_index = 0;
        for (String linker: ordered_linkers) {
            List<Long> _ids = linker_id_map.get(linker);
            /*
                _ids can be null, as not all linkers in df may be present in
                manifest
            */
            if (_ids != null) {
                List <Long> ordered_ids;
                if (within_linker_ordered_ids.containsKey(linker)) {
                    ordered_ids = within_linker_ordered_ids.get(linker);
                } else {
                    ordered_ids = linker_id_map.get(linker);
                }
                for (Long _id: ordered_ids)
                    manifest.get(_id)._index = _seq_index++;
            }
        }
        
        /*
            Set missing indices
        */
        if (set_missing_indices) {
            for (Long _id: manifest.keySet()) {
                Index index = manifest.get(_id);
                if (index._index == -1) {
                    index._index = _seq_index++;
                }
            }
        }
        
        /*
            Before setting fdr and significance, reset them
        */
        for (Long _id : manifest.keySet()) {
            Index index = manifest.get(_id);
            index._significance = 1.0;
            index._fdr = 1.0;
        }
        
        /*
            _linkers or _ids (in case of self) may ne missing from table if 
            1. in case analysis was run on a dfferent datset, not this dataset
            2. in case gene filtering is on
            therefore for both independent nd linked cases create a hashmap first
        */
        if (df_is_self) {
            
            /*
                if setting from self, get _significance and _fdr based on _id
                else get them based on linker
            */
            
            HashMap<Long, Double> linker_significance = new HashMap<>();
            HashMap<Long, Double> linker_fdr = new HashMap<>();
            
            for (String row_id : table) {
                if (has_significance) {
                    linker_significance.put(table.getLong(row_id + "", "_id"), table.getDouble(row_id + "", "_significance"));
                }
                if (has_fdr) {
                    linker_fdr.put(table.getLong(row_id + "", "_id"), table.getDouble(row_id + "", "_fdr"));
                }
            }
            
            for (Long _id: manifest.keySet()) {
                Index index = manifest.get(_id);
                if (linker_significance.containsKey(_id))
                    index._significance = linker_significance.get(_id);
                else
                    index._significance = 1.0;
                
                if (linker_fdr.containsKey(_id))
                    index._fdr = linker_fdr.get(_id);
                else
                    index._fdr = 1.0;
            }
            
        } else {
            
            HashMap<String, Double> linker_significance = new HashMap<>();
            HashMap<String, Double> linker_fdr = new HashMap<>();
            
            for (String row_id : table) {
                if (has_significance) {
                    linker_significance.put(table.getString(row_id + "", "_linker"), table.getDouble(row_id + "", "_significance"));
                }
                if (has_fdr) {
                    linker_fdr.put(table.getString(row_id + "", "_linker"), table.getDouble(row_id + "", "_fdr"));
                }
            }
            
            for (Long _id: manifest.keySet()) {
                Index index = manifest.get(_id);
                
                if (linker_significance.containsKey(index._linker))
                    index._significance = linker_significance.get(index._linker);
                else
                    index._significance = 1.0;
                    
                if (linker_fdr.containsKey(index._linker))
                    index._fdr = linker_fdr.get(index._linker);
                else
                    index._fdr = 1.0;
            }
        }

    }
    
    /*
    Gets
    */
    public int count() {
        return this.manifest.size();
    }
    
    @Override
    public Iterator <Long> iterator() {
        return manifest.keySet().iterator();
    }
    
    public Long indexOf(Long _id) {
        if (manifest.containsKey(_id))
            return manifest.get(_id)._index;
        else
            return null;
    }
    
    public double significanceOf(Long _id) {
        if (manifest.containsKey(_id))
            return manifest.get(_id)._significance;
        else
            return 1.0;
    }
    
    public double fdrOf(Long _id) {
        if (manifest.containsKey(_id))
            return manifest.get(_id)._fdr;
        else
            return 1.0;
    }
    
    public List<Long> getIDs() {
        return new ArrayList<>(manifest.keySet());
    }
    
    /*
    Utilities
    */
    
    /*
        align when there is only primary indices
    */
    public List <Row> align(List <Row> d) {
        /*
            returns a reordered version of d based on manifest._index
            d must contain an _id column
            the returned list will have the same length as manifest and contain only
            those rows whose _ids are present in manifest
            if a manifest._id is missing in d, a null row will be inserted
        */
        
        List <Row> aligned_list = new ArrayList <> ();
        HashMap <Long, Row> t = new HashMap <> ();
        
        if (d.size() > 0) {
            int _id_col = d.get(0).fieldIndex("_id");
            /*
                First create a HashMap for quick retrieval on d
            */
            for (Row row: d)
                t.put(row.getLong(_id_col), row);
        }
    
        /*
            Create ordered list
        */
        Row[] aligned = new Row[this.count()];
        for (Long _id: manifest.keySet()) {
            int _index = Math.toIntExact(this.manifest.get(_id)._index);
            if (t.containsKey(_id))
                aligned[_index] = t.get(_id);
        }
        
        for (Row r: aligned)
            aligned_list.add(r);
        
        return aligned_list;

    }
    
    public FeatureIndex filter(double significance_threshold, double fdr_threshold) {
        
        FeatureIndex feature_index = new FeatureIndex ();
        
        /*
        First sort based on _index
        */
        Long[] ids =  new Long[this.count()];
        Index[] rows = new Index[this.count()];
        for (Long _id: manifest.keySet()) {
            int _index = Math.toIntExact(this.manifest.get(_id)._index);
            rows[_index] = manifest.get(_id);
            ids[_index] = _id;
        }
        
        /*
        Create filtered feature_index with new ordered indices
        */
        long filtered_count = 0;
        boolean significance_filter, fdr_filter;
        for (int i=0; i<rows.length; i++) {
            
            Index _i = rows[i];
            
            if (_i != null) {
                significance_filter = !(this.has_significance && _i._significance > significance_threshold);
                fdr_filter = !(this.has_fdr && _i._fdr == 0.0);

                if (significance_filter && fdr_filter) {
                    feature_index.manifest.put(ids[i], new Index(filtered_count++, _i._significance, _i._fdr, _i._linker));
                }
            }
        }
        
        /*
        Utils.log_info("filter: feature_index");
        this.show();
        
        Utils.log_info("filtered_feature_index");
        feature_index.show();
        */
        
        return feature_index;
    }
    
    public List <Long> get_internal_ordering (Table dists, List<Long> _ids) {
        return null;
    }
    
    public void show() {
        for (Long _id: manifest.keySet()) {
            Index _i = manifest.get(_id);
            Utils.log_info(_id + "\t" + _i._linker + "\t" + _i._index + "\t" + _i._significance + "\t" + _i._fdr);
        }
    }
    
    public Map <String, List<Long>> getLinkerIdMap() {
        
        Map <String, List<Long>> linker_id_map = new CaseInsensitiveMap <> ();
        for (Long _id: manifest.keySet()) {
            String _linker = manifest.get(_id)._linker;
            if (linker_id_map.containsKey(_linker)) {
                linker_id_map.get(_linker).add(_id);
            } else {
                List<Long> a = new ArrayList<>();
                a.add(_id);
                linker_id_map.put(_linker, a);
            }
        }
        
        return linker_id_map;
    }
    
}
