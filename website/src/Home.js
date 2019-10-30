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
          <Col>
          Welcome to CarLab!
          </Col>
        </Row>
      </Container>

    );
  }
}
