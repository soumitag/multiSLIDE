import numpy as np
import matplotlib.pyplot as plt
import tifffile as tiff
import os

def gen_colordict(cmap_name, nBins):
    cmap = plt.cm.get_cmap(cmap_name)
    step_sz = 1/nBins
    bin_centres = np.arange(0,1.0001,step_sz)
    # print(bin_centres)
    colors = [cmap(c) for c in bin_centres]
    rgb = [[int(c[0]*255), int(c[1]*255), int(c[2]*255)] for c in colors]
    # print(rgb)
    return rgb

def load_colordict(cmap_name):
    filepath = os.path.join("../../db/py_colormaps/", cmap_name + ".txt")

    with open(filepath,'r') as f:
        for i in range(252):
            line = f.readline()
        line = f.readline()

    rgbs = line.split(";")
    colors = np.zeros((255,3), dtype=int)
    for index,rgb in enumerate(rgbs):
        colors[index] = rgb.split(",")

    return colors

def gen_maps1():
    mapnames = ['PiYG', 'PRGn', 'BrBG', 'RdBu',
                'RdYlBu', 'RdYlGn', 'Spectral', 'coolwarm', 'bwr', 'seismic', 
                'viridis', 'plasma', 'inferno', 'magma', 'cividis']
                
    for mapname in mapnames:
        colorband_size = 1
        image = np.zeros((15,255*colorband_size,3), dtype=np.int8)
        rgb_values = gen_colordict(mapname, 255)
        for nColor in range(255):
            image[:,nColor*colorband_size:(nColor+1)*colorband_size,0] = rgb_values[nColor][0]
            image[:,nColor*colorband_size:(nColor+1)*colorband_size,1] = rgb_values[nColor][1]
            image[:,nColor*colorband_size:(nColor+1)*colorband_size,2] = rgb_values[nColor][2]
        tiff.imsave(os.path.join('colormap_images',mapname.upper()+'.tiff'), image)
    
def gen_maps2():

    mapnames = ['BBKY', 'BLWR', 'BYR', 'GBR']

    for mapname in mapnames:
        colorband_size = 1
        image = np.zeros((15,255*colorband_size,3), dtype=np.int8)
        rgb_values = load_colordict(mapname.upper())
        for nColor in range(255):
            image[:,nColor*colorband_size:(nColor+1)*colorband_size,0] = rgb_values[nColor][0]
            image[:,nColor*colorband_size:(nColor+1)*colorband_size,1] = rgb_values[nColor][1]
            image[:,nColor*colorband_size:(nColor+1)*colorband_size,2] = rgb_values[nColor][2]
        tiff.imsave(mapname.upper()+'.tiff', image)

if __name__ == "__main__":
    
    # gen_maps1()
    gen_maps2()
