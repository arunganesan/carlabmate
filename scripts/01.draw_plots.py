#! /usr/bin/env python3

from library import *
from networkx.algorithms import bipartite
from pprint import pprint

import matplotlib.pyplot as plt
import networkx as nx
import numpy as np

min_y, max_y = 0, 5
info_x, impl_x = 0, 2

def create_digraph():
    dg = nx.DiGraph()
    for info in information:
        dg.add_node(info.node(), type='information')
   
    for impl in implementations:
        dg.add_node(impl.node(), type='implementation')
        dg.add_edge(impl.node(), impl.implements.node())
        for info in impl.requires:
            dg.add_edge(info.node(), impl.node())
    
    assert bipartite.is_bipartite(dg), 'Not a bipartite graph'
    return dg

def position_labels(dg, positioning):
    attributes = nx.get_node_attributes(dg, 'type')
    label_positioning = {}
    for n in dg.nodes():
        p = positioning[n]
        if attributes[n] == 'information':
            label_positioning[n] = [p[0]-0.35, p[1]]
        else:
            label_positioning[n] = [p[0]+0.5, p[1]]

    return label_positioning

def position_nodes(dg):
    attributes = nx.get_node_attributes(dg, 'type')

    info_y = 0; impl_y = 0
    info_ystep = (max_y - min_y) / len(information)
    impl_ystep = (max_y - min_y) / len(implementations)

    plt.figure(figsize=(10,10))

    positioning = {}
    for n in dg.nodes():
        if attributes[n] == 'information':
            positioning[n] = np.array([info_x, info_y])
            info_y += info_ystep
        else:
            positioning[n] = np.array([impl_x, impl_y])
            impl_y += impl_ystep
    
    return positioning


def collect_nodes (dg, selected_nodes):
    attributes = nx.get_node_attributes(dg, 'type')

    highlighted = {
        'nodes': [],
        'edges': []
    }
    
    remaining = {
        'nodes': [],
        'edges': [],
    }
    
    for n in dg.nodes():
        if n in selected_nodes:
            highlighted['nodes'].append(n)
        else:
            remaining['nodes'].append(n)

    for n0, n1 in dg.edges():
        if n0 in selected_nodes and n1 in selected_nodes:
            highlighted['edges'].append((n0, n1))
        else:
            remaining['edges'].append((n0, n1))
    
    return highlighted, remaining

def _draw_helper (dg, positioning, label_positioning, subset, alpha):
    colors = []
    attributes = nx.get_node_attributes(dg, 'type')
    for n in dg.nodes():
        if n not in subset['nodes']:
            continue
        if attributes[n] == 'information':
            colors.append(0)
        else: # implementation
            colors.append(1)
    


    nx.draw_networkx(
        dg,
        # with_labels=True,
        pos=positioning, 
        nodelist=subset['nodes'],
        edgelist=subset['edges'],
        node_color=colors,
        alpha=alpha,
        with_labels=False,
        node_size=75,
        cmap=plt.get_cmap('seismic'))
    
    labels = {
        n: n[1] 
        for n in dg.nodes()
        if n in subset['nodes']
    }

    nx.draw_networkx_labels(
        dg, 
        pos=label_positioning, 
        font_size=8, 
        labels=labels)

def draw_network(selected_nodes, filename):
    # create bipartite graph
    dg = create_digraph()
    positioning = position_nodes(dg)
    label_positioning = position_labels(dg, positioning)
    
    # collect nodes and edges
    highlighted, remaining = collect_nodes(dg, selected_nodes)
    _draw_helper(
        dg, 
        positioning, label_positioning, 
        highlighted, 1)
    
    # remaining['edges'] = []
    print(remaining)
    _draw_helper(
        dg, 
        positioning, label_positioning, 
        remaining, 0.1)

    ax = plt.gca()
    ax.set_aspect('equal')
    print(ax.set_xlim(info_x - 2, impl_x + 2))
    plt.draw()
    plt.savefig(filename)

def solve_graph (required=[], blacklisted=[], exclusive={}):
    # exclusive[info] = implementation. 
    #   this means IF we need that requirement to solve this graph
    #   then we force use that implementation over others 
    """
    Our goal is to find the smallest sub-graph subject to:
        * Required: Must contain required information nodes
        * Blacklisted: We may have restrictions on which nodes are available (blacklist some information, blacklist some devices) but none of those are required
        * Conditional exclusion: If a certain information is required, we may have a specific implementation that we require
    """

    # information
    # implementations
    # return a set of nodes/edges

    return [n.node() for n in information[:5] + implementations[:5]]

if __name__ == '__main__':
    # solve graph --> get a list of nodes, some of which are highlighted and some which are not
    selected_nodes = solve_graph()
    draw_network(selected_nodes, 'network.png')
