import os
from pymongo import MongoClient
import scipy.stats as sp
import numpy as np
import pandas as pd
from pyslide.utils.fileutils import read_file, read_delimited_file, save_file, save_delimited_file, map_java_dtype, \
    _bool, read_spark_file
from pyslide.stats.significance_testing import significance_testing, fdr


class EnrichmentAnalysis:

    def __init__(self, species, include_pathways=True, include_ontologies=True):

        self._species = species

        self._include_pathways = include_pathways
        self._include_ontologies = include_ontologies

        self._collection = None
        self._client = MongoClient('localhost', 27017)
        self._connect_db()

        '''
        N: population size
        n: sample size
        K: successes in population
        k: successes in sample
        '''
        self._population_size_big_n = 0
        self._sample_size_small_n = 0

        self._path_key_name_map = {}
        self._pathway_map = {}
        self._pathway_sz = {}
        self._pathway_probabilities = {}
        self._pathway_gene_map = {}

        self._go_key_term_map = {}
        self._go_map = {}
        self._go_sz = {}
        self._go_probabilities = {}
        self._go_gene_map = {}

    def _connect_db(self):
        if self._species == "human":
            db = self._client['geneVocab_HomoSapiens']
            self._collection = db['HS_geneMap2']
        elif self._species == "mouse":
            db = self._client['geneVocab_MusMusculus']
            self._collection = db['MM_geneMap2']

    def create_pathway_maps(self, background_entrez_list):  # both pathway and GO implemented here

        self._population_size_big_n = len(background_entrez_list)

        # background_entrez_list = ['2099','1956','3082']
        for entrez in background_entrez_list:

            query = {"_id": entrez}
            docs = self._collection.find(query)
            for doc in docs:

                if self._include_pathways:
                    pathway_list = doc['pathways']
                    for p in pathway_list:
                        path_key = p['external_id']
                        path_name = p['pathway']
                        if path_key in self._pathway_map:
                            self._pathway_map[path_key][entrez] = True
                        else:
                            self._pathway_map[path_key] = {}
                            self._pathway_map[path_key][entrez] = True

                        self._path_key_name_map[path_key] = path_name

                if self._include_ontologies:
                    goid_list = doc['goids']
                    for g in goid_list:
                        go_key = g['go']
                        go_onto = g['ontology']
                        go_term = g['term']
                        # print(go_key, go_onto)
                        if go_onto == "bp":
                            if go_key in self._go_map:
                                self._go_map[go_key][entrez] = True
                            else:
                                self._go_map[go_key] = {}
                                self._go_map[go_key][entrez] = True

                            if go_key not in self._go_key_term_map:
                                self._go_key_term_map[go_key] = go_term

        '''
        for key in self._pathway_map:
            print(key, self._path_key_name_map[key], len(self._pathway_map[key]))
        '''

    def compute_pathway_sizes(self, sig_entrez_list):

        self._sample_size_small_n = len(sig_entrez_list)

        if self._include_pathways:
            for path in self._pathway_map:
                self._pathway_sz[path] = {}
                self._pathway_sz[path]['successes_in_population_big_k'] = len(self._pathway_map[path])
                self._pathway_sz[path]['successes_in_sample_small_k'] = 0
                for entrez in sig_entrez_list:
                    if entrez in self._pathway_map[path]:
                        self._pathway_sz[path]['successes_in_sample_small_k'] += 1
                        if path in self._pathway_gene_map:
                            self._pathway_gene_map[path][entrez] = True
                        else:
                            self._pathway_gene_map[path] = {}
                            self._pathway_gene_map[path][entrez] = True

        if self._include_ontologies:
            for go in self.go_map:
                self.go_sz[go] = {}
                self.go_sz[go]['successes_in_population_big_k'] = len(self.go_map[go])
                self.go_sz[go]['successes_in_sample_small_k'] = 0
                for entrez in sig_entrez_list:
                    if entrez in self.go_map[go]:
                        self.go_sz[go]['successes_in_sample_small_k'] += 1
                        if go in self._go_gene_map:
                            self._go_gene_map[go][entrez] = True
                        else:
                            self._go_gene_map[go] = {}
                            self._go_gene_map[go][entrez] = True
        '''
        for key in self._pathway_map:
            print(key, self._path_key_name_map[key], self._pathway_sz[key])
        '''

    def compute_hyper_geom(self):

        if self._include_pathways:
            for path in self._pathway_sz:
                self._pathway_probabilities[path] = self.calculate_probabilities(
                    self._pathway_sz[path]['successes_in_population_big_k'],
                    self._pathway_sz[path]['successes_in_sample_small_k'])

        if self._include_ontologies:
            for go in self._go_sz:
                self._go_probabilities[go] = self.calculate_probabilities(
                    self._go_sz[go]['successes_in_population_big_k'],
                    self._go_sz[go]['successes_in_sample_small_k'])

    def calculate_probabilities(self, successes_in_population_big_k, successes_in_sample_small_k):
        prb = 1 - (
            sp.hypergeom.cdf(successes_in_sample_small_k, self._population_size_big_n, successes_in_population_big_k,
                             self._sample_size_small_n))
        return prb

    def get_enrichment_analysis_results(self, write_to_file):
        # returns a panda DataFrame
        d = {}
        if self._include_pathways:
            for index, path in enumerate(self._pathway_sz):
                _name = self._path_key_name_map[path]
                s = ""
                if path in self._pathway_gene_map and write_to_file:
                    s = self._get_formatted_genes(self._pathway_gene_map[path])

                d[str(index) + 'p'] = ['pathway', path, _name,
                                       self._pathway_sz[path]['successes_in_population_big_k'],
                                       self._pathway_sz[path]['successes_in_sample_small_k'],
                                       self._pathway_probabilities[path], s]

                '''    
                if self._pathway_probabilities[path] < 0.001:
                    print(d[str(index) + 'p'])
                '''

        if self._include_ontologies:
            for index, go in enumerate(self._go_sz):
                _term = self._go_key_term_map[go]
                s = ""
                if go in self._go_gene_map and write_to_file:
                    s = self._get_formatted_genes(self._go_gene_map[go])

                d[str(index) + 'p'] = ['gene-ontology', go, _term,
                                       self._go_sz[go]['successes_in_population_big_k'],
                                       self._go_sz[go]['successes_in_sample_small_k'],
                                       self._go_probabilities[go], s]

        df = pd.DataFrame(data=d).T
        df.columns = ['type', 'id', 'name', 'big_K', 'small_k', 'prob', 'members']
        return df

    def _get_formatted_genes(self, entrez_map):

        '''
        gene_symbol_list = []
        for entrez in entrez_map:
            gene_symbol_list.append(self._get_gene_symbol(entrez))
        '''
        gene_symbol_list = [self._get_gene_symbol(entrez) + "(" + entrez + ")" for entrez in entrez_map]
        entrez_str = ', '.join(gene_symbol_list)
        return entrez_str

    def _get_gene_symbol(self, entrez):
        query = {"_id": entrez}
        docs = self._collection.find(query)
        gene_symbol = ""
        for doc in docs:
            gene_symbol = doc['genesymbol']

        return gene_symbol.upper()


def enrichment_analysis(
        request_id, n_rows, n_cols, dtype, species,
        phenotype_datatype, significance_level_d, use_parametric, apply_fdr_d, fdr_rate_d,
        include_pathways, include_ontologies, significance_level_e, apply_fdr_e, fdr_rate_e,
        apply_pathway_sz_filter, min_pathway_sz, path_to_data_pool, display_headers
):
    _n_rows = int(n_rows)
    _n_cols = int(n_cols)

    _use_parametric = _bool(use_parametric)
    _apply_fdr_d = _bool(apply_fdr_d)
    _fdr_rate_d = float(fdr_rate_d)
    _significance_level_d = float(significance_level_d)

    _include_pathways = _bool(include_pathways)
    _include_ontologies = _bool(include_ontologies)
    _apply_fdr_e = _bool(apply_fdr_e)
    _fdr_rate_e = float(fdr_rate_e)
    _significance_level_e = float(significance_level_e)

    _apply_pathway_sz_filter = _bool(apply_pathway_sz_filter)
    _min_pathway_sz = int(min_pathway_sz)

    _display_headers = _bool(display_headers)

    '''
    Load expression and phenotype data
    '''
    exp_data = read_spark_file(path_to_data_pool, os.path.join(request_id, 'expressions'), n_rows, n_cols)
    phen_data = read_spark_file(path_to_data_pool, os.path.join(request_id, 'phenotypes'), n_rows, 2)

    '''
    join by _Sample_IDs
    '''
    _ids = exp_data['_entrez']
    df = exp_data.drop(columns=['_entrez']).T
    phen_data = phen_data.set_index('_Sample_IDs')
    phen_data.index = phen_data.index.map(str)
    df = df.join(phen_data)

    '''
    Extract as numpy arrays
    '''
    phenotype_name = ''
    for col in phen_data.columns:
        if col != '_Sample_IDs':
            phenotype_name = col

    _phenotypes = df[phenotype_name].tolist()
    _expressions = np.transpose((df.loc[:, df.columns != phenotype_name]).to_numpy(dtype=np.float))

    _entrez = _ids
    _entrez = ['{0:d}'.format(int(e)) for e in _entrez]

    z = significance_testing(_expressions, _phenotypes, phenotype_datatype, _use_parametric, _fdr_rate_d)

    if _apply_fdr_d:
        _significant_entrez = [_entrez[i] for i in range(len(z)) if z[i, 1] == 1.0 and z[i, 0] <= _significance_level_d]
    else:
        _significant_entrez = [_entrez[i] for i in range(len(z)) if z[i, 0] <= _significance_level_d]

    ea = EnrichmentAnalysis(species, include_pathways=_include_pathways, include_ontologies=_include_ontologies)
    ea.create_pathway_maps(_entrez)
    ea.compute_pathway_sizes(_significant_entrez)
    ea.compute_hyper_geom()
    df = ea.get_enrichment_analysis_results(_display_headers)

    sorted_df = df.sort_values('prob', inplace=False, ascending=True)

    significant = fdr(sorted_df['prob'].values, _fdr_rate_e)
    sorted_df.insert(7, 'fdr', significant, allow_duplicates=True)

    if _apply_fdr_e:
        ind = np.logical_and(sorted_df['prob'] <= _significance_level_e, sorted_df['fdr'])
        filtered_df = sorted_df.loc[ind]
    else:
        filtered_df = sorted_df.loc[(sorted_df['prob'] <= _significance_level_e)]

    if _apply_pathway_sz_filter:
        filtered_df = filtered_df.loc[(filtered_df['big_K'] >= _min_pathway_sz)]

    filtered_sorted_df = filtered_df.sort_values(['prob', 'big_K'], inplace=False, ascending=True)
    filtered_sorted_df.columns = ['Functional_Group_Type', 'Group_ID', 'Group_Name', 'Num_Genes_In_Background',
                                  'Num_Significant_Genes', 'p_value', 'Genes', 'FDR']
    if _display_headers:
        filtered_sorted_df.to_csv(os.path.join(path_to_data_pool, request_id, "result.txt"), sep="\t", header=True,
                                  index=False)
    else:
        filtered_sorted_df.to_csv(os.path.join(path_to_data_pool, request_id, "result.txt"), sep="\t", header=False,
                                  index=False)

    return 1
