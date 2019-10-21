import os
import sys
import numpy as np
import scipy as sp
import scipy.stats as stats

TEST_TYPE_ONE_WAY_ANOVA = 0
TEST_TYPE_LINEAR_REGRESSION = 1
TEST_TYPE_T_TEST = 2

def test (phenotypes, phen_dict, expressions, test_type) :

    group_indices = []
    phen_arr = np.array(phenotypes)
    for phen in phen_dict:
        indices = np.where(phen_arr == phen)
        group_indices.append(indices)

    if test_type == TEST_TYPE_T_TEST and len(group_indices) > 2:
        raise ("")

    exp_d = np.transpose(expressions)
    n_genes = exp_d.shape[0]
    Z = np.zeros((n_genes,2), dtype=np.float)
    for gene_index,gene_data in enumerate(exp_d):
        grouped_data = []
        for indices in group_indices:
            L = [gene_data[i] for i in indices]
            grouped_data.append(L)
        # print(grouped_data)
        args = [np.transpose(g) for g in grouped_data]
        if test_type == TEST_TYPE_ONE_WAY_ANOVA:
            Z[gene_index] = stats.f_oneway(*args)
        elif test_type == TEST_TYPE_T_TEST:
            Z[gene_index] = stats.ttest_ind(*args,equal_var=False)
        else:
            raise ("")

    Z1 = Z[:,1]
    Z1[np.isnan(Z1)] = 1
    Z1 = [np.nan_to_num(v) for v in Z1]
    return Z1

def linear_regression (phenotypes, phen_dict, expressions):

    phen_t = np.transpose(phenotypes)
    exp_d = np.transpose(expressions)
    n_genes = exp_d.shape[0]

    Z = np.zeros((n_genes,5), dtype=np.float)

    for gene_index,gene_data in enumerate(exp_d):
        args = [gene_data, phen_t]
        # print(args)
        Z[gene_index] = stats.linregress(*args)

    Z3 = Z[:,3]
    Z3[np.isnan(Z3)] = 1
    Z3 = [np.nan_to_num(v) for v in Z3]
    return Z3

def doSignificanceTesting(folderpath, filename, test_type=1, run_id=""):

    print ("Loading Data...")
    data = np.loadtxt(os.path.join (folderpath, filename), dtype=float, delimiter='\t')
    print ("Done.")

    phenotypes = data[:,0]
    expressions = data[:,1:]
    phen_dict = {}
    for phen in phenotypes:
        phen_dict[phen] = phen

    if test_type == TEST_TYPE_ONE_WAY_ANOVA:
        Z = test (phenotypes, phen_dict, expressions, test_type)
    elif test_type == TEST_TYPE_T_TEST:
        Z = test (phenotypes, phen_dict, expressions, test_type)
    elif test_type == TEST_TYPE_LINEAR_REGRESSION:
        Z = linear_regression (phenotypes, phen_dict, expressions)
    else:
        raise ("")
    
    print ("Saving To File...")
    np.savetxt(os.path.join (folderpath, 'SigTestOutput_' + run_id + '.txt'), Z, fmt='%.3f')
    print ("Done.")

if __name__ == "__main__":

    # try:

    _folderpath = str(sys.argv[1])
    _infilename = str(sys.argv[2])
    _test_type = int(sys.argv[3])
    _run_id = str(sys.argv[4])

    doSignificanceTesting(_folderpath, _infilename, test_type=_test_type, run_id=_run_id)

    '''
    except Exception as e:

        print(e)
        logf = open(os.path.join (_folderpath, 'SigTestError_' + _run_id + '.txt'), "w")
        logf.write(str(e))
    '''
