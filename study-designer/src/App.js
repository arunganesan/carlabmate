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
  ToggleButton,
  ToggleButtonGroup,
  Row } from 'react-bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import Lorem from 'react-lorem-component'


class App extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      inprogressQuery: '',
      submittedQuery: '',// 'What time do I leave my house every day?',
      loading: false,
      devices: [1, 3, 4],
      optimizeFor: 1,
      
      editInfo: '',
      showEditInfoForm: false,
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
          
          <Col lg={{span: 2, offset: 0}} >
            { this.state.showEditInfoForm && <Card className="study-card">
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
          </Card>}

          </Col>
          
         
        </Row>
      </Container>);
  }
   
    /*  

         
          { this.state.submittedQuery !== '' && !this.state.loading && 
            this.renderStudy()
    } */

  render() {

    let plan = this.createStudyPlan();

    return (
      <div className="App">
        <Container>
          <Row>
            <Col id='input-left-side' lg={{span: 7}}>
              <Row>
                <Col lg={{span: 1}} className="section-title">input</Col>
                <Col lg={{span: 6, offset: 3}} className="section-content input-section">
                  <Form onSubmit={(e) => this.handleSubmit(e)}>
                    <InputGroup size="sm">
                      <FormControl 
                        value={this.state.inprogressQuery}
                        onChange={evt => this.setState({
                          inprogressQuery: evt.target.value
                        })}
                        aria-label="small" 
                        aria-describedby="inputGroup-sizing-sm"
                        placeholder="What do you want to know?"
                        />
                    </InputGroup>
                    
                    <div className="input-options">
                      <div><Form.Label>What devices do you own?</Form.Label></div>
                      <ToggleButtonGroup 
                        type="checkbox" 
                        size="sm" 
                        value={this.state.devices} 
                        onChange={val => this.setState({ devices: val })}>

                        <ToggleButton variant="outline-primary" value={1}>Android</ToggleButton>
                        <ToggleButton variant="outline-primary" value={2}>iPhone</ToggleButton>
                        <ToggleButton variant="outline-primary" value={3}>Texting</ToggleButton>
                        <ToggleButton variant="outline-primary" value={4}>FitBit</ToggleButton>
                        <ToggleButton variant="outline-primary" value={5}>Smartwatch</ToggleButton>
                      </ToggleButtonGroup>
                    </div>


                    <div className="input-options">
                      <div><Form.Label>What do you prioritize?</Form.Label></div>

                      <ToggleButtonGroup 
                        type="radio" 
                        size="sm"
                        name="options"
                        value={this.state.optimizeFor} 
                        onChange={val => this.setState({ optimizeFor: val })}>

                        <ToggleButton variant="outline-primary" value={1}>Minimize sensors</ToggleButton>
                        <ToggleButton variant="outline-primary" value={2}>Minimize effort</ToggleButton>
                      </ToggleButtonGroup>
                    </div>

                    <Button size="sm" type="submit" block>{
                      this.state.submittedQuery === '' 
                      ? "Create study" 
                      : "Update study"
                    }</Button>
                  </Form>
                </Col>
              </Row>
              
              { this.state.submittedQuery !== '' && <>
              <Row>
                <Col lg={{span: 1}} id='design-section' className="section-title">design</Col>
                <Col lg={{span: 6, offset: 3}} className="section-content">
                  <div class='input-section-title'>Required information</div>
                  <div class='info-entry-list'>
                  {plan.information.required.map(
                  item => <div 
                            onClick={() => this.setState({  
                              editInfo: item,
                              showEditInfoForm: true})} 
                            className='info-entry'>
                            {item}
                          </div>)}
                    </div>

                <div class='input-section-title'>Secondary information</div>
                <div class='info-entry-list'>
                {plan.information.secondary.map(
                item => <div 
                          onClick={() => this.setState({ 
                            editInfo: item,
                            showEditInfoForm: true})} 
                          className='info-entry'>
                          {item}
                        </div>)}
                  </div>


                <div class='input-section-title'>Sensors collection</div>
                <div class='info-entry-list'>
                {plan.information.secondary.map(
                item => <div 
                          onClick={() => this.setState({ 
                            editInfo: item,
                            showEditInfoForm: true})} 
                          className='info-entry'>
                          {item}
                        </div>)}
                </div>

                </Col>
              </Row>
 
              <Row>                
                <Col lg={{span: 1}} className="section-title">confirm</Col>
                <Col lg={{span: 6, offset: 3}} className="section-content">
                  <Button size="sm" block>Print summary</Button>
                  <Button size="sm" block>Launch study</Button>
                </Col>
              </Row>
              </>}
            </Col>



            <Col>
              { this.state.showEditInfoForm && <Card className="information-edit-modal">
              <Card.Header>{this.state.editInfo}</Card.Header>
              <Card.Body>
                <Card.Title>Special title treatment</Card.Title>
                <Card.Text>
                  With supporting text below as a natural lead-in to additional content.
                </Card.Text>
                <Button variant="primary" onClick={() => this.setState({
                  showEditInfoForm: false
                })}>Save</Button>
              </Card.Body>
              </Card>}
            </Col>
          </Row>
        </Container>
      </div>
    );
  }
}

export default App;
