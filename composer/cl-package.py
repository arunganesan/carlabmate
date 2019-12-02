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

    for python:
        symbolically link the module but replace '-' with '_'
    
    for android:
        add to settings.gradle
        import into build.gradle
        gradle build
    """

def write_code_for_android (func_per_algorithms):
    loaded_algorithms = []
    loaded_functions = []
    to_save_information = []

    for alg, functions in func_per_algorithms.items():
        Alg = alg.replace('-', '_')
        AlgCls = 'carlab.{}.Algorithm'.format(Alg)

        loaded_algorithms.append('{}.class'.format(AlgCls))

        
        for funcname, funcdetails in functions.items():
            Output = list(funcdetails['output'].keys())[0]
            Output = transform_variable_name(Output)
            loaded_functions.append('{}.{}'.format(AlgCls, funcname))
            to_save_information.append('Registry.{}'.format(Output))
    
    return JAVA_PACKAGE_CODE % (
        ', '.join(loaded_algorithms),
        ', '.join(loaded_functions),
        ', '.join(to_save_information)
    ) 


"""
1: Loaded algorithms
carlab.android_passthroughs.Algorithm.class, carlab.obd_devices.Algorithm.class

2: Loaded functions 
carlab.android_passthroughs.Algorithm.getLocation, carlab.obd_devices.Algorithm.readFuelLevel

3: Information to save (right now - MAYBE everything. We can fine tune it as needed)
Registry.Location, Registry.CarFuel
"""


def write_code_for_python (func_per_algorithms):
    import_calls = []
    # create_alg = []
    list_of_functions = []

    for alg, functions in func_per_algorithms.items():
        Alg = alg.replace('-', '_')
        # TmpAlg = '_tmp_{0}'.format(Alg)
        import_calls.append('import {}'.format(Alg))
        # create_alg.append('{} = {}.algorithm.AlgorithmImpl()'.format(TmpAlg, Alg))
        
        for funcname, funcdetails in functions.items():
            list_of_functions.append('\t{}.algorithm.AlgorithmImpl.{}_function'.format(
              Alg, 
              funcname))

    return PYTHON_PACKAGE_CODE % (
        '\n'.join(import_calls),
        ',\n'.join(list_of_functions)
    ) 
        
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

  componentDidMount() {
    if (this.state.session != null) 
      this.getLatest();
  }

  getLatest() {
    this.libcarlab.checkLatestInfo((data: DataMarshal) => {
      console.log(data);
      if (data.info.name == 'car-fuel') {
        this.setState({
          carFuel: data.message.message
        });
      } else if (data.info.name == 'phone-number') {
        this.setState({
          phoneNumber: data.message.message
        });
      }
      console.log("Got info ", data.info, "with data", data.message);
    });

  }
  tryLoggingIn() {
    let loginurl = `http://localhost:8080/login?username=${this.state.username}&password=${this.state.password}`;
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

        this.getLatest();
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




JAVA_PACKAGE_CODE = """package edu.umich.carlab.packaged;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Arrays;

import edu.umich.carlab.Constants;
import edu.umich.carlab.Registry;
import edu.umich.carlab.Strategy;


public class PackageCLService extends edu.umich.carlab.CLService {

    public static void turnOffCarLab (Context context) {
        Intent intent = new Intent(context, PackageCLService.class);
        intent.setAction(Constants.MASTER_SWITCH_OFF);
        context.startService(intent);
    }

    public static void turnOnCarLab (Context context) {
        Intent intent = new Intent(context, PackageCLService.class);
        intent.setAction(Constants.MASTER_SWITCH_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    protected void loadRequirements () {
        strategy = new PackageStrategy();
    }

    public class PackageStrategy extends Strategy {
        public PackageStrategy () {
            loadedAlgorithms = Arrays.asList(%s);
            loadedFunctions = Arrays.asList(%s);
            saveInformation = Arrays.asList(%s);
        }
    }
}
"""



PYTHON_PACKAGE_CODE = """#! /usr/bin/env python3.7
from libcarlab.libcarlab import AlgorithmFunction, Algorithm, Information, LinkGatewayService, Registry, DataMarshal
from termcolor import cprint

import argparse
import json
import os
import pickle
import time
from typing import List, Dict

%s

loaded_functions: List[AlgorithmFunction] = [
%s
]

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--session', default='dd2a4372516dab38535282070785853f')
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
        [], # output info
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
            storage[info] += values
        
        for info, value in storage.items():
            gateway.output_new_info(info, value)
       
        gateway.upload_data()
        time.sleep(1)
        storage.clear()
        
        for info, value in gateway.check_new_info().items():
            storage[info] = value
    
if __name__ == '__main__':
    main()
"""












"""
JAVA, really its just this (and some Gradle stuff but that's OK)

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





if __name__ == '__main__':
    main()
