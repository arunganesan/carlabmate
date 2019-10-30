import './App.css';
import 'bootstrap/dist/css/bootstrap.css';

import _ from 'lodash';
import Designer from './Designer'
import Documentation from './Documentation'
import Home from './Home'
import React from 'react';


import { LinkContainer } from "react-router-bootstrap";
import { Nav, NavItem, Navbar, Button } from "react-bootstrap";
import { Modal, Container, Row, Col } from 'react-bootstrap';
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


class App extends React.Component {

  constructor(props) {
    super(props);
    this.state = {}
  }


  render() {
    return (
      <Router>
        <div id='navigation'>

        <Navbar expand="lg">
          <Nav className="mr-auto">
            <MyLink to="home" />
            <MyLink to="documentation/overview" label='Documentation' />
            <MyLink to="designer" />
          </Nav>
        </Navbar>

        <div id='content'>
          <Route path={'/home'} component={Home} />
          <Route path={'/documentation'} component={Documentation} />
          <Route path={'/designer'} component={Designer} />
        </div>
        </div>
      </Router>
      
    );
  }
}

export default App;
