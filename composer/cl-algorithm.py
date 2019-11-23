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
  return '1'



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