#! /usr/bin/env python3.7

from jsmin import jsmin
from pprint import pprint
from typing import *
from termcolor import cprint

import argparse
import json
import numpy as np
import os
# import graphviz
from graphviz import Digraph

REGISTRY = 'registry.jsonc'
SPECS = 'specs.jsonc'


ODIR = 'images'
if not os.path.exists(ODIR):
    os.makedirs(ODIR)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('strategy')
    args = parser.parse_args()

    registry = json.loads(jsmin(open(REGISTRY, 'r').read()))
    specs = json.loads(jsmin(open(SPECS, 'r').read()))
    strategy = json.loads(jsmin(open(args.strategy, 'r').read()))

    all_nodes = {}
    for algfunc in strategy:
        algname = algfunc['algorithm']
        platform = specs[algname]['platform']
        fname = algfunc['function']

        expanded_func = {}
        funcspec = specs[algname]['functions'][fname]
        
        if 'output' in funcspec:
            expanded_func['output'] = {funcspec['output']: registry[funcspec['output']]}
        
        if 'input' in funcspec:
            expanded_func['input'] = {
                    i: registry[i] for i in funcspec['input']}
        
        all_nodes['{}/{}'.format(algname, fname)] = expanded_func
    
    
    # 1. create the nodes
    # 2. connect the nodes
    # 3. done LOL

    dot = Digraph(comment='The Round Table')

    for fname, _ in all_nodes.items():
        dot.node(fname, fname)
    
    
    for _f in all_nodes.keys():
        for _f2 in all_nodes.keys():
            dot.edge(_f, _f2)
    
    dot.render('{}/round-table.gv'.format(ODIR), view=False) 

    
    
if __name__ == '__main__':
    main()