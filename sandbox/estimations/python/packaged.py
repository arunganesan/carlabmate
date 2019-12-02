#! /usr/bin/env python3.7
from libcarlab.libcarlab import AlgorithmFunction, Algorithm, Information, LinkGatewayService, Registry, DataMarshal
from termcolor import cprint

import argparse
import json
import os
import pickle
import time
from typing import List, Dict

import tensorflow_models

_tmp_tensorflow_models = tensorflow_models.algorithm.AlgorithmImpl(None)
loaded_functions: List[AlgorithmFunction] = [
	_tmp_tensorflow_models.get_gear_model_file_function
]

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--session', default='6068c667d402578ea20a31a809667d98')
    args = parser.parse_args()
    
    LOCALFILE = '{}.db'.format(args.session)

    running_algorithms: List[Algorithm] = []
    for func in loaded_functions:
        if func.belongsto not in running_algorithms:
            running_algorithms.append(func.belongsto)

    multiplex_routing: Dict[Information, List[Algorithm]] = {}

    for func in loaded_functions:
      # Loop through
      # this is all the required info
      required_info: List[Information] = []
      state_refers_info: List[Information] = []
      for func in loaded_functions:
          required_info += func.inputinfo
          required_info += func.usesinfo
          state_refers_info += func.usesinfo
    
    # which ones to output are specified in the spec
    gateway = LinkGatewayService(
        args.session,
        required_info,
        state_refers_info,
        [Registry.GearModelFile], # output info
        LOCALFILE,
        False,
    )

    for func in loaded_functions:
        # instantiate all classes
        alg = func.belongsto(gateway)
        running_algorithms.append(alg)

        # set up multiplexing
        for info in func.inputinfo:
            multiplex_routing.setdefault(info, [])
            multiplex_routing[info].append(alg)
        
        for info in func.usesinfo:
            multiplex_routing.setdefault(info, [])
            multiplex_routing[info].append(alg)

   
    storage = {}
    if os.path.exists(LOCALFILE):
        storage = pickle.load(open(LOCALFILE, 'rb'))
    for info in required_info:
        storage.setdefault(info, None)

    for info, value in gateway.initialize_state().items():
        storage[info] = value

    while True:
        new_storage = {}
        for info, values in storage.items():
            if info in multiplex_routing:
                for alg in multiplex_routing[info]:
                    if type(value) is list and len(value) == 0:
                        continue
                    dm = DataMarshal(info, value)
                    output_values = alg.add_new_data(dm)
                    for output in output_values:
                        if output is None:
                            continue
                        new_storage.setdefault(output.info, [])
                        new_storage[output.info] = output.value

        for info, values in new_storage.items():
            storage.setdefault(info, [])
            if type(info) is list:
                storage[info] += values
            else:
                storage[info].append(values)
        
        for info, value in storage.items():
            gateway.output_new_info(info, value)

       
        gateway.upload_data()
        time.sleep(1)
        storage.clear()
        
        for info, value in gateway.check_new_info().items():
            storage[info] = value
    
if __name__ == '__main__':
    main()
