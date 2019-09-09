import React from 'react';
import { LinkContainer } from 'react-router-bootstrap'
import { Nav, NavItem, Navbar, Button } from 'react-bootstrap'
import { BrowserRouter as Router, Route } from 'react-router-dom

import 'bootstrap/dist/css/bootstrap.css'

import './App.css';

function App() {
  return (
    <Router>
      <div id='wrapper'>
        <Navbar expand='lg' bg='primary' variant='dark'>
          <Nav className='mr-auto'>
            {/*
              for each implementation:

              <NavItem>
                <LinkContainer to={full url}>
                  <Nav.Link eventKey={to-address}>
                    {label}
                  </Nav.Link>
                </LinkContainer>
              </NavItem>
            */}
          </Nav>
        </Navbar>


        <div id='content'>
            {/*
            for each implementation:
              <Route path={full url} exact component={Component} />
            */}
        </div>

      </div>
    </Router>
  );
}

export default App;
