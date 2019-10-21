/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.util.ArrayList;
import java.util.HashMap;
import org.cssblab.multislide.beans.data.NeighborhoodSearchResults;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.searcher.network.NetworkSearchHandler;
import org.cssblab.multislide.structure.MultiSlideException;

/**
 *
 * @author soumitag
 */
public class NetworkSearchTests {
    
    public static void main(String[] args) {
        try {
            NetworkSearchHandler searchP = new NetworkSearchHandler();
            HashMap <String, ArrayList<String>> search_results = searchP.processQuery("tf-entrez=3640");
            //searchP.processQuery("tf-entrez=3640");
            //searchP.processQuery("tf-entrez=26469");
            //searchP.processQuery("ppi-entrez=2303");
            //HashMap <String, ArrayList<String>> search_results = searchP.processQuery("mirna-id=hsa-mir-99b-5p");
            HashMap <String, Boolean> entrezMaster = new HashMap <String, Boolean> ();
            
            HashMap <String, Integer> identifier_index_map = createIdentifierIndexMap();
            int idenitifier_index = identifier_index_map.get("genesymbol_2021158607524066");
            
            Searcher searcher = new Searcher("human");
            
            NeighborhoodSearchResults nsr = new NeighborhoodSearchResults();
            nsr.makeNeighborhoodSearchResultObject(searcher, "genesymbol_2021158607524066", idenitifier_index, search_results, entrezMaster);
            
            for(int i = 0; i < nsr.neighbor_display_tag.length; i++){
                System.out.println(nsr.neighbor_display_tag[i]);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    private static HashMap <String, Integer> createIdentifierIndexMap() {
        HashMap <String, Integer> identifier_index_map  = new HashMap <> ();
        identifier_index_map.put("entrez_2021158607524066", 0);
        identifier_index_map.put("genesymbol_2021158607524066", 1);
        identifier_index_map.put("refseq_2021158607524066", 2);
        identifier_index_map.put("ensembl_gene_id_2021158607524066", 3);
        identifier_index_map.put("ensembl_transcript_id_2021158607524066", 4);
        identifier_index_map.put("ensembl_protein_id_2021158607524066", 5);
        identifier_index_map.put("uniprot_id_2021158607524066", 6);
        return identifier_index_map;
    }
    
}
