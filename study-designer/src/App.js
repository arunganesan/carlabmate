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


class App extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      inprogressQuery: 'yolo',
      submittedQuery: 'yolo',// 'What time do I leave my house every day?',
      loading: false,
      devices: [1, 3, 4],
      optimizeFor: 1,

      plan: {
        information: {
          required: [],
          secondary: [],
        },
        sensors: []
      },
      
      editInfo: '',
      showEditInfoForm: false,


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

  componentDidMount() {
    this.generateDummyImplementations();
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

    const n_information = 15;
    const n_implementation = 50;
    const atmax_requirements = 10;

    // start with bottom most layer
    // each node can depend on other nodes in previous layers
    let implementations = {}
    let information = {}

    for (let i = 0; i < n_information; i++)  {
      let info = `information-${i}` 
      let impl = `default-impl-${i}`;
      information[info] = {
        name: info,
        implemented_by: [impl]
      }

      implementations[impl] = {
        name: impl,
        supplies: info,
        requires: []
      }
    }
    
    let information_names = _.keys(information)
    
    for (let i = 0; i < n_implementation; i++) {
      let name = `implementation-${i}`
      let supplies = _.sample(information_names)
      implementations[name] = {
        name: name,
        supplies: supplies,
        requires: _.sampleSize(information_names, 
                    _.random(0, atmax_requirements))
      }
      information[supplies].implemented_by.push(name);
    }

    this.setState({
      information: information,
      implementations: implementations,
    })
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
    // The tricky part is really which implementation+config do you choose? 
    //    Some choices may lead to more sensors collectioned
    
    // 1. sample random information that we may want -- initial round are the required sensors
    // 2. for each information, assign an implementation
    // 3. go through all dependents, and repeat 1-3 until we reach leaf node
    // A 'leaf node' is an implementation that has no dependents -- these are sensors

    let information_names = _.keys(this.state.information);
    let required_information = _.sampleSize(information_names, 3);
    let secondary_information = []
    let leaf_nodes = []

    // for each information, pick an implementation, get their list of informations
    let plan = []

    let additional_dependencies = _.clone(required_information);

    console.log(this.state.implementations)

    let loop_count = 1000;  
    while (additional_dependencies.length != 0) {
      let new_additional_dependencies = [];
      for (let info of additional_dependencies) {
        let impl_name = _.sample(this.state.information[info].implemented_by)
        let plan_step = {
          'info': info,
          'impl': impl_name
        }


        plan.push(plan_step)
  
        let dependencies = this.state.implementations[impl_name].requires;
        if (dependencies.length === 0) leaf_nodes.push(plan_step)
      
        let new_deps = _.filter(dependencies, dep => !_.includes(required_information, dep) && !_.includes(secondary_information, dep))
        new_additional_dependencies = _.concat(new_additional_dependencies, new_deps);
        secondary_information = _.concat(secondary_information, new_deps);

        console.log(loop_count, new_additional_dependencies)
      }
      
      new_additional_dependencies = _.uniq(new_additional_dependencies);
      additional_dependencies = _.clone(new_additional_dependencies);
      if (loop_count-- < 0) {
        alert('Looped too many times');
        break;
      }
    }

    console.log(plan, leaf_nodes)
    let summarized_plan = {
      information: {
        required: required_information,
        secondary: secondary_information,
      },
      sensors: leaf_nodes.map(step => step.info)
    }

      // information: {
      //   required: ['leave house', 'driving'],
      //   secondary: ['semantic location', 'activity recognition', 'openxc', 'user input', 'smartwatch:motion'],
      // },
      // sensors: ['gps', 'user input', 'imu']
    this.setState({
      plan: summarized_plan,
      loading: false});
  }

  handleSubmit (event) {
    this.setState({ 
      loading: true,
      submittedQuery: this.state.inprogressQuery,
    });

    this.createStudyPlan();
    event.preventDefault();
  }

  renderStudy () {
    let plan = this.state.plan;
    return (<>
      <div className='input-section-title'>Required information</div>
        <div className='info-entry-list'>
        {plan.information.required.map(
        item => <div 
                  onClick={() => this.setState({  
                    editInfo: item,
                    showEditInfoForm: true})} 
                  className='info-entry'>
                  {item}
                </div>)}
          </div>

      <div className='input-section-title'>Secondary information</div>
      <div className='info-entry-list'>
      {plan.information.secondary.map(
      item => <div 
                onClick={() => this.setState({ 
                  editInfo: item,
                  showEditInfoForm: true})} 
                className='info-entry'>
                {item}
              </div>)}
        </div>


      <div className='input-section-title'>Sensors collected</div>
      <div className='info-entry-list'>
      {plan.sensors.map(
      item => <div 
                onClick={() => this.setState({ 
                  editInfo: item,
                  showEditInfoForm: true})} 
                className='info-entry'>
                {item}
              </div>)}
      </div>
    </>);
  }
   
    /*  

         
          { this.state.submittedQuery !== '' && !this.state.loading && 
            this.renderStudy()
    } */

  render() {

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

                    <Button size="sm" disabled={this.state.inprogressQuery === ''} type="submit" block>{
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
                  { this.renderStudy() }
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
