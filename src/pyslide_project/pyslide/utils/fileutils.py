import os
import numpy as np
import pandas as pd


def read_file(fileid, n_rows, n_cols, dtype):
    with open(fileid, "rb") as f:
        b = f.read()
        a_flat = np.frombuffer(b, dtype=map_java_dtype(dtype))
    a = a_flat.reshape((n_rows, n_cols))
    return a


def save_file(fileid, a):
    b = a.flatten().tobytes()
    with open(fileid, "wb") as f:
        f.write(b)


def read_delimited_file(file_id, n_rows, n_cols):
    df = pd.read_table(file_id, header=None)
    if df.shape[0] != n_rows and df.shape[1] != n_cols:
        raise Exception('File data shape not as expected')
    return df


def read_spark_file(path_to_data_pool, request_id, n_rows=-1, n_cols=-1, validate_shape=False):
    _p = os.path.join(path_to_data_pool, request_id)
    _src_file = [os.path.join(_p, f) for f in os.listdir(_p) if f.endswith(".csv")][0]
    df = pd.read_table(_src_file, header=0)
    if validate_shape:
        if df.shape[0] != n_rows and df.shape[1] != n_cols:
            raise Exception('File data shape not as expected')
    return df


def save_delimited_file(file_id, data):
    df = pd.DataFrame(data=data)
    df.to_csv(file_id, sep="\t")


def map_java_dtype(dtype):
    dtypes = {'byte': np.int8,
              'short': np.int16,
              'int': np.int32,
              'long': np.int64,
              'uint8': np.uint8,
              'uint16': np.uint16,
              'uint32': np.uint32,
              'uint64': np.uint64,
              'float': np.float32,
              'double': np.float64,
              'char': np.uint16}

    if dtype not in dtypes:
        raise Exception('File dtype not understood')
    else:
        return dtypes[dtype]


def _bool(s):
    if s.lower() == 'true':
        return True
    else:
        return False
