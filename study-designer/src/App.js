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
      inprogressQuery: 'What time do I leave my house every day?',
      submittedQuery: 'What time do I leave my house every day?',
      loading: false,

      information: [],
      sensors: [],
      implementations: [
        { name: 'pothole patrol', implements: ['pothole'], requires: ['map matched location', 'aligned imu'], sensors: [] },
        { name: 'steering wheel estimation', implements: ['steering wheel'], requires: ['map matched location', 'aligned imu', 'speed'], sensors: [] },
        { name: 'speed limit monitor', implements: ['exceed speed limit'], requires: ['map matched location', 'speed'], sensors: [] },
        
        { name: 'android:imu aligner', implements: ['aligned imu'], requires: ['imu'], sensors: [] },
        { name: '-', implements: ['map matched location'], requires: ['location', 'imu'], sensors: [] },
        
        { name: 'obfuscation', implements: ['location'], requires: ['location'], sensors: [] },
        { name: 'spoofer', implements: ['imu', 'location'], requires: [], sensors: [] },

        // Low-level values
        { name: 'raw', implements: ['imu', 'location'], requires: [], sensors: ['imu', 'location'] },
        { name: 'web-based-input', implements: ['location'], requires: [], sensors: ['web:input'] },
        { name: 'phone-based-input', implements: ['location'], requires: [], sensors: ['phone:input'] },
      ],
    }
  }


  createStudyPlan() {
    /*
      1. fix some information as requirements
      2. for each information, gather all implemnentations of that information 
      3. fix some implementations, list all sensors those require
      4. list all leaf-node information/sensors required by implementations
      5. loop 2 - 4 until we meet requirements and some optimality condition
    */
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
    return (<Container className="study-design">

        <Row>
          <Col><Card className="study-card">
            <Card.Header>Required Information</Card.Header>
            <Card.Body>
              <Card.Text>
                <Button variant="outline-primary" block>Leave House</Button>
                <Button variant="outline-primary" block>Driving</Button>
              </Card.Text>
            </Card.Body>
          </Card></Col>

          <Col></Col>

          <Col><Card className="study-card">
            <Card.Header>Secondary Information</Card.Header>
            <Card.Body>
              <Card.Text>
                <Button variant="outline-primary" block>Sematic Location</Button>
                <Button variant="outline-primary" block>Activity Recognition</Button>
                <Button variant="outline-primary" block>OpenXC</Button>
                <Button variant="outline-primary" block>User Input</Button>
                <Button variant="outline-primary" block>Smartwatch:Motion</Button>
              </Card.Text>
            </Card.Body>
          </Card></Col>

          <Col></Col>

          <Col><Card className="study-card">
            <Card.Header>Collected Sensors</Card.Header>
            <Card.Body>
              <Card.Text>
                <Button variant="outline-primary" block>GPS</Button>
                <Button variant="outline-primary" block>User input</Button>
                <Button variant="outline-primary" block>IMU</Button>
              </Card.Text>
            </Card.Body>
          </Card></Col>
        </Row>

        <Row>
          <Col><Button>Launch study</Button></Col>
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
