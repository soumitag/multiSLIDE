try:

    import fastcluster as fc
    import numpy as np
    import scipy as sp
    import sys
    import os
    import scipy.cluster.hierarchy as hac

    data_folderpath = str(sys.argv[1])
    infilename = str(sys.argv[2])
    linkage_startegy = str(sys.argv[3])
    distance_metric = str(sys.argv[4])
    process_id = str(sys.argv[5])
    row_col_type = str(sys.argv[6])

    print ("Loading Data...")
    data = np.loadtxt(os.path.join(data_folderpath, infilename), dtype=float, delimiter='\t')
    print ("Done.")

    print ("Creating Distance Matrix...")
    D = sp.spatial.distance.pdist (data, distance_metric)
    print ("Done.")

    print ("Clustering...")
    Z = fc.linkage(D, method=linkage_startegy, metric=distance_metric, preserve_input=False)
    print ("Done.")

    print ("Saving To File...")
    np.savetxt(os.path.join(data_folderpath, row_col_type + '_ClusteringOutput_' + process_id + '.txt'), Z, fmt='%.3f')
    print ("Done.")

    print ("Process Completed")

except Exception as e:

    logf = open(os.path.join(data_folderpath, row_col_type + '_ClusteringError_' + process_id + '.txt'), "w")
    logf.write(str(e))