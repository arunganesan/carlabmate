import React from 'react';
import logo from './logo.svg';
import { Form, Button } from "react-bootstrap";
import { Container, Row, Col } from 'react-bootstrap';

import _ from 'lodash'
import './App.css';
import 'bootstrap/dist/css/bootstrap.css';


const Platforms = [
  'android', 'python', 'react'
]

const LowlevelSensors = [
  "gps", "accel", "magnetometer", "gyro", "gravity", "obd-fuel", "user-text"
]

const Registry = {
  "map-matched-location": {"type": "string,float", "description": "Road name, percentage into road"},
  "location": {"type": "float[2]", "description": "latitude, longitude"},

  // Watchfone-related
  "car-speed": {"type": "float"},
  "car-gear": {"type": "int"}, // Actually gear is enum
  "car-fuel": {"type": "float"},
  "car-steering": {"type": "float"},
  // IMU transformations
  "vehicle-pointing-rotation": {"type": "float[9]" },
  "world-pointing-rotation": {"type": "float[9]" },
  "vehicle-aligned-accel": {"type": "float[3]" },
  "world-aligned-accel": {"type": "float[3]" },
  "world-aligned-gyro": {"type": "float[3]" },
  "gravity-aligned-gyro": {"type": "float" },
  "gear-model-file": {"type": "string" },
  "car-model": {"type": "string" }, // Car model might also be an enum
  "phone-number": {"type": "string"},
  "sighting": {"type": "float[3]", "description": "time, lat, lng"}, 
  "sightings-map": {"type": "list[float[3]]" }, // list of sightings. Indeterminate length of array
  
  // Raw sensors
  "gps": {"type": "float[3]", "sensor": true},
  "accel": {"type": "float[3]", "sensor": true},
  "magnetometer": {"type": "float[3]", "sensor": true},
  "gyro": {"type": "float[3]", "sensor": true},
  "gravity": {"type": "float[3]", "sensor": true},
  "obd-fuel": {"type": "float", "sensor": true},
  "user-text": {"type": "string"},

}


class App extends React.Component {
  constructor(props) {
    super(props);

    
    this.state = {
      required: [],
      exclude: [],
      platforms: Platforms,
      choices: {},
      loading: false,
      imageHash: Date.now()
    }
  }

  componentDidMount() {

  }

  updateRequiredInfo(options) {
    let value = [];
    for (let i = 0, l = options.length; i < l; i++) {
      if (options[i].selected) {
        value.push(options[i].value);
      }
    }
    this.setState({
      required: value
    })
  }

  updateExcludedInfo(options) {
    let value = [];
    for (let i = 0, l = options.length; i < l; i++) {
      if (options[i].selected) {
        value.push(options[i].value);
      }
    }
    this.setState({
      exclude: value
    })
  }



  updatePlatforms(options) {
    let value = [];
    for (let i = 0, l = options.length; i < l; i++) {
      if (options[i].selected) {
        value.push(options[i].value);
      }
    }
    this.setState({
      platforms: value
    })
  }

  generateRequiredInfo() {
    let elts = [];

    let allInfo = _.sortBy(_.keys(Registry));
    for (let info of allInfo) {
      if (LowlevelSensors.includes(info))
        continue

      elts.push(<option 
        value={info} 
        disabled={this.state.exclude.includes(info)}
        key={'required-' + info}>{info}</option>)
    }

    return <Form.Control 
              onChange={(evt) => this.updateRequiredInfo(evt.target.options)}
              multiple 
              as='select' 
              style={{height: 500}}>
      { elts }
    </Form.Control>
  }

  generateStrategy() {
    // Call server
    // Server will take this, generate JSON file, call Python script.

    this.setState({loading: true});
    fetch("http://localhost:1234/plan/design", {
        method: 'post',
        body: JSON.stringify(this.state)
    }).then(res => {
      if (res.status != 200) {
        alert('Strategy not possible');
      }

      this.setState({ 
        loading: false, 
        imageHash: Date.now() 
      })})
    .catch((error) => alert("Could not satisfy requirements"));
  }

  generateExcludedInfo() {
    let elts = [];
    
    let allInfo = _.sortBy(_.keys(Registry));


    for (let info of allInfo) {
      elts.push(<option 
        value={info} 
        disabled={this.state.required.includes(info)}
        key={'excluded-' + info}>{info}</option>)
    }

    return <Form.Control 
              onChange={(evt) => this.updateExcludedInfo(evt.target.options)}
              multiple 
              as='select' 
              style={{height: 500}}>
      { elts }
    </Form.Control>
  }

  render() {
    console.log("Re-rendering with hash: " + this.state.imageHash);
    return (
      <Container>
        <Row>
          <Col>
            <Form>
              <Form.Label>Platforms</Form.Label>
              <Form.Control as="select" onChange={(evt) => this.updatePlatforms(evt.target.options)} multiple>
              {Platforms.map(elt => <option value={elt} key={elt}>{elt}</option>)}
              </Form.Control>

              <Row>
                <Col>
                  <Form.Label>Required Information</Form.Label>
                  { this.generateRequiredInfo() }
                </Col>

                <Col>
                  <Form.Label>Exclude information</Form.Label>
                  { this.generateExcludedInfo() }
                </Col>
              </Row>
              
              <Button onClick={() => this.generateStrategy()} block style={{marginTop: 25}}>
                Update Strategy
              </Button>
            </Form>            
          </Col>
          <Col>

            { this.state.loading ? "Loading..." :             <img width='100%' src={`http://localhost:1234/images/strategy-both.gv.png?${this.state.imageHash}`} />}
          </Col>
        </Row>
      </Container>
    );
  }
}

export default App;
