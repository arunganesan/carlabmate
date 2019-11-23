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


"""
1: props and state definition

type acceptFuelLevelProps = { 
  produce: Function
};

type acceptFuelLevelState = {
  fuelLevel: string
}


"""
REACT_TEMPLATE = """
import * as React from "react";

/************************************
 * AUTO GENERATED - do not change
 **********************************/

%s

class acceptFuelLevelBase extends React.Component<acceptFuelLevelProps, acceptFuelLevelState> {
  constructor(props: acceptFuelLevelProps) {
    super(props);

    this.state = {
      // This value changes per thing
      fuelLevel: '',
    };
  }
}

type acceptPhoneNumberProps = { 
  produce: Function
};

type acceptPhoneNumberState = {
  phoneNumber: string
}

class acceptPhoneNumberBase extends React.Component<acceptPhoneNumberProps, acceptPhoneNumberState> {
  constructor(props: acceptPhoneNumberProps) {
    super(props);
    this.state = {
      phoneNumber: '',
    };
  }
}

/************************************
 * End of auto generated
 **********************************/





const style = {
  input: {
    marginTop: "25px"
  },
  button: {
    marginTop: "25px"
  }
};




interface TextInputProps {
  name: string,
  value: string,
  changeVal: Function,
  produce: Function,
 }


 

 const TextInput: React.SFC<TextInputProps> = (props) => <Container>
    <Form.Label style={style.input}>{props.name}</Form.Label>
      
    <Form.Control
        type="text"
        value={props.value}
        onChange={(evt: React.ChangeEvent<HTMLInputElement>) =>
          props.changeVal(evt.target.value)
        }
      />
   
   
    <Button
      onClick={() => props.produce(props.value)}
      style={style.button}
      size="lg"
    >
      Submit
    </Button>
  </Container>;



export class acceptFuelLevel extends acceptFuelLevelBase {

  render() {
    return <TextInput
      name="Enter fuel level"
      value={this.state.fuelLevel}
      changeVal={(val: string) => this.setState({fuelLevel: val})}
      produce={this.props.produce} />
  }
}



export class acceptPhoneNumber extends acceptPhoneNumberBase {
  render() {
      return <TextInput
        name="Enter phone number"
        value={this.state.phoneNumber}
        changeVal={(val: string) => this.setState({phoneNumber: val})}
        produce={this.props.produce} />
    }
}



"""