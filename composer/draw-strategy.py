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

lowlevel = ['gps', 'obd-fuel', 'user-text', 'accel', 'magnetometer', 'gravity', 'gyro']

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('strategy')
    args = parser.parse_args()

    registry = json.loads(jsmin(open(REGISTRY, 'r').read()))
    specs = json.loads(jsmin(open(SPECS, 'r').read()))
    strategy = json.loads(jsmin(open(args.strategy, 'r').read()))

    all_nodes = {}
    output_used_by = {}

    for algfunc in strategy:
        algname = algfunc['algorithm']
        platform = specs[algname]['platform']
        fname = algfunc['function']
        nodename = '{}/{}'.format(algname, fname)

        expanded_func = {}
        funcspec = specs[algname]['functions'][fname]
        
        if 'output' in funcspec:
            expanded_func['output'] = funcspec['output']
        
        all_input = []

        if 'input' in funcspec:
            all_input += funcspec['input']
        
        if 'uses' in funcspec:
            all_input += funcspec['uses']
        
        for i in all_input:
            output_used_by.setdefault(i, [])
            output_used_by[i].append(nodename)

            if i in lowlevel:
                all_nodes[i] = {
                    'output': i
                }
        
        
        all_nodes[nodename] = expanded_func

    dot = Digraph(comment='....')
    
    for fname, _ in all_nodes.items():
        dot.node(fname, fname)
    
    for fname, fdetails in all_nodes.items():
        if 'output' in fdetails:
            outinfo = fdetails['output']
            if outinfo in output_used_by:
                for f2 in output_used_by[outinfo]:
                    dot.edge(fname, f2)
    
    basename = os.path.basename(args.strategy)
    basename, _ = os.path.splitext(basename)
    dot.render('{}/{}.gv'.format(ODIR, basename), view=True) 

if __name__ == '__main__':
    main()