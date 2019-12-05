#! /usr/bin/env python3.7
from jsmin import jsmin
from pprint import pprint
from typing import *
from termcolor import cprint

from itertools import *
# from itertools import powerset
import argparse
import json
import numpy as np
import os
from tqdm import tqdm
import subprocess

SPECS = 'specs.jsonc'

def powerset(iterable):
    "powerset([1,2,3]) --> () (1,) (2,) (3,) (1,2) (1,3) (2,3) (1,2,3)"
    s = list(iterable)
    return chain.from_iterable(combinations(s, r) for r in range(len(s)+1))


def main():
    specs = json.loads(jsmin(open(SPECS, 'r').read()))
    list_of_functions = []
    for alg, details in specs.items():
        for function in details['functions'].keys():
            list_of_functions.append('{}/{}'.format(alg, function))


    per_algorithm_deps = {}
    for algfn in tqdm(list_of_functions, "function"):
        ofile = open('tmpreq.jsonc', 'w')
        json.dump({
            'required': [], 'platforms': ['android', 'python', 'react'], 'exclude': [], 
            'algorithms': [algfn],
            'choices': {}
        }, ofile)
        ofile.close()

        proc = subprocess.Popen(['./cl-strategy.py', 'tmpreq.jsonc'], stdout=subprocess.PIPE)
        dependencies = json.loads(proc.communicate()[0])
        per_algorithm_deps[algfn] = [
            '{}/{}'.format(dep['algorithm'], dep['function'])
            for dep in dependencies
        ]
        print(algfn, per_algorithm_deps[algfn])
    
    unique_apps = []
    allcombos = list(powerset(list_of_functions))
    for funcset in tqdm(allcombos):
        required_apps = []
        for f in funcset:
            required_apps += per_algorithm_deps[f]
        appstr = '&'.join(required_apps)
        if not appstr in unique_apps:
            unique_apps.append(appstr)
    
    print(len(allcombos))
    print(len(unique_apps))


    # print(list_of_functions)
    # {
    #     "required": [],
    #     "platforms": [ "android", "python", "react" ],
    #     "exclude": [],
    #     "algorithms": ["obstacle-warning-react/acceptSightingReport"],
    #     "choices": {}
    # }


if __name__ == '__main__':
    main()