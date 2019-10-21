package org.cssblab.multislide.searcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.cssblab.multislide.structure.MetaData;
import org.cssblab.multislide.structure.MultiSlideException;

/**
 *
 * @author Soumita
 */

/*
A static wrapper class for Searcher.java
*/

public final class SearchHandler implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private SearchHandler() {}

    public static HashMap <String, ArrayList <SearchResultObject>> processSearchQuery (
            String queryStr, Searcher searcher, MetaData metadata, boolean has_miRNA_data
    ) throws MultiSlideException {
        
        if(queryStr.contains(";")) {
            String[] outer_list = queryStr.split(";", -1);
            HashMap <String, ArrayList <SearchResultObject>> search_results_map = new HashMap <String, ArrayList <SearchResultObject>> ();
            for(int i = 0; i < outer_list.length; i++){
                String query = outer_list[i].trim().toLowerCase();
                ArrayList <SearchResultObject> current_search_results = processQuery(query, searcher, metadata, has_miRNA_data);
                if (SearchHandler.queryHasKeyword(query)) {
                    search_results_map.put(query, current_search_results);
                } else {
                    HashMap <String, ArrayList <SearchResultObject>> temp = makeSearchMap(current_search_results);
                    search_results_map.putAll(temp);
                }
            }
            return search_results_map;
        } else {
            HashMap <String, ArrayList <SearchResultObject>> search_results_map = new HashMap <String, ArrayList <SearchResultObject>> ();
            String query = queryStr.trim().toLowerCase();
            ArrayList <SearchResultObject> current_search_results = processQuery(query, searcher, metadata, has_miRNA_data);
            if (SearchHandler.queryHasKeyword(query)) {
                search_results_map.put(query, current_search_results);
            } else {
                search_results_map = makeSearchMap(current_search_results);
            }
            return search_results_map;
        }
    }
    
    public static ArrayList <SearchResultObject> processQuery(
            String query, Searcher searcher, MetaData metadata, boolean has_miRNA_data
    ) throws MultiSlideException {
        
        if(query.contains("[") && query.contains("]")) {
            return metadata.processMetaColQuery(query);
        } else {
            return processDBQuery(query, searcher, has_miRNA_data);
        }
        
    }
    
    public static ArrayList <SearchResultObject> processDBQuery(
            String query, Searcher searcher, boolean has_miRNA_data
    ) throws MultiSlideException {
        
        if(query.contains(":")) { // wildcard
            
            String[] parts = query.split(":", -1); 
            String keyword = parts[0].trim().toLowerCase();
            String search_type = "contains";
            String search_term = parts[1].trim().toLowerCase();
            return doSearch(keyword, search_type, search_term, searcher, has_miRNA_data);
            
        } else if(query.contains("=")) { //exact
            
            String[] parts = query.split("=", -1); 
            String keyword = parts[0].trim().toLowerCase();
            String search_type = "exact";
            String search_term = parts[1].trim().toLowerCase();
            return doSearch(keyword, search_type, search_term, searcher, has_miRNA_data);
            
        } else {
            if (query.contains(",")) {
                String keyword = "none";
                String search_type = "contains";
                return searchMultipleInAllDBs(keyword, search_type, query, searcher);   // search multiple comma separated terms in all 11 DBs
            } else {
                String keyword = "none";
                String search_type = "contains";
                return searchSingleInAllDBs(keyword, search_type, query, searcher);     // search a single query term in all 11 DBs
            }
        }
        
    }
    
    public static String mapUserKeywords(
            String query_type
    ) throws MultiSlideException {
        
        if (query_type.equalsIgnoreCase("path") || 
            query_type.equalsIgnoreCase("pathname") || 
            query_type.equalsIgnoreCase("pathway") || 
            query_type.equalsIgnoreCase("path-name") ||
            query_type.equalsIgnoreCase("path name") ||
            query_type.equalsIgnoreCase("path-way") ||
            query_type.equalsIgnoreCase("path way") ||
            query_type.equalsIgnoreCase("paths") || 
            query_type.equalsIgnoreCase("pathnames") || 
            query_type.equalsIgnoreCase("pathways") || 
            query_type.equalsIgnoreCase("path-names") ||
            query_type.equalsIgnoreCase("path names") ||
            query_type.equalsIgnoreCase("path-ways") ||
            query_type.equalsIgnoreCase("path ways")){
            
                return "pathname";
                
        } else if 
            (query_type.equalsIgnoreCase("pathid") || 
            query_type.equalsIgnoreCase("path_id") || 
            query_type.equalsIgnoreCase("pid") ||
            query_type.equalsIgnoreCase("path-id") ||
            query_type.equalsIgnoreCase("path id") ||
            query_type.equalsIgnoreCase("pathids") || 
            query_type.equalsIgnoreCase("path_ids") || 
            query_type.equalsIgnoreCase("pids") ||
            query_type.equalsIgnoreCase("path-ids") ||
            query_type.equalsIgnoreCase("path ids")){
            
                return "pathid";
                
        } else if 
            (query_type.equalsIgnoreCase("goid") || 
            query_type.equalsIgnoreCase("go_id") || 
            query_type.equalsIgnoreCase("go-id") ||
            query_type.equalsIgnoreCase("go id") ||
            query_type.equalsIgnoreCase("goids") || 
            query_type.equalsIgnoreCase("go_ids") || 
            query_type.equalsIgnoreCase("go-ids") ||
            query_type.equalsIgnoreCase("go ids")){
            
                return "goid";
                
        } else if
            (query_type.equalsIgnoreCase("goterm") ||
            query_type.equalsIgnoreCase("go-term") ||
            query_type.equalsIgnoreCase("go_term") ||
            query_type.equalsIgnoreCase("go") ||
            query_type.equalsIgnoreCase("term") ||
            query_type.equalsIgnoreCase("go term") ||
            query_type.equalsIgnoreCase("goterms") ||
            query_type.equalsIgnoreCase("go-terms") ||
            query_type.equalsIgnoreCase("go_terms") ||
            query_type.equalsIgnoreCase("gos") ||
            query_type.equalsIgnoreCase("terms") ||
            query_type.equalsIgnoreCase("go terms")) {
            
                return "goterm";
                
        } else if 
            (query_type.equalsIgnoreCase("genesymbol") ||
            query_type.equalsIgnoreCase("gene symbol") ||
            query_type.equalsIgnoreCase("gene") ||
            query_type.equalsIgnoreCase("gs") ||
            query_type.equalsIgnoreCase("genename") ||
            query_type.equalsIgnoreCase("gene name") ||
            query_type.equalsIgnoreCase("genesym") ||
            query_type.equalsIgnoreCase("gene-symbol") ||
            query_type.equalsIgnoreCase("gene-name") ||
            query_type.equalsIgnoreCase("gene-sym") ||
            query_type.equalsIgnoreCase("symbol") ||
            query_type.equalsIgnoreCase("genesymbol") ||
            query_type.equalsIgnoreCase("gene symbol") ||
            query_type.equalsIgnoreCase("gene") ||
            query_type.equalsIgnoreCase("gs") ||
            query_type.equalsIgnoreCase("genenames") ||
            query_type.equalsIgnoreCase("gene names") ||
            query_type.equalsIgnoreCase("genesyms") ||
            query_type.equalsIgnoreCase("gene-symbols") ||
            query_type.equalsIgnoreCase("gene-names") ||
            query_type.equalsIgnoreCase("gene-syms") ||
            query_type.equalsIgnoreCase("symbols")) {
            
                return "genesymbol";
                
        } else if 
            (query_type.equalsIgnoreCase("entrez") ||
            query_type.equalsIgnoreCase("gene_id") ||
            query_type.equalsIgnoreCase("gene id") ||
            query_type.equalsIgnoreCase("entrezid") ||
            query_type.equalsIgnoreCase("entrez id") ||
            query_type.equalsIgnoreCase("entrez-id") ||
            query_type.equalsIgnoreCase("entrezs") ||
            query_type.equalsIgnoreCase("gene_ids") ||
            query_type.equalsIgnoreCase("gene ids") ||
            query_type.equalsIgnoreCase("entrezids") ||
            query_type.equalsIgnoreCase("entrez ids") ||
            query_type.equalsIgnoreCase("entrez-ids")) {
            
                return "entrez";
                
        } else if
            (query_type.equalsIgnoreCase("refseq") ||
            query_type.equalsIgnoreCase("refseqid") ||
            query_type.equalsIgnoreCase("refseq-id") ||
            query_type.equalsIgnoreCase("refseq id") ||
            query_type.equalsIgnoreCase("refseq_id") ||
            query_type.equalsIgnoreCase("refseqs") ||
            query_type.equalsIgnoreCase("refseqids") ||
            query_type.equalsIgnoreCase("refseq-ids") ||
            query_type.equalsIgnoreCase("refseq ids") ||
            query_type.equalsIgnoreCase("refseq_ids")) {
                
                return "refseq";
                
        } else if 
            (query_type.equalsIgnoreCase("ensembl") ||
            query_type.equalsIgnoreCase("ensemblid") ||
            query_type.equalsIgnoreCase("ensembls") ||
            query_type.equalsIgnoreCase("ensemblids") ||
            query_type.equalsIgnoreCase("ensembl id") || 
            query_type.equalsIgnoreCase("ensembl-id") ||
            query_type.equalsIgnoreCase("ensembl_id") ||
            query_type.equalsIgnoreCase("ensembl ids") || 
            query_type.equalsIgnoreCase("ensembl-ids") ||
            query_type.equalsIgnoreCase("ensembl_ids") ||
            query_type.equalsIgnoreCase("ensembl-gene") ||
            query_type.equalsIgnoreCase("ensembl gene") ||
            query_type.equalsIgnoreCase("ensembl_gene") ||
            query_type.equalsIgnoreCase("ensembl-genes") ||
            query_type.equalsIgnoreCase("ensembl genes") ||
            query_type.equalsIgnoreCase("ensembl_genes") ||                
            query_type.equalsIgnoreCase("ensembl_gene_id") ||
            query_type.equalsIgnoreCase("ensembl-gene-id") ||
            query_type.equalsIgnoreCase("ensembl gene id") ||
            query_type.equalsIgnoreCase("ensembl_gene_ids") ||
            query_type.equalsIgnoreCase("ensembl-gene-ids") ||
            query_type.equalsIgnoreCase("ensembl gene ids") ||
            query_type.equalsIgnoreCase("ensemblg") ||
            query_type.equalsIgnoreCase("ensembl-g") ||
            query_type.equalsIgnoreCase("ensembl g")) {
            
                return "ensembl gene";
                
        } else if
            (query_type.equalsIgnoreCase("ensembl transcript") ||
            query_type.equalsIgnoreCase("ensembl-transcript") ||
            query_type.equalsIgnoreCase("ensembl_transcript") ||
            query_type.equalsIgnoreCase("ensembl_transcript_id") ||
            query_type.equalsIgnoreCase("ensembl-transcript-id") ||
            query_type.equalsIgnoreCase("ensembl transcript id") ||
            query_type.equalsIgnoreCase("ensembl_transcript_ids") ||
            query_type.equalsIgnoreCase("ensembl-transcript-ids") ||
            query_type.equalsIgnoreCase("ensembl transcript ids") ||
            query_type.equalsIgnoreCase("ensemblt") ||
            query_type.equalsIgnoreCase("ensembl-t")||
            query_type.equalsIgnoreCase("ensembl t")) {
            
                return "ensembl transcript";
                
        } else if
            (query_type.equalsIgnoreCase("ensembl protein") ||
            query_type.equalsIgnoreCase("ensembl-protein") ||
            query_type.equalsIgnoreCase("ensembl_protein") ||
            query_type.equalsIgnoreCase("ensembl_protein_id") ||
            query_type.equalsIgnoreCase("ensembl-protein-id") ||
            query_type.equalsIgnoreCase("ensembl protein id") ||
            query_type.equalsIgnoreCase("ensembl_protein_ids") ||
            query_type.equalsIgnoreCase("ensembl-protein-ids") ||
            query_type.equalsIgnoreCase("ensembl protein ids") ||
            query_type.equalsIgnoreCase("ensemblp") ||
            query_type.equalsIgnoreCase("ensembl-p")||
            query_type.equalsIgnoreCase("ensembl p")) {
            
                return "ensembl protein";
                
        } else if 
            (query_type.equalsIgnoreCase("uniprot") ||
            query_type.equalsIgnoreCase("uniprot id") ||
            query_type.equalsIgnoreCase("uniprot_id") ||
            query_type.equalsIgnoreCase("uniprot-id") ||
            query_type.equalsIgnoreCase("uniprot id") ||
            query_type.equalsIgnoreCase("uniprot ids") ||
            query_type.equalsIgnoreCase("uniprot_ids") ||
            query_type.equalsIgnoreCase("uniprot-ids") ||
            query_type.equalsIgnoreCase("uniprot ids")){
            
                return "uniprot";
        } else {
            throw new MultiSlideException("Unknown keyword: " + query_type);
        }
           
    }
    
    public static ArrayList <SearchResultObject> doSearch(
            String query_type, String search_type, String search_term, Searcher searcher, boolean has_miRNA_Data
    ) throws MultiSlideException  {
        
        query_type = mapUserKeywords(query_type);
        
        ArrayList <SearchResultObject> current_search_results = new ArrayList <SearchResultObject>();
        
        StringTokenizer st = new StringTokenizer(search_term, ",");

        if (query_type != null) {
            if (query_type.equals("pathid") || query_type.equals("pathname")) {
                while (st.hasMoreTokens()) {
                    String query = st.nextToken();
                    ArrayList<PathwayObject> part_paths = searcher.processPathQuery(query, search_type, query_type);
                    if (has_miRNA_Data) {
                        ArrayList<PathwayObject> part_mirna_paths = searcher.processmiRNAPathQuery(query, search_type, query_type);
                        for (int x = 0; x < part_mirna_paths.size(); x++) {
                            current_search_results.add(SearchResultObject.makeSearchResultObject(query, part_mirna_paths.get(x)));
                        }
                    }
                    
                    for (int i = 0; i < part_paths.size(); i++) {
                        current_search_results.add(SearchResultObject.makeSearchResultObject(query, part_paths.get(i)));
                    }
                }

            } else if (query_type.equals("goid") || query_type.equals("goterm")) {

                while (st.hasMoreTokens()) {
                    String query = st.nextToken();
                    ArrayList<GoObject> part_go_terms = searcher.processGOQuery(query, search_type, query_type);
                    if (has_miRNA_Data) {
                        ArrayList<GoObject> part_mirna_go_terms = searcher.processmiRNAGOQuery(query, search_type, query_type);
                        for (int x = 0; x < part_mirna_go_terms.size(); x++) {
                            current_search_results.add(SearchResultObject.makeSearchResultObject(query, part_mirna_go_terms.get(x)));
                        }
                    }
                    for (int i = 0; i < part_go_terms.size(); i++) {
                        current_search_results.add(SearchResultObject.makeSearchResultObject(query, part_go_terms.get(i)));
                    }
                }

            } else if (query_type.equals("entrez") || query_type.equals("genesymbol") || query_type.equals("refseq")
                    || query_type.equals("ensembl gene") || query_type.equals("ensembl transcript") || query_type.equals("ensembl protein")
                    || query_type.equals("uniprot")) {

                while (st.hasMoreTokens()) {
                    String query = st.nextToken();
                    ArrayList<GeneObject> part_genes = searcher.processGeneQuery(query, search_type, query_type);
                    for (int i = 0; i < part_genes.size(); i++) {
                        current_search_results.add(SearchResultObject.makeSearchResultObject(query, part_genes.get(i)));
                    }
                }

            }
        }
        
        return current_search_results;

    }
    
    public static ArrayList <SearchResultObject> searchSingleInAllDBs(String keyword, String search_type, String queryString, Searcher searcher) {
        
        ArrayList <SearchResultObject> collated_search_results = new ArrayList <SearchResultObject> ();
        List <Future<ArrayList <SearchResultObject>>> futuresList = new ArrayList<>();
        int num_threads = 11;
        ExecutorService executor = Executors.newFixedThreadPool(num_threads);
        try {
            for (byte i = 1; i <= num_threads; i++) {
                Future <ArrayList <SearchResultObject>> result_i = executor.submit(new SearchThreader(i, queryString, search_type, keyword, searcher));
                futuresList.add(result_i);
            }
            for (Future<ArrayList <SearchResultObject>> future : futuresList) {
		ArrayList <SearchResultObject> result_i = null;
		try {
                    result_i = future.get();
		} catch (InterruptedException | ExecutionException ee) {
                    System.out.println("Execution Exception\n");
                    System.out.println(ee);
                    ee.printStackTrace();
                }
		collated_search_results.addAll(result_i);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            
        }
        System.out.println("Finished all threads");
        return collated_search_results;
    }
    
    public static ArrayList <SearchResultObject> searchMultipleInAllDBs(String keyword, String search_type, String query, Searcher searcher) {
        ArrayList <SearchResultObject> collated_search_results = new ArrayList <SearchResultObject> ();
        StringTokenizer st = new StringTokenizer(query, ",");
        while(st.hasMoreTokens()){
            ArrayList <SearchResultObject> result_i = null;
            result_i = searchSingleInAllDBs (keyword, search_type, st.nextToken(), searcher);
            collated_search_results.addAll(result_i);
        }
        return collated_search_results;
    }
    
    public static boolean queryHasKeyword(String query) {
        return (query.contains(":") || query.contains("="));
    }
    
    public static HashMap<String, ArrayList<SearchResultObject>> makeSearchMap(ArrayList<SearchResultObject> current_search_results) {
        HashMap<String, ArrayList<SearchResultObject>> search_results_map = new HashMap<String, ArrayList<SearchResultObject>>();
        for (int i = 0; i < current_search_results.size(); i++) {
            SearchResultObject sro = current_search_results.get(i);
            String query_key = sro.getQueryAsString();
            if (search_results_map.containsKey(query_key)) {
                search_results_map.get(query_key).add(sro);
            } else {
                ArrayList<SearchResultObject> temp = new ArrayList<SearchResultObject>();
                temp.add(sro);
                search_results_map.put(query_key, temp);
            }
        }
        return search_results_map;
    }
}
