#! /usr/bin/env python3.7
import matplotlib
matplotlib.use('Agg')

from jsmin import jsmin
from pprint import pprint
from typing import *
from termcolor import cprint

import argparse
import json
import matplotlib.pyplot as plt
import networkx as nx
import numpy as np
import os

REGISTRY = 'registry.jsonc'
SPECS = 'specs.jsonc'


ODIR = 'images'
if not os.path.exists(ODIR):
    os.makedirs(ODIR)

min_y, max_y = 0, 5
info_x, impl_x = 0, 2



class Information ():
    def __init__ (self, name):
        self.name = name
        self.implemented_by = []

    def node (self):
        return ('info', self.name)

class Algorithm ():
    name: str
    outputinfo: Information
    inputinfo: List[Information]
    usesinfo: List[Information]
    platform: str
    
    def __init__ (self, 
        name: str, 
        outputinfo: Information, 
        inputinfo: List[Information], 
        usesinfo: List[Information],
        platform: str
    ):
        self.name = name
        self.outputinfo = outputinfo
        self.inputinfo = inputinfo
        self.usesinfo = usesinfo
        self.platform = platform
        self.requires = self.inputinfo + self.usesinfo

    def node (self):
        return ('impl', self.name)


def create_digraph(information, algorithms):
    dg = nx.DiGraph()
    for info in information:
        dg.add_node(info.node(), type='information')
   
    for impl in algorithms:
        dg.add_node(impl.node(), type='implementation')
        dg.add_edge(impl.node(), impl.outputinfo.node())
        for info in impl.requires:
            dg.add_edge(info.node(), impl.node())
    
    return dg



def position_labels(dg, positioning):
    attributes = nx.get_node_attributes(dg, 'type')
    label_positioning = {}
    for n in dg.nodes():
        p = positioning[n]
        if attributes[n] == 'information':
            label_positioning[n] = [p[0]-0.40, p[1]]
        else:
            label_positioning[n] = [p[0]+0.80, p[1]]
    return label_positioning


def position_nodes(dg, information, algorithms):
    attributes = nx.get_node_attributes(dg, 'type')

    info_y = 0; impl_y = 0
    info_ystep = (max_y - min_y) / len(information)
    impl_ystep = (max_y - min_y) / len(algorithms)

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


def solve_graph (
    required:List[Information]=[], 
    platforms:List[str]=[], 
    blacklist_info:List[Information]=[], 
    exclusive: Dict[Information, Algorithm]={}, 
    limit_rounds=np.inf):
    
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
                if impl.platform in platforms:
                    new_required_impl.append(impl)
            else:
                for impl in info.implemented_by:
                    if impl not in required_impl and impl not in new_required_impl:
                        if impl.platform in platforms:
                            new_required_impl.append(impl)

            
        for impl in _next_required_impl:
            for info in impl.requires:
                if info not in required_info and info not in new_required_info:
                    if info not in blacklist_info:
                        new_required_info.append(info)

        round += 1
        if round > limit_rounds:
            break
    
    return [n.node() for n in required_info + required_impl]


def draw_network(selected_nodes, filename, information, algorithms):
    # create bipartite graph
    dg = create_digraph(information, algorithms)
    positioning = position_nodes(dg, information, algorithms)
    label_positioning = position_labels(dg, positioning)
    
    for n in dg.nodes():
        print(n, list(dg.neighbors(n)))

    # collect nodes and edges
    highlighted, remaining = collect_nodes(dg, selected_nodes)
    _draw_helper(
        dg, 
        positioning, label_positioning, 
        highlighted, 1)
    
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
        impl_x, max_y, 'Algorithm', 
        fontsize='large', family='serif', fontweight='bold', 
        ha='center')

    ax.set_aspect('equal')
    print(ax.set_xlim(info_x - 1.5, impl_x + 2))
    plt.draw()
    plt.savefig(filename)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--plot', action='store_true')
    parser.add_argument('requirements')
    args = parser.parse_args()

    registry = json.loads(jsmin(open(REGISTRY, 'r').read()))
    specs = json.loads(jsmin(open(SPECS, 'r').read()))
    requirements = json.loads(jsmin(open(args.requirements, 'r').read()))

    # 1. Create dependency graph out of registry (aka informations) and specs (aka algorithms)
    indexed_information: Dict[str, Information] = {
        infoname: Information(infoname)
        for infoname in registry.keys()
    }


    indexed_functions = {}

    for algname, algdetails in specs.items():
        for funcname, funcdetails in algdetails['functions'].items():
            algfn = '{}/{}'.format(algname, funcname)
            outputinfo = indexed_information[funcdetails['output']]
            
            inputinfos = []
            if 'input' in funcdetails:
                for infoname in funcdetails['input']:
                    inputinfos.append(indexed_information[infoname])

            usesinfos = []
            if 'uses' in funcdetails:
                for infoname in funcdetails['uses']:
                    usesinfos.append(indexed_information[infoname])
            
            alg = Algorithm(
                name=algfn,
                outputinfo=outputinfo,
                inputinfo=inputinfos,
                usesinfo=usesinfos,
                platform=algdetails['platform']
            )

            indexed_functions[algfn] = alg
            outputinfo.implemented_by.append(alg)

    # 2. Use requirements to try and find a suitable algorithm
    required_information = []
    for infoname in requirements['required information']:
        required_information.append(indexed_information[infoname])
    
    platforms = requirements['available platforms']

    blacklisted_information = []
    for infoname in requirements['exclude information']:
        blacklisted_information.append(indexed_information[infoname])

    selected_nodes = solve_graph(required_information, platforms, blacklisted_information)
    strategy = []
    for node in selected_nodes:
        if node[0] == 'impl':
            alg, func = node[1].split('/')
            strategy.append({ 'algorithm': alg, 'function': func})
    print(json.dumps(strategy))
        
    if args.plot:
        draw_network(
            selected_nodes, 
            '{}/full-graph.png'.format(ODIR), 
            indexed_information.values(), 
            indexed_functions.values())



if __name__ == '__main__':
    main()