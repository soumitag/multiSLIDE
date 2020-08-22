import argparse
import os
import traceback
from pathlib import Path
from flask import Flask, request
from waitress import serve
from pyslide.server_response import StandardResponse
from pyslide.stats.clustering import hierarchical_clustering, hierarchical_clustering_by_groups
from pyslide.stats.significance_testing import significance_testing_from_file
from pyslide.stats.enrichment_analysis import enrichment_analysis
from pyslide.utils.fileutils import _bool


def dir_path(path):
    if os.path.isdir(path):
        return path
    else:
        raise argparse.ArgumentTypeError(f"readable_dir:{path} is not a valid path")


def parse_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument("install_dir", type=dir_path, help='specify installation directory')
    return parser.parse_args()


app = Flask(__name__)
parsed_args = parse_arguments()
app.config['install_dir'] = Path(parsed_args.install_dir)
app.config['data_pool'] = os.path.join(parsed_args.install_dir, 'temp', 'cache')


@app.route("/")
def hello():
    return "Hello World!"


@app.route("/get_install_dir")
def get_install_dir():
    try:
        resp = StandardResponse(1, message=app.config['install_dir'])
        return resp.as_json()
    except Exception:
        resp = StandardResponse(0, message='Error in get_install_dir()', detailed_message=traceback.format_exc())
        return resp.as_json()


@app.route("/do_clustering")
def do_clustering():
    try:
        _request_id = request.args.get('request_id')
        _linkage_strategy = request.args.get('linkage_strategy')
        _distance_metric = request.args.get('distance_metric')
        _leaf_ordering = request.args.get('leaf_ordering')
        _transpose = _bool(request.args.get('transpose'))
        _n_rows = int(request.args.get('n_rows'))
        _n_cols = int(request.args.get('n_cols'))
        _grp_by = request.args.get('group_by')
        if _grp_by == '':
            _group_by = []
        else:
            _group_by = [x.strip() for x in request.args.get('group_by').split(',')]

        print(_group_by)
        print(len(_group_by))

        if len(_group_by) > 0:
            status = hierarchical_clustering_by_groups(_request_id, _linkage_strategy, _distance_metric,
                                                       _leaf_ordering, _transpose, _group_by,
                                                       _n_rows, _n_cols, app.config['data_pool'])
        else:
            status = hierarchical_clustering(_request_id, _linkage_strategy, _distance_metric,
                                             _leaf_ordering, _transpose,
                                             _n_rows, _n_cols, app.config['data_pool'])
        if status == 1:
            resp = StandardResponse(1, message='', detailed_message='')
        else:
            resp = StandardResponse(0, message='Error in do_clustering()', detailed_message='Unknown origin')
        return resp.as_json()
    except Exception:
        resp = StandardResponse(0, message='Error in do_clustering()', detailed_message=traceback.format_exc())
        return resp.as_json()


@app.route("/do_significance_testing")
def do_significance_testing():
    try:
        _request_id = request.args.get('request_id')
        _phenotype_datatype = request.args.get('phenotype_datatype')
        _dtype = request.args.get('dtype')
        _use_parametric = _bool(request.args.get('use_parametric'))
        _significance_level = float(request.args.get('significance_level'))
        _fdr_rate = float(request.args.get('fdr_rate'))
        _n_rows = int(request.args.get('n_rows'))
        _n_cols = int(request.args.get('n_cols'))

        status = significance_testing_from_file(
            _request_id, _n_rows, _n_cols, _phenotype_datatype, _dtype,
            _use_parametric, _fdr_rate,
            app.config['data_pool'])

        if status == 1:
            resp = StandardResponse(1, message='', detailed_message='')
        else:
            resp = StandardResponse(0, message='Error in do_significance_testing()', detailed_message='Unknown origin')
        return resp.as_json()
    except Exception:
        resp = StandardResponse(0, message='Error in do_significance_testing()',
                                detailed_message=traceback.format_exc())
        return resp.as_json()


@app.route("/do_enrichment_analysis")
def do_enrichment_analysis():
    try:
        _request_id = request.args.get('request_id')
        _n_rows = request.args.get('n_rows')
        _n_cols = request.args.get('n_cols')
        _dtype = request.args.get('dtype')

        _species = request.args.get('species')

        _phenotype_datatype = request.args.get('phenotype_datatype')
        _significance_level_d = request.args.get('significance_level_d')
        _use_parametric = request.args.get('use_parametric')
        _apply_fdr_d = request.args.get('apply_fdr_d')
        _fdr_rate_d = request.args.get('fdr_rate_d')

        _include_pathways = request.args.get('include_pathways')
        _include_ontologies = request.args.get('include_ontologies')
        _significance_level_e = request.args.get('significance_level_e')
        _apply_fdr_e = request.args.get('apply_fdr_e')
        _fdr_rate_e = request.args.get('fdr_rate_e')

        _apply_pathway_sz_filter = request.args.get('apply_pathway_sz_filter')
        _min_pathway_sz = request.args.get('min_pathway_sz')
        _display_headers = request.args.get('display_headers')

        status = enrichment_analysis(
            _request_id, _n_rows, _n_cols, _dtype, _species,
            _phenotype_datatype, _significance_level_d, _use_parametric, _apply_fdr_d, _fdr_rate_d,
            _include_pathways, _include_ontologies, _significance_level_e, _apply_fdr_e, _fdr_rate_e,
            _apply_pathway_sz_filter, _min_pathway_sz, app.config['data_pool'], _display_headers
        )
        if status == 1:
            resp = StandardResponse(1, message='', detailed_message='')
        else:
            resp = StandardResponse(0, message='Error in do_enrichment_analysis()', detailed_message='Unknown origin')
        return resp.as_json()

    except Exception:
        resp = StandardResponse(0, message='Error in do_enrichment_analysis()', detailed_message=traceback.format_exc())
        return resp.as_json()


if __name__ == "__main__":
    # app.run()
    serve(app, port=5000)
