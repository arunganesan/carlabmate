#! /usr/bin/env python3
import argparse
from libcarlab import AlgorithmFunction, Algorithm, Information, LinkGatewayService, Fall
import fall_detect
import json
import time
from typing import List, Dict
import os, pickle

"""
This is the wrapper script 

It is responsible for just a few things:

    0. Initialize and give life to algorithms
    1. Multiplex data around
    2. IO from gateway server
"""

# per algorithm stuff

alg = fall_detect.algorithm.FallDetect()

loaded_functions: List[AlgorithmFunction] = [
    alg.produce_fall_function
]

to_save_information: List[Information] = [
    Fall
]

LOCALFILE = 'local.db'
USERID = 12

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--source', choices=['fixed', 'random'])
    args = parser.parse_args()
    
    running_algorithms: List[Algorithm] = []
    for func in loaded_functions:
        if func.belongsto not in running_algorithms:
            running_algorithms.append(func.belongsto)

    multiplex_routing: Dict[Information, List[Algorithm]] = {}

    for func in loaded_functions:
        # instantiate all classes
        alg = func.belongsto()
        running_algorithms.append(alg)

        # set up multiplexing
        for info in func.inputinfo:
            multiplex_routing.setdefault(info, [])
            multiplex_routing[info].append(alg)
        
    
    # Loop through
    infonames = [info.name for info in running_algorithms.keys()]
    gateway = LinkGatewayService(
        USERID,
        infonames,
        [], 
        LOCALFILE,
        False,
    )

    storage = {}
    if os.path.exists(LOCALFILE):
        storage = pickle.load(open(LOCALFILE, 'rb'))
    for name in infonames:
        storage.setdefault(name, None)
    
    while True:
        for info, value in gateway.check_new_info().items():
            storage[info] = json.loads(value)

        for info, values in storage.items():
            if info in multiplex_routing:
                output_values = multiplex_routing[info].add_new_data(values)
                for output in output_values:
                    if output is None: continue
                    storage.setdefault(output.info, [])
                    storage[output.info] = output.value
        
                # TODO need to throw it away once consumed
        
        for info, value in storage.items():
            gateway.output_new_info(info, value)
        
        time.sleep(10)

if __name__ == '__main__':
    main()
