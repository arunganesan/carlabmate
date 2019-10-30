import _ from 'lodash';
import React from 'react';
import './App.css';
import 'bootstrap/dist/css/bootstrap.css';
import Lorem from 'react-lorem-component'

import { 
  Badge, 
  Button, 
  Col,
  Container,
  Card, 
  Form, 
  FormControl,
  InputGroup, 
  ToggleButton,
  ToggleButtonGroup,
  Row } from 'react-bootstrap';

import { LinkContainer } from "react-router-bootstrap";
import { Nav, NavItem, Navbar } from "react-bootstrap";
import { Link, HashRouter as Router, Route } from "react-router-dom";

import Highlight from 'react-highlight';
import developer_image from './images/algorithm-generation.png'


function MyLink (props) {
  return (
  <NavItem>
    <LinkContainer to={`/${props.to}`}>
    <Nav.Link style={{color: 'red'}}>
        {props.label ? props.label : _.capitalize(props.to)}
    </Nav.Link>
    </LinkContainer>
  </NavItem>);
}

function Overview (props) {
  return (<>
    `CarLab` is an on-demand data collection builder for vehicular research. Using a high-level specification of the data collection requirements, it creates a custom-build data collection platform, which includes all the tools necessary to carry out the data collection campaign. 
    <p />
    There are three main ways to interact with `CarLab`. **Developers** can contribute algorithms which power the core of `CarLab`. An algorithm takes information as input and outputs information. A **data collection campaign designer** can use `CarLab` to craft a data collection campaign. The designer inputs a data collection specification into `CarLab` and iteratively refines it until it meets the data collection requirements. A **participant** who participates in the data collection signs up through `CarLab` web interface and installs the data collection tools.
    <p />
    <h3>Table of contents</h3>
    <ul>
<li>Overview
  <ul>
    <li>Purpose of `CarLab`</li>
    <li>Data collection campaign examples (a visual example of the entire process)</li>
  </ul></li>
<li>Data collection designer
  <ul>
    <li>Data collection requirement specification</li>
    <li>Refining the data collection strategy</li>
  </ul></li>
<li> Experiment participant
  <ul>
    <li>Signing up for an experiment</li>
    <li>Installing different components</li>
  </ul></li>
<li> Algorithm developer
  <ul>
    <li> Creating a template algorithm</li>
    <li> Local testing for each library</li>
  </ul></li>
<li> Reference documentation
  <ul>
    <li> Libraries for each language</li>
    <li> Available algorithms</li>
  </ul></li>
</ul>
  </>
  );
}





function Designer (props) {
  return (<>
    The design phase is divided into two steps. The first step the designer inputs the high-level requirements. These requirements are the list of information they wish to collect, and the available devices, and any blacklisted information (e.g. location) or sensors (e.g. GPS). Using this, `CarLab` searches through the library of available algorithms and automatically finds a suitable data collection plan. 
    <p />
    In the second step, the designer revises this plan by editing any individual data collection modules or by overwriting any of the strategy. After the data collection is designed, it outputs the following strategy JSON file. This is finally used to build the data collection platform.
    <p />
    <Highlight language="javascript">
      {`{
  "algorithms": [
    { "algorithm": "watchfon", "information": "car-steering", "save": true },
    { "algorithm": "watchfon", "information": "car-speed" },
    { "algorithm": "aligned-imu", "information": "world-aligned-gyro" },
    { "algorithm": "aligned-imu", "information": "world-aligned-accel" },
    { "algorithm": "aligned-imu", "information": "rotation" },
    { "algorithm": "android-raw-sensors", "information": "gravity"},
    { "algorithm": "android-raw-sensors", "information": "magnetometer"},
    { "algorithm": "android-raw-sensors", "information": "gyro"},
    { "algorithm": "android-raw-sensors", "information": "accel"},
    { "algorithm": "android-raw-sensors", "information": "location"}
  ],
  "wiring": [
    { "algorithm": 0, "inputs": [1, 3, 2] },
    { "algorithm": 1, "inputs": [10, 4] },
    { "algorithm": 3, "inputs": [8, 5] },
    { "algorithm": 4, "inputs": [9, 5] },
    { "algorithm": 5, "inputs": [6, 7] }
  ]
}
      `}
    </Highlight>    
  </> 
  );
}





function Participant (props) {
  return (<>
    Still writing...
  </> 
  );
}

function Developer (props) {
  return (<>
    Algorithms are implemented as standalone libraries under each of the supported languages. The entry point into each algorithm is a class with a set of callback functions to generate each output information. This function is invoked with each of the required information as parameters. The class is created and sustained during the data collection so it can keep any internal state. 
    <p /> 
    The entry point into an algorithm is the algorithm spec JSON file. This file gives the main details of the algorithm. It specifies which information is produced by this algorithm and their corresponding input. 
    <p/>

    <Highlight language="javascript">
      {`[{
  "module": "aligned-imu",
  "classname": "AlignedIMU",
  "functions": [
      { 
          "output": "rotation", 
          "input": ["gravity", "magnetometer"] 
      },{ 
          "output": "world-aligned-gyro", 
          "input": ["gyro", "rotation"] 
      },{ 
          "output": "world-aligned-accel", 
          "input": ["accel", "rotation"] 
      }
  ]
}]      
      `}
    </Highlight>
    <p />
    This information is used in the algorithm-generation script. The script is invoked using `gen-algorithm android spec.json` command. This creates the function stubs which are invoked during run-time. The developer can then simply fill in the stub. The generation script also creates a sandbox wrapper script which imports the algorithm library. The wrapper also provides support for feeding in dummy data for any of the input/output of the algorithm allowing rapid testing.
    <p />
    <Highlight language="java">
      {`package edu.umich.aligned_imu;
import android.content.Context;
import edu.umich.carlab.CLDataProvider;

public class AlignedIMU extends Algorithm {
    public AlignedIMU(CLDataProvider cl, Context context) {
        super(cl, context);
    }

    @Override
    public float[][] produceRotation (Float [] m, Float [] g) {}

    @Override
    public Float[] produceAlignedGyro (Float [] gyro, float [][] rm) {}

    @Override
    public Float[] produceAlignedAccel (Float [] accel, float [] [] rm) {}
}
      `}
    </Highlight>
    <p />
    The overall process of generating and filling in the algorithm is shown in the following figure. 
    <p />
    <img src={developer_image} height={350} />
    <p />
  </> 
  );
}



function Reference (props) {
  return (<>
    Algorithms can be implemented in one of four development environments -- Android, Python, React and React-Native. Each modality is summarized in the following table.
    <p />

    <table border={1}>
      <tr>
        <th>Modality</th>
        <th>Resides on</th>
        <th>Initiated by</th>
        <th>Language</th>
        <th>Compilation steps</th>
      </tr>

      <tr>
        <td>Android</td>
        <td>Phone</td>
        <td>Wakes up</td>
        <td>Java</td>
        <td>`gradle build`</td>
      </tr>

      <tr>
        <td>React-Native</td>
        <td>Phone</td>
        <td>Wakes up</td>
        <td>Javascript</td>
        <td>`react-native build`</td>
      </tr>

      <tr>
        <td>React</td>
        <td>Browser</td>
        <td>User initiated</td>
        <td>Javascript</td>
        <td>`react build`</td>
      </tr>

      <tr>
        <td>Python scripts</td>
        <td>Server</td>
        <td>Wakes up</td>
        <td>Python</td>
        <td>Copt over scripts</td>
      </tr>
    </table>

    <p />
    They are stored in the `algorithms` folder in the repository. Each modality has a sub-section which 
  </> 
  );
}

export default class Home extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
    }
  }

  render() {
    return (
        <Container>
          <Row>
          <Col lg={{span: 3}}>
            <MyLink to='documentation/overview' label='Overview' />
            <MyLink to='documentation/designer' label='Data collection designer' />
            <MyLink to='documentation/participant' label='Experiment participant' />
            <MyLink to='documentation/developer' label='Algorithm developer' />
            <MyLink to='documentation/reference' label='Reference' />
          </Col>

          <Col>
            <Route path={'/documentation/overview'} component={Overview} />
            <Route path={'/documentation/designer'} component={Designer} />
            <Route path={'/documentation/participant'} component={Participant} />
            <Route path={'/documentation/developer'} component={Developer} />
            <Route path={'/documentation/reference'} component={Reference} />
          </Col>
          </Row>
        </Container>
    );
  }
}
