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


class Information ():
    def __init__ (self, name):
        self.name = name
        self.implemented_by = []
        self.input_by = []
        self.used_by = []

    def node (self):
        return ('info', self.name)
    
    def __repr__ (self):
        return self.name

    
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
    
    def __repr__ (self):
        return self.name



COLOR_BY_PLATFORM = {
    'python': "#FFB8B5",
    'android': "#C8CC68",
    'react': '#9CD3FF'
}

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--strategy', default='strategy.jsonc')
    parser.add_argument('--requirements', default='requirements.jsonc')
    parser.add_argument('--draw', choices=['information', 'algorithms', 'both'], default='both')
    args = parser.parse_args()

    registry = json.loads(jsmin(open(REGISTRY, 'r').read()))
    specs = json.loads(jsmin(open(SPECS, 'r').read()))
    strategy = json.loads(jsmin(open(args.strategy, 'r').read()))
    reqs = json.loads(jsmin(open(args.requirements, 'r').read()))
    

    """
    draw both:
        for all input, create the node if it doesn't exist
        add node. add strategy. add all output node.
    
    draw only algorithms:
        an algorithm is connected to another if ITS OUTPUT is connected to any other INPUT
            * we can label the edges with the informations name
    
    draw only information:
        first go through and collect all input information
        infoA -> infoB if A was input to an algorithm and B was it's output
            * we can label the edges with the algorithm's name
    """

    expanded_strategy = {}

    indexed_information = {}
    indexed_algorithm = {}
    required_sensor_names = reqs['required']
    reqs.setdefault('algorithms', [])
    required_function_names = reqs['algorithms']
    
    def make_or_get_info (name):
        indexed_information.setdefault(name, Information(name))
        return indexed_information[name]

    for algfunc in strategy:

        algname = algfunc['algorithm']
        platform = specs[algname]['platform']
        fname = algfunc['function']
        funcspec = specs[algname]['functions'][fname]
        nodename = '{}/{}'.format(algname, fname)

        outnode = make_or_get_info(funcspec['output'])
        
        inputnodes = [] if not 'input' in funcspec else [make_or_get_info(i) for i in funcspec['input']]
        usesnodes = [] if not('uses' in funcspec) else [make_or_get_info(i) for i in funcspec['uses']]

        alg = Algorithm(
            name=nodename,
            outputinfo=outnode,
            inputinfo=inputnodes,
            usesinfo=usesnodes,
            platform=platform
        )

        outnode.implemented_by.append(alg)
        for i in usesnodes:
            i.used_by.append(alg)

        for i in inputnodes:
            i.input_by.append(alg)
        
        indexed_algorithm[nodename] = alg

    dot = Digraph()

    if args.draw == 'both':
        dot.attr('node', shape='box', style='filled')
        for alg in indexed_algorithm.values():
            dot.attr('node', fillcolor=COLOR_BY_PLATFORM[alg.platform], penwidth='5' if alg.name in required_function_names else '1')
            dot.node(alg.name)
        
        dot.attr('node', shape='box', style='filled', fillcolor='lightgrey')
        for info in indexed_information.values():
            if info.name in required_sensor_names:
                dot.attr('node', pencolor='black', penwidth='5')
            else:
                dot.attr('node', pencolor='black',  penwidth='1')
            dot.node(info.name)
            

            for nn in info.used_by:
                dot.attr('edge', style='dashed')
                dot.edge(info.name, nn.name)

            for nn in info.input_by:
                dot.attr('edge', style='solid')
                dot.edge(info.name, nn.name)
            
            for nn in info.implemented_by:
                dot.attr('edge', style='solid')
                dot.edge(nn.name, info.name)
    
    elif args.draw == 'information':
        dot.attr('node', shape='box', style='filled', fillcolor='lightgrey')
        for info in indexed_information.values():
            if info.name in required_sensor_names:
                dot.attr('node', pencolor='red', penwidth='5')
            else:
                dot.attr('node', pencolor='black',  penwidth='1')
            dot.node(info.name)

        for alg in indexed_algorithm.values():
            dot.attr('edge', color=COLOR_BY_PLATFORM[alg.platform], penwidth='3')
            for ifrom in alg.inputinfo + alg.usesinfo:
                dot.edge(ifrom.name, alg.outputinfo.name, label=alg.name)

    elif args.draw == 'algorithms':
        dot.attr('node', shape='box', style='filled')
        for alg in indexed_algorithm.values():
            dot.attr('node', fillcolor=COLOR_BY_PLATFORM[alg.platform])
            dot.node(alg.name)
        
        for alg in indexed_algorithm.values():
            for a2 in alg.outputinfo.used_by + alg.outputinfo.input_by:
                dot.edge(alg.name, a2.name, label=alg.outputinfo.name)

    # else if args.draw == 'both':

    basename = os.path.basename(args.strategy)
    basename, _ = os.path.splitext(basename)
    dot.format = 'png'
    dot.render('{}/{}-{}.gv'.format(ODIR, basename, args.draw), view=True) 


if __name__ == '__main__':
    main()
