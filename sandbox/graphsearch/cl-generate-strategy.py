#! /usr/bin/env python3.7

from jsmin import jsmin
from pprint import pprint
from typing import List

import json
import networkx

REGISTRY = 'registry.jsonc'
SPECS = 'specs.jsonc'
REQUIREMENTS = 'requirements.jsonc'

class Information ():
    def __init__ (self, name):
        self.name = name
        self.implemented_by = []

    def node (self):
        return ('info', self.name)

class Algorithm ():
    def __init__ (self, 
        name: str, 
        inputinfo: List[Information], 
        outputinfo: Information, 
        usesinfo: List[Information]
    ):
        self.name = name
        self.outputinfo = outputinfo
        indexed_information[implements].implemented_by.append(self)

        if type(devices) == list:
            self.devices = devices
        else:
            self.devices = [devices]
        
        if type(requires) == list:
            self.requires = [indexed_information[r] for r in requires]
        else:
            self.requires = [indexed_information[requires]]
        
    def node (self):
        return ('impl', self.name)

def main():
    registry = json.loads(jsmin(open(REGISTRY, 'r').read()))
    specs = json.loads(jsmin(open(SPECS, 'r').read()))
    requirements = json.loads(jsmin(open(REQUIREMENTS, 'r').read()))

    pprint(registry)

    # 1. Create dependency graph out of registry (aka informations) and specs (aka algorithms)
    # 2. Use requirements to try and find a suitable algorithm




if __name__ == '__main__':
    main()