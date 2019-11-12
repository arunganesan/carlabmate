#! /usr/bin/env python3
from libcarlab import AlgorithmFunction, Algorithm, Information, LinkGatewayService, Fall
from termcolor import cprint

import argparse
import fall_detect
import json
import time
from typing import List, Dict
import os
import pickle

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
    alg.produce_fall_function,
    alg.check_user_is_ok_function,
]


to_save_information: List[Information] = [
    Fall
]

LOCALFILE = 'local.db'
USERID = 21


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
    # infonames = [info.name for alg in running_algorithms]
    # this is all the required info
    required_info: List[Information] = []
    for func in loaded_functions:
        required_info += func.inputinfo

    gateway = LinkGatewayService(
        USERID,
        required_info,
        [],
        LOCALFILE,
        False,
    )

    storage = {}
    if os.path.exists(LOCALFILE):
        storage = pickle.load(open(LOCALFILE, 'rb'))
    for info in required_info:
        storage.setdefault(info, None)

    while True:
        cprint('Running', 'green')
        for info, value in gateway.check_new_info().items():
            # cprint('\tReceived {} => {}'.format(
            #     info.name, 
            #     len(value)
            # ))
            storage[info] = value

        for info, values in storage.items():
            if info in multiplex_routing:
                for alg in multiplex_routing[info]:
                    output_values = alg.add_new_data(info, values)
                    for output in output_values:
                        if output is None:
                            continue
                        storage.setdefault(output.info, [])
                        storage[output.info] = output.value

                # TODO need to throw it away once consumed

        for info, value in storage.items():
            gateway.output_new_info(info, value)

        gateway.upload_data()

        time.sleep(1)


if __name__ == '__main__':
    main()
