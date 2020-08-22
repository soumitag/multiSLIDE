import numpy as np
import pandas as pd
from pyslide.utils.fileutils import read_spark_file
import fastcluster as fc
import scipy as sp
import os
from scipy.cluster.hierarchy import dendrogram, optimal_leaf_ordering, leaves_list


def _impute(data):
    """
    :param data: input data, already transposed
    :return imputed_data: imputed for row clustering
    :return nan_row_idx: imputed row indices
    """

    """
    Find row indices that are all NaNs
    and drop those rows
    """
    nan_row_idx = data.index[data.isnull().all(1)]
    d1 = data.drop(nan_row_idx)

    """
    Find columns that are all NaNs and remove them
    """
    t = d1.isnull().all(0)
    nan_col_idx = t.index[t]
    d2 = d1.drop(nan_col_idx, axis=1)

    """
    Impute remaining NaNs with column means
    """
    imputed_data = d2.fillna(d2.mean())
    return imputed_data, nan_row_idx


def _reorder_dendrogram(z, dists, leaf_ordering):

    if leaf_ordering == 'optimal':
        z = optimal_leaf_ordering(z, dists)
        h = leaves_list(z)

    elif leaf_ordering == 'count_sort_ascending':
        r = dendrogram(z, get_leaves=True, count_sort='ascending', no_plot=True, no_labels=True,
                       show_leaf_counts=False)
        h = r['leaves']

    elif leaf_ordering == 'count_sort_descending':
        r = dendrogram(z, get_leaves=True, count_sort='descending', no_plot=True, no_labels=True,
                       show_leaf_counts=False)
        h = r['leaves']

    elif leaf_ordering == 'distance_sort_ascending':
        r = dendrogram(z, get_leaves=True, distance_sort='ascending', no_plot=True, no_labels=True,
                       show_leaf_counts=False)
        h = r['leaves']

    elif leaf_ordering == 'distance_sort_descending':
        r = dendrogram(z, get_leaves=True, distance_sort='descending', no_plot=True, no_labels=True,
                       show_leaf_counts=False)
        h = r['leaves']

    else:
        raise ValueError('Unsupported leaf ordering')

    return h


def hierarchical_clustering(request_id, linkage, distance_metric, leaf_ordering, transpose,
                            n_rows, n_cols, path_to_data_pool):

    if linkage not in ['average', 'complete', 'median', 'centroid', 'ward', 'weighted', 'single']:
        raise ValueError('Unsupported linkage function')

    if distance_metric not in ['euclidean', 'manhattan', 'cosine', 'correlation', 'chebyshev']:
        raise ValueError('Unsupported distance metric')

    data = read_spark_file(path_to_data_pool, request_id, n_rows, n_cols)

    if transpose:
        """
        Sample clustering
        """
        df = data.T
        _axis = 1
    else:
        """
        Feature clustering
        """
        df = data.drop(columns=['_id'])
        _axis = 0

    imputed_data, nan_row_idx = _impute(df)

    d = imputed_data.to_numpy(dtype=np.float)
    print('d.shape =', d.shape)
    if d.shape[0] == 1:
        h = [0]
    else:
        dists = sp.spatial.distance.pdist(d, distance_metric)
        z = fc.linkage(dists, method=linkage, metric=distance_metric, preserve_input=False)
        h = _reorder_dendrogram(z, dists, leaf_ordering)

    """
    re-calculate indices in h to reflect original indices (prior to pruning of NaN rows)
    append indices of NaN rows
    """
    _indices_sans_nans = [idx for idx in range(data.shape[_axis]) if idx not in nan_row_idx]
    new_order = np.concatenate(([_indices_sans_nans[idx] for idx in h], nan_row_idx))

    if transpose:
        """
        Sample clustering
        """
        sample_ids = list(data.columns)
        _ids = [sample_ids[idx] for idx in new_order]
        result = pd.DataFrame({'_Sample_IDs': _ids, '_index': np.arange(len(_ids))})
    else:
        """
        Feature clustering
        """
        _ids = data['_id'].reindex(new_order)
        result = pd.DataFrame({'_id': _ids, '_index': np.arange(len(_ids))})

    result.to_csv(os.path.join(path_to_data_pool, request_id, "result.txt"), sep="\t", header=True, index=False)

    return 1


def hierarchical_clustering_by_groups(request_id, linkage, distance_metric, leaf_ordering, transpose, group_by,
                                      n_rows, n_cols, path_to_data_pool):

    if linkage not in ['average', 'complete', 'median', 'centroid', 'ward', 'weighted', 'single']:
        raise ValueError('Unsupported linkage function')

    if distance_metric not in ['euclidean', 'manhattan', 'cosine', 'correlation', 'chebyshev']:
        raise ValueError('Unsupported distance metric')

    data = read_spark_file(path_to_data_pool, request_id, n_rows, n_cols)
    data['_id'] = data['_id'].apply(str)

    if transpose:
        """
        Sample clustering
        """
        return_linkers = False
        data = data.drop(columns=['_id']).T

        phen_data = read_spark_file(path_to_data_pool, os.path.join(request_id, 'phenotypes'), n_rows, len(group_by))
        phen_data = phen_data.set_index('_Sample_IDs')
        phen_data.index = phen_data.index.map(str)

        data = data.join(phen_data).reset_index().rename(columns={'index': '_Sample_IDs'})
        metadata_cols = ['_Sample_IDs'] + group_by
        _id = '_Sample_IDs'
    else:
        """
        Feature clustering
        """
        if len(group_by) == 1 and group_by[0] == '_linker':
            return_linkers = True
        else:
            return_linkers = False
        metadata_cols = ['_id'] + group_by
        _id = '_id'

    exp_data = data.drop(columns=metadata_cols)

    groups = data.groupby(group_by, as_index=True)[group_by].tail(1) \
                 .sort_values(group_by, axis=0).set_index(group_by) \
                 .index.to_list()

    group_ids = data.groupby(group_by, as_index=True)[_id].apply(list)

    ordered_ids = []
    if return_linkers:
        ordered_linkers = []

    for group in groups:

        _ids = group_ids.loc[group]

        if len(_ids) > 2:

            d = exp_data[data[_id].isin(_ids)]
            imputed_data, nan_row_idx = _impute(d)

            dists = sp.spatial.distance.pdist(imputed_data.to_numpy(), distance_metric)
            z = fc.linkage(dists, method=linkage, metric=distance_metric, preserve_input=False)
            h = _reorder_dendrogram(z, dists, leaf_ordering)

            _indices_sans_nans = [idx for idx in range(d.shape[0]) if idx not in nan_row_idx]
            _within_linker_ordered_ids = np.concatenate(([_ids[_indices_sans_nans[idx]] for idx in h],
                                                         [_ids[idx] for idx in nan_row_idx]))

        else:
            _within_linker_ordered_ids = _ids

        if return_linkers:
            _linkers = data[data[_id].isin(_ids)]['_linker'].tolist()
            ordered_linkers = np.append(ordered_linkers, [_linkers])

        ordered_ids = np.append(ordered_ids, _within_linker_ordered_ids)

    if return_linkers:
        result = pd.DataFrame({_id: ordered_ids, '_linker': ordered_linkers})
    else:
        result = pd.DataFrame({_id: ordered_ids, '_index': np.arange(len(ordered_ids))})

    result.to_csv(os.path.join(path_to_data_pool, request_id, "result.txt"), sep="\t", header=True, index=False)

    return 1


if __name__ == "__main__":

    request_id = 'req_1587879851133.-1203031602'
    path_to_data_pool = '/Users/soumitaghosh/Documents/GitHub/multi-slide/temp/cache'
    linkage = 'complete'
    distance_metric = 'euclidean'
    leaf_ordering = 'optimal'
    transpose = False
    n_rows = 65
    n_cols = 73

    hierarchical_clustering(request_id, linkage, distance_metric, leaf_ordering, transpose,
                            n_rows, n_cols, path_to_data_pool)
