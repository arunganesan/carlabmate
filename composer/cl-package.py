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
    parser.add_argument('strategy')
    args = parser.parse_args()
    
    specs = json.loads(jsmin(open(SPECS, 'r').read()))
    registry = json.loads(jsmin(open(REGISTRY, 'r').read()))
    strategy = json.loads(jsmin(open(args.strategy, 'r').read()))
    # pprint(strategy)


    # collect each algorithm/function into the platfortm it belongs to
    # then ... like package them together.
    per_platform = {}
    for algfunc in strategy:
        algname = algfunc['algorithm']
        platform = specs[algname]['platform']

        expanded_func = {}
        funcspec = specs[algname]['functions'][algfunc['function']]
        if 'output' in funcspec:
            expanded_func['output'] = {funcspec['output']: registry[funcspec['output']]}
        
        if 'input' in funcspec:
            expanded_func['input'] = {
                    i: registry[i] for i in funcspec['input']}
        
        per_platform.setdefault(platform, {}) 
        per_platform[platform].setdefault(algname, {})
        per_platform[platform][algname][algfunc['function']] = expanded_func
    
    for platform, func_per_algorithms in per_platform.items():
        cprint(platform, 'green')
        # pprint(func_per_algorithms, depth=3, indent=2, width=20)
        code = PER_PLATFORM_CODEGEN[platform](func_per_algorithms)
        ofile = open('{}/{}.{}'.format(ODIR, platform, EXT[platform]), 'w')
        ofile.write(code)
        ofile.close()


        perform_per_platform_linking(platform, func_per_algorithms)

    # registry_entries = []
    # for infoname, details in registry.items():
    #     registry_entries.append(write_for_language(
    #         args.platform, infoname, details))

    # print(TEMPLATES[args.platform] % '\n    '.join(registry_entries))

def perform_per_platform_linking (platform, func_per_algorithms):
    """
    for react:
        we need to add the module to packages.json
        then an npm install should suffice
    """

def write_code_for_android (func_per_algorithms):
  return 'TODO android'


def write_code_for_python (func_per_algorithms):
  return 'TODO python'

def write_code_for_react (func_per_algorithms):
    import_calls = []
    inputs = []
    components = []


    for alg, functions in func_per_algorithms.items():
        for funcname, funcdetails in functions.items():
            Funcname = funcname[0].capitalize() + funcname[1:]
            Output = list(funcdetails['output'].keys())[0]
            Output = transform_variable_name(Output)

            import_calls.append(
                'import { %s as %s } from "%s";' 
                % (funcname, Funcname, alg))
            
            if 'input' in funcdetails:
                for inpinfo, inpdetails in funcdetails['input'].items():
                    # pprint(inpinfo)
                    inputs.append(transform_variable_name(inpinfo))
            

            components.append("""                 <%s
                    produce={(val: string) => {
                        this.libcarlab.outputNewInfo(
                            new DataMarshal(Registry.%s, val),
                            () => {}
                        );
                    }}
                />""" 
                % (Funcname, Output))

    code = REACT_PACKAGE_CODE % ('\n'.join(import_calls), ', '.join(inputs), ', \n'.join(components))
    return code



# In general the packaging has the strategy and all other shit.
# the strategy doesn't usually matter. That only really matters for Android and Python.
# for React, we don't multiplex data anyway. That is done through the props. We still do need to fetchNewData for the required information but we dont need to explicitly define algorithm functions is my point. IT IS DONE ENTIRELT STATICALLY WHERE AS FOR JHAVA ND PYTHON IT IS DONE STATIC+DYNAMICALLY. (IT COULD BE DONE ENTIRELY STATICALLY THERE TOO LOL)


EXT = {
  'react': 'tsx',
  'python': 'py',
  'android': 'java'
}

PER_PLATFORM_CODEGEN = {
  'react': write_code_for_react,
  'python': write_code_for_python,
  'android': write_code_for_android,
}

"""

1: imports
import { acceptFuelLevel as AcceptFuelLevel } from "user-input";
import { acceptPhoneNumber as AcceptPhoneNumber } from "user-input";

2: list of required input information names
e.g. "Registry.UserText, Registry.PhoneNumber"

3: actual components for each function 

    <AcceptFuelLevel
        produce={(fuelLevel: Number) => {
            this.libcarlab.outputNewInfo(
            new DataMarshal(Registry.CarFuel, fuelLevel), 
            () => {}
            );
        }}
        />,

    <AcceptPhoneNumber
        produce={(phoneNumber: Number) => {
            this.libcarlab.outputNewInfo(
            new DataMarshal(Registry.PhoneNumber, phoneNumber), 
            () => {}
            );
        }}
    />

"""





REACT_PACKAGE_CODE = """import React from "react";
import "./App.css";
import { Nav, NavItem, Navbar, Button } from "react-bootstrap";
import { Modal, Container, Row, Form, Col } from "react-bootstrap";
import { Libcarlab, Information, DataMarshal, Registry } from 'libcarlab';

import "bootstrap/dist/css/bootstrap.css";

%s

type AppState = {
  message: string;
  session: string | null;
  showLoginForm: boolean;
  required_info: Information[];
  username: string;
  password: string;
};

class App extends React.Component<{}, AppState> {
  libcarlab: Libcarlab;

  constructor(props: any) {
    super(props);

    let sessionString: string | null = window.localStorage.getItem(
      "localSession"
    );

    let sessionLocal = null;
    if (sessionString != null) sessionLocal = JSON.parse(sessionString);
    else
      sessionLocal = {
        session: null,
        username: ""
      };
    this.state = {
      message: "",
      required_info: [%s],
      showLoginForm: sessionLocal["session"] == null,
      session: sessionLocal["session"],
      username: sessionLocal["username"],
      password: ""
    };

    this.libcarlab = new Libcarlab(
      this.state.session,
      this.state.required_info
    );
  }

  // TODO need to call this on a timer
  // TODO ONCE we get the relevant data, we just have to set the state,
  // and that'll automatically propagate to the components
  // And this is already initialized with the required info, so it should happen quite automatically...
  componentDidMount() {
    this.libcarlab.checkNewInfo((data: DataMarshal) => {
      // console.log("Got info ", data.info, "with data", data.value);
    });
  }

  tryLoggingIn() {
    let loginurl = `http://localhost:3000/login?username=${this.state.username}&password=${this.state.password}`;
    fetch(loginurl, {
      method: "post",
      mode: "cors",
      cache: "no-cache",
      headers: { "content-type": "application/json" }
    })
      .then(res => res.json())
      .then(data => {
        window.localStorage.setItem("localSession", JSON.stringify(data));
        this.setState({
          session: data["session"],
          username: data["username"],
          showLoginForm: false
        });
      })
      .catch(function() {
        console.log("error");
      });
  }

  generateLoginForm() {
    let onHide = () => this.setState({ showLoginForm: false });

    return (
      <Modal
        show={this.state.showLoginForm}
        onHide={onHide}
        {...this.props}
        size="lg"
        aria-labelledby="contained-modal-title-vcenter"
        centered
      >
        <Modal.Header closeButton>
          <Modal.Title id="contained-modal-title-vcenter">
            Please log in
          </Modal.Title>
        </Modal.Header>

        <Modal.Body>
          <Form.Group>
            <Form.Label>Username</Form.Label>
            <Form.Control
              type="text"
              value={this.state.username}
              onChange={(evt: any) =>
                this.setState({
                  username: evt.target.value
                })
              }
            />
          </Form.Group>

          <Form.Group>
            <Form.Label>Password</Form.Label>
            <Form.Control
              type="password"
              value={this.state.password}
              onChange={(evt: any) =>
                this.setState({
                  password: evt.target.value
                })
              }
            />
          </Form.Group>

          <Button onClick={() => this.tryLoggingIn()}>Login</Button>
        </Modal.Body>
      </Modal>
    );
  }

  showLoginInfo() {
    if (this.state.session == null) {
      return (
        <Button
          onClick={() =>
            this.setState({
              showLoginForm: true
            })
          }
        >
          Log in
        </Button>
      );
    } else {
      return (
        <>
          Logged in as {this.state.username}
          <Button
            style={{marginLeft: 10}}
            onClick={() =>
              this.setState({
                session: null
              })
            }
          >
            Log out
          </Button>
        </>
      );
    }
  }

  render() {
    return (
      <Container style={{paddingTop: 25}}>
        {this.state.showLoginForm && this.generateLoginForm()}
        <Row>
          <Col>{this.showLoginInfo()}</Col>
        </Row>

        {this.state.session != null && [
%s
        ]}
      </Container>
    );
  }
}

export default App;

"""


















if __name__ == '__main__':
    main()












"""


# PYTHON

import map_match

# per algorithm stuff
alg = map_match.algorithm.AlgorithmImpl()

loaded_functions: List[AlgorithmFunction] = [
    alg.mapmatch_function,
]



# XXX this needs to be used somewhere to actually save the data.
# But it may not be relevant for Python 
to_save_information: List[Information] = [
    Registry.MapMatchedLocation
]



JAVA, really its just this:

package edu.umich.carlab.packaged;

import java.util.Arrays;

import carlab.android_passthroughs.Algorithm;
import edu.umich.carlab.Registry;
import edu.umich.carlab.Strategy;

public class PackageStrategy extends Strategy {
    public PackageStrategy () {
        loadedAlgorithms = Arrays.asList(carlab.android_passthroughs.Algorithm.class,
                                         carlab.obd_devices.Algorithm.class);
        loadedFunctions =
                Arrays.asList(Algorithm.getLocation, carlab.obd_devices.Algorithm.readFuelLevel);
        saveInformation = Arrays.asList(Registry.Location, Registry.CarFuel);
    }
}

"""






