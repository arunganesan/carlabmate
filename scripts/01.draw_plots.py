#! /usr/bin/env python3

from library import *
from networkx.algorithms import bipartite
from pprint import pprint
from tqdm import tqdm

import matplotlib.pyplot as plt
import networkx as nx
import numpy as np


odir = 'images'

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
        font_family='serif',
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
    plt.text(
        info_x, max_y, 'Information', 
        fontsize='large', family='serif', fontweight='bold', 
        ha='center')
    plt.text(
        impl_x, max_y, 'Implementation', 
        fontsize='large', family='serif', fontweight='bold', 
        ha='center')

    ax.set_aspect('equal')
    print(ax.set_xlim(info_x - 2, impl_x + 2))
    plt.draw()
    plt.savefig(filename)

def solve_graph (required=[], devices=[], blacklist_info=[], exclusive={}, limit_rounds=np.inf):
    # exclusive[info] = implementation. 
    #   this means IF we need that requirement to solve this graph
    #   then we force use that implementation over others 
    
    """
    Our goal is to find the smallest sub-graph subject to:
        * Required: Must contain required information nodes
        * Blacklisted: We may have restrictions on which nodes are available (blacklist some information, blacklist some devices) but none of those are required
        * Conditional exclusion: If a certain information is required, we may have a specific implementation that we require
    """

    # still it's a bit tricky how we pick an implementation given 
    # for now, I think we can just pick all possible implementations

    # information
    # implementations
    # return a set of nodes/edges
    all_nodes = []

    required_info = []
    required_impl = []

    new_required_info = [i for i in required if i not in blacklist_info]
    new_required_impl = []

    round = 1

    while (len(new_required_info) > 0 or len(new_required_impl) > 0):
        _next_required_impl = new_required_impl[:]
        _next_required_info = new_required_info[:]
        required_info += new_required_info[:]
        required_impl += new_required_impl[:]
        new_required_impl = []
        new_required_info = []

        for info in _next_required_info:
            if info in exclusive:
                impl = exclusive[info]
                if any([d in devices for d in impl.devices]):
                    new_required_impl.append(impl)
            else:
                for impl in info.implemented_by:
                    if impl not in required_impl and impl not in new_required_impl:
                        if any([d in devices for d in impl.devices]):
                            new_required_impl.append(impl)
                        else:
                            # none of the devices are available
                            continue

            
        for impl in _next_required_impl:
            for info in impl.requires:
                if info not in required_info and info not in new_required_info:
                    if info not in blacklist_info:
                        new_required_info.append(info)

        round += 1
        if round > limit_rounds:
            break
    
    print(required_info, required_impl)
    
    # initialize set of information
    # get all implementations for information set
    # get their required information
    # loop until we don't add any more to info or impl sets

    return [n.node() for n in required_info + required_impl]

if __name__ == '__main__':
    
    # list of information we need
    #   car related sensors
    #   location.
    required_information = [
        indexed_information['car/speed'],
        indexed_information['car/odometer'],
        indexed_information['car/fuel'],
        indexed_information['car/rpm'],
        indexed_information['car/steering'],
        indexed_information['car/gear'],
        indexed_information['location'],
    ]


    android = 'android'
    openxc = 'openxc'
    obd = 'obd'
    

    
    # selected_nodes = solve_graph(required=[indexed_information['car/fuel'], 'car/...'])
    # question: Am I dangerous driver?
    for round in tqdm(list(range(1, 10)), 'Num rounds'):
        selected_nodes = solve_graph(
            required=required_information, 
            devices=[android, openxc, obd], 
            limit_rounds=round)
        #draw_network(selected_nodes, '{}/network-{:02d}.png'.format(odir, round))
        
    
    # i have all devices (openxc, obd, phone, etc) -- highlight that implementation
    """
    draw_network(solve_graph(
                    required=required_information, 
                    devices=[android, openxc, obd]),
                '{}/devices-all-devices.png'.format(odir))

    draw_network(solve_graph(
                    required=required_information, 
                    devices=[android]),
                '{}/devices-only-phone.png'.format(odir))
    
    draw_network(solve_graph(
                    required=required_information, 
                    devices=[android, obd]),
                '{}/devices-phone-and-obd.png'.format(odir))
    
    draw_network(solve_graph(
                    required=required_information, 
                    devices=[obd]),
                '{}/devices-only-obd.png'.format(odir))
    """ 


    draw_network(solve_graph(
                    required=required_information, 
                    blacklist_info=[indexed_information['location']],
                    devices=[android]),
                '{}/blacklist-location.png'.format(odir))

    draw_network(solve_graph(
                    required=required_information, 
                    blacklist_info=[],
                    exclusive={
                        indexed_information['location']: indexed_implementation['react-native/dummy']
                    },
                    devices=[android]),
                '{}/blacklist-dummy-location.png'.format(odir))
    
    #   - I only have phone -- blacklist non-phone implementation
    #   - I only have OBD and phone
    
    # suppose i don't want to share location
    #   show what happens if you DO share location + dummy location
    
