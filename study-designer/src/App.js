import _ from 'lodash';
import React from 'react';
import './App.css';

import { 
  Button, 
  Col,
  Container,
  Card, 
  Form, 
  FormControl,
  InputGroup, 
  Row } from 'react-bootstrap';
import 'bootstrap/dist/css/bootstrap.css';



class App extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      inprogressQuery: '',
      submittedQuery: '',// 'What time do I leave my house every day?',
      loading: false,

      implementations: this.generateDummyImplementations()
      // implementations: [
      //   { name: 'pothole patrol', implements: ['pothole'], requires: ['map matched location', 'aligned imu'], sensors: [] },
      //   { name: 'steering wheel estimation', implements: ['steering wheel'], requires: ['map matched location', 'aligned imu', 'speed'], sensors: [] },
      //   { name: 'speed limit monitor', implements: ['exceed speed limit'], requires: ['map matched location', 'speed'], sensors: [] },
        
      //   { name: 'android:imu aligner', implements: ['aligned imu'], requires: ['imu'], sensors: [] },
      //   { name: '-', implements: ['map matched location'], requires: ['location', 'imu'], sensors: [] },
        
      //   { name: 'obfuscation', implements: ['location'], requires: ['location'], sensors: [] },
      //   { name: 'spoofer', implements: ['imu', 'location'], requires: [], sensors: [] },

      //   // Low-level values
      //   { name: 'raw', implements: ['imu', 'location'], requires: [], sensors: ['imu', 'location'] },
      //   { name: 'web-based-input', implements: ['location'], requires: [], sensors: ['web:input'] },
      //   { name: 'phone-based-input', implements: ['location'], requires: [], sensors: ['phone:input'] },
      // ],
    }
  }



  sampleRandomRequirements(atmax_requirements, nodes) {
    let num_requirements = _.random(1, atmax_requirements)
    let random_requirements = _.sampleSize(nodes, num_requirements);
    return random_requirements.map(impl => impl.supplies)
  }


  generateDummyImplementations () {
    // each implementation discovers a set of information and requires a set of information
    // if an implementation doesn't have any requirements or sensors, that is a "leaf node" - often a raw sensor
    // only special implementations may use or require low-level data such as sensors

    const nlayers = 5;
    const perlayer = 10;
    const atmax_requirements = 5;

    // start with bottom most layer
    // each node can depend on other nodes in previous layers

    let nodes = [];
    for (let i = 0; i < nlayers; i++) {
      let next_layer_nodes = [];

      // eventually we want multiple nodes which implement the same thing
      // this will allow us to swap in and out implementations for same info
      // we also want a node to require different devices
      
      for (let j = 0; j < perlayer; j++) {
        next_layer_nodes.push({
          name: `name-${i}-${j}`,
          supplies: `information-${i}-${j}`,
          requires: this.sampleRandomRequirements(atmax_requirements, nodes),
        })
      }

      nodes = nodes.concat(next_layer_nodes);
    }
    
    return nodes;
  }


  createStudyPlan() {
    /*
      1. fix some information as requirements
      2. for each information, gather all implemnentations of that information 
      3. fix some implementations, list all sensors those require
      4. list all leaf-node information/sensors required by implementations
      5. loop 2 - 4 until we meet requirements and some optimality condition
    */
    
    // Some plans are objectively better because they require 
    // fewer information. This is kinda like a graph search.
    
    // You start at two nodes -- those are the only ones required.
    // You find a subset of the remaining nodes so that it is a complete network.
    // First pass -- 
    //    just take the node
    //    depth first search through the graph
    //    if you ever encounter same node again, skip it
    return {
      information: {
        required: ['leave house', 'driving'],
        secondary: ['semantic location', 'activity recognition', 'openxc', 'user input', 'smartwatch:motion'],
      },
      sensors: ['gps', 'user input', 'imu']
    }
  }

  handleSubmit (event) {
    this.setState({ 
      loading: true,
      submittedQuery: this.state.inprogressQuery
    });

    setTimeout(() => {
      this.setState({loading: false});
    }, 3000);
    event.preventDefault();
  }

  renderStudy () {
    let plan = this.createStudyPlan();
    return (<Container className="study-design">
        <Row className="justify-content-lg-center">

          <Col lg={{span:2}}>
            <Button block>Select devices</Button>
            <Button block>Minimize sensors</Button>
            <Button block>Minimize effort</Button>
          </Col>

          <Col lg={{span: 2, offset: 1}}><Card className="study-card">
            <Card.Body>
              <Card.Title>Information</Card.Title>
              <Card.Subtitle>Required</Card.Subtitle>
              <Card.Text>
                {plan.information.required.map(
                  item => <Button 
                            variant="outline-primary" 
                            block>
                            {item}
                          </Button>)}
              </Card.Text>

              <Card.Subtitle>Secondary</Card.Subtitle>
              <Card.Text>
                {plan.information.secondary.map(
                  item => <Button 
                            variant="outline-primary" 
                            block>
                            {item}
                          </Button>)}
              </Card.Text>
            </Card.Body>
          </Card></Col>
          
          <Col lg={{span: 2, offset: 0}} ><Card className="study-card">
            <Card.Body>
              <Card.Title>Sensors</Card.Title>
              <Card.Text>
                {plan.sensors.map(
                  item => <Button 
                            variant="outline-primary" 
                            block>
                            {item}
                          </Button>)}
              </Card.Text>
            </Card.Body>
          </Card></Col>
          
          <Col lg={{span:2, offset: 1}}>
            <Button block>Print summary</Button>
            <Button block>Launch study</Button>
          </Col>
        </Row>
      </Container>);
  }

  render() {
    return (
      <div className="App">
        <header className="App-header">
          <div className="query-input">
            <Form onSubmit={(e) => this.handleSubmit(e)}>
              <InputGroup size="lg">
                <FormControl 
                  value={this.state.inprogressQuery}
                  onChange={evt => this.setState({inprogressQuery: evt.target.value})}
                  aria-label="Large" 
                  aria-describedby="inputGroup-sizing-sm"
                  placeholder="What do you want to know?"
                  />
                
                <InputGroup.Append>
                  <Button type="submit">Search</Button>      
                </InputGroup.Append>
              </InputGroup>
            </Form>
          </div>
        </header>

        <div className="App-body">
            { this.state.loading && 
              <>
                Creating study, please wait...
              </>
            }
            
            { this.state.submittedQuery !== '' && !this.state.loading && 
              this.renderStudy()
            }
          </div>
      </div>
    );
  }
}

export default App;
