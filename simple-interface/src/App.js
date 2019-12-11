import React from 'react';
import logo from './logo.svg';
import './App.css';
import { Form, Button, ToggleButton, ToggleButtonGroup, ProgressBar } from "react-bootstrap";
import { Container, Row, Col, Card, Modal } from 'react-bootstrap';

import _ from 'lodash'
import 'bootstrap/dist/css/bootstrap.css';


function SelectInformation (props) {
  return (<Col>
      <Card>
      <Card.Header>{props.title}</Card.Header>
      <Card.Body>

      { props.state[props.arrkey].map ((info) => (
        <div 
          key={props.title + '-'+info}
          className="ready_bhajan" onClick={() => {
          let new_list = props.state[props.arrkey].filter((v) => v != info);
          props.setState({
            [props.arrkey]: new_list
          });
        }}>{info}</div>
      ))}
       

      <Form.Control
          as="select"
          onChange={(event) => {
            props.setState({
              [props.arrkey]: _.concat(props.state[props.arrkey], event.target.value)
            })
          }}
        >
          {
            _.sortBy(_.filter(_.keys(props.masterlist), (inf) => {
              return !props.state.excluded.includes(inf) && !props.state.required.includes(inf) && !props.state.applications.includes(inf)
            })).map(item => (
              <option
                key={props.arrkey + '-' + item}
                value={item}
                readOnly>
                  {item}
                </option>
            ))
          }
       </Form.Control>
      </Card.Body>
      </Card>
    </Col>)
}


class App extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      name: '',
      required: [],
      excluded: [],
      applications: [],
      platforms: Platforms,
      
      showUrl: false,
      launching: false,
      progress: 0,
    }


    this.submitForm = this.submitForm.bind(this);
    this.checkStatus = this.checkStatus.bind(this);
  }

  componentDidMount() {

  }

  checkStatus () {
    fetch(`http://localhost:1234/create/status?appname=${this.state.name}`)
    .then(res => res.text())
    .then(text => {   
      this.setState({
        progress: text,
        launching: text != -1 && text < 7,
        showUrl: text == 7,
      })
      console.log("RECEIVED: ", text)
      if (text != -1 && text < 7) {
        setTimeout(this.checkStatus, 5000);
      }
    });
  }

  submitForm() {
    fetch("http://localhost:1234/create/launch", {
        method: 'post',
        body: JSON.stringify(this.state)
    }).then(res => {
      this.setState({
        launching: true,
        progress: 0
      });

      setTimeout(this.checkStatus, 5000);
    })
    .catch((error) => alert("Could not satisfy requirements"));
  }

  getLaunchStage() {
    switch (this.state.progress) {
      case '1':
        return 'Generating strategy'
      case '2':
        return 'Initializing platform'
      case '3':
        return 'Installing linking server'
      case '4':
        return 'Building React components'
      case '5':
        return 'Building Python components'
      case '6':
        return 'Building Android components'
      case '7': 
        return 'Launching'
    }

    return '';
    
  }

  render() {
    let handleClose = () => this.setState({showUrl: false})

    return <Container>
      <Row style={{marginTop: 25}}>
        <Col><Form.Label>Name</Form.Label></Col>
      
      </Row>
      <Row style={{marginBottom: 25, marginTop: 0}}>
        <Col>        
          <Form.Control 
            onChange={(evt) =>  this.setState({ name: evt.target.value }) }
            size="lg" 
            type="text" 
            placeholder="name" />
        </Col>
      </Row>
      
      {/* <Row style={{marginTop: 25}}>
        <Col><Form.Label>Platform</Form.Label></Col>
      
      </Row>
      <Row style={{marginBottom: 25, marginTop: 0}}>
        <Col>

        
        <ToggleButtonGroup value={[0,1,2]} type="checkbox">
          {Platforms.map((elt, i) => {
            return (<ToggleButton 
              value={i} 
              key={elt}
              defaultChecked
              variant="outline-primary">
                {elt}
              </ToggleButton>)
          })}

        </ToggleButtonGroup>
        </Col>
      </Row> */}
      
      <Row>
        
        <SelectInformation
          title="Required Information" 
          state={this.state}
          setState={(s) => this.setState(s)}
          arrkey="required"
          masterlist={Registry}
          />

        <SelectInformation
            title="Exclude Information" 
            state={this.state}
            setState={(s) => this.setState(s)}
            arrkey="excluded"
            masterlist={Registry}
            />

        <SelectInformation
            title="Include Application" 
            state={this.state}
            setState={(s) => this.setState(s)}
            arrkey="applications"
            masterlist={Applications}
            />

      </Row>


      <Row style={{marginTop:25}}>
        <Col>
            
            { this.state.launching
              ?  <ProgressBar style={{height: 50}} label={this.getLaunchStage()} now={this.state.progress} max={8} />
              :  <Button onClick={this.submitForm} block>Create Application</Button>
            }
        </Col>
      </Row>


      <Modal show={this.state.showUrl} onHide={handleClose}>
        <Modal.Header closeButton>
          <Modal.Title>Launched!</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Distribute this URL to experiment participants <br /><br />
          <h3><a target='blank' href='http://35.3.62.141:8080'>http://35.3.62.141:8080</a></h3>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="primary" onClick={handleClose}>
            Close
          </Button>
        </Modal.Footer>
      </Modal>

    </Container>
  }

}



export default App;







const Applications = {'aligned-imu/produceWorldPointingRotation': 0,'aligned-imu/produceWorldAlignedGyro': 0,'aligned-imu/produceWorldAlignedAccel': 0,'aligned-imu/produceVehicleAlignedAccel': 0,'aligned-imu/produceVehiclePointingRotation': 0,'aligned-imu/produceGravityAlignedGyro': 0,'map-match/mapmatch': 0,'user-input/acceptFuelLevel': 0,'user-input/acceptPhoneNumber': 0,'user-input/acceptCarModel': 0,'obd-devices/readFuelLevel': 0,'text-input/accept_fuel_level': 0,'vehicle-estimate/estimateSpeed': 0,'vehicle-estimate/estimateGear': 0,'vehicle-estimate/estimateSteering': 0,'tensorflow-models/get_gear_model_file': 0,'obstacle-warning-react/acceptSightingReport': 0,'obstacle-warning-python/update_sightings': 0,'android-passthroughs/getLocation': 0};



const Platforms = [
  'android', 'python', 'react'
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
