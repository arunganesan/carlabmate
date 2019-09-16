import _ from 'lodash';
import React from 'react';
import './App.css';
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
import 'bootstrap/dist/css/bootstrap.css';
import Lorem from 'react-lorem-component'
import { createStudyPlan, Library } from './library'

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
      plan: [],
      
      openedPlanStep: {
        info: '', impl: ''
      },

      implDetails: '',
      showEditInfoForm: false,
      useNewImplementation: '',
    }
  }

  componentDidMount() {
    this.setState({
      ...Library
    })
  }

  handleSubmit (event) {
    this.setState({ 
      loading: true,
      submittedQuery: this.state.inprogressQuery,
    }, () => {
      this.setState({
        plan: createStudyPlan(this.state),
        loading: false
      })
    });

    event.preventDefault();
  }

  openPlanStep (step) {
    console.log('Clicked on plan step: ', step)

    this.setState({  
      openedPlanStep: step,
      implDetails: step.impl,
      showEditInfoForm: true})
  }

  renderStudy () {
    let plan = this.state.plan;

    return (<>
      <div className='input-section-title'>Information</div>
        <div className='info-entry-list'>
        {plan.map(
        item => <div 
                  onClick={() => this.openPlanStep(item)} 
                  className={'info-entry ' + (this.state.openedPlanStep.info == item.info ? 'opened-info-entry' : '')}>
                  {item.info}
                </div>)}
          </div>

      <div className='input-section-title'>Algorithm</div>
      <div className='info-entry-list'>
      {plan.map(
      item => <div 
                onClick={() => this.openPlanStep(item)} 
                className={'info-entry ' + (this.state.openedPlanStep.impl == item.impl ? 'opened-info-entry' : '')}>
                {item.impl}
              </div>)}
        </div>
    </>);
  }


  renderInfoCard () {
    if (!this.state.showEditInfoForm) 
      return null;
    
    return <Card className="information-edit-modal">
    <Card.Body className="information-edit-modal-body">
      <Card.Title>Info: <div className='tttext'>{this.state.openedPlanStep.info}</div></Card.Title>
      <Card.Text>
        <Lorem className="info-entry-list" count={1} />
      </Card.Text>

      <Card.Title>Available Algorithms</Card.Title>
      <Card.Text>
        <ul>
          { 
            // console.log(this.state.openedPlanStep)
            this.state.information[this.state.openedPlanStep.info].implemented_by.map(impl_name => {
              return <li>
                  <div className='impl-item' onClick={() => this.showImplDetails(impl_name)}>{impl_name}</div>
                  { (impl_name == this.state.openedPlanStep.impl)
                    ? <Badge pill variant="primary">
                      Currently chosen
                    </Badge>

                    : (impl_name == this.state.useNewImplementation) 
                    ? <Badge onClick={() => this.setState({
                      useNewImplementation: '',
                    })} className='impl-badge' pill variant="warning">
                      New implementation chosen
                    </Badge>

                    : <Badge onClick={() => this.setState({
                      useNewImplementation: impl_name,
                    })} className='impl-badge' pill variant="secondary">
                    Select this instead
                  </Badge>
                  }
                </li>
            }
          )}
        </ul>
      </Card.Text>
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
        this.setState({
          plan: createStudyPlan(this.state),
          useNewImplementation: '',
          implDetails: '',
          showEditInfoForm: false,
        })
      }}>
        Update study
      </Button>}
      </Card.Footer>
    </Card>;
  }

  renderImplementationCard () {
    if (this.state.implDetails === '')
      return null;
    
    return <Card className="information-edit-modal">
        <Card.Body className="information-edit-modal-body">
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
              <div className="passive-info-entry">{info}</div>)}
          </div>
        </Card.Text>
      </Card.Body>     
    </Card>;
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
                        value={this.state.optimizeFor} 
                        type="radio" size="sm" name="options"
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

            <Col lg={{span: 5}}>
              { this.renderInfoCard() }
              { this.renderImplementationCard() }
            </Col>
          </Row>
        </Container>
      </div>
    );
  }
}

export default App;
