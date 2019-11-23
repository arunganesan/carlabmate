#! /usr/bin/env python3.7
from jsmin import jsmin
from pprint import pprint
from typing import *
from termcolor import cprint

import argparse
import json
import os

REGISTRY = 'registry.jsonc'
SPECS = 'specs.jsonc'
ODIR = 'generated'
if not os.path.exists(ODIR):
    os.makedirs(ODIR)



def transform_variable_name(name):
    if name == 'gps':
        return 'GPS'

    parts = name.split('-')
    return ''.join([p.capitalize() for p in parts])



def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--algorithm', default='user-input')
    args = parser.parse_args()
    
    specs = json.loads(jsmin(open(SPECS, 'r').read()))
    registry = json.loads(jsmin(open(REGISTRY, 'r').read()))
    algdetails = specs[args.algorithm]

    platform = algdetails['platform']
    code = CODEGEN_PER_PLATFORM[platform](algdetails)
    ofile = open('{}/{}-stub.{}'.format(ODIR, platform, EXT[platform]), 'w')
    ofile.write(code)
    ofile.close()



def write_code_for_android (algdetails):
    return '1'


def write_code_for_react (algdetails):
    stubs = []
    for fname, fndetails in algdetails['functions'].items():
        stubs.append(REACT_STUB % fname)
    return REACT_TEMPLATE % '\n'.join(stubs)


def write_code_for_python (algdetails):
    function_definitions = []
    function_invocation = []
    function_stubs = []

    for fname, fndetails in algdetails['functions'].items():
        FnUses = [] if 'uses' not in fndetails else fndetails['uses']
        Output = transform_variable_name(fndetails['output'])
        Inputs = map(transform_variable_name, fndetails['input'])
        Uses = map(transform_variable_name, FnUses)
        function_definitions.append(PYTHON_FUNC_DEF % (
            fname, fname,
            Output,
            ', '.join(['Registry.{}'.format(i) for i in Inputs]),
            ', '.join(['Registry.{}'.format(i) for i in Uses])
        ))

        
        PyInputs = [i.replace('-', '_') for i in fndetails['input']]
        PyUses = [i.replace('-', '_') for i in FnUses]
    
        invoke = ['self.latest_values["%s"]' % i for i in fndetails['input'] + FnUses]
        function_invocation.append(PYTHON_FUNC_INVO % (
            fname, fname, fname, fname,
            invoke, Output
        ))


        function_stubs.append(PYTHON_FUNC_STUB % (
            fname, ', '.join(PyInputs + PyUses), Output
        ))


    return PYTHON_TEMPLATE % (
        '\n\n'.join(function_definitions),
        '\n\n'.join(function_invocation),
        '\n\n'.join(function_stubs)
    )



PYTHON_FUNC_INVO = """
    if self.%s_function.matches_required(dobj.info) and self.%s_function.have_received_all_required_data(self.latest_values.keys()):        
        if dobj.info in self.%s_function.inputinfo:
            retval = self.%s(%s)
            if retval is not None:
                return_values.append(DataMarshal(
                    Registry.%s,
                    retval
                ))
"""

PYTHON_FUNC_STUB = """
def %s (self, %s) -> Registry.%s.datatype:
    # Write code here
    return None
"""


PYTHON_FUNC_DEF = """
self.%s_function = AlgorithmFunction(
          "%s",
          AlgorithmImpl,
          Registry.%s, 
          [%s],
          [%s])
"""

PYTHON_TEMPLATE = """#! /usr/bin/env python3.7

from libcarlab.libcarlab import *
from termcolor import cprint
import os, json

class AlgorithmBase (Algorithm):
    def __init__ (self):
        self.latest_values = {}

%s       
    
    def add_new_data(self, dobj: DataMarshal) -> List[Union[DataMarshal, None]]:
        return_values = []

        self.latest_values[dobj.info] = dobj.value
        
        cprint('\tReceived information: {} = {}'.format(dobj.info, dobj.value), 'magenta')
        cprint('\tLatest value has keys: {}'.format(self.latest_values.keys()), 'blue')
        
%s

        return return_values


# Split into 2 files if it makes it cleaner 
class AlgorithmImpl (AlgorithmBase):
    def __init__ (self):
        super(AlgorithmImpl, self).__init__()

%s
"""



REACT_STUB = """
export class %s extends React.Component<Props, {}> {
  render() {
    const { update, produce, value } = this.props;
    /*
    Enter code here.
    */
    return null;
  }
}
"""



REACT_TEMPLATE = """import * as React from "react";
import { Button, Container, Form } from "react-bootstrap";
import "bootstrap/dist/css/bootstrap.css";

type Props = { 
  produce: Function,
  update: Function,
  value: any
};

%s
"""

EXT = {
  'react': 'tsx',
  'python': 'py',
  'android': 'java'
}

CODEGEN_PER_PLATFORM = {
  'react': write_code_for_react,
  'python': write_code_for_python,
  'android': write_code_for_android,
}



if __name__ == '__main__':
  main()