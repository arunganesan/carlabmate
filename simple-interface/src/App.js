import React from 'react';
import logo from './logo.svg';
import './App.css';
import { Form, Button, ToggleButton, ButtonGroup, DropdownButton, Dropdown, ToggleButtonGroup } from "react-bootstrap";
import { Container, Row, Col, Spinner, Card } from 'react-bootstrap';

import _ from 'lodash'
import 'bootstrap/dist/css/bootstrap.css';


class App extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      required: [],
      excluded: [],
      applications: [],
      platforms: Platforms,
      choices: {},
    }
  }

  componentDidMount() {

  }

  render() {
    return <Container>
    

      <Row style={{marginTop: 25}}>
        <Col><Form.Label>Platform</Form.Label></Col>
      
      </Row>
      <Row style={{marginBottom: 25, marginTop: 0}}>
        <Col>

        

        <ToggleButtonGroup  type="checkbox">
          <ToggleButton  value={1} variant="outline-primary">Android</ToggleButton>
          <ToggleButton  value={2} variant="outline-primary">React</ToggleButton>
          <ToggleButton  value={3} variant="outline-primary">Python</ToggleButton>
        </ToggleButtonGroup>
        </Col>
      </Row>
      
      <Row>

      <Col>
        <Card>
          <Card.Header>Required Information</Card.Header>
          <Card.Body>

          { this.state.required.map ((info) => (
            <div className="ready_bhajan">{info}</div>
          ))}
           

           <Form.Control
              as="select"
              onChange={(event) => {
                this.setState({
                  required: _.concat(this.state.required, event.target.value)
                })
              }}
            >
              {
                _.sortBy(_.keys(Registry)).map(item => (
                  <option
                    key={'required-' + item}
                    value={item}
                    readOnly>
                      {item}
                    </option>
                ))
              }
           </Form.Control>
            </Card.Body>
          </Card>
        </Col>



        <Col>
        <Card>
          <Card.Header>Exclude Information</Card.Header>
          <Card.Body>

          { this.state.excluded.map ((info) => (
            <div className="exclude_bhajan">{info}</div>
          ))}
           

           <Form.Control
              as="select"
              onChange={(event) => {
                this.setState({
                  excluded: _.concat(this.state.excluded, event.target.value)
                })
              }}
            >
              {
                _.sortBy(_.keys(Registry)).map(item => (
                  <option
                    key={'excluded-' + item}
                    value={item}
                    readOnly>
                      {item}
                    </option>
                ))
              }
           </Form.Control>
            </Card.Body>
          </Card>
        </Col>













        <Col>
        <Card>
          <Card.Header>Include Application</Card.Header>
          <Card.Body>

          { this.state.applications.map ((info) => (
            <div className="ready_bhajan">{info}</div>
          ))}
           

           <Form.Control
              as="select"
              onChange={(event) => {
                this.setState({
                  applications: _.concat(this.state.applications, event.target.value)
                })
              }}
            >
              {
                _.sortBy(Applications).map(item => (
                  <option
                    key={'apps-' + item}
                    value={item}
                    readOnly>
                      {item}
                    </option>
                ))
              }
           </Form.Control>
            </Card.Body>
          </Card>
        </Col>
      </Row>


      <Row style={{marginTop:25}}>
        <Col>
            <Button block>Create Application</Button>
        </Col>
      </Row>

    </Container>
  }

}



export default App;







const Applications = ['aligned-imu/produceWorldPointingRotation', 'aligned-imu/produceWorldAlignedGyro', 'aligned-imu/produceWorldAlignedAccel', 'aligned-imu/produceVehicleAlignedAccel', 'aligned-imu/produceVehiclePointingRotation', 'aligned-imu/produceGravityAlignedGyro', 'map-match/mapmatch', 'user-input/acceptFuelLevel', 'user-input/acceptPhoneNumber', 'user-input/acceptCarModel', 'obd-devices/readFuelLevel', 'text-input/accept_fuel_level', 'vehicle-estimate/estimateSpeed', 'vehicle-estimate/estimateGear', 'vehicle-estimate/estimateSteering', 'tensorflow-models/get_gear_model_file', 'obstacle-warning-react/acceptSightingReport', 'obstacle-warning-python/update_sightings', 'android-passthroughs/getLocation'];



const Platforms = [
  'android', 'python', 'react'
]



const Specs = {
  "aligned-imu" : {
      "platform": "android",
      "functions": { 
          "produceWorldPointingRotation": {
              "output": "world-pointing-rotation", 
              "input": ["gravity", "magnetometer"] 
          },

          "produceWorldAlignedGyro": {
              "output": "world-aligned-gyro", 
              "input": ["gyro", "world-pointing-rotation"] 
          },

          "produceWorldAlignedAccel": {
              "output": "world-aligned-accel", 
              "input": ["accel", "world-pointing-rotation"] 
          },

          "produceVehicleAlignedAccel": {
              "output": "vehicle-aligned-accel",
              "input": ["accel", "vehicle-pointing-rotation"]
          },

          "produceVehiclePointingRotation": {
              "output": "vehicle-pointing-rotation",
              "input": ["magnetometer", "gps", "gravity"]
          },

          "produceGravityAlignedGyro": {
              "output": "gravity-aligned-gyro",
              "input": ["gravity", "gyro"]
          }
      }
  },



  "map-match": {
      "platform": "python",
      "functions": {
          "mapmatch": {
              "input": ["location"],
              "output": "map-matched-location"
          }
      }
  },

  "user-input": {
      "platform": "react",
      "functions": {
          "acceptFuelLevel": { "output": "car-fuel" },
          "acceptPhoneNumber": {"output": "phone-number"},

          // For now literally just use the car models for which we already have models
          "acceptCarModel": { "output": "car-model" }
      }
  },


  "obd-devices": {
      "platform": "android",
      "functions": { 
          "readFuelLevel": { "output": "car-fuel", "input": ["obd-fuel"] }
      }
  },


  "text-input": {
      "platform": "python",
      "functions": {
          "accept_fuel_level": { "output": "car-fuel", "input": ["user-text"], "uses": ["phone-number"] }
      }
  },


  "vehicle-estimate": {
      "platform": "android",
      "functions": {
          "estimateSpeed": {
              "uses": ["car-model"],
              "input": ["vehicle-aligned-accel", "gps"],
              "output": "car-speed"
          },

          "estimateGear": {
              "uses": ["gear-model-file"],
              "input": ["car-speed"],
              "output": "car-gear"
          },
          
          "estimateSteering": {
              "uses": ["car-model"],
              "input": ["car-speed", "gravity-aligned-gyro"],
              "output": "car-steering"
          }
      }
  },


  // If this only USES the car model, then when is it called?
  // When we load it up, we'll call it and try to initialize with the car model
  // If that fails, we will call it when the car model is set
  // I.e., "uses" relationships also invoke the function. Just not as input. 
  "tensorflow-models": {
      "platform": "python",
      "functions": {
          "get_gear_model_file": {
              "uses": ["car-model"],
              "output": "gear-model-file"
          }
      }
  },

  "obstacle-warning-react": {
      "platform": "react",
      "functions": {
          "acceptSightingReport": {
              "input": ["location", "sightings-map"],
              "output": "sighting"
          }
      }
  },

  "obstacle-warning-python": {
      "platform": "python",
      "functions": {
          "update_sightings": {
              "input": ["sighting"],
              "output": "sightings-map"
          }
      }
  },

  // Low level passthrough algorithms so other modalities can read Android sensors
  "android-passthroughs": {
      "platform": "android",
      "functions": {
          "getLocation": {
              "input": ["gps"],
              "output": "location"
          }
      }
  }
}



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
  "user-text": {"type": "string"},

  // Ubi related
  "sighting": {"type": "float[3]", "description": "time, lat, lng"}, 
  "sightings-map": {"type": "list[float[3]]" }, // list of sightings. Indeterminate length of array
  
  // Raw sensors
  "gps": {"type": "float[3]", "sensor": true},
  "accel": {"type": "float[3]", "sensor": true},
  "magnetometer": {"type": "float[3]", "sensor": true},
  "gyro": {"type": "float[3]", "sensor": true},
  "gravity": {"type": "float[3]", "sensor": true},
  "obd-fuel": {"type": "float", "sensor": true}
}
