import _ from 'lodash';
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

import { LinkContainer } from "react-router-bootstrap";
import { Nav, NavItem, Navbar } from "react-bootstrap";
import { Link, HashRouter as Router, Route } from "react-router-dom";



function MyLink (props) {
  return (
  <NavItem>
    <LinkContainer to={`/${props.to}`}>
    <Nav.Link style={{color: 'red'}}>
        {props.label ? props.label : _.capitalize(props.to)}
    </Nav.Link>
    </LinkContainer>
  </NavItem>);
}

function Overview (props) {
  return (<>
    Overview
    <Lorem />
  </>
  );
}






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
          <Col lg={{span: 3}}>
            <MyLink to='documentation/overview' label='Overview' />
            <MyLink to='documentation/designer' label='Data collection designer' />
            <MyLink to='documentation/participant' label='Experiment participant' />
            <MyLink to='documentation/developer' label='Algorithm developer' />
            <MyLink to='documentation/reference' label='Reference' />
          </Col>

          <Col>
            <Route path={'/documentation/overview'} component={() => <>Overview <Lorem /></>} />
            <Route path={'/documentation/designer'} component={() => <>Designer <Lorem /></>} />
            <Route path={'/documentation/participant'} component={() => <>Participant <Lorem /></>} />
            <Route path={'/documentation/developer'} component={() => <>Developer <Lorem /></>} />
            <Route path={'/documentation/reference'} component={() => <>Reference <Lorem /></>} />
          </Col>
          </Row>
        </Container>
    );
  }
}
