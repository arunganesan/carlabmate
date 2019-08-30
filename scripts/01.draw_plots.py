#! /usr/bin/env python3

from library import *
from networkx.algorithms import bipartite
from pprint import pprint

import matplotlib.pyplot as plt
import networkx as nx
import numpy as np


def main():
    # create bipartite graph
    dg = nx.DiGraph()
    for info in information:
        dg.add_node(('info', info), type='information')
    
    for impl in implementations:
        dg.add_node(('impl', impl.name), type='implementation')
        dg.add_edge(('impl', impl.name), ('info', impl.implements))
        for info in impl.requires:
            dg.add_edge(('info', info), ('impl', impl.name))

    assert bipartite.is_bipartite(dg), 'Not a bipartite graph'
    attributes = nx.get_node_attributes(dg, 'type')
    nodes = dg.nodes()

    min_y, max_y = 0, 5
    info_x, impl_x = 0, 2

    info_y = 0
    impl_y = 0
    info_ystep = (max_y - min_y) / len(information)
    impl_ystep = (max_y - min_y) / len(implementations)

    plt.figure(figsize=(10,10))

    positioning = {}
    for n in nodes:
        if attributes[n] == 'information':
            positioning[n] = np.array([info_x, info_y])
            info_y += info_ystep
        else:
            positioning[n] = np.array([impl_x, impl_y])
            impl_y += impl_ystep
    
    # shells = []
    # shells.append([n for n in dg.nodes() if attributes[n] == 'information']) 
    # shells.append([n for n in dg.nodes() if attributes[n] == 'implementation']) 
    # positioning = nx.shell_layout(dg, shells)
    pprint(positioning)
    
    colors = [0 if attributes[n] == 'information' else 1 for n in nodes]
    nx.draw_networkx(dg, pos=positioning, node_color=colors, with_labels=False, node_size=75)
    
    labels = {}
    label_positioning = {}
    for n in nodes:
        p = positioning[n]
        if attributes[n] == 'information':
            label_positioning[n] = [p[0]-0.35, p[1]]
            labels[n] = n[1]
        else:
            label_positioning[n] = [p[0]+0.5, p[1]]
            labels[n] = n[1]

    nx.draw_networkx_labels(
        dg, 
        pos=label_positioning, 
        font_size=8, 
        labels=labels)
    
    ax = plt.gca()
    ax.set_aspect('equal')
    print(ax.set_xlim(info_x - 2, impl_x + 2))
    plt.draw()
    plt.savefig('network.png')
    

if __name__ == '__main__':
    main()
