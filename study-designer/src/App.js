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
      inprogressQuery: 'What time do I leave my house every day?',
      submittedQuery: '',
      loading: false,
      devices: [1, 3, 4],
      optimizeFor: 1,

      selectedPlanStep: '',
      plan: {
        information: {
          required: [],
          secondary: [],
        },
        sensors: []
      },
      
      editInfo: '',
      implDetails: '',
      showEditInfoForm: false,
      useNewImplementation: '',
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
  
    let information_names = _.keys(this.state.information);
    let required_information = _.sampleSize(information_names, 3);
    let secondary_information = []
    let leaf_nodes = []

    // for each information, pick an implementation, get their list of informations
    let plan = { 
      required: [],
      secondary: [],
      leaf: []
    } 

    let additional_dependencies = _.clone(required_information);

    console.log(this.state.implementations)

    let loop_count = 1000;  
    while (additional_dependencies.length != 0) {
      let new_additional_dependencies = [];
      for (let info of additional_dependencies) {
        let impl_name = _.sample(this.state.information[info].implemented_by)
        let plan_step = {
          info: info,
          impl: impl_name
        }

        let dependencies = this.state.implementations[impl_name].requires;
      
        if (_.includes(required_information, info))
          plan.required.push(plan_step)
        else if (dependencies.length === 0)
          plan.leaf.push(plan_step)
        else
          plan.secondary.push(plan_step)
        


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

    this.setState({
      plan: plan,
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

  openPlanStep (step) {
    this.setState({  
      editInfo: step,
      implDetails: '',
      showEditInfoForm: true})
  }

  renderStudy () {
    let plan = this.state.plan;
    return (<>
      <div className='input-section-title'>Required information</div>
        <div className='info-entry-list'>
        {plan.required.map(
        item => <div 
                  onClick={() => this.openPlanStep(item)} 
                  className='info-entry'>
                  {item.info}
                </div>)}
          </div>

      <div className='input-section-title'>Secondary information</div>
      <div className='info-entry-list'>
      {plan.secondary.map(
      item => <div 
                onClick={() => this.openPlanStep(item)} 
                className='info-entry'>
                {item.info}
              </div>)}
        </div>


      <div className='input-section-title'>Sensors collected</div>
      <div className='info-entry-list'>
      {plan.leaf.map(
      item => <div 
                onClick={() => this.openPlanStep(item)} 
                className='info-entry'>
                {item.info}
              </div>)}
      </div>
    </>);
  }

  showImplDetails (impl_name) {
    this.setState({
      implDetails: impl_name,
    })
  }

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
              <Card.Body>
                <Card.Title>Info: <div className='tttext'>{this.state.editInfo.info}</div></Card.Title>
                <Card.Text>
                    <Lorem className="info-entry-list" count={1} />
                </Card.Text>

                <Card.Title>Implementations</Card.Title>
                <Card.Text>
                  <ul>
                    { this.state.information[this.state.editInfo.info].implemented_by.map(impl_name => 
                      impl_name == this.state.editInfo.impl 
                      ? <li><div className="impl-item chosen-impl" onClick={() => this.showImplDetails(impl_name)}>{impl_name}</div></li>
                      : <li><div className="impl-item" onClick={() => this.showImplDetails(impl_name)}>{impl_name}</div></li>
                    )}
                  </ul>
                </Card.Text>


                { this.state.implDetails !== '' && (<>
                   <Card.Title>Impl: <div className='tttext'>{this.state.implDetails}</div>
                   </Card.Title>
                   <Card.Text>
                     <Lorem className="info-entry-list" count={1} />
                   </Card.Text>

                  <Card.Subtitle className="">Devices</Card.Subtitle>
                  
                  <Card.Text>
                  <ToggleButtonGroup 
                   className="info-entry-list" 
                        type="checkbox" 
                        size="sm" 
                        value={[1,2,4]}>

                        <ToggleButton variant="outline-primary" value={1}>Android</ToggleButton>
                        <ToggleButton variant="outline-primary" value={2}>iPhone</ToggleButton>
                        <ToggleButton variant="outline-primary" value={3}>Texting</ToggleButton>
                        <ToggleButton variant="outline-primary" value={4}>FitBit</ToggleButton>
                        <ToggleButton variant="outline-primary" value={5}>Smartwatch</ToggleButton>
                      </ToggleButtonGroup>

                  </Card.Text>
                  
                  <Card.Subtitle>Required Information</Card.Subtitle>
                  <Card.Text>
                    <div  className="info-entry-list">
                      { this.state.implementations[this.state.implDetails].requires.map(info => 
                        <div class="passive-info-entry">{info}</div>)}
                    </div>
                    
                    {
                      this.state.implDetails !== this.state.editInfo.impl &&
                      <div 
                        className="update-new-impl"
                        onClick={() => this.setState({
                          useNewImplementation: this.state.implDetails
                        })}
                      >
                      Use this implementation instead
                      </div>
                    }
                  </Card.Text>


                </>)}
              </Card.Body>
              <Card.Footer className="info-edit-form-footer text-muted"> 

              <Button 
                variant="outline-primary" onClick={() => 
                  this.setState({
                    showEditInfoForm: false,
                    implDetails: '',
                })}>
                  { this.state.useNewImplementation === '' ? 'Close' : 'Cancel' }
                </Button>


                { this.state.useNewImplementation !== '' && <Button 
                variant="outline-primary" onClick={() => {
                  this.createStudyPlan();

                  this.setState({
                    useNewImplementation: '',
                    implDetails: '',
                    showEditInfoForm: false,
                  })
                }}>
                  Update study
                </Button>}
                </Card.Footer>
              </Card>}
            </Col>
          </Row>
        </Container>
      </div>
    );
  }
}

export default App;
