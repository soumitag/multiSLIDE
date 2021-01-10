import os
import numpy as np
import scipy as sp
import pandas as pd
import scipy.stats as stats
from pyslide.utils.fileutils import read_file, save_file, read_spark_file
import sys


# Benjamini–Hochberg classic FDR procedure
def fdr(p_values, q):  # q is the desired false discovery rate
    m = len(p_values)
    sort_ind = np.argsort(p_values)
    k = [i for i, p in enumerate(p_values[sort_ind]) if p < (i + 1.) * q / m]
    # k = [i for i, p in enumerate(p_values[sort_ind]) if p < ((i + 1.)/ m) * q]
    significant = np.zeros(m, dtype=np.float32)
    if k:
        significant[sort_ind[0:k[-1] + 1]] = 1.
    return significant


def test(phenotypes, phen_dict, exp_d, phenotype_datatype, use_parametric):

    assert phenotype_datatype in ['binary', 'categorical', 'continuous'], 'Phenotype with unsupported data type'

    assert not(phenotype_datatype == 'binary' and len(phen_dict) > 2), \
        'Phenotype data type is binary but phenotype has more than two unique values'

    group_data = {}
    phen_arr = np.array(phenotypes)
    for phen in phen_dict:
        group_data[phen] = exp_d[:, phen_arr == phen]

    n_genes = exp_d.shape[0]
    z = np.zeros((n_genes, 2), dtype=np.float)
    for gene_index in range(n_genes):
        args = [np.transpose(group_data[phen][gene_index]) for phen in phen_dict]

        if use_parametric:
            if phenotype_datatype == 'binary':
                z[gene_index] = stats.ttest_ind(*args, equal_var=False)
            elif phenotype_datatype == 'categorical':
                z[gene_index] = stats.f_oneway(*args)
        else:
            if phenotype_datatype == 'binary':
                z[gene_index] = stats.mannwhitneyu(*args, alternative='two-sided')
            elif phenotype_datatype == 'categorical':
                z[gene_index] = stats.kruskal(*args)

    z1 = z[:, 1]
    z1[np.isnan(z1)] = 1
    z1 = [np.nan_to_num(v) for v in z1]

    return z1


def linear_regression(phenotypes, expressions):

    # phen_t = np.transpose(phenotypes)
    # exp_d = np.transpose(expressions)
    phen_t = phenotypes
    exp_d = expressions
    n_genes = exp_d.shape[0]

    z = np.zeros((n_genes, 5), dtype=np.float)

    for gene_index, gene_data in enumerate(exp_d):
        # args = [gene_data, phen_t]
        args = [phen_t, gene_data]
        z[gene_index] = stats.linregress(*args)

    z3 = z[:, 3]
    z3[np.isnan(z3)] = 1
    z3 = [np.nan_to_num(v) for v in z3]

    return z3


def significance_testing_from_file(request_id, n_rows, n_cols, phenotype_datatype, dtype, use_parametric, fdr_rate, path_to_data_pool):

    assert phenotype_datatype in ['binary', 'categorical', 'continuous'], 'Phenotype with unsupported data type'

    '''
    Load expression and phenotype data
    '''
    exp_data = read_spark_file(path_to_data_pool, os.path.join(request_id, 'expressions'), n_rows, n_cols)
    phen_data = read_spark_file(path_to_data_pool, os.path.join(request_id, 'phenotypes'), n_rows, 2)

    '''
    join by _Sample_IDs
    '''
    _ids = exp_data['_id']
    df = exp_data.drop(columns=['_id']).T
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

    '''
    Get significance and fdr
    '''
    z = significance_testing(_expressions, _phenotypes, phenotype_datatype, use_parametric, fdr_rate)

    '''
    Add _ids back to results
    '''
    result = pd.DataFrame({'_significance': z[:, 0], '_fdr': z[:, 1]})
    result.insert(0, '_id', _ids)

    '''
    sort by _significance and add _index
    '''
    result_sorted = result.sort_values(by='_significance', axis=0, ascending=True)
    result_sorted.insert(3, '_index', np.arange(len(z)))

    '''
    Serialize results
    '''
    result_sorted.to_csv(os.path.join(path_to_data_pool, request_id, "result.txt"), sep="\t", header=True, index=False)

    return 1


def significance_testing(expressions, phenotypes, _phenotype_datatype, _use_parametric, _fdr_rate):

    assert _phenotype_datatype in ['binary', 'categorical', 'continuous'], 'Phenotype with unsupported data type'

    phen_dict = {}
    for phen in phenotypes:
        phen_dict[phen] = phen

    if _phenotype_datatype in ['binary', 'categorical']:
        z = test(phenotypes, phen_dict, expressions, _phenotype_datatype, _use_parametric)
    else:
        z = linear_regression(phenotypes, expressions)

    za = np.array(z, dtype=np.float32)
    significant = fdr(za, _fdr_rate)
    # print(significant)
    z1 = np.column_stack((za, significant.astype(np.float32)))

    return z1


if __name__ == "__main__":

    request_id = 'req_1589554795302.-1630792986'
    path_to_data_pool = '/Users/soumitaghosh/Documents/GitHub/multi-slide/temp/cache'
    significance_level = 0.05
    phenotype_datatype = 'categorical'
    fdr_rate = 0.2
    dtype = 'float'
    use_parametric = True
    n_rows = 65
    n_cols = 74

    significance_testing_from_file(request_id, n_rows, n_cols, phenotype_datatype, dtype, use_parametric, fdr_rate,
                                   path_to_data_pool)